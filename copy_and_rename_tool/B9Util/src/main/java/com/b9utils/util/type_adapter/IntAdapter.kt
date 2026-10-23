package com.b9utils.util.type_adapter

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

// Avoid NumberFormatException when parsing JSON
class IntDeserializer: JsonDeserializer<Int?> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Int? {
        return try {
            if (json.isJsonNull) {
                null
            } else if (json.isJsonPrimitive) {
                val prim = json.asJsonPrimitive
                when {
                    prim.isNumber -> prim.asInt
                    else -> null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}