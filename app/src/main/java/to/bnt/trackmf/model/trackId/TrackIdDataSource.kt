package to.bnt.trackmf.model.trackId

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class TrackIdDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val trackIdKey = stringPreferencesKey("track_id")

    val trackId = context.dataStore.data.map { it[trackIdKey] }

    suspend fun setTrackId(value: String?) {
        context.dataStore.edit {
            if (value == null) {
                it.remove(trackIdKey)
            } else {
                it[trackIdKey] = value
            }
        }
    }
}
