import io.ktor.client.*
import org.openqa.selenium.By
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.devtools.v127.network.Network
import java.time.Duration
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.encodeToJsonElement
import models.DatesInformation
import models.GetDesksRequestModel
import java.util.*
import java.util.function.Consumer

fun main() {

    println("Logging in with: " + System.getenv("username"))
    val driver = ChromeDriver()

    val devTools = driver.devTools
    devTools.createSession()
    devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()))

    var bearerToken = ""
    var accessToken = ""
    devTools.addListener(Network.requestWillBeSent(), Consumer {
        if (it.documentURL.contains("enterpriselite/auth")) {
            val entries = it.request.postData
            if (entries != null) {
                bearerToken = entries.get().split("&").first().split("=").last()
            }
        }
    })
    devTools.addListener(Network.responseReceived(), Consumer {
        if (it.response.url.contains("GetAppSetting")) {
            accessToken = it.response.url.split("?").last()
        }
    })


    //https://edekabank.condecosoftware.com/
    driver.get("https://edekabank.condecosoftware.com/")
    driver.manage().timeouts().implicitlyWait(Duration.ofMillis(1000))
    val usernameField = driver.findElement(By.id("txtUserName"))
    usernameField.click()
    usernameField.sendKeys(System.getenv("username"))
    val passwordField = driver.findElement(By.id("txtPassword"))
    passwordField.click()
    passwordField.sendKeys(System.getenv("password"))

    driver.findElement(By.id("btnLogin")).click()
    driver.navigate().refresh()
    driver.findElement(By.id("welcomeMsgLink")).click()
    Thread.sleep(5000)
    driver.quit()
    val client = HttpClient(CIO)

    println(accessToken)
    println(bearerToken)
    val dates = getPossibleBookingDates()
    val floor = 5
    val desks = getDesks(client, bearerToken, accessToken, dates.first(), floor)
    println(desks)


//    println(getPossibleBookingDates())

}

fun getPossibleBookingDates(): List<String> {
    val calendar = Calendar.getInstance(Locale.GERMAN)
    calendar.time = Date()
    val startWeek = calendar.get(Calendar.WEEK_OF_YEAR)
    val dates = mutableListOf<String>()
    var dayOffset = 0
    while(calendar.get(Calendar.WEEK_OF_YEAR) < startWeek + 3 ) {
        if(calendar.get(Calendar.DAY_OF_WEEK)  != Calendar.SUNDAY && calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY){
            dates.add("${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}")

        }

        dayOffset++;
        calendar.time = Date().apply { time += (dayOffset * 24 * 60 * 60 * 1000) }

    }
    return dates
}

fun getDesks(client: HttpClient, bearerToken: String, accessToken: String,  date: String, floor: Int): HttpResponse {
    return runBlocking {
        val response: HttpResponse = client.post("https://edekabank.condecosoftware.com/EnterpriseLite/api/Desk/Search") {
            contentType(ContentType.Application.Json)
            headers {
                append(HttpHeaders.Authorization, "Bearer $bearerToken")
            }
            setBody(GetDesksRequestModel(
                accessToken = accessToken,
                locationID = 6,
                groupID = 76,
                floorID = floor,
                pagingEnabled = false,
                wsType = 2,
                datesInformation = listOf(DatesInformation(
                    startDate = date,
                    bookingType = "3"
                ))
            ))
        }
        return@runBlocking response
    }

}

//POST https://edekabank.condecosoftware.com/EnterpriseLite/api/Desk/Search
//Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJuYW1laWQiOiIwYmIxNGYwMi00NWY0LTRiMWMtODhkNS05NTQ5ZGZhNDg3ZGYiLCJlbWFpbCI6ImpvbmFzLnJpYmJhY2tAZWRla2FiYW5rLmRlIiwidW5pcXVlX25hbWUiOiJKb25hcyBSaWJiYWNrIiwiZ2l2ZW5fbmFtZSI6IkpvbmFzIiwiZmFtaWx5X25hbWUiOiJSaWJiYWNrIiwicm9sZSI6IlVzZXIiLCJzdWIiOiIwYmIxNGYwMi00NWY0LTRiMWMtODhkNS05NTQ5ZGZhNDg3ZGYiLCJ1c2VyaWQiOiJUM1VNSXZ3N0FkY0Z5OFh4TUpqMFl3PT0iLCJ1c2VyQXV0aFR5cGUiOiJGb3JtcyIsImh0dHA6Ly9zY2hlbWFzLnhtbHNvYXAub3JnL3dzLzIwMDUvMDUvaWRlbnRpdHkvY2xhaW1zL2xvY2FsaXR5IjoiZGUtREUiLCJ1c2VyX3VwZ3JhZGVkIjoiMSIsInVzZXJfYWRtaW4iOiIyIiwidXNlcl9ncm91cEFkbWluVXNlcnMiOiJGYWxzZSIsImlzT3V0bG9vayI6IjAiLCJzZmJMb2dpbiI6IjAiLCJzaG93UHJvZmlsZSI6IjEiLCJyZWRpcmVjdGlvblNvdXJjZSI6IjAiLCJpc1BlbmRvRW5hYmxlZCI6IjIiLCJpc0FuYWx5dGljc0VuYWJsZWQiOiIwIiwiaXNPcHRlZElCb29raW5nIjoiMCIsImVuYWJsZUludGVsbGlnZW50Qm9va2luZyI6IjAiLCJpc3MiOiJDb25kZWNvIiwiYXVkIjoiYWMyOWVmMzhlYzlhNDYwNTlmOGQ2YmM2OGUzYzZkZDIiLCJleHAiOjE3Mjk3MTc5NDQsIm5iZiI6MTcyOTcxNzA0NH0.MGiCrkxAzhOk6YqNIoBXBwmaq7uLFcT8q-GaTBtoC1o
//Content-Type: application/json
//
//{
//    "accessToken": "0bb14f02-45f4-4b1c-88d5-9549dfa487df",
//    "locationID": 6,
//    "groupID": 76,
//    "floorID": 5,
//    "pagingEnabled": false,
//    "wsType": 2,
//    "datesInformation": [
//    {
//        "startDate": "23/10/2024",
//        "bookingType": "3"
//    }
//    ]
//}

//POST https://edekabank.condecosoftware.com/EnterpriseLite/api/Desk/Book
//Accept: application/json, text/plain, */*
//Accept-Language: de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7
//Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJuYW1laWQiOiI0Mzk0ODJhMS0xN2Y2LTQzMWItYWFiNy1hMTNjYzJhZGRiYTIiLCJlbWFpbCI6ImpvbmFzLnJpYmJhY2tAZWRla2FiYW5rLmRlIiwidW5pcXVlX25hbWUiOiJKb25hcyBSaWJiYWNrIiwiZ2l2ZW5fbmFtZSI6IkpvbmFzIiwiZmFtaWx5X25hbWUiOiJSaWJiYWNrIiwicm9sZSI6IlVzZXIiLCJzdWIiOiI0Mzk0ODJhMS0xN2Y2LTQzMWItYWFiNy1hMTNjYzJhZGRiYTIiLCJ1c2VyaWQiOiJUM1VNSXZ3N0FkY0Z5OFh4TUpqMFl3PT0iLCJ1c2VyQXV0aFR5cGUiOiJGb3JtcyIsImh0dHA6Ly9zY2hlbWFzLnhtbHNvYXAub3JnL3dzLzIwMDUvMDUvaWRlbnRpdHkvY2xhaW1zL2xvY2FsaXR5IjoiZGUtREUiLCJ1c2VyX3VwZ3JhZGVkIjoiMSIsInVzZXJfYWRtaW4iOiIwIiwidXNlcl9ncm91cEFkbWluVXNlcnMiOiJGYWxzZSIsImlzT3V0bG9vayI6IjAiLCJzZmJMb2dpbiI6IjAiLCJzaG93UHJvZmlsZSI6IjAiLCJyZWRpcmVjdGlvblNvdXJjZSI6IjAiLCJpc1BlbmRvRW5hYmxlZCI6IjIiLCJpc0FuYWx5dGljc0VuYWJsZWQiOiIwIiwiaXNPcHRlZElCb29raW5nIjoiMCIsImVuYWJsZUludGVsbGlnZW50Qm9va2luZyI6IjAiLCJpc3MiOiJDb25kZWNvIiwiYXVkIjoiYWMyOWVmMzhlYzlhNDYwNTlmOGQ2YmM2OGUzYzZkZDIiLCJleHAiOjE3Mjk3MTA0NzMsIm5iZiI6MTcyOTcwOTU3M30.WQ3T_KR3KqBhuAqUvnZXrDZQnpOkzPkZvJGEyDBwS4A
//Cache-Control: no-cache
//Connection: keep-alive
//Cookie: ASP.NET_SessionId=rbq3umq4npvmewfclbm4ckyv; ARRAffinity=c5d8c21d2f8376f630cabc60182b2ff2d349eef5ec3dc9194c46a2ac84a416eb; ARRAffinitySameSite=c5d8c21d2f8376f630cabc60182b2ff2d349eef5ec3dc9194c46a2ac84a416eb; CONDECO=GUID=439482a1-17f6-431b-aab7-a13cc2addba2; CondecoSessionID=28d03063-386b-4f66-9255-68d5136eaf45; __RequestVerificationToken=qQxkmpyP5DNrBxDZNkCqzcMPma9iWfqszlhAKqP1_spj_AqLHZxESt2OamBJaNWwfVsIebSpjPHGTrQ5z4a0ivRs1swtbmgQj9pVnqf0bQA1; AccessToken=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6IjQzOTQ4MmExLTE3ZjYtNDMxYi1hYWI3LWExM2NjMmFkZGJhMiIsInVzZXJuYW1lIjoieWYxbGxyaiIsImhkbiI6Im5sY29uZGVjbzAzOTUiLCJyb2xlIjoidXNlciIsImlzcyI6IkNvbmRlY28iLCJhdWQiOiJhYzI5ZWYzOGVjOWE0NjA1OWY4ZDZiYzY4ZTNjNmRkMiIsImV4cCI6MTcyOTc1Mjc3MCwibmJmIjoxNzI5NzA5NTcwfQ.Ymw0m-5XdWGnnIxarlvg-mdTXhXc6FllYcHjWK5LVXg; ai_user=vULSxE0vx7BcIfhn3+wmmF|2024-10-23T18:52:53.521Z; CONDECOLOGINTRANSLATE=LangLogin=15; EliteSession=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJuYW1laWQiOiI0Mzk0ODJhMS0xN2Y2LTQzMWItYWFiNy1hMTNjYzJhZGRiYTIiLCJlbWFpbCI6ImpvbmFzLnJpYmJhY2tAZWRla2FiYW5rLmRlIiwidW5pcXVlX25hbWUiOiJKb25hcyBSaWJiYWNrIiwiZ2l2ZW5fbmFtZSI6IkpvbmFzIiwiZmFtaWx5X25hbWUiOiJSaWJiYWNrIiwicm9sZSI6IlVzZXIiLCJzdWIiOiI0Mzk0ODJhMS0xN2Y2LTQzMWItYWFiNy1hMTNjYzJhZGRiYTIiLCJ1c2VyaWQiOiJUM1VNSXZ3N0FkY0Z5OFh4TUpqMFl3PT0iLCJ1c2VyQXV0aFR5cGUiOiJGb3JtcyIsImh0dHA6Ly9zY2hlbWFzLnhtbHNvYXAub3JnL3dzLzIwMDUvMDUvaWRlbnRpdHkvY2xhaW1zL2xvY2FsaXR5IjoiZGUtREUiLCJ1c2VyX3VwZ3JhZGVkIjoiMSIsInVzZXJfYWRtaW4iOiIwIiwidXNlcl9ncm91cEFkbWluVXNlcnMiOiJGYWxzZSIsImlzT3V0bG9vayI6IjAiLCJzZmJMb2dpbiI6IjAiLCJzaG93UHJvZmlsZSI6IjAiLCJyZWRpcmVjdGlvblNvdXJjZSI6IjAiLCJpc1BlbmRvRW5hYmxlZCI6IjIiLCJpc0FuYWx5dGljc0VuYWJsZWQiOiIwIiwiaXNPcHRlZElCb29raW5nIjoiMCIsImVuYWJsZUludGVsbGlnZW50Qm9va2luZyI6IjAiLCJpc3MiOiJDb25kZWNvIiwiYXVkIjoiYWMyOWVmMzhlYzlhNDYwNTlmOGQ2YmM2OGUzYzZkZDIiLCJleHAiOjE3Mjk3MTA0NzMsIm5iZiI6MTcyOTcwOTU3M30.WQ3T_KR3KqBhuAqUvnZXrDZQnpOkzPkZvJGEyDBwS4A; ai_session=Pb0z9xVGVii+4wmoQWyFgm|1729709573839|1729709574286
//Origin: https://edekabank.condecosoftware.com
//Pragma: no-cache
//Referer: https://edekabank.condecosoftware.com/EnterpriseLite/
//Sec-Fetch-Dest: empty
//Sec-Fetch-Mode: cors
//Sec-Fetch-Site: same-origin
//User-Agent: Mozilla/5.0 (iPad; CPU OS 16_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1
//Content-Type: application/json
//
//{
//  "accessToken": "439482a1-17f6-431b-aab7-a13cc2addba2",
//  "locationID": 6,
//  "groupID": 76,
//  "floorID": 5,
//  "pagingEnabled": false,
//  "wsType": 2,
//  "datesInformation": [
//    {
//      "startDate": "23/10/2024",
//      "bookingType": "3"
//    }
//  ],
//  "deskID": 157
//}

//POST https://edekabank.condecosoftware.com/webapi/BookingService/DeleteHotDeskBooking
//Accept: application/json, text/javascript, */*; q=0.01
//Accept-Language: de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7
//Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6IjQzOTQ4MmExLTE3ZjYtNDMxYi1hYWI3LWExM2NjMmFkZGJhMiIsInVzZXJuYW1lIjoieWYxbGxyaiIsImhkbiI6Im5sY29uZGVjbzAzOTUiLCJyb2xlIjoidXNlciIsImlzcyI6IkNvbmRlY28iLCJhdWQiOiJhYzI5ZWYzOGVjOWE0NjA1OWY4ZDZiYzY4ZTNjNmRkMiIsImV4cCI6MTcyOTc1Mjc3MCwibmJmIjoxNzI5NzA5NTcwfQ.Ymw0m-5XdWGnnIxarlvg-mdTXhXc6FllYcHjWK5LVXg
//Connection: keep-alive
//Cookie: ASP.NET_SessionId=rbq3umq4npvmewfclbm4ckyv; ARRAffinity=c5d8c21d2f8376f630cabc60182b2ff2d349eef5ec3dc9194c46a2ac84a416eb; ARRAffinitySameSite=c5d8c21d2f8376f630cabc60182b2ff2d349eef5ec3dc9194c46a2ac84a416eb; CONDECO=GUID=439482a1-17f6-431b-aab7-a13cc2addba2; CondecoSessionID=28d03063-386b-4f66-9255-68d5136eaf45; __RequestVerificationToken=qQxkmpyP5DNrBxDZNkCqzcMPma9iWfqszlhAKqP1_spj_AqLHZxESt2OamBJaNWwfVsIebSpjPHGTrQ5z4a0ivRs1swtbmgQj9pVnqf0bQA1; AccessToken=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6IjQzOTQ4MmExLTE3ZjYtNDMxYi1hYWI3LWExM2NjMmFkZGJhMiIsInVzZXJuYW1lIjoieWYxbGxyaiIsImhkbiI6Im5sY29uZGVjbzAzOTUiLCJyb2xlIjoidXNlciIsImlzcyI6IkNvbmRlY28iLCJhdWQiOiJhYzI5ZWYzOGVjOWE0NjA1OWY4ZDZiYzY4ZTNjNmRkMiIsImV4cCI6MTcyOTc1Mjc3MCwibmJmIjoxNzI5NzA5NTcwfQ.Ymw0m-5XdWGnnIxarlvg-mdTXhXc6FllYcHjWK5LVXg; ai_user=vULSxE0vx7BcIfhn3+wmmF|2024-10-23T18:52:53.521Z; CONDECOLOGINTRANSLATE=LangLogin=15; EliteSession=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJuYW1laWQiOiI0Mzk0ODJhMS0xN2Y2LTQzMWItYWFiNy1hMTNjYzJhZGRiYTIiLCJlbWFpbCI6ImpvbmFzLnJpYmJhY2tAZWRla2FiYW5rLmRlIiwidW5pcXVlX25hbWUiOiJKb25hcyBSaWJiYWNrIiwiZ2l2ZW5fbmFtZSI6IkpvbmFzIiwiZmFtaWx5X25hbWUiOiJSaWJiYWNrIiwicm9sZSI6IlVzZXIiLCJzdWIiOiI0Mzk0ODJhMS0xN2Y2LTQzMWItYWFiNy1hMTNjYzJhZGRiYTIiLCJ1c2VyaWQiOiJUM1VNSXZ3N0FkY0Z5OFh4TUpqMFl3PT0iLCJ1c2VyQXV0aFR5cGUiOiJGb3JtcyIsImh0dHA6Ly9zY2hlbWFzLnhtbHNvYXAub3JnL3dzLzIwMDUvMDUvaWRlbnRpdHkvY2xhaW1zL2xvY2FsaXR5IjoiZGUtREUiLCJ1c2VyX3VwZ3JhZGVkIjoiMSIsInVzZXJfYWRtaW4iOiIwIiwidXNlcl9ncm91cEFkbWluVXNlcnMiOiJGYWxzZSIsImlzT3V0bG9vayI6IjAiLCJzZmJMb2dpbiI6IjAiLCJzaG93UHJvZmlsZSI6IjAiLCJyZWRpcmVjdGlvblNvdXJjZSI6IjAiLCJpc1BlbmRvRW5hYmxlZCI6IjIiLCJpc0FuYWx5dGljc0VuYWJsZWQiOiIwIiwiaXNPcHRlZElCb29raW5nIjoiMCIsImVuYWJsZUludGVsbGlnZW50Qm9va2luZyI6IjAiLCJpc3MiOiJDb25kZWNvIiwiYXVkIjoiYWMyOWVmMzhlYzlhNDYwNTlmOGQ2YmM2OGUzYzZkZDIiLCJleHAiOjE3Mjk3MTA0NzMsIm5iZiI6MTcyOTcwOTU3M30.WQ3T_KR3KqBhuAqUvnZXrDZQnpOkzPkZvJGEyDBwS4A; ai_session=Pb0z9xVGVii+4wmoQWyFgm|1729709573839|1729709790610
//Origin: https://edekabank.condecosoftware.com
//Referer: https://edekabank.condecosoftware.com/
//Sec-Fetch-Dest: empty
//Sec-Fetch-Mode: cors
//Sec-Fetch-Site: same-origin
//User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36
//X-Requested-With: XMLHttpRequest
//sec-ch-ua: "Google Chrome";v="129", "Not=A?Brand";v="8", "Chromium";v="129"
//sec-ch-ua-mobile: ?0
//sec-ch-ua-platform: "Windows"
//Content-Type: application/x-www-form-urlencoded; charset=UTF-8
//
//bookingID = 51989 &
//resourceItemID = 154 &
//startDate = 08%2F11%2F2024+00%3A00 &
//endDate = 08%2F11%2F2024+23%3A59 &
//userID = 355 &
//UserLongID = 439482a1-17f6-431b-aab7-a13cc2addba2 &
//BookingOwnerUserID = 0