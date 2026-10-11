package io.vonley.mi.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPreferenceManagerImpl(
    override val context: Context,
    override val sharedPreferences: SharedPreferences
) : SharedPreferenceManager
