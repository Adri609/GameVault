package com.gamevault.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Formatea la fecha de lanzamiento. Si es en el futuro, devuelve el tiempo restante.
 * Si es en el pasado, devuelve la fecha formateada tradicionalmente.
 */

fun formatReleaseDate(releaseDateTimestamp: Long?): String {
    if (releaseDateTimestamp == null) return "Fecha por confirmar."

    // Igdb utiliza segundos, kotlin milisegundos
    val releaseTimeMillis = releaseDateTimestamp * 1000
    val currentTimeMillis = System.currentTimeMillis()

    return if (releaseTimeMillis > currentTimeMillis){
        // Fecha futura -> cuenta atrás
        val diffMillis = releaseTimeMillis -currentTimeMillis
        val daysRemaining = TimeUnit.MILLISECONDS.toDays(diffMillis)

        when {
            daysRemaining == 0L -> "Sale hoy!"
            daysRemaining < 30L -> "Sale en $daysRemaining días."
            daysRemaining < 365L -> "Sale en ${daysRemaining / 30} meses."
            else -> "Sale en ${daysRemaining / 365} años."
        }
    } else {
        val formatter = SimpleDateFormat("dd MM yyyy", Locale.getDefault())
        "Lanzado el ${formatter.format(Date(releaseTimeMillis))}"
    }
}