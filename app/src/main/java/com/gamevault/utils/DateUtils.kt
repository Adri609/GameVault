package com.gamevault.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Formatea la fecha de lanzamiento de un videojuego de forma inteligente.
 *
 * - Si el lanzamiento está en el **futuro**: muestra una cuenta atrás humanizada
 *   (ej: "Sale hoy!", "Sale en 5 días", "Sale en 2 meses")
 * - Si el lanzamiento está en el **pasado**: muestra la fecha formateada
 *   (ej: "Lanzado el 25 12 2023")
 * - Si la fecha es **nula**: devuelve "Fecha por confirmar"
 *
 * ## Notas de Implementación
 * - IGDB proporciona timestamps en **segundos**, que se convierten a milisegundos
 * - La lógica utiliza la hora actual del dispositivo para la comparación
 *
 * @param releaseDateTimestamp Timestamp en segundos proporcionado por IGDB API.
 * @return String formateado según el estado de la fecha.
 *
 * @sample
 * ```kotlin
 * formatReleaseDate(1704067200) // "Lanzado el 25 12 2023"
 * formatReleaseDate(null)         // "Fecha por confirmar."
 * ```
 */
fun formatReleaseDate(releaseDateTimestamp: Long?): String {
    if (releaseDateTimestamp == null) return "Fecha por confirmar."

    // Igdb utiliza segundos, kotlin milisegundos
    val releaseTimeMillis = releaseDateTimestamp * 1000
    val currentTimeMillis = System.currentTimeMillis()

    return if (releaseTimeMillis > currentTimeMillis){
        // Fecha futura -> cuenta atrás
        val diffMillis = releaseTimeMillis - currentTimeMillis
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

/**
 * Formatea un timestamp de registro a un formato legible de mes y año.
 *
 * Convierte un timestamp (en milisegundos) a un formato humanizado
 * ideal para mostrar fechas de registro de usuarios (ej: "Abril 2024").
 *
 * La primera letra del mes se convierte a mayúscula automáticamente.
 *
 * @param timestamp Timestamp en milisegundos desde la época Unix.
 * @return String en formato "mes año" capitalizado (ej: "Abril 2024").
 *
 * @sample
 * ```kotlin
 * formatRegistrationDate(1704067200000) // "Enero 2024"
 * ```
 */
fun formatRegistrationDate(timestamp: Long): String {
    val date = Date(timestamp)
    val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    return sdf.format(date).replaceFirstChar { it.uppercase() }
}
