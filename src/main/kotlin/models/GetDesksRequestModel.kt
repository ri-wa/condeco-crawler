package models

import kotlinx.serialization.Serializable

@Serializable
data class GetDesksRequestModel(
    val accessToken: String,
    val datesInformation: List<DatesInformation>,
    val floorID: Int,
    val groupID: Int,
    val locationID: Int,
    val pagingEnabled: Boolean,
    val wsType: Int
)