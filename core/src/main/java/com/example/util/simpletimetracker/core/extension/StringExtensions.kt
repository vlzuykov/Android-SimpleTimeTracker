package com.example.util.simpletimetracker.core.extension

import android.text.Spanned
import androidx.core.text.HtmlCompat

fun String.fromHtml(): Spanned {
    return HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY)
}

fun String.trimIfNotBlank(): String {
    return if (this.isNotBlank()) return this.trim() else this
}