package com.daonvshu.shared.utils

import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

/**
 * Compose‑only implementation of the Arco/Ant 10‑step color palette generator.
 * No Android‑specific APIs — works on Compose Multiplatform.
 */
object ColorPalette {
    /** Generate full 10‑step palette. */
    fun generateList(hex: Long): List<Color> =
        (1..10).map { generate(hex, it) }

    /**
     * Generate the i‑th step (1‥10).
     * @param hex    Primary hex color as Long (e.g. 0xFF165DFF)
     * @param i      Step index (1 = lightest … 10 = darkest)
     */
    fun generate(hex: Long, i: Int): Color {
        if (i == 6) {
            return Color(hex)
        }

        val color = Color(hex)
        val (h, s, v) = colorToHSV(color)

        val hueStep = 2f
        val maxSaturation = 100f
        val minSaturation = 9f
        val maxValue = 100f
        val minValue = 30f

        fun newHue(isLight: Boolean, idx: Int): Float {
            val sign = if (h in 60f..240f) if (isLight) -1 else 1 else if (isLight) 1 else -1
            return (h + sign * hueStep * idx + 360f) % 360f
        }

        fun newSaturation(isLight: Boolean, idx: Int): Float =
            if (isLight)
                if (s <= minSaturation) s else s - ((s - minSaturation) / 5f) * idx
            else
                s + ((maxSaturation - s) / 4f) * idx

        fun newValue(isLight: Boolean, idx: Int): Float =
            if (isLight)
                v + ((maxValue - v) / 5f) * idx
            else
                if (v <= minValue) v else v - ((v - minValue) / 4f) * idx

        val isLight = i < 6
        val idx = if (isLight) 6 - i else i - 6

        val (nh, ns, nv) = Triple(newHue(isLight, idx), newSaturation(isLight, idx), newValue(isLight, idx))
        return Color.hsv(nh, ns / 100f, nv / 100f)
    }

    /* =============  Internal helpers  ============= */

    private fun colorToHSV(color: Color): Triple<Float, Float, Float> {
        val r = color.red; val g = color.green; val b = color.blue
        val cMax = maxOf(r, g, b)
        val cMin = minOf(r, g, b)
        val delta = cMax - cMin

        val h = when {
            delta == 0f    -> 0f
            cMax == r      -> (60f * ((g - b) / delta) + 360f) % 360f
            cMax == g      -> (60f * ((b - r) / delta) + 120f)
            else           -> (60f * ((r - g) / delta) + 240f)
        }

        val s = if (cMax == 0f) 0f else delta / cMax
        val v = cMax
        return Triple(h, s * 100f, v * 100f) // H 0‥360, S/V 0‥100
    }
}

fun Color.toHex(): String =
    "#%02X%02X%02X".format((red * 255).roundToInt(), (green * 255).roundToInt(), (blue * 255).roundToInt())

fun Long.color(level: Int): Color {
    require(level in 1..10)
    return ColorPalette.generate(this, level)
}