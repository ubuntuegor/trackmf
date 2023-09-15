package to.bnt.trackmf.model.parcel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import to.bnt.trackmf.di.FilesDir
import java.io.File
import javax.inject.Inject

class ParcelLocalDataSource @Inject constructor(
    @FilesDir private val directory: File
) {
    private val filename = "saved.json"

    private val _savedParcel = MutableSharedFlow<Parcel?>(replay = 1)
    val savedParcel = _savedParcel.asSharedFlow()

    suspend fun init() {
        _savedParcel.emit(readParcel())
    }

    private suspend fun readParcel(): Parcel? {
        return withContext(Dispatchers.IO) {
            val file = File(directory, filename)

            if (file.exists()) {
                Json.decodeFromString<Parcel>(file.readText())
            } else {
                null
            }
        }
    }

    suspend fun writeParcel(parcel: Parcel?) {
        withContext(Dispatchers.IO) {
            val file = File(directory, filename)

            if (parcel != null) {
                file.writeText(Json.encodeToString(parcel))
            } else if (file.exists()) {
                file.delete()
            }

            _savedParcel.emit(parcel)
        }
    }
}
