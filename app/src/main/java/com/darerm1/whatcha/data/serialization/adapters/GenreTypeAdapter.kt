package com.darerm1.whatcha.data.serialization.adapters

import com.darerm1.whatcha.data.enums.Genre
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class GenreTypeAdapter : JsonDeserializer<Genre>, JsonSerializer<Genre> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Genre {
        val genreName = json.asString
        return Genre.values().find { it.russianName == genreName }
            ?: throw JsonParseException("Unknown genre: $genreName")
    }
    
    override fun serialize(src: Genre, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(src.russianName)
    }
}
