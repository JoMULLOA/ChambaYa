# ChambaYa - Aplicación de Servicios con Mapa 🗺️

Aplicación Android que permite publicar, buscar y visualizar servicios en un mapa interactivo.

## 🎯 Características

- ✅ Mapa interactivo con Google Maps
- ✅ Visualización de ofertas y demandas de servicios
- ✅ Filtros por tipo de trabajo
- ✅ Lista horizontal con trabajos cercanos
- ✅ Ubicación del usuario en tiempo real
- ✅ Marcadores personalizados (azul=oferta, verde=demanda)
- ✅ Arquitectura MVVM con ViewModel y LiveData

## 📱 Tecnologías

- **Lenguaje**: Kotlin
- **UI**: Material Design 3, View Binding
- **Arquitectura**: MVVM (Model-View-ViewModel)
- **Mapa**: Google Maps SDK for Android
- **Ubicación**: Google Play Services Location
- **Componentes**: RecyclerView, LiveData, ViewModel

## 🚀 Configuración Rápida

### 1. Obtener API Key de Google Maps

1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Crea o selecciona un proyecto
3. Habilita "Maps SDK for Android"
4. Ve a "Credenciales" y crea una API Key
5. Copia tu API Key

### 2. Configurar la API Key

Edita el archivo `local.properties` y agrega:
```properties
MAPS_API_KEY=TU_API_KEY_AQUI
```

### 3. Ejecutar

1. Abre el proyecto en Android Studio
2. Sync Gradle: `File > Sync Project with Gradle Files`
3. Ejecuta la app: `Run > Run 'app'`

## 📂 Estructura del Proyecto

```
app/src/main/
├── java/com/example/chambaya/
│   ├── MainActivity.kt          # Actividad principal con mapa
│   ├── model/
│   │   └── Job.kt              # Modelo de datos
│   └── ui/
│       ├── JobViewModel.kt     # ViewModel
│       └── JobsAdapter.kt      # Adapter del RecyclerView
├── res/
│   ├── layout/
│   │   ├── activity_main.xml   # Layout principal
│   │   └── item_job.xml        # Item de trabajo
│   └── values/
│       └── strings.xml         # Textos
└── AndroidManifest.xml         # Configuración y permisos
```

## 🎨 Características del Mapa

### Marcadores
- **Azules**: Ofertas de servicio (alguien ofrece un servicio)
- **Verdes**: Demandas de servicio (alguien busca un servicio)

### Interacción
- Click en marcador: Muestra información y centra el mapa
- Click en tarjeta de lista: Centra el mapa en ese trabajo
- Botón de ubicación: Centra en tu ubicación actual

### Filtros
- **Todos**: Muestra todos los trabajos
- **Ofertas**: Solo ofertas de servicio
- **Demandas**: Solo demandas de servicio

## 📊 Datos de Ejemplo

La app incluye 5 trabajos de ejemplo:
1. Pintura y remodelación - $300/h (Oferta)
2. Se busca plomero - Presupuesto (Demanda)
3. Cuidado de niños - $180/h ⭐4.8 (Oferta)
4. Se requiere jardinería - Presupuesto (Demanda)
5. Electricista certificado - $250/h ⭐4.9 (Oferta)

## 🔧 Solución de Problemas

### El mapa no se carga
- ✅ Verifica que tu API Key esté correctamente configurada
- ✅ Asegúrate de que "Maps SDK for Android" esté habilitado en Google Cloud
- ✅ Revisa los logs de Android Studio

### Permisos de ubicación
- En emulador: Configura ubicación en `...` > `Location`
- En dispositivo: Activa servicios de ubicación en ajustes

### Errores de compilación
```bash
# Limpiar y reconstruir
./gradlew clean
./gradlew build

# O en Android Studio:
Build > Clean Project
Build > Rebuild Project
```

## 📝 Próximas Funcionalidades

- [ ] Integración con backend (Firebase/API REST)
- [ ] Sistema de autenticación
- [ ] Chat entre usuarios
- [ ] Publicación de nuevos trabajos
- [ ] Pantalla de detalles de trabajo
- [ ] Sistema de calificaciones
- [ ] Notificaciones push
- [ ] Búsqueda avanzada

## 📖 Documentación Adicional

- [MAPS_IMPLEMENTATION.md](MAPS_IMPLEMENTATION.md) - Guía detallada de implementación
- [Documentación Google Maps](https://developers.google.com/maps/documentation/android-sdk)

## 📄 Licencia

Este proyecto es de código abierto para fines educativos.

---

Desarrollado con ❤️ usando Kotlin y Google Maps

