package com.dew.ed828.aihuaPlayer.util

import android.content.Context
import android.support.v7.preference.PreferenceManager
import com.dew.ed828.aihuaPlayer.about.R
import java.util.regex.Pattern

/**
 * Created by Edward on 12/13/2018.
 */

object FilenameUtils {

    /**
     * #143 #44 #42 #22: make sure that the filename does not contain illegal chars.
     * @param context the context to retrieve strings and preferences from
     * @param title the title to create a filename from
     * @return the filename
     */
    fun createFilename(context: Context, title: String): String {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val key = context.getString(R.string.settings_file_charset_key)
        val value = sharedPreferences.getString(key, context.getString(R.string.default_file_charset_value))
        val pattern = Pattern.compile(value)

        val replacementChar = sharedPreferences.getString(context.getString(R.string.settings_file_replacement_character_key), "_") ?: "dewtube"

        return createFilename(title, pattern, replacementChar)
    }

    /**
     * Create a valid filename
     * @param title the title to create a filename from
     * @param invalidCharacters patter matching invalid characters
     * @param replacementChar the replacement
     * @return the filename
     */
    private fun createFilename(title: String, invalidCharacters: Pattern, replacementChar: String): String {
        return title.replace(invalidCharacters.pattern().toRegex(), replacementChar)
    }
}