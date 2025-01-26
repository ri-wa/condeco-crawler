package models

import kotlinx.serialization.Serializable

@Serializable
data class BookDeskResponseModel(
    val CallResponse: CallResponse,
    val CreatedBookings: List<CreatedBooking>
)

@Serializable
data class CreatedBooking(
    val BookingDate: String,
    val BookingID: Int,
    val BookingType: Int,
    val Status: Int
)