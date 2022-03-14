package es.betsson.cups.presentation

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.activity.viewModels
import com.plug.cupgame.PlugActivity
import es.betsson.cups.databinding.ActivityMainBinding
import es.betsson.cups.utils.Constants.TEST_URL
import es.betsson.cups.utils.Constants.TRACKER_URL
import es.betsson.cups.utils.SharedPref
import es.betsson.cups.utils.ConnectionLiveData
import dagger.hilt.android.AndroidEntryPoint
import es.betsson.cups.utils.AppState
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var sharedPref: SharedPref

    @Inject
    lateinit var connectionLiveData: ConnectionLiveData

    private var webError = false

    private var progressChecked = false
    private var handler = Handler(Looper.getMainLooper())

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.state.observe(this) {
            Timber.d("APPSTATE::: $it")
            when(it) {
                is AppState.PrevCheck -> {
                    if(sharedPref.getTargetLink() != null && sharedPref.getTargetLink() == TEST_URL){
                        viewModel.state.postValue(AppState.PlugLogic())
                    } else {
                        viewModel.state.postValue(AppState.ConnectionCheck())
                    }
                }

                is AppState.ConnectionCheck -> {
                    connectionLiveData.observe(this) { isConnected ->
                        when(isConnected) {
                            true -> {
                                viewModel.connectionController(true)
                            }
                            false, null -> {
                                viewModel.connectionController(false)
                            }
                        }
                    }
                }

                is AppState.WebViewLogic -> {
                    binding.webView.apply {
                        CookieManager.getInstance().acceptThirdPartyCookies(this)
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            setSupportZoom(false)
                            cacheMode = WebSettings.LOAD_NO_CACHE
                        }
                        webChromeClient = object : WebChromeClient(){
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                super.onProgressChanged(view, newProgress)
                                CookieManager.getInstance().flush();
                                if(newProgress > 80) {
                                    if(!progressChecked){
                                        Timber.d("PAGE OPENED::: ${view?.url}")
                                        progressChecked = true
                                        if(sharedPref.getTargetLink() != null && sharedPref.getTargetLink() != TEST_URL){
                                            viewModel.state.postValue(AppState.TargetLogic())
                                            binding.loadingPlug.visibility = View.GONE
                                        } else if(sharedPref.getTargetLink() == null) {
                                            handler.postDelayed({
                                                if(webError){
                                                    viewModel.state.postValue(AppState.PlugLogic())
                                                } else {
                                                    sharedPref.setTargetLink(view?.url)
                                                    if(view?.url == TEST_URL){
                                                        viewModel.state.postValue(AppState.PlugLogic())
                                                    } else {
                                                        viewModel.state.postValue(AppState.TargetLogic())
                                                        binding.loadingPlug.visibility = View.GONE
                                                    }
                                                }
                                            }, 1000)
                                        }
                                    }
                                } else if(progressChecked) {
                                    progressChecked = false
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
                                if(error?.errorCode == -2 || error?.description == "net::ERR_TIMED_OUT" || error?.description == "net::ERR_NAME_NOT_RESOLVED" || error?.description == "net::ERR_INTERNET_DISCONNECTED"){
                                    webError = true
                                }
                            }
                        }
                        loadUrl(getLink()!!)
                    }
                }

                is AppState.TargetLogic -> {

                }

                is AppState.PlugLogic -> {
                    Timber.d("START PLUG FIRED:::")
                    val intent = Intent(this@MainActivity, PlugActivity::class.java)
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    startActivity(intent)
                    finish()
                }
            }
        }

        viewModel.connectionStatus.observe(this) {
            Timber.d("CONNECTION STATUS::: $it")
            when(it) {
                true -> {
                    handler.removeCallbacksAndMessages(null)
                    if(sharedPref.getTargetLink() == TEST_URL){
                        viewModel.state.postValue(AppState.PlugLogic())
                    } else if(viewModel.state.value !is AppState.TargetLogic) {
                        viewModel.state.postValue(AppState.WebViewLogic())
                    }
                }
                false -> {
                    handler.postDelayed({
                        if(viewModel.state.value is AppState.TargetLogic || viewModel.state.value is AppState.WebViewLogic) {
                            Toast.makeText(this, "Connection Lost", Toast.LENGTH_SHORT).show()
                        } else {
                            if(connectionLiveData.value == true){
                                viewModel.state.postValue(AppState.WebViewLogic())
                            } else {
                                Timber.d("TIMEOUT:::")
                                viewModel.state.postValue(AppState.PlugLogic())
                            }
                        }
                    }, 5000)
                }
            }
        }
    }

    private fun getLink() = if(sharedPref.getTargetLink() != null) sharedPref.getTargetLink() else
        TRACKER_URL
    //  "https://www.ozon.ru/category/pazly-13502/stoneford-87327476/"

    override fun onBackPressed() {
        if(binding.webView.visibility == View.VISIBLE && binding.webView.canGoBack()){
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}