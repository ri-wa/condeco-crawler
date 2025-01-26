package models

import kotlinx.serialization.Serializable

@Serializable
data class BookDeskRequestModel(
    val accessToken: String,
    val datesInformation: List<DatesInformation>,
    val floorID: Int,
    val groupID: Int,
    val locationID: Int,
    val pagingEnabled: Boolean,
    val wsType: Int,
    val deskID: Int
)