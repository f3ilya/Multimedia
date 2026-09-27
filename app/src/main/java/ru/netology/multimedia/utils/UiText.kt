package ru.netology.multimedia.utils

import android.content.Context
import androidx.annotation.StringRes

sealed class UiText {
    data class DynamicString(val value: String) : UiText()

    class ResourceString(
        @param:StringRes val resId: Int,
        vararg val args: Any,
    ) : UiText()

    fun asString(context: Context): String {
        return when(this) {
            is DynamicString -> value
            is ResourceString -> context.getString(resId, *args)
        }
    }
}