# Sistema de Navegación con Autenticación Google - AppMobile

## 🚀 Implementación Completada

### 📁 Estructura Creada/Modificada

#### **1. Navigation (Navegación)**
- `Routes.kt` - Definición de rutas centralizadas
- `AppNavHost.kt` - Sistema de navegación con Compose Navigation

#### **2. Data Layer (Capa de Datos)**
- `AuthDto.kt` - DTOs para las APIs de Firebase Functions
- `AuthApiService.kt` - Servicio Retrofit para searchUser y registerUser
- `AuthRepositoryImpl.kt` - Implementación del repositorio

#### **3. Domain Layer (Capa de Dominio)**
- `AuthRepository.kt` - Interface del repositorio
- `SearchUserUseCase.kt` - Caso de uso para verificar si usuario existe
- `RegisterUserUseCase.kt` - Caso de uso para registrar usuario

#### **4. Presentation Layer (Capa de Presentación)**
- `SignInViewModel.kt` - ViewModel actualizado con Hilt y searchUser API
- `RegisterViewModel.kt` - Nuevo ViewModel para registro
- `UserScreen.kt` - Actualizado para usar RegisterViewModel
- `MainActivity.kt` - Simplificado con Hilt

#### **5. Dependency Injection (Inyección de Dependencias)**
- `AppModule.kt` - Módulo Hilt con todas las dependencias
- `WereableApplication.kt` - Aplicación Hilt

## 🔄 Flujo de Navegación Implementado

### **1. SignInScreen → Autenticación Google**
- Usuario hace clic en "Iniciar sesión con Google"
- Se ejecuta Google Sign-In
- Al completarse exitosamente, se llama a la API `searchUser`

### **2. API searchUser → Verificación de Usuario**
```
GET /searchUser?uidUser={firebase_uid}
```
**Respuestas:**
- `status: true` → Usuario registrado → **ProfileScreen**
- `status: false` → Usuario nuevo → **UserScreen (Register)**

### **3. UserScreen → Registro de Usuario**
- Formulario biométrico (peso, altura, edad, sexo)
- Al enviar, se llama a la API `registerUser`

### **4. API registerUser → Registro en Firebase**
```
POST /registerUser
{
  "uidUser": "firebase_uid_123",
  "weightKg": 70.5,
  "heightCm": 175,
  "age": 28,
  "sex": "M"
}
```

### **5. ProfileScreen → Pantalla Principal**
- Acceso a Monitor, History, configuración de usuario
- Funcionalidad del wearable preservada

## 🛠️ APIs de Firebase Functions Integradas

### **searchUser**
- **Endpoint:** `GET /searchUser?uidUser={firebase_uid}`
- **Propósito:** Verificar si usuario completó registro
- **Uso:** Después de Google Sign-In para determinar navegación

### **registerUser**
- **Endpoint:** `POST /registerUser`
- **Propósito:** Registrar nuevo usuario con datos biométricos
- **Uso:** Desde UserScreen para completar registro inicial

## 📱 Navegación Preservada

- **Monitor** → Monitoreo de sueño del wearable ✅
- **History** → Historial de datos ✅
- **User Settings** → Configuración de usuario ✅
- **Sign Out** → Cerrar sesión y volver a SignIn ✅

## 🏗️ Arquitectura Clean + MVVM + Hilt

- **Clean Architecture** con capas bien definidas
- **MVVM** para separación UI/Lógica
- **Hilt** para inyección de dependencias
- **Compose Navigation** para navegación moderna
- **StateFlow** para manejo de estado reactivo

## ⚡ Características Clave

1. **Autenticación Google** integrada
2. **Verificación automática** de usuarios registrados
3. **Registro inicial** con formulario biométrico
4. **Navegación limpia** sin breaks en funcionalidad existente
5. **Error handling** robusto
6. **Loading states** para mejor UX
7. **Preservación total** de la lógica del wearable

## 🎯 Resultado Final

La aplicación ahora tiene un flujo de autenticación completo que:

1. **Autentica con Google** → Obtiene UID de Firebase
2. **Verifica registro** → Usa API searchUser
3. **Registra si es nuevo** → Usa API registerUser  
4. **Navega a ProfileScreen** → Funcionalidad principal
5. **Mantiene funcionalidad wearable** → Sin cambios

¡Listo para usar! 🚀
