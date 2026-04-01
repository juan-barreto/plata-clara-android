#  Plata Clara

> **Tu panorama financiero, siempre claro.**  
> App de finanzas personales diseñada para la realidad económica argentina.

<br>

[![Estado](https://img.shields.io/badge/Estado-Closed%20Beta-22c55e?style=flat-square)](https://play.google.com/store)
[![Platform](https://img.shields.io/badge/Platform-Android-3ddc84?style=flat-square&logo=android)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-7f52ff?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-UI-4285f4?style=flat-square)](https://developer.android.com/jetpack/compose)
[![By CandleLabs](https://img.shields.io/badge/By-CandleLabs-f97316?style=flat-square)](https://github.com/juan-barreto)

---

## ¿Qué es Plata Clara?

Plata Clara es una app Android de finanzas personales construida específicamente para Argentina. No es una app genérica traducida — cada feature fue pensada teniendo en cuenta la inflación, el dólar paralelo y las particularidades del mercado local.

**El problema que resuelve:** en Argentina, administrar las finanzas personales es complejo. El dólar cambia todos los días, la inflación ajusta los precios constantemente, y los alquileres se actualizan por índices oficiales. Ninguna app de finanzas mainstream contempla esto.

---

## Features

### Dashboard financiero
- Disponible del mes, gastado y balance en tiempo real
- Dólar Blue y Oficial actualizados automáticamente vía API propia
- IPC mensual visible como dato de primer nivel
- Presupuesto por categoría ajustado por inflación
- Consejo financiero diario contextualizado

### Gasto Express
- Registro de gastos en 3 pasos: monto → categoría → confirmar
- Teclado numérico nativo optimizado para velocidad
- Gesto "deslizá para confirmar" para evitar registros accidentales
- Categorías: Súper, Transporte, Salidas, Servicios, Salud, Varios

### ClarAI — Asistente financiero
- Asistente de IA con contexto financiero argentino real
- Analiza los datos reales del usuario dentro de la app
- Identifica patrones de gasto y sugiere acciones concretas
- Responde preguntas sobre el presupuesto personal

### Calculadora de alquiler
- Ajuste por IPC e ICL (Índice de Contratos de Locación)
- Períodos: trimestral, cuatrimestral, semestral y anual
- Consume datos oficiales de BCRA e INDEC
- Implementa el desfasaje correcto de publicación del INDEC

### Autenticación completa
- Login con email y contraseña
- Google OAuth (Continuar con Google)
- Recuperación de contraseña por email
- Sincronización en la nube vía Supabase

### Historial y reportes
- Vista semanal, mensual y anual
- Gráficos de ingresos vs gastos por período
- Movimientos editables y eliminables
- Categorización de fuentes de ingreso (sueldo, freelance, changas, jubilación, negocio propio)

---

## Arquitectura

```
plata-clara-android/
├── app/
│   ├── src/main/
│   │   ├── java/com/candlelabs/plataclara/
│   │   │   ├── ui/
│   │   │   │   ├── screens/          # Pantallas (Compose)
│   │   │   │   │   ├── home/
│   │   │   │   │   ├── dolar/
│   │   │   │   │   ├── alquiler/
│   │   │   │   │   ├── clarai/
│   │   │   │   │   ├── historial/
│   │   │   │   │   └── auth/
│   │   │   │   ├── components/       # Componentes reutilizables
│   │   │   │   └── theme/            # Colores, tipografía, shapes
│   │   │   ├── data/
│   │   │   │   ├── remote/           # Retrofit + APIs
│   │   │   │   ├── repository/       # Repositorios
│   │   │   │   └── model/            # Data classes
│   │   │   └── viewmodel/            # ViewModels (MVVM)
│   │   └── res/
└── gradle/
```

**Patrón:** MVVM (Model-View-ViewModel)  
**UI:** Jetpack Compose 100%  
**Estado:** StateFlow + ViewModel  
**Navegación:** Navigation Compose  

---

## Stack tecnológico

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Kotlin |
| UI | Jetpack Compose |
| Arquitectura | MVVM |
| HTTP Client | Retrofit 2 |
| Auth & DB | Supabase (PostgreSQL) |
| Backend propio | Python / Flask (Railway) |
| APIs externas | BCRA, INDEC, dolarapi.com |
| AI | API LLM con contexto financiero |
| Build | Gradle KTS |
| Min SDK | Android 7.0 (API 24) |

---

## Backend propio

Plata Clara consume una API REST propia deployada en Railway, construida con Python y Flask. Esta API:

- Consulta y normaliza datos del **BCRA** (tipo de cambio oficial)
- Consume el **INDEC** para IPC mensual con el desfasaje correcto
- Calcula el índice **ICL** para actualización de alquileres
- Mantiene historial de cotizaciones en SQLite
- Expone endpoints REST consumidos por la app vía Retrofit

---

## Estado del proyecto

```
✅ MVP funcional
✅ Auth completo (email + OAuth)
✅ Dashboard con datos reales
✅ Gasto Express
✅ ClarAI integrado
✅ Calculadora de alquiler IPC/ICL
✅ Historial de movimientos
✅ Closed beta en Google Play (12 testers activos)
🔄 Testing cerrado — 14 días requeridos para producción
⏳ Lanzamiento público — próximamente
```

---

## Google Play — Proceso de testing

El lanzamiento en Google Play requiere completar un testing cerrado con mínimo 12 testers durante 14 días consecutivos. Actualmente:

- **12 testers activos** de Argentina y otros países
- **Testing en curso** — bugs reportados y corregidos en tiempo real
- **Objetivo:** completar los 14 días para habilitar el lanzamiento público

---

## Screenshots

> *Próximamente — en proceso de captura para el store listing*



## Autor

**Juan Barreto** — Founder @ CandleLabs  
Buenos Aires, Argentina

[![LinkedIn](https://img.shields.io/badge/LinkedIn-Juan%20Barreto-0077b5?style=flat-square&logo=linkedin)](https://linkedin.com/in/juan-barreto-827128191)
[![GitHub](https://img.shields.io/badge/GitHub-juan--barreto-333?style=flat-square&logo=github)](https://github.com/juan-barreto)

---

## Licencia

Proyecto privado — © 2026 CandleLabs. Todos los derechos reservados.

---

*Built with intent. 🕯️*
