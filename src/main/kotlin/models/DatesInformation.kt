package models

import kotlinx.serialization.Serializable

@Serializable
data class DatesInformation(
    val bookingType: String,
    val startDate: String
)