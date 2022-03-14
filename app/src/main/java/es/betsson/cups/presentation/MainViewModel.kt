package es.betsson.cups.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.betsson.cups.utils.SharedPref
import es.betsson.cups.utils.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import es.betsson.cups.utils.AppState
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val sharedPref: SharedPref,
): ViewModel() {
    val state = MutableLiveData<AppState>(AppState.PrevCheck())

    private var _connectionStatus = MutableLiveData<Boolean>(null)
    val connectionStatus = _connectionStatus.asLiveData()

    fun connectionController(state: Boolean) {
        _connectionStatus.postValue(state)
    }
}