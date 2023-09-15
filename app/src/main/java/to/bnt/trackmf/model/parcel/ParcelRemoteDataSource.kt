package to.bnt.trackmf.model.parcel

import android.util.Log
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import javax.inject.Inject

private const val TAG = "ParcelRemoteDataSource"

class ParcelRemoteDataSource @Inject constructor() {
    private val client = HttpClient {
        expectSuccess = true
        buildHeaders {
            set(
                "User-Agent",
                "Mozilla/5.0 (X11; Linux x86_64; rv:104.0) Gecko/20100101 Firefox/104.0"
            )
        }
    }

    private val sourceEndpoint = "https://litemf.com/en/tracking/%s"

    suspend fun getParcel(id: String): List<ParcelStatus> = withContext(Dispatchers.IO) {
        val response = try {
            client.get(sourceEndpoint.format(id))
        } catch (e: ResponseException) {
            throw NetworkError(e.response.status.description)
        } catch (e: Exception) {
            throw NetworkError(e.message ?: "Unknown error")
        }

        val doc = try {
            Jsoup.parse(response.bodyAsText())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse LiteMF: ${e.message}")
            throw ParsingError()
        }

        val checkpoints = doc.getElementsByClass("checkpoint")

        if (checkpoints.size == 0) {
            throw NoParcelError()
        }

        checkpoints.map {
            if (it.childrenSize() != 3) {
                Log.e(TAG, "Checkpoint has an unusual amount of children: ${it.childrenSize()}")
                throw ParsingError()
            }

            val (date, description, place) = it.children()

            ParcelStatus(date.text(), description.text(), place.text())
        }
    }
}