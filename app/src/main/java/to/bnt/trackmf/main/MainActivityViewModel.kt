package to.bnt.trackmf.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import to.bnt.trackmf.model.parcel.ParcelRepository
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val repository: ParcelRepository
) : ViewModel() {
    val trackId = repository.trackId
    val parcel = repository.parcel

    private val _status = MutableStateFlow(StateStatus.Loading)
    val status = _status.asStateFlow()

    private val _errors = MutableSharedFlow<Throwable>()
    val errors = _errors.asSharedFlow()

    private var lastJob: Job? = viewModelScope.launch {
        repository.init()
        _status.value = StateStatus.Loaded
    }

    fun setTrackId(trackId: String) {
        lastJob?.cancel()
        lastJob = viewModelScope.launch {
            _status.value = StateStatus.Loading
            try {
                trackId.ifEmpty { null }.let {
                    repository.setTrackId(it)
                }
            } catch (e: Throwable) {
                _errors.emit(e)
            } finally {
                _status.value = StateStatus.Loaded
            }
        }
    }

    fun refresh() {
        lastJob = viewModelScope.launch {
            _status.value = StateStatus.Loading
            try {
                repository.refresh()
            } catch (e: Throwable) {
                _errors.emit(e)
            } finally {
                _status.value = StateStatus.Loaded
            }
        }
    }
}
