package com.example.nattklar.model

import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * @return the [Double], but rounded to the provided [numFractionDigits] number of decimals.
 */
fun Double.round(numFractionDigits: Int): Double {
    val factor = 10.0.pow(numFractionDigits.toDouble())
    return (this * factor).roundToInt() / factor
}