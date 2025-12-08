package com.example.smarthr_app.data.model

import com.google.gson.*
import java.lang.reflect.Type

/**
 * Custom TypeAdapter to handle Laravel's Int ID conversion to String userId
 */
class UserDtoTypeAdapter : JsonDeserializer<UserDto> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): UserDto {
        if (json == null || !json.isJsonObject) {
            throw JsonParseException("Invalid UserDto JSON")
        }
        
        val jsonObject = json.asJsonObject
        val idElement = jsonObject.get("id")
        
        // Convert Int or String id to String
        val userId = when {
            idElement.isJsonPrimitive -> {
                val primitive = idElement.asJsonPrimitive
                if (primitive.isNumber) {
                    primitive.asInt.toString()
                } else {
                    primitive.asString
                }
            }
            else -> throw JsonParseException("Invalid id type in UserDto")
        }
        
        return UserDto(
            userId = userId,
            name = jsonObject.get("name")?.asString ?: "",
            email = jsonObject.get("email")?.asString ?: "",
            phone = jsonObject.get("phone")?.asString ?: "",
            gender = jsonObject.get("gender")?.asString,
            role = jsonObject.get("role")?.asString ?: "Employee",
            companyCode = jsonObject.get("company_code")?.asString,
            imageUrl = jsonObject.get("image_url")?.asString,
            position = jsonObject.get("position")?.asString,
            department = jsonObject.get("department")?.asString,
            waitingCompanyCode = jsonObject.get("waiting_company_code")?.asString,
            joiningStatus = jsonObject.get("joining_status")?.asString,
            createdAt = jsonObject.get("created_at")?.asString,
            updatedAt = jsonObject.get("updated_at")?.asString
        )
    }
}

