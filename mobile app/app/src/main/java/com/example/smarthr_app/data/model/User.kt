package com.example.smarthr_app.data.model

import com.google.gson.annotations.SerializedName
import kotlin.jvm.JvmName

// UserDto is already defined in AuthRequest.kt, so we should remove it from here to avoid redeclaration.
// However, DataStoreManager and other files seem to expect UserDto to have certain fields.
// The UserDto in AuthRequest.kt has: userId, name, email, phone, gender, role, companyCode, imageUrl, position, department, waitingCompanyCode, joiningStatus.
// The UserDto in User.kt had: id (Int), name, email, createdAt, updatedAt, role, avatar.

// Since AuthRequest.kt seems to be the main model for Auth, we should probably keep that one and adapt usage.
// But wait, the errors show `Unresolved reference 'id'` in DataStoreManager.
// The UserDto in AuthRequest.kt has `userId` (String) but annotated with "id".
// The UserDto in User.kt has `id` (Int).

// The redeclaration error: "Redeclaration: data class UserDto".
// This means UserDto is defined in both AuthRequest.kt and User.kt.
// We should consolidate them.

// Looking at the errors in DataStoreManager:
// "Unresolved reference 'id'" -> implies it expects 'id' property.
// "No parameter with name 'id' found" -> implies constructor expects 'id'.
// "No value passed for parameter 'userId'" -> implies constructor has 'userId'.

// It seems we should merge them.
// I will comment out UserDto in User.kt for now, and rely on AuthRequest.kt's UserDto.
// But I need to make sure AuthRequest.kt's UserDto has what's needed.
// Or better, I should delete UserDto from User.kt and ensure AuthRequest.kt's UserDto is compatible or updated.

// AuthRequest.kt's UserDto:
// val userId: String (serialized as "id")
// val name: String
// val email: String
// val phone: String
// val gender: String?
// val role: String
// val companyCode: String?
// val imageUrl: String?
// ...

// User.kt's UserDto:
// val id: Int
// val name: String
// val email: String
// ...

// The app seems to have inconsistent expectations of User ID type (Int vs String).
// `DataStoreManager` seems to save `id` (Int based on `intPreferencesKey`).
// But `AuthRequest.kt` uses `String` for `userId`.

// Let's look at `DataStoreManager.kt` again (from error log):
// "Unresolved reference 'id'".
// "No parameter with name 'id' found".
// "No parameter with name 'createdAt' found".
// "No parameter with name 'updatedAt' found".

// This suggests DataStoreManager was written for the UserDto version that was in User.kt (which had id, createdAt, updatedAt).
// But AuthRequest.kt's UserDto is likely the one being used in other places or intended to be the single source.

// I will consolidate UserDto into a separate file or keep it in AuthRequest.kt and update it to match requirements.
// Since AuthRequest.kt is recently edited and seems more complete for the auth flow, I will favor it.
// However, I need to fix the id type mismatch.
// Laravel backends usually use Int IDs, but if using UUIDs, String is correct.
// The error "Argument type mismatch: actual type is 'Int', but 'String' was expected" in AllUserListScreen.kt line 113 suggests some IDs are Ints but Strings are expected, or vice versa.

// Let's check `UserInfo` in `User.kt`. It has `id: Int`.
// This suggests Int is used for IDs in lists.

// I will remove UserDto from User.kt to fix redeclaration.
// And I will update AuthRequest.kt's UserDto to accommodate the missing fields if possible, or check where they are used.
// But `DataStoreManager` errors are many.

// Ideally, I should keep only one UserDto.
// I will remove UserDto from User.kt.
// And I will verify `UserInfo` usage.

data class UserInfo(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("avatar") val avatar: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null // Adding imageUrl as alternative to avatar
) {
    // Helper to get image URL (prioritize imageUrl over avatar)
    @JvmName("getUserImageUrl")
    fun getImageUrl(): String? = imageUrl ?: avatar
}
