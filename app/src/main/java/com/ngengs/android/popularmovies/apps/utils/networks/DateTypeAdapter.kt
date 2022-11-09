package com.ngengs.android.popularmovies.apps.utils.networks

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.internal.JavaVersion
import com.google.gson.internal.PreJava9DateFormatProvider
import com.google.gson.internal.bind.util.ISO8601Utils
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.text.DateFormat
import java.text.ParseException
import java.text.ParsePosition
import java.util.Date
import java.util.Locale

/**
 * Created by rizky.kharisma on 10/11/22.
 * @ngengs
 */
class DateTypeAdapter : TypeAdapter<Date?>() {
    /**
     * List of 1 or more different date formats used for de-serialization attempts.
     * The first of them (default US format) is used for serialization as well.
     */
    private val dateFormats: MutableList<DateFormat> = ArrayList()

    init {
        dateFormats.add(DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US))
        if (Locale.getDefault() != Locale.US) {
            dateFormats.add(DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT))
        }
        if (JavaVersion.isJava9OrLater()) {
            dateFormats.add(PreJava9DateFormatProvider.getUSDateTimeFormat(DateFormat.DEFAULT, DateFormat.DEFAULT))
        }
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): Date? {
        if (`in`.peek() == JsonToken.NULL) {
            `in`.nextNull()
            return null
        }
        return deserializeToDate(`in`)
    }

    @Throws(IOException::class)
    private fun deserializeToDate(`in`: JsonReader): Date {
        val s = `in`.nextString()
        synchronized(dateFormats) {
            for (dateFormat in dateFormats) {
                try {
                    return dateFormat.parse(s)
                } catch (ignored: ParseException) {
                }
            }
        }
        return try {
            ISO8601Utils.parse(s, ParsePosition(0))
        } catch (e: ParseException) {
            Date(0)
        }
    }

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: Date?) {
        if (value == null) {
            out.nullValue()
            return
        }
        val dateFormat = dateFormats[0]
        var dateFormatAsString: String?
        synchronized(dateFormats) { dateFormatAsString = dateFormat.format(value) }
        out.value(dateFormatAsString)
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        val FACTORY: TypeAdapterFactory = object : TypeAdapterFactory {
            override fun <T> create(gson: Gson, typeToken: TypeToken<T>): TypeAdapter<T>? {
                return if (typeToken.rawType == Date::class.java) (DateTypeAdapter() as TypeAdapter<T>) else null
            }
        }
    }
}