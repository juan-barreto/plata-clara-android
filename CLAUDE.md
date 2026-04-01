# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Build app bundle (Play Store)
./gradlew bundleRelease

# Install debug on connected device
./gradlew installDebug

# Run unit tests
./gradlew test

# Run a single unit test class
./gradlew test --tests "com.candlelabs.gestionpersonal.ExampleUnitTest"

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Lint
./gradlew lint
```

## Architecture

**Single-module MVVM** with three internal layers:

- `model/` — Kotlin data classes for API request/response serialization (Gson)
- `network/` — `RetrofitClient` (singleton with JWT interceptor), `SupabaseClient` (singleton), `ApiService` (Retrofit interface with all 19 endpoints)
- `ui/` — ViewModels, Composable screens, theme, and shared components

**State pattern:** Each ViewModel exposes `StateFlow<SealedClass>` with states like `Cargando`, `Exito(data)`, `Error(message)`. UI collects via `collectAsState()`.

**Navigation:** Centralized in `NavGraph.kt`. Top-level routes: SPLASH → AUTH → ONBOARDING → MAIN. MAIN has a nested `NavHost` for tab navigation (HOME, DOLAR, ALQUILER, ASISTENTE, PRESUPUESTO, HISTORIAL, INFO, PERFIL, DOLAR_DETALLE).

## Backend & Auth

- **Supabase** handles authentication (Email/Password + Google PKCE) and is the source of truth for the JWT token.
- **Retrofit** calls a separate Python Flask backend on Railway (`https://web-production-f82cf.up.railway.app/`). Every request gets the Supabase JWT injected via an OkHttp interceptor in `RetrofitClient`.
- **Multi-tenancy:** The backend uses the JWT to scope data per user. Client-side `SharedPreferences` are also scoped by Supabase user ID.

## Key Libraries

| Library | Purpose |
|---|---|
| Jetpack Compose + Material3 | All UI |
| Supabase 3.1.4 (Auth + Ktor) | Authentication, session management |
| Retrofit 2.9.0 + Gson | REST API calls to Flask backend |
| OkHttp 4.12.0 | HTTP client with JWT logging interceptor |
| Vico 1.13.1 | Charts (exchange rate history) |
| Navigation Compose | In-app navigation |

## Important Conventions

- All text visible to the user is in **Spanish** (Argentine Spanish). Keep new UI strings in Spanish.
- Screen files are named `XxxScreen.kt`; their corresponding ViewModels are `XxxViewModel.kt` in the same `ui/` directory.
- The `@Streaming` annotation is required on Retrofit endpoints that return file downloads (Excel/PDF export).
- `minSdk` is 26; do not use APIs unavailable on Android 8.0.
- The app is phone-only (`android:resizableActivity="false"` with phone-only screen size restrictions in the manifest).
