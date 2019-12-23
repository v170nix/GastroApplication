package net.arwix.gastro.library.common

import android.graphics.Color
import androidx.annotation.ColorInt


fun isColorDark(@ColorInt color: Int): Boolean {
//    return ColorUtils.calculateLuminance(color) < 0.5 //0.25
    val darkness: Double =
        1 - (0.299 * Color.red(color) +
                0.587 * Color.green(color) +
                0.114 * Color.blue(color)) / 255
    return darkness >= 0.5
}

@ColorInt
fun getTextColor(
    @ColorInt bgColor: Int,
    @ColorInt darkTextColor: Int = Color.WHITE,
    @ColorInt lightTextColor: Int = Color.BLACK
): Int = if (isColorDark(bgColor)) darkTextColor else lightTextColor


