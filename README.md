# Plata Clara

Aplicación Android de finanzas personales desarrollada para Argentina.

Plata Clara nació como un proyecto para explorar el desarrollo de productos móviles y, al mismo tiempo, resolver problemas concretos relacionados con la administración de dinero en un contexto de inflación, variación del tipo de cambio y actualización de alquileres.

El proyecto fue desarrollado y publicado como aplicación Android. Actualmente se encuentra pausado y sujeto a futuras mejoras.

## Funcionalidades

### Dashboard financiero

* Disponible del mes, gastos y balance.
* Cotización del dólar Blue y Oficial mediante API.
* Visualización del IPC mensual.
* Presupuesto por categoría.
* Consejo financiero contextualizado.

### Gasto Express

Registro rápido de gastos:

```text
Monto → Categoría → Confirmar
```

Incluye teclado numérico, confirmación mediante gesto y categorías predefinidas como supermercado, transporte, salidas, servicios y salud.

### ClarAI

Asistente financiero integrado en la aplicación.

Puede utilizar información financiera del usuario para responder consultas, analizar patrones de gasto y generar sugerencias relacionadas con su presupuesto.

### Calculadora de alquiler

Permite calcular actualizaciones utilizando:

* IPC
* ICL (Índice de Contratos de Locación)
* Períodos trimestrales, cuatrimestrales, semestrales y anuales.

Los datos utilizados provienen de fuentes como BCRA e INDEC y se contempla el desfasaje entre la publicación de los índices y su disponibilidad.

### Autenticación

* Registro e inicio de sesión con email y contraseña.
* Google OAuth.
* Recuperación de contraseña.
* Sincronización de datos mediante Supabase.

### Historial y reportes

* Historial semanal, mensual y anual.
* Gráficos de ingresos y gastos.
* Movimientos editables y eliminables.
* Categorización de ingresos.

Entre las categorías de ingresos se incluyen sueldo, freelance, changas, jubilación y negocio propio.

## Arquitectura

La aplicación utiliza una arquitectura basada en MVVM.

```text
plata-clara-android/
├── app/
│   ├── src/main/
│   │   ├── java/com/candlelabs/plataclara/
│   │   │   ├── ui/
│   │   │   │   ├── screens/
│   │   │   │   │   ├── home/
│   │   │   │   │   ├── dolar/
│   │   │   │   │   ├── alquiler/
│   │   │   │   │   ├── clarai/
│   │   │   │   │   ├── historial/
│   │   │   │   │   └── auth/
│   │   │   │   ├── components/
│   │   │   │   └── theme/
│   │   │   ├── data/
│   │   │   │   ├── remote/
│   │   │   │   ├── repository/
│   │   │   │   └── model/
│   │   │   └── viewmodel/
│   │   └── res/
└── gradle/
```

### Componentes principales

* **Arquitectura:** MVVM
* **UI:** Jetpack Compose
* **Estado:** StateFlow + ViewModel
* **Navegación:** Navigation Compose
* **HTTP:** Retrofit

## Stack tecnológico

| Capa                            | Tecnología                 |
| ------------------------------- | -------------------------- |
| Lenguaje                        | Kotlin                     |
| UI                              | Jetpack Compose            |
| Arquitectura                    | MVVM                       |
| Estado                          | StateFlow + ViewModel      |
| HTTP Client                     | Retrofit 2                 |
| Autenticación y base de datos   | Supabase / PostgreSQL      |
| Backend                         | Python / Flask             |
| Base de datos local del backend | SQLite                     |
| APIs externas                   | BCRA, INDEC, dolarapi.com  |
| IA                              | API de modelos de lenguaje |
| Build                           | Gradle KTS                 |
| Min SDK                         | Android 7.0 (API 24)       |

## Backend

Plata Clara cuenta con una API REST propia desarrollada con Python y Flask y desplegada en Railway.

La API se encarga de centralizar parte del procesamiento y de integrar distintas fuentes de información.

Entre sus responsabilidades:

* Consulta y normalización de datos del BCRA.
* Consulta de datos del INDEC.
* Procesamiento del IPC.
* Cálculo del ICL para actualización de alquileres.
* Almacenamiento del historial de cotizaciones en SQLite.
* Exposición de endpoints REST consumidos por la aplicación Android mediante Retrofit.

## Datos y servicios

La aplicación integra distintas fuentes y servicios:

* BCRA para información relacionada con cotizaciones.
* INDEC para índices económicos.
* dolarapi.com para cotizaciones.
* Supabase para autenticación y almacenamiento de datos.
* API de modelos de lenguaje para ClarAI.

## Estado del proyecto

Plata Clara fue desarrollado como un proyecto funcional y llegó a publicarse en Google Play.

El proyecto actualmente se encuentra **pausado** y no está siendo desarrollado activamente.

La experiencia adquirida durante su desarrollo incluyó:

* Desarrollo Android con Kotlin.
* Construcción de interfaces con Jetpack Compose.
* Arquitectura MVVM.
* Consumo de APIs REST.
* Desarrollo de backend con Python y Flask.
* Integración con PostgreSQL y Supabase.
* Autenticación mediante email y OAuth.
* Integración de servicios externos.
* Procesamiento de datos económicos.
* Integración de modelos de lenguaje en una aplicación real.

## Capturas

Las capturas de pantalla de la aplicación se incluyen en el repositorio para mostrar parte de la interfaz y las funcionalidades desarrolladas.

## Autor

**Juan Barreto**

Buenos Aires, Argentina.

* Portfolio: https://juan-barreto-portfolio.vercel.app/
* LinkedIn: https://www.linkedin.com/in/juan-barreto-profile/
* GitHub: https://github.com/juan-barreto

## Licencia

Proyecto privado. Todos los derechos reservados.

El código se publica con fines demostrativos y de portfolio. No se otorga una licencia para utilizar, modificar o distribuir el software.
