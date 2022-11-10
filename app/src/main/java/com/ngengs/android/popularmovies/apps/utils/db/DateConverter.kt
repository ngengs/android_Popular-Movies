package com.ngengs.android.popularmovies.apps.utils.db

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Created by rizky.kharisma on 11/11/22.
 * @ngengs
 */
object DateConverter {
    @TypeConverter
    fun fromDate(date: Date?): String {
        if (date == null) return ""
        val formatter = SimpleDateFormat("EEE MMM dd kk:mm:ss zzz yyyy", Locale.ROOT)
        return try {
            formatter.format(date)
        } catch (e: Exception) {
            ""
        }
    }

    @TypeConverter
    fun toDate(dateString: String): Date? {
        if (dateString.isEmpty()) return null
        val formatter = SimpleDateFormat("EEE MMM dd kk:mm:ss zzz yyyy", Locale.ROOT)
        return try {
            formatter.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
}