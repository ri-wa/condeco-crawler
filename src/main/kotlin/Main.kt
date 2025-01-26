import kotlinx.coroutines.runBlocking
import models.BookingConfiguration
import models.SearchedDesk
import services.CondecoRequestService
import services.FirebaseService
import services.TokenService
import utils.getPossibleBookingDates

fun main() {
    val firebaseService = FirebaseService()
    val credentials = firebaseService.getCredentials()
    val bookingConfiguration = firebaseService.getBookingConfiguration()

    val tokenService = TokenService(bookingConfiguration.baseUrl, credentials.username, credentials.password)
    val (bearerToken, accessToken) = tokenService.getTokens()



    val condecoRequestService = CondecoRequestService(bookingConfiguration, accessToken, bearerToken)
    val dates = getPossibleBookingDates(bookingConfiguration)
    dates.forEach Dates@{ date ->
        val status = bookBestMatchingWorkstation(bookingConfiguration, condecoRequestService, date)
        println(status)
    }
}

fun bookBestMatchingWorkstation(bookingConfiguration: BookingConfiguration, service: CondecoRequestService, date: String): String {
    return runBlocking {
        // try to book preferred Workstation
        val preferredBookingResponse = service.bookDesk(
            date, bookingConfiguration.preferredDeskId, bookingConfiguration.preferredFloorId
        )
        // if preferredWorkstation is not bookable
        if (preferredBookingResponse.CreatedBookings.isEmpty()) {
            val possibleWorkStations: MutableList<SearchedDesk> = mutableListOf()
            var singleWorkspaceBooked = false
            bookingConfiguration.floors.forEach Floors@{ floor ->
                val searchedDesksResponse = service.getDesks(date, floor)
                possibleWorkStations.addAll(searchedDesksResponse.SearchedDesks.filter { it.CanBeBooked })

                if (possibleWorkStations.any { bookingConfiguration.singleWorkplaces.contains(it.DeskName) }) {
                    // alternative found
                    val desk = possibleWorkStations.first { bookingConfiguration.singleWorkplaces.contains(it.DeskName) }
                    val bookingResponse = service.bookDesk(date, desk.DeskID, floor)
                    if (bookingResponse.CreatedBookings.isNotEmpty()) {
                        singleWorkspaceBooked = true
                        return@runBlocking "[$date] alternative single Workstation found"
                    }
                }
            }

            if (!singleWorkspaceBooked) {
                if(possibleWorkStations.isEmpty()){
                    return@runBlocking "[$date] no booking possible"
                }
                val bookingResponse =
                    service.bookDesk(date, possibleWorkStations.first().DeskID, possibleWorkStations.first().floorId())
                if (bookingResponse.CreatedBookings.isNotEmpty()) {
                    return@runBlocking "[$date] any Workstation booked: ${possibleWorkStations.first().DeskName}"

                } else {
                    return@runBlocking "[$date] nothing was booked: ${bookingResponse.CallResponse.ResponseMessage}"
                }
            }
        } else {
            return@runBlocking "[$date] preferred Workstation booked"
        }
        return@runBlocking "success"
    }
}

