package com.ngengs.android.popularmovies.apps.utils.networks

import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.ngengs.android.popularmovies.apps.BuildConfig
import java.io.IOException
import java.lang.reflect.Modifier
import java.lang.reflect.Type
import java.util.Date

/**
 * Created by rizky.kharisma on 10/11/22.
 * @ngengs
 */
object GsonConfig {
    val gson: Gson by lazy {
        val gsonBuilder = GsonBuilder().apply {
            serializeNulls()
            if (BuildConfig.DEBUG) setPrettyPrinting()
            registerTypeAdapter(Date::class.java, DateTypeAdapter())
            registerTypeAdapter(String::class.java, StringTypeAdapter())
            registerTypeAdapter(Int::class.java, IntTypeAdapter())
            registerTypeAdapter(Long::class.java, LongTypeAdapter())
            registerTypeAdapter(Float::class.java, FloatTypeAdapter())
            registerTypeAdapter(Double::class.java, DoubleTypeAdapter())
            registerTypeAdapter(Boolean::class.java, BooleanTypeAdapter())
            registerTypeHierarchyAdapter(Uri::class.java, UriTypeHierarchyAdapter())
            excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC)
        }
        gsonBuilder.create()
    }

    private class StringTypeAdapter : TypeAdapter<String>() {
        @Throws(IOException::class)
        override fun read(reader: JsonReader): String {
            return if (reader.peek() == JsonToken.NULL) {
                reader.nextNull()
                ""
            } else try {
                reader.nextString()
            } catch (exception: Exception) {
                reader.skipValue()
                ""
            }
        }

        @Throws(IOException::class)
        override fun write(writer: JsonWriter, value: String?) {
            if (value == null) writer.nullValue() else writer.value(value)
        }
    }

    private class IntTypeAdapter : TypeAdapter<Int>() {
        @Throws(IOException::class)
        override fun read(reader: JsonReader): Int {
            return if (reader.peek() == JsonToken.NULL) {
                reader.nextNull()
                0
            } else try {
                reader.nextInt()
            } catch (exception: Exception) {
                reader.skipValue()
                0
            }
        }

        @Throws(IOException::class)
        override fun write(writer: JsonWriter, value: Int?) {
            if (value == null) writer.nullValue() else writer.value(value)
        }
    }

    private class LongTypeAdapter : TypeAdapter<Long>() {
        @Throws(IOException::class)
        override fun read(reader: JsonReader): Long {
            return if (reader.peek() == JsonToken.NULL) {
                reader.nextNull()
                0L
            } else try {
                reader.nextLong()
            } catch (exception: Exception) {
                reader.skipValue()
                0L
            }
        }

        @Throws(IOException::class)
        override fun write(writer: JsonWriter, value: Long?) {
            if (value == null) writer.nullValue() else writer.value(value)
        }
    }

    private class FloatTypeAdapter : TypeAdapter<Float>() {
        @Throws(IOException::class)
        override fun read(reader: JsonReader): Float {
            return if (reader.peek() == JsonToken.NULL) {
                reader.nextNull()
                0.0f
            } else try {
                reader.nextDouble().toFloat()
            } catch (exception: Exception) {
                reader.skipValue()
                0.0f
            }
        }

        @Throws(IOException::class)
        override fun write(writer: JsonWriter, value: Float?) {
            if (value == null) writer.nullValue() else writer.value(value)
        }
    }

    private class DoubleTypeAdapter : TypeAdapter<Double>() {
        @Throws(IOException::class)
        override fun read(reader: JsonReader): Double {
            return if (reader.peek() == JsonToken.NULL) {
                reader.nextNull()
                0.0
            } else try {
                reader.nextDouble()
            } catch (exception: Exception) {
                reader.skipValue()
                0.0
            }
        }

        @Throws(IOException::class)
        override fun write(writer: JsonWriter, value: Double?) {
            if (value == null) writer.nullValue() else writer.value(value)
        }
    }


    private class BooleanTypeAdapter : TypeAdapter<Boolean>() {
        @Throws(IOException::class)
        override fun read(reader: JsonReader): Boolean {
            return if (reader.peek() == JsonToken.NULL) {
                reader.nextNull()
                false
            } else try {
                reader.nextBoolean()
            } catch (exception: Exception) {
                reader.skipValue()
                false
            }
        }

        @Throws(IOException::class)
        override fun write(writer: JsonWriter, value: Boolean?) {
            if (value == null) writer.nullValue() else writer.value(value)
        }
    }

    class UriTypeHierarchyAdapter : JsonDeserializer<Uri>, JsonSerializer<Uri> {
        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): Uri {
            return Uri.parse(json.asString)
        }

        override fun serialize(
            src: Uri,
            typeOfSrc: Type,
            context: JsonSerializationContext
        ): JsonElement {
            // Note that Uri is abstract class.
            return JsonPrimitive(src.toString())
        }
    }
}