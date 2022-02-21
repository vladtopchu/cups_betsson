package es.betsson.cups.presentation

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.*
import com.plug.cupgame.PlugActivity
import es.betsson.cups.databinding.ActivityMainBinding
import es.betsson.cups.utils.Constants.TEST_URL
import es.betsson.cups.utils.Constants.TRACKER_URL
import es.betsson.cups.utils.SharedPref
import es.betsson.cups.utils.ConnectionLiveData
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var connectionLiveData: ConnectionLiveData

    @Inject
    lateinit var sharedPref: SharedPref

    private var webError = false

    private var progressChecked = false
    private var handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if(sharedPref.getTargetLink() != null && sharedPref.getTargetLink() == TEST_URL){
            Timber.d("STARTPLUG 1")
            startPlug()
        } else {
            connectionLiveData = ConnectionLiveData(this)
            connectionLiveData.observe(this) {
                Timber.d("CONNECTION STATUS::: $it")
                when(it) {
                    true -> {
                        handler.removeCallbacksAndMessages(null)
                        if(sharedPref.getTargetLink() == null){
                            webViewLogic()
                        } else {
                            if(sharedPref.getTargetLink() == TEST_URL){
                                startPlug()
                            } else {
                                webViewLogic()
                            }
                        }
                    }
                    false, null -> {
                        handler.postDelayed({
                            if(connectionLiveData.value == true){
                                webViewLogic()
                            } else {
                                Timber.d("TIMEOUT:::")
                                startPlug()
                            }
                        }, 4000)
                    }
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun webViewLogic() {
        binding.webView.apply {
            CookieManager.getInstance().acceptThirdPartyCookies(this)
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                setSupportZoom(false)
                cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            }
            webChromeClient = object : WebChromeClient(){
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    CookieManager.getInstance().flush();
                    if(newProgress > 80 && !progressChecked){
                        Timber.d("TEST::: ${view?.url}")
                        progressChecked = true
                        if(sharedPref.getTargetLink() != null && sharedPref.getTargetLink() != TEST_URL){
                            binding.loadingPlug.visibility = View.GONE
                        } else if(sharedPref.getTargetLink() == null) {
                            handler.postDelayed({
                                if(webError){
                                    Timber.d("WEB ERROR::: START PLUG")
                                    startPlug()
                                } else {
                                    sharedPref.setTargetLink(view?.url)
                                    if(view?.url == TEST_URL){
                                        startPlug()
                                    } else {
                                        binding.loadingPlug.visibility = View.GONE
                                    }
                                }
                            }, 1000)
                        }
                    }
                }
            }
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Timber.d("ONPAGEFINISHED:::$url")
                    CookieManager.getInstance().flush();
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    Timber.e("WEBVIEW ERROR::: ${error?.description} ${error?.errorCode}")
                    if(error?.errorCode == -2){
                        webError = true
                    }
                }
            }
            loadUrl(getLink()!!)
        }
    }

    private fun startPlug() {
        Timber.d("START PLUG FIRED:::")
        val intent = Intent(this@MainActivity, PlugActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        startActivity(intent)
        finish()
    }

    private fun getLink() = if(sharedPref.getTargetLink() != null) sharedPref.getTargetLink() else TRACKER_URL

    override fun onBackPressed() {
        if(binding.webView.visibility == View.VISIBLE && binding.webView.canGoBack()){
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}