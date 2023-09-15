package to.bnt.trackmf.model.parcel

@kotlinx.serialization.Serializable
data class Parcel(
    val lastUpdated: Long,
    val statuses: List<ParcelStatus>,
)
