# ChambaYa - Activities

## Descripción del Proyecto
ChambaYa es una plataforma Android en Kotlin que conecta personas que necesitan servicios puntuales (pasear perros, cortar leña, podar césped, limpieza, reparaciones menores, etc.) con quienes pueden realizarlos. La app incluye sistema de reputación, mensajería, geolocalización y gestión de trabajos.

---

## 📱 Activities Principales

### 1. **LoginActivity**
Autenticación de usuarios en la plataforma.

**Funcionalidad:**
- Login con email o teléfono y contraseña
- Validación de campos
- Guardar sesión
- Recuperación de contraseña

---

### 2. **RegisterActivity**
Registro de nuevos usuarios.

**Funcionalidad:**
- Validación de datos
- Crear Usuario en base de datos (se especializará en Demandante u Oferente)
- Métodos: `obtenerRun()`, `obtenerNombre()`, `obtenerReputacion()`
- Aceptación de términos obligatoria

---

### 3. **MainActivity**
Pantalla principal con navegación por pestañas.

**Funcionalidad:**
- Feed de servicios disponibles
- Acceso a búsqueda
- Mensajes y notificaciones
- Perfil de usuario

---

### 4. **CreateServiceActivity**
Publicar nuevo servicio o solicitud de trabajo (Publicacion).

**Funcionalidad:**
- Crear objeto Publicacion asociado a Oferente
- Establecer Ubicacion con coordenadas
- Definir CondicionTarifa (modalidad de pago y monto)
- Métodos: `obtenerTipo()`, `obtenerTitulo()`, `obtenerCategoria()`, `obtenerUbicacion()`, `obtenerModalidad()`, `obtenerEstado()`, `publicar()`

---

### 5. **JobDetailActivity**
Ver detalles completos de una Publicacion.

**Funcionalidad:**
- Mostrar información completa de Publicacion
- Ver Ubicacion en mapa
- Demandante puede solicitar servicio (genera Contrato)
- Ver lista de solicitudes si es Oferente
- Métodos utilizados: `obtenerTipo()`, `obtenerCategoria()`, `obtenerUbicacion()`, `obtenerModalidad()`

---

### 6. **SearchActivity**
Búsqueda avanzada y filtrado de servicios.

**Funcionalidad:**
- Búsqueda por palabras clave
- Filtros múltiples
- Vista en lista o mapa
- Guardar búsquedas

---

### 7. **MapActivity**
Mapa interactivo con servicios cercanos.

**Funcionalidad:**
- Servicios en mapa con marcadores
- Clusters para múltiples servicios
- Filtrar por categoría
- Ver detalles al tocar marcador

---

### 8. **ProfileActivity**
Perfil de Usuario (Demandante u Oferente).

**Funcionalidad:**
- Mostrar información del Usuario
- Llamar a `obtenerRun()`, `obtenerNombre()`, `obtenerReputacion()`
- Listar Reseñas asociadas
- Si es Oferente: `publicarServicio()`, `obtenerHistorialOfertar()`
- Si es Demandante: `solicitarServicio()`, `obtenerHistorialSolicitudes()`

---

### 9. **EditProfileActivity**
Editar información del perfil.

**Funcionalidad:**
- Actualizar foto de perfil
- Editar información personal
- Cambiar contraseña

---

### 10. **ChatActivity**
Mensajería 1:1 entre usuarios.

**Funcionalidad:**
- Chat en tiempo real
- Enviar mensajes e imágenes
- Notificaciones push
- Estado de lectura

---

### 11. **MessagesListActivity**
Lista de conversaciones.

**Funcionalidad:**
- Mostrar conversaciones activas
- Badge de mensajes no leídos
- Buscar conversaciones

---

### 12. **ApplicantsActivity**
Gestionar Contratos (solicitudes a un servicio).

**Funcionalidad:**
- Ver Contratos pendientes de una Publicacion
- Oferente puede: `obtenerOferente()`, `obtenerSolicitante()`, `aceptar()`, `cancelar()`, `completar()`
- Generar Pago al completar: `obtenerPago()`, `procesarPago()`
- Cambiar estado del Contrato

---

### 13. **MyServicesActivity**
Gestionar servicios propios.

**Funcionalidad:**
- Ver servicios publicados
- Ver postulaciones realizadas
- Ver servicios en proceso
- Historial completado

---

### 14. **RateUserActivity**
Crear Reseña tras completar servicio.

**Funcionalidad:**
- Crear objeto Reseña asociado a Usuario destinatario
- Atributos de Reseña: emisor (Usuario), destinatario (Usuario), puntaje, comentario
- Métodos: `obtenerPuntaje()`, `obtenerComentario()`
- Al publicar, actualiza la reputación del Usuario evaluado

---

### 15. **NotificationsActivity**
Centro de notificaciones.

**Funcionalidad:**
- Listar notificaciones
- Nuevos mensajes, postulaciones, estados
- Marcar como leída
- Eliminar notificaciones

---

### 16. **FavoritesActivity**
Servicios guardados.

**Funcionalidad:**
- Ver servicios guardados
- Eliminar de favoritos
- Crear alertas

---

### 17. **ReportActivity**
Reportar contenido o usuario.

**Funcionalidad:**
- Reportar servicios inapropiados
- Reportar usuarios
- Enviar evidencias

---

### 18. **SettingsActivity**
Configuración de la aplicación.

**Funcionalidad:**
- Gestionar preferencias
- Configurar notificaciones
- Ayuda y soporte
- Cerrar sesión

---