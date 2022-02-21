package es.betsson.cups.utils

import android.content.Context
import android.content.SharedPreferences
import es.betsson.cups.utils.Constants.SP_FILE_NAME
import es.betsson.cups.utils.Constants.SP_FIRST_LAUNCH
import es.betsson.cups.utils.Constants.SP_TARGET_LINK
import es.betsson.cups.utils.Constants.SP_TRACKER_URL

class SharedPref(context: Context) {
    private var sharedPreferences: SharedPreferences =
        context.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE)

    fun getTargetLink(): String? = sharedPreferences.getString(SP_TARGET_LINK, null)
    fun setTargetLink(targetLink: String?){
        sharedPreferences.edit().putString(SP_TARGET_LINK, targetLink).apply()
    }
}
