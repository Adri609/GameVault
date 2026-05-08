package com.gamevault.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gamevault.domain.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Enumeración que define los modos de tema disponibles en la aplicación.
 *
 * Controla cómo se renderiza la interfaz de usuario:
 * - **SYSTEM**: Sigue la preferencia del dispositivo (claro/oscuro)
 * - **DARK**: Fuerza modo oscuro siempre
 * - **LIGHT**: Fuerza modo claro siempre
 */
enum class ThemeMode {
    /** Sigue las preferencias de configuración del dispositivo. */
    SYSTEM,

    /** Fuerza el uso de tema oscuro en la interfaz. */
    DARK,

    /** Fuerza el uso de tema claro en la interfaz. */
    LIGHT
}

/**
 * Gestor centralizado de preferencias de la aplicación usando DataStore.
 *
 * DataStore es el sistema moderno de almacenamiento de preferencias en Android,
 * sustituyendo a SharedPreferences. Ofrece:
 * - **Seguridad**: Encriptación automática
 * - **Reactividad**: Basado en Kotlin Flows
 * - **Consistencia**: Transacciones ACID
 * - **Rendimiento**: Optimizado para lectura/escritura
 *
 * ## Datos Almacenados
 * - Preferencias de tema (claro/oscuro/automático)
 * - Información de usuario en caché (ID, username, foto)
 *
 * ## Uso Típico
 * ```kotlin
 * @Inject
 * lateinit var settingsDataStore: SettingsDataStore
 *
 * // Leer tema actual
 * val themeMode by settingsDataStore.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
 *
 * // Cambiar tema
 * LaunchedEffect(Unit) {
 *     settingsDataStore.setThemeMode(ThemeMode.DARK)
 * }
 * ```
 *
 * @property context Contexto de la aplicación requerido por DataStore.
 */
@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    private val USER_ID_KEY = stringPreferencesKey("user_id")
    private val USERNAME_KEY = stringPreferencesKey("username")
    private val PROFILE_PIC_KEY = stringPreferencesKey("profile_pic_url")

    /**
     * Flujo reactivo que emite el modo de tema actual.
     *
     * Si el valor guardado es inválido, devuelve automáticamente [ThemeMode.SYSTEM] como default.
     */
    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val themeString = preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(themeString)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    /**
     * Cambia el modo de tema guardado en DataStore.
     *
     * El cambio es **immediatamente reflejado** en todos los observadores del flujo [themeMode].
     *
     * @param mode Nuevo modo de tema a establecer.
     */
    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }

    /**
     * Flujo reactivo que emite el perfil de usuario en caché.
     *
     * El caché se rellena cuando el usuario inicia sesión y se sincroniza con Firestore.
     * Puede ser nulo si el usuario aún no se ha autenticado.
     */
    val cachedUser: Flow<User?> = context.dataStore.data.map { prefs ->
        val id = prefs[USER_ID_KEY] ?: return@map null
        User(id = id, username = prefs[USERNAME_KEY] ?: "", profilePictureUrl = prefs[PROFILE_PIC_KEY] ?: "")
    }

    /**
     * Almacena en caché el perfil del usuario después del login.
     *
     * Se llamaautomáticamente en `LoginWithGoogleUseCase` y `SignInWithEmailUseCase`.
     * Evita consultas innecesarias a Firestore para datos que cambian raramente.
     *
     * @param user Perfil del usuario a almacenar.
     */
    suspend fun cacheUserProfile(user: User) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID_KEY] = user.id
            prefs[USERNAME_KEY] = user.username
            prefs[PROFILE_PIC_KEY] = user.profilePictureUrl
        }
    }
}
