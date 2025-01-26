package models

import kotlinx.serialization.Serializable

@Serializable
data class BookingConfiguration(
    val weekDays: List<Int> = emptyList(),
    val excludedDates: List<String> = emptyList(),
    val locationID: Int = 6,
    val groupID: Int = 76,
    val baseUrl: String = "https://condecosoftware.com",
    val singleWorkplaces: List<String> = listOf("Desk 513", "Desk 514", "Desk 606", "Desk 605", "Desk 604", "Desk 709", "Desk 702"),
    val preferredDeskId: Int = 244,
    val preferredFloorId: Int = 5,
    val floors: List<Int> = listOf(5,6,7)
)
