# 🎮 GameVault

> **Una biblioteca personal de videojuegos moderna, segura y completamente sincronizada en la nube**

[![Language](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org/)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
[![API](https://img.shields.io/badge/API-24%2B-orange.svg)](https://developer.android.com/studio/releases/gradle-plugin)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20Architecture-red.svg)](#-arquitectura)

---

## 📋 Descripción General

**GameVault** es una aplicación Android nativa que permite a los usuarios **organizar, gestionar y descubrir** su colección personal de videojuegos. Combina datos en tiempo real de múltiples APIs externas (IGDB, Steam, Twitch) con sincronización en la nube mediante Firebase, ofreciendo una experiencia premium y sin fricciones.

### 🌟 Características Principales

- 🔐 **Autenticación Segura**: Registro e inicio de sesión con Firebase Auth con verificación de email
- ☁️ **Sincronización en Tiempo Real**: Biblioteca sincronizada entre dispositivos vía Firestore
- 🎮 **Integración Multi-API**: IGDB (datos), Steam (logros), Cloudinary (Imágenes de perfil)
- 📊 **Estadísticas Avanzadas**: Análisis de tu colección con gráficos y filtros
- 🎨 **Interfaz Premium**: Diseñada con Material Design 3 y Jetpack Compose
- 🌓 **Tema Personalizable**: Soporte para modo claro, oscuro y automático
- 📸 **Gestión de Fotos**: Subida de imágenes de perfil con Cloudinary
- 🔄 **Sincronización Offline**: Acceso total a datos aunque no haya conexión
- 🎯 **Filtrado Inteligente**: Filtra por estado (Jugando, Completado, etc.), favoritos, géneros

---

## 🏗️ Arquitectura

El proyecto sigue **Clean Architecture** con separación clara de responsabilidades en tres capas principales:

```
┌─────────────────────────────────────────────────┐
│           UI LAYER (Presentación)               │
│  ┌──────────────────────────────────────────┐   │
│  │ Screens    │ ViewModel │ Composables     │   │
│  └──────────────────────────────────────────┘   │
├─────────────────────────────────────────────────┤
│        DOMAIN LAYER (Lógica de Negocio)         │
│  ┌──────────────────────────────────────────┐   │
│  │ UseCase    │ Model    │ Repository Iface │   │
│  └──────────────────────────────────────────┘   │
├─────────────────────────────────────────────────┤
│          DATA LAYER (Fuentes de Datos)          │
│  ┌──────────────────────────────────────────┐   │
│  │ Remote (API) │ Local (BD) │ Repository   │   │
│  └──────────────────────────────────────────┘   │
└─────────────────────────────────────────────────┘
```

### 📂 Estructura de Carpetas

```
GameVault/app/src/main/java/com/gamevault/
├── data/                    # Data Layer
│   ├── local/              # Room Database & DataStore
│   ├── remote/             # APIs (IGDB, Steam, Twitch)
│   ├── repository/         # Abstracción de datos
│   └── mapper/             # DTO ↔ Domain Model
├── domain/                 # Domain Layer
│   ├── model/             # Modelos de negocio puros
│   └── usecase/           # Casos de uso
├── ui/                    # UI Layer
│   ├── screens/           # Pantallas principales
│   ├── components/        # Componentes reutilizables
│   ├── navigation/        # Grafo de navegación
│   └── theme/             # Tema Material Design 3
├── di/                    # Inyección de Dependencias (Hilt)
├── utils/                 # Utilidades genéricas
├── GameVaultApp.kt        # Application class
└── MainActivity.kt        # Activity principal
```

---

## 🛠️ Tecnologías Utilizadas

### Framework & UI
- **Jetpack Compose** - UI moderna y declarativa
- **Material Design 3** - Componentes visuales premium
- **Navigation Compose** - Navegación entre pantallas

### Arquitectura & Patrones
- **Clean Architecture** - Separación clara de capas
- **MVVM** - Model-View-ViewModel pattern
- **Repository Pattern** - Abstracción de datos
- **Use Cases** - Lógica de negocio centralizada

### Backend & Datos
- **Firebase Auth** - Autenticación y seguridad
- **Firebase Firestore** - Base de datos en tiempo real
- **Firebase Storage** - Almacenamiento de archivos
- **Room** - Base de datos local (SQLite)
- **DataStore** - Preferencias seguras

### Inyección de Dependencias
- **Hilt** - inyección de dependencias simplificada

### Networking
- **Retrofit 2** - Cliente HTTP
- **OkHttp** - Logging de requests
- **Gson** - Serialización JSON

### APIs Externas
- **IGDB API** - Información de videojuegos
- **Steam API** - Logros de juegos
- **Twitch API** - Autenticación OAuth

### Servicios
- **Cloudinary** - CDN para imágenes

### Testing
- **JUnit 4** - Tests unitarios
- **Espresso** - Tests de UI
- **MockK** - Mocking en tests

---

## 🚀 Instalación & Configuration

### Requisitos Previos

- Android Studio **Flamingo** o superior
- JDK 11+
- Gradle 8.0+
- Android SDK 24+

### Paso 1: Clona el Repositorio

```bash
git clone https://github.com/usuario/GameVault.git
cd GameVault
```

### Paso 2: Configuración de APIs

#### 2.1 Firebase Setup

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Crea un nuevo proyecto
3. Habilita **Authentication** (Email/Password, Google)
4. Crea una BD **Firestore** en modo producción
5. Descarga `google-services.json` y colócalo en `app/`

#### 2.2 Configuración de Credenciales

Crea `local.properties` en la raíz del proyecto:

```properties
# IGDB API (https://api-docs.igdb.com/)
IGDB_CLIENT_ID=your_igdb_client_id
IGDB_CLIENT_SECRET=your_igdb_client_secret

# Steam API (https://steamcommunity.com/dev)
STEAM_API_KEY=your_steam_api_key

# Cloudinary (https://cloudinary.com/)
CLOUDINARY_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_cloudinary_api_key
CLOUDINARY_CLIENT_SECRET=your_cloudinary_client_secret
```

**⚠️ IMPORTANTE**: Nunca commitees `local.properties` o `google-services.json`

### Paso 3: Compilación & Ejecución

```bash
# Compilar el proyecto
./gradlew build

# Ejecutar en emulador/dispositivo
./gradlew installDebug

# Ejecutar tests
./gradlew test
```

---

## 📱 Pantallas & Flujo de Usuario

### 1. **Register Screen** 🔐
- Registro con email/contraseña
- Inicio de sesión con Google
- Verificación de email requerida

### 2. **Main Screen** (Con Bottom Bar)

#### **Vault Tab** 📚
- Vista de lista/cuadrícula con animaciones
- Filtrado por estado, favoritos
- Menú desplegable con opciones avanzadas
- Badge counters dinámicos

#### **Search Tab** 🔍
- Búsqueda en tiempo real

#### **Profile Tab** 👤
- Información del usuario
- Personalización del perfil
- Estadísticas de la colección
- Gestión de preferencias
- Cierre de sesión

### 3. **Game Detail Screen** 📖
- Información extendida del juego
- Logros de Steam (si disponible)
- Gestión del estado (Jugando, Completado, etc.)
- Rating personal y favoritos
- Botones contextuales según estado

### 4. **Settings Screen** ⚙️
- Selector de tema (Claro/Oscuro/Automático)
- Seguridad de cuenta
- Enlace para cambiar contraseña

---

## 🔑 Conceptos Clave

### StateFlow & Reactividad

La app usa Kotlin Flows para reactividad continua:

```kotlin
// StateFlow en ViewModel
val games: StateFlow<List<Game>> = repository.getGames()
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

// En Composable
val games by viewModel.games.collectAsState()
```

### Resource Pattern

Encapsula el estado de operaciones asincrónicas:

```kotlin
sealed class Resource<T> {
    class Success<T>(val data: T)
    class Error<T>(val message: String, val data: T? = null)
    class Loading<T>(val data: T? = null)
}
```

### Use Cases

Cada operación de negocio importante es un Use Case:

```
SaveVaultEntryUseCase → Guarda un juego en la bóveda
ToggleVaultUseCase → Añade/elimina de colección
SyncVaultUseCase → Sincroniza locales con Firestore
```

---

## 🎨 Diseño & Animaciones

### Material Design 3
- Colores coherentes con tema del dispositivo
- Componentes modernos (Chips, Cards, FABs)
- Tipografía escalable

### Animaciones Premium
- Transiciones entre pantallas (fadeIn/slideOut)
- Menú desplegable con efecto stagger
- Badge counters con spring physics
- Rotaciones suaves de iconos

### Accesibilidad
- Descripciones apropiadas en todos los Iconos
- Contraste suficiente de colores
- Tamaños de tap targets >= 48.dp

---

## 🧪 Testing

El proyecto incluye la infraestructura para tests, aunque actualmente no hay cobertura.

---

## 📊 Estadísticas del Proyecto

| Métrica                  | Valor                       |
|--------------------------|-----------------------------|
| **Lenguaje**             | Kotlin 100%                 |
| **API Mínima**           | 24 (Android 7.0)            |
| **Líneas de Código**     | ~15,000+                    |
| **Número de Pantallas**  | 6 principales               |
| **APIs Externas**        | 3 (IGDB, Steam, Cloudinary) |
| **Componentes Custom**   | 20+                         |
| **ViewModel/ViewModels** | 8                           |
| **Use Cases**            | 6                           |
| **Repositories**         | 4                           |

---

## 🤝 Contribuir

Las contribuciones son bienvenidas. Por favor sigue estos pasos:

1. **Fork** el repositorio
2. **Crea una rama** para tu feature: `git checkout -b feature/Feature`
3. **Commit** tus cambios: `git commit -m 'Add Feature'`
4. **Push** a la rama: `git push origin feature/Feature`
5. **Abre un Pull Request**

### Guía de Estilo

- Usa **nombrado descriptivo** para variables y funciones
- Escribe **KDoc** obligatorio para clases públicas
- Sigue **Clean Code principles**
- Usa **sealed classes** en lugar de enums cuando sea posible

---

## 📋 Roadmap

### Fase 1 (Actual) ✅
- [x] Arquitectura Clean
- [x] Autenticación con Firebase
- [x] Integración IGDB/Steam
- [x] Base de datos local con Room
- [x] UI con Compose

### Fase 2 (Próxima) 🔄
- [ ] Cobertura de tests (>80%)
- [ ] Paginación en búsqueda
- [ ] Logros/Badges personales
- [ ] Notificaciones push

### Fase 3 (Futuro) 🎯
- [ ] Compartir colecciones entre usuarios
- [ ] Comparar bibliotecas
- [ ] Recomendaciones personalizadas
- [ ] Soporte offline mejorado

---

## 🐛 Reporte de Bugs

Si encuentras un bug, por favor:

1. Verifica que no esté ya reportado en [Issues](../../issues)
2. Crea un nuevo issue con:
   - Descripción clara del problema
   - Pasos para reproducir
   - Versión de Android y dispositivo
   - Logs (si aplica)

---

## 📄 Licencia

Este proyecto está licenciado bajo la **Apache 2.0** - mira el archivo [LICENSE](LICENSE) para más detalles.

---

## 👨‍💻 Autor

**GameVault Development Team**

- 🐙 GitHub: [@usuario](https://github.com/usuario)
- 📧 Email: adri.bog3@gmail.com

---

## 🙋 Soporte & Contacto

¿Preguntas o sugerencias? Abre un [Discussion](../../discussions) o contacta directamente.

### Recursos Útiles

- 📚 [Jetpack Compose Docs](https://developer.android.com/jetpack/compose/documentation)
- 🔥 [Firebase Documentation](https://firebase.google.com/docs)
- 🎮 [IGDB API Docs](https://api-docs.igdb.com/)
- 🎨 [Material Design 3 Guide](https://m3.material.io/)
- 🏗️ [Clean Architecture Book](https://blog.cleancoder.com/)

---

## 📈 Estadísticas de Desarrollo

```
Total Commits: 147
Total Contributors: 1
Lines of Code: ~15,000
```

---

**⭐ Si te gusta el proyecto, considera dejar una estrella en GitHub**

```
╔═══════════════════════════════════════╗
║   GameVault - Your Game Collection    ║
║        Management Made Easy           ║
╚═══════════════════════════════════════╝
```

---

**Última actualización**: Mayo 2026 | **Versión**: 1.0.0
