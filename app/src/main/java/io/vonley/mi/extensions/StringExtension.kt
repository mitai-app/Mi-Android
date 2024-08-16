package io.vonley.mi.extensions

import android.util.Patterns

fun String.isLink(): Boolean {
    return Patterns.WEB_URL.matcher(this).matches();
}
