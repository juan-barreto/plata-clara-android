# 📱 Gestión Personal ARG

App Android para seguimiento de cotizaciones del dólar en Argentina en tiempo real.
Consume una API REST propia desplegada en Railway, construida en Python con Flask.

---

## 🧱 Stack tecnológico

### Android
- **Kotlin** — lenguaje principal
- **Jetpack Compose** — UI declarativa
- **Retrofit** — cliente HTTP para consumir la API
- **ViewModel + StateFlow** — arquitectura MVVM para manejo de estado
- **Material Design 3** — componentes visuales

### Backend (repositorio separado)
- **Python + Flask** — API REST
- **SQLite** — base de datos local
- **Railway** — deployment en la nube

---

## 📐 Arquitectura

El proyecto sigue el patrón **MVVM (Model - View - ViewModel)**:
```
model/          → Data classes que representan la respuesta de la API
network/        → Retrofit: cliente HTTP y definición de endpoints
ui/             → ViewModel, pantallas y componentes Compose
```

- El **ViewModel** llama a la API y expone tres estados: `Cargando`, `Exito`, `Error`
- La **pantalla** observa el estado y se redibuja sola ante cualquier cambio
- La **Activity** solo inicializa el tema y delega todo a la pantalla

---

## 🚀 Cómo correrlo localmente

1. Clonar el repositorio
```bash
git clone https://github.com/tuusuario/GestionPersonalARG.git
```

2. Abrirlo en **Android Studio Hedgehog** o superior

3. Correr en emulador o dispositivo físico con **API 26+**

> El backend ya está desplegado en Railway — no necesitás correrlo localmente para probar la app.

---

## 📸 Screenshots

*Próximamente*

---

## 🗺️ Roadmap

- [x] Cotizaciones del dólar en tiempo real
- [ ] Calculadora de ajuste de alquiler (IPC / ICL / RIPTE)
- [ ] Historial de cálculos
- [ ] Selector de índice de ajuste

---

## 👨‍💻 Autor

**Juan** — Estudiante de Ingeniería Mecánica, UTN Argentina  
Proyecto personal para aprender desarrollo mobile y backend.
