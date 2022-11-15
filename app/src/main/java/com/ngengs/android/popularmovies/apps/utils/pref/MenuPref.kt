package com.ngengs.android.popularmovies.apps.utils.pref

import android.content.Context
import android.content.SharedPreferences
import com.ngengs.android.popularmovies.apps.globals.Values

/**
 * Created by rizky.kharisma on 11/11/22.
 * @ngengs
 */
class MenuPref(private val pref: SharedPreferences) {
    var sortType: Int
        get() = pref.getInt("SORT_TYPE_NOW", Values.TYPE_POPULAR)
        set(value) {
            pref.edit().putInt("SORT_TYPE_NOW", value).apply()
        }

    companion object {
        private const val PREF_NAME = "movies_menu_pref"

        fun instantiate(context: Context): MenuPref {
            return MenuPref(context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE))
        }
    }
}