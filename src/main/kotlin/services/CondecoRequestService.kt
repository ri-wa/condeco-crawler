package services

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import models.*

class CondecoRequestService(private val bookingConfiguration: BookingConfiguration, private val accessToken: String, private val bearerToken: String){
    private val client = HttpClient(CIO){
        install(ContentNegotiation){
            json()
        }
    }

    fun getDesks(date: String, floor: Int): GetDesksResponseModel {
        return runBlocking {
            val response: HttpResponse = client.post("${bookingConfiguration.baseUrl}/EnterpriseLite/api/Desk/Search") {
                contentType(ContentType.Application.Json)
                headers {
                    append(HttpHeaders.Authorization, "Bearer $bearerToken")
                }
                setBody(
                    GetDesksRequestModel(
                    accessToken = accessToken,
                    locationID = bookingConfiguration.locationID,
                    groupID = bookingConfiguration.groupID,
                    floorID = floor,
                    pagingEnabled = false,
                    wsType = 2,
                    datesInformation = listOf(
                        DatesInformation(
                        startDate = date,
                        bookingType = "3"
                    )
                    )
                )
                )
            }
            return@runBlocking response.body<GetDesksResponseModel>()
        }
    }

    fun bookDesk(date: String, deskId: Int, floor: Int): BookDeskResponseModel{
        return runBlocking {
            val response: HttpResponse = client.post("${bookingConfiguration.baseUrl}/EnterpriseLite/api/Desk/Book") {
                contentType(ContentType.Application.Json)
                headers {
                    append(HttpHeaders.Authorization, "Bearer $bearerToken")
                }
                setBody(
                    BookDeskRequestModel(
                        accessToken = accessToken,
                        locationID = bookingConfiguration.locationID,
                        groupID = bookingConfiguration.groupID,
                        floorID = floor,
                        pagingEnabled = false,
                        wsType = 2,
                        datesInformation = listOf(
                            DatesInformation(
                                startDate = date,
                                bookingType = "3"
                            )
                        ),
                        deskID = deskId,
                    )
                )
            }
            return@runBlocking response.body<BookDeskResponseModel>()
        }
    }
}