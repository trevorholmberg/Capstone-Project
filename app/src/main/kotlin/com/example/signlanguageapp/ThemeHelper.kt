/**
 * Helper class containing static fields for containing the current theme for the application.
 * @author Matthew Talle
 */

package com.example.signlanguageapp

class ThemeHelper {
    companion object {
        /** Current theme of the application. */
        @JvmStatic
        var currTheme = Themes.LightTheme
        /** Set current theme to light. */
        @JvmStatic
        fun setLight(){this.currTheme = Themes.LightTheme}
        /** Set current theme to dark. */
        @JvmStatic
        fun setDark(){this.currTheme = Themes.DarkTheme};
    }
}