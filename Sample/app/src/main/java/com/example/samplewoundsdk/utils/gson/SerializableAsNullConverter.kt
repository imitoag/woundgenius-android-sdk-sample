package com.example.samplewoundsdk.utils.gson

import com.google.gson.*
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

class SerializableAsNullConverter : TypeAdapterFactory {

    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        val declaredFields = type.rawType.declaredFields
        val nullableFieldNames = declaredFields.filter {
            it.declaredAnnotations.filterIsInstance<SerializeNull>().isNotEmpty()
        }.map { it?.getAnnotation(SerializedName::class.java)?.value ?: it.name }
        val nonNullableFields = declaredFields.map {
            it?.getAnnotation(SerializedName::class.java)?.value ?: it.name
        } - nullableFieldNames

        return object : TypeAdapter<T>() {
            private val delegateAdapter =
                gson.getDelegateAdapter(this@SerializableAsNullConverter, type)
            private val elementAdapter = gson.getAdapter(JsonElement::class.java)

            override fun write(writer: JsonWriter, value: T?) {
                val jsonElement = delegateAdapter.toJsonTree(value)
                try {
                    val jsonObject = jsonElement.asJsonObject
                    nonNullableFields.forEach {
                        if (jsonObject.get(it) is JsonNull) {
                            jsonObject.remove(it)
                        }
                    }
                    nullableFieldNames.forEach {
                        if (!jsonObject.has(it)) {
                            jsonObject.add(it, JsonNull.INSTANCE)
                        }
                    }
                    writer.serializeNulls = true
                    elementAdapter.write(writer, jsonObject)
                } catch (e: Exception) {
                    elementAdapter.write(writer, jsonElement)
                }
            }

            override fun read(reader: JsonReader): T {
                return delegateAdapter.read(reader)
            }
        }
    }
}