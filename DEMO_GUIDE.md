# ⏱️ 5-Minute Presentation Guide (Guía de Presentación de 5 Minutos)

Este documento contiene el guión cronometrado paso a paso para realizar una presentación de alto impacto en exactamente **5 minutos**, demostrando todas las capacidades técnicas requeridas.

---

## 🕒 Cronograma de la Presentación

| Minuto | Sección | Acción en Vivo | Conceptos Clave Demostrados |
| :--- | :--- | :--- | :--- |
| **0:00 - 1:00** | **Arquitectura y Fundamentos** | Mostrar estructura de paquetes en Android Studio | Clean Architecture, UDF, Kotlin Coroutines, Jetpack Compose |
| **1:00 - 2:00** | **Mensajería en Tiempo Real y UDF** | Enviar mensajes y cambiar de usuario con el selector | Flujo Unidireccional (`ChatUiState`, `ChatUiEvent`), persistencia |
| **2:00 - 3:15** | **Cámara (Runtime Permission) y Android Sharesheet** | Tomar una foto, adjuntarla y abrir el Sharesheet | `CAMERA` permission, `FileProvider`, `Intent.ACTION_SEND`, `createChooser` |
| **3:15 - 4:15** | **Firebase & Crashlytics** | Pulsar "Test Crashlytics" y revisar logs de sesión | Crashlytics SDK, logs de migas de pan (breadcrumbs), métricas personalizadas |
| **4:15 - 5:00** | **UI Adaptativa y Conclusión** | Rotar pantalla o abrir en tablet/foldable | Material 3 Adaptive, Scaffold de doble panel, persistencia ante recreación |

---

## 🎬 Guión Detallado para el Presentador

### 1. Introducción y Arquitectura (0:00 - 1:00)
- **Presentador:** *"Buenos días/tardes. Hoy les presento **ShareChat**, una solución moderna para Android desarrollada bajo Jetpack Compose, Clean Architecture y Unidirectional Data Flow (UDF)."*
- **Acción:**
  - Abre el proyecto en Android Studio y muestra los paquetes:
    - `data`: Modelos (`ChatMessage`, `ChatUser`) y repositorio (`FirebaseChatRepository`).
    - `ui`: MVI con `ChatUiState`, `ChatUiEvent` y `ChatViewModel`.
    - `util`: Integraciones nativas (`CameraUtils`, `ShareSheetHelper`, `CrashlyticsHelper`).
  - Explica: *"Todas las tareas asíncronas y operaciones de red están aisladas en Corrutinas bajo `Dispatchers.IO`, garantizando 60/120 FPS constantes en la interfaz."*

---

### 2. Mensajería en Vivo y Flujo Centralizado (1:00 - 2:00)
- **Presentador:** *"La interfaz se alimenta de un único estado inmutable `StateFlow<ChatUiState>`. Para evaluar una conversación real entre usuarios sin necesidad de dos dispositivos físicos, implementamos un conmutador de contexto."*
- **Acción:**
  1. Escribe un mensaje: *"¡Hola! Bienvenidos a la demo."* y pulsa **Enviar**.
  2. En el menú superior (o panel lateral en tablets), cambia el usuario activo de **Alice** a **Bob**.
  3. Responde como Bob: *"Mensaje recibido en tiempo real."*
  4. Muestra cómo las burbujas de chat se adaptan visualmente (derecha para emisor, izquierda con iniciales para receptor).

---

### 3. Cámara con Runtime Permission y Android Sharesheet (2:00 - 3:15)
- **Presentador:** *"A continuación demostraremos la captura multimedia y el sistema nativo de compartición de Android: Sharesheet."*
- **Acción:**
  1. Pulsa el icono de la **Cámara** en la barra de entrada.
  2. Muestra el diálogo del sistema solicitando el permiso en tiempo de ejecución: `Manifest.permission.CAMERA`.
  3. Acepta el permiso. La app genera un URI seguro con `FileProvider` y abre la cámara.
  4. Captura la foto y confirma. Observa la previsualización flotante sobre el campo de texto.
  5. Pulsa el botón del **Sharesheet** (icono de compartir):
     - Se invoca `ShareSheetHelper.launchShareSheet(...)` utilizando `Intent.ACTION_SEND` y `Intent.createChooser()`.
     - Destaca que se otorgan permisos de lectura temporales (`FLAG_GRANT_READ_URI_PERMISSION`) para transferir la imagen a otras aplicaciones (WhatsApp, Gmail, Drive, etc.).
  6. Envía el mensaje con la foto al chat.

---

### 4. Conexión con Firebase y Crashlytics (3:15 - 4:15)
- **Presentador:** *"El backend está conectado a Cloud Firestore y Firebase Crashlytics para telemetría en tiempo real."*
- **Acción:**
  1. Explica que la app registra automáticamente:
     - Identificador del usuario actual (`crashlytics.setUserId`).
     - Claves personalizadas como el estado del permiso de cámara (`crashlytics.setCustomKey`).
     - Migas de pan (breadcrumbs) con cada acción del chat.
  2. En el menú superior o panel lateral, pulsa **"Trigger Crashlytics Crash"**.
  3. La app lanza una excepción controlada de verificación (`Demo Presentation Crash`).
  4. Al reiniciar la app, Crashlytics envía el informe automáticamente a la consola de Firebase.

---

### 5. Layout Adaptativo y Cierre (4:15 - 5:00)
- **Presentador:** *"Por último, observemos la capacidad adaptativa del layout."*
- **Acción:**
  1. Rota el dispositivo a horizontal (Landscape) o ejecuta la app en un emulador de Tablet/Foldable (`WindowWidthSizeClass >= 600dp`).
  2. La vista cambia instantáneamente a un diseño de **Doble Panel**:
     - **Panel Izquierdo:** Centro de control del presentador (selector de participantes y verificaciones rápidas).
     - **Panel Derecho:** Conversación completa en alta resolución.
  3. Muestra que el texto que estaba escrito en el borrador no se pierde gracias a `SavedStateHandle` y persistencia de estado ante la reconstrucción de configuración.
- **Cierre:** *"ShareChat cumple con una arquitectura desacoplada, diseño responsivo, interoperabilidad mediante Android Sharesheet y observabilidad completa con Firebase. ¡Muchas gracias!"*
