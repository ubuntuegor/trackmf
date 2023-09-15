package to.bnt.trackmf.work

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import to.bnt.trackmf.Constants
import to.bnt.trackmf.R
import to.bnt.trackmf.model.parcel.Parcel
import to.bnt.trackmf.model.parcel.ParcelLocalDataSource
import to.bnt.trackmf.model.parcel.ParcelRemoteDataSource
import to.bnt.trackmf.model.trackId.TrackIdDataSource
import kotlin.random.Random

@HiltWorker
class UpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val trackIdSource: TrackIdDataSource,
    private val localSource: ParcelLocalDataSource,
    private val remoteSource: ParcelRemoteDataSource,
) :
    CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val trackId = trackIdSource.trackId.first()

            if (trackId == null) {
                Result.success()
            } else {
                localSource.init()
                val savedParcel = localSource.savedParcel.first()
                val statuses = remoteSource.getParcel(trackId)
                val parcel = Parcel(System.currentTimeMillis(), statuses)

                localSource.writeParcel(parcel)

                if (statuses != savedParcel?.statuses) {
                    val builder =
                        NotificationCompat.Builder(applicationContext, Constants.DEFAULT_CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_local_shipping_20)
                            .setContentTitle(applicationContext.getString(R.string.app_name))
                            .setContentText(applicationContext.getString(R.string.parcel_updated))
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                    NotificationManagerCompat.from(applicationContext)
                        .notify(Random.nextInt(), builder.build())
                }

                Result.success()
            }
        }
    }
}