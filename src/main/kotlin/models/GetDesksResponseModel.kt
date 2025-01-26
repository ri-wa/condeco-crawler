package models

import kotlinx.serialization.Serializable

@Serializable
data class GetDesksResponseModel(
    val CallResponse: CallResponse,
    val SearchedDesks: List<SearchedDesk>
)

@Serializable
data class CallResponse(
    val ResponseCode: Int,
    val ResponseMessage: String?
)

@Serializable
data class SearchedDesk(
    val CanBeBooked: Boolean,
    val DeskID: Int,
    val DeskName: String,
    val IsBestMatching: Boolean,
    val IsMapped: Boolean,
    val WSTypeId: Int
){
    fun floorId() = DeskName[5].toString().toInt()
}