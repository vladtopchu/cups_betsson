package es.betsson.cups.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.betsson.cups.utils.SharedPref
import es.betsson.cups.utils.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val sharedPref: SharedPref,
): ViewModel() {

    var isRequestSend = false

    val _error = MutableLiveData(false)
    val _errorWasSet = MutableLiveData(false)

    private val _response: MutableLiveData<String> = MutableLiveData(null)
    val response = _response.asLiveData()

    fun sendRequest() {
        isRequestSend = true
        viewModelScope.launch {
            try {
//                val serverResponse = apiService.getLink(requestLinkBody)
//                _response.postValue(serverResponse.link)
//                sharedPref.setTrackerUrl(serverResponse.link)
            } catch(e: HttpException){
                Timber.e(e)
                _error.postValue(true)
            } catch(e: Exception) {
                Timber.e(e)
                _error.postValue(true)
            }
        }
    }
}