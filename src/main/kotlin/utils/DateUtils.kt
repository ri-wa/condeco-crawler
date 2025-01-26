package utils

import models.BookingConfiguration
import java.util.*

fun getPossibleBookingDates(bookingConfiguration: BookingConfiguration): List<String> {
    val calendar = Calendar.getInstance(Locale.GERMAN)
    calendar.time = Date()
    val weeksOfTheYear = calendar.getMaximum(Calendar.WEEK_OF_YEAR)
    val startWeek = calendar.get(Calendar.WEEK_OF_YEAR)
    val endWeek = (startWeek + 3) % weeksOfTheYear


    var dayOffset = 0

    val dates = mutableListOf<String>()
    while(calendar.get(Calendar.WEEK_OF_YEAR) != endWeek + 1) {
        if(calendar.get(Calendar.DAY_OF_WEEK)  != Calendar.SUNDAY && calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY){
            if(bookingConfiguration.weekDays.isEmpty() || bookingConfiguration.weekDays.contains(calendar.get(Calendar.DAY_OF_WEEK))){
                val formattedDate = String.format("%02d/%02d/%d",
                    calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.YEAR)
                )
                dates.add(formattedDate)
            }
        }

        dayOffset++;
        calendar.time = Date().apply { time += (dayOffset * 24 * 60 * 60 * 1000L) }

    }
    return dates.filter { !bookingConfiguration.excludedDates.contains(it) }
}