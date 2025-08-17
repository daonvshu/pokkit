package com.daonvshu.shared.utils

import androidx.compose.ui.graphics.Color

/**
 * 主颜色枚举
 */
enum class PrimaryColors(val value: Long) {
    RED(0xFFF53F3F),
    ORANGE_RED(0xFFF77234),
    ORANGE(0xFFFF7D00),
    GOLD(0xFFF7BA1E),
    YELLOW(0xFFFADC19),
    LIME(0xFF9FDB1D),
    GREEN(0xFF00B42A),
    CYAN(0xFF14C9C9),
    BLUE(0xFF3491FA),
    ARCO_BLUE(0xFF165DFF),
    PURPLE(0xFF722ED1),
    PINK_PURPLE(0xFFD91AD9),
    MAGENTA(0xFFF5319D),
    BROWN(0xFF6B4D36),
    GRAY(0xFF8E8E8E),

    Unspecified(0x00000000)
    ;

    fun color(level: Int = 6, alpha: Float = 1f): Color {
        return value.color(level).copy(alpha = alpha)
    }

    companion object {
        val Bangumi_Primary = MAGENTA.color(level = 5)
        val Bangumi_Body = MAGENTA.color(level = 5, alpha = 0.1f)

        val Pixiv_Primary = ARCO_BLUE.color(level = 4)

        val Button_Normal = BROWN.color(level = 6)
        val Button_Normal_Primary = BROWN

        val Text_Normal = BROWN.color(level = 6)
        val Text_Secondary = BROWN.color(level = 4)
        val Text_Selected = CYAN.color(level = 7)

        val White = Color(0xFFFFFFFF)

        val Black = Color(0xFF363A42)

        val Icon_Button = MAGENTA.color(level = 5, alpha = 0.4f)
        val Icon_Button_Primary = MAGENTA
        val Icon_Button_Disabled = GRAY
    }
}