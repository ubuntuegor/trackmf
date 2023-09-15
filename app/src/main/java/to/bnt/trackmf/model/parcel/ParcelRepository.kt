package to.bnt.trackmf.model.parcel

import kotlinx.coroutines.flow.first
import to.bnt.trackmf.model.trackId.TrackIdDataSource
import javax.inject.Inject

class ParcelRepository @Inject constructor(
    private val localSource: ParcelLocalDataSource,
    private val remoteSource: ParcelRemoteDataSource,
    private val trackIdSource: TrackIdDataSource,
) {
    val trackId = trackIdSource.trackId
    val parcel = localSource.savedParcel

    suspend fun init() {
        return localSource.init()
    }

    suspend fun setTrackId(value: String?) {
        trackIdSource.setTrackId(value)

        val parcel = value?.let {
            val statuses = remoteSource.getParcel(it)
            Parcel(System.currentTimeMillis(), statuses)
        }

        localSource.writeParcel(parcel)
    }

    suspend fun refresh() {
        setTrackId(trackId.first())
    }
}
