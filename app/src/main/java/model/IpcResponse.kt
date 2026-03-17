package com.candlelabs.gestionpersonal.model

import com.google.gson.annotations.JsonAdapter
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

// El IPC viene como ["2024-01-01", 3.14] — una lista mixta, no un objeto
// Necesitamos un deserializador custom que entienda ese formato
@JsonAdapter(IpcItem.Deserializer::class)
data class IpcItem(
    val fecha: String,
    val valor: Double
) {
    class Deserializer : JsonDeserializer<IpcItem> {
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): IpcItem {
            val array = json.asJsonArray
            return IpcItem(
                fecha = array[0].asString,
                valor = array[1].asDouble
            )
        }
    }
}