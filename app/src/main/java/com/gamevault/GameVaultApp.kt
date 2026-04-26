package com.gamevault

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase de aplicación principal que inicializa Hilt para la inyección de dependencias.
 */
@HiltAndroidApp
class GameVaultApp : Application()