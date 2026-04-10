# RemoteAquarium

An interactive neon aquarium Android app showcasing **AndroidX Remote Compose** — a server-driven UI framework that serializes drawing operations into compact binary documents rendered natively on Android.

Fish swim, seaweed sways, bubbles rise, and waves ripple — all defined as Remote Compose binary operations. Tilt your phone and the entire scene reacts to the accelerometer, as if you're holding a real aquarium.

## What is Remote Compose?

[AndroidX Remote Compose](https://developer.android.com/jetpack/androidx/releases/compose-remote) (`androidx.compose.remote`) is an official AndroidX library (currently alpha) that lets you define UI on a server using Kotlin, serialize it into a binary format, and render it on Android without app updates.

Instead of JSON or XML, it captures **actual drawing operations** (rects, ovals, circles, lines, gradients, animations) into a compact binary document that a player renders natively via Canvas.

## Architecture

```
┌──────────────────────────────────────────────────────────────────────┐
│                                                                      │
│                        PRESENTATION LAYER                            │
│                                                                      │
│  ┌─────────────────┐   ┌──────────────────┐   ┌───────────────────┐ │
│  │ AquariumActivity │──▶│ AquariumViewModel │──▶│  AquariumScreen   │ │
│  │                  │   │                  │   │                   │ │
│  │ Fullscreen       │   │ Loads document   │   │ Hosts player +   │ │
│  │ immersive mode   │   │ Exposes sensor   │   │ bridges sensor   │ │
│  │                  │   │ data flow        │   │ data to player   │ │
│  └─────────────────┘   └──────┬───────────┘   └────────┬──────────┘ │
│                               │                        │            │
│                    ┌──────────┘                        │            │
│                    │                                    │            │
│  ┌─────────────────▼──────────────────┐  ┌─────────────▼──────────┐ │
│  │        SensorDataProvider          │  │  RemoteComposePlayer   │ │
│  │                                    │  │  ┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄  │ │
│  │  DeviceSensorDataProvider          │  │  REMOTE COMPOSE UI     │ │
│  │   └─ SensorManager (accelerometer) │  │                        │ │
│  │   └─ SensorDataMapper (normalize)  │  │  Renders binary doc    │ │
│  │   └─ Flow<SensorData> output       │──▶  setUserLocalFloat()   │ │
│  │                                    │  │  pushes sensor values  │ │
│  └────────────────────────────────────┘  └────────────────────────┘ │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
                               │
                    GetAquariumSceneUseCase
                               │
┌──────────────────────────────▼───────────────────────────────────────┐
│                                                                      │
│                          DOMAIN LAYER                                │
│                                                                      │
│  ┌────────────────────┐  ┌──────────────┐  ┌──────────────────────┐ │
│  │  AquariumDocument   │  │  SensorData   │  │  AquariumRepository  │ │
│  │                     │  │              │  │  (interface)          │ │
│  │  documentBytes[]    │  │  accelX      │  │                      │ │
│  │  sensorVariableNames│  │  accelY      │  │  getAquariumDocument()│ │
│  └────────────────────┘  └──────────────┘  └──────────────────────┘ │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
                               │
                    AquariumRepositoryImpl
                               │
┌──────────────────────────────▼───────────────────────────────────────┐
│                                                                      │
│                           DATA LAYER                                 │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐    │
│  │                    AquariumDataSource (interface)             │    │
│  │                           ▲                                  │    │
│  │              ┌────────────┴────────────┐                     │    │
│  │              │                         │                     │    │
│  │  ┌──────────▼──────────┐  ┌───────────▼──────────┐          │    │
│  │  │ MockAquariumDataSource│  │ RemoteAquariumDataSource│          │    │
│  │  │ (current)            │  │ (future — HTTP)       │          │    │
│  │  └──────────┬──────────┘  └───────────────────────┘          │    │
│  └─────────────┼────────────────────────────────────────────────┘    │
│                │                                                     │
│  ┌─────────────▼──────────────────────────────────────────────────┐  │
│  │              REMOTE COMPOSE DOCUMENT CREATION                  │  │
│  │         ┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄                │  │
│  │  This is what a real server would do — build binary documents  │  │
│  │                                                                │  │
│  │  ┌────────────────────────┐                                    │  │
│  │  │ AquariumDocumentBuilder │◀── Orchestrator                   │  │
│  │  │                        │    Registers sensor named floats   │  │
│  │  │  RemoteComposeContext  │    Creates canvas, delegates to:   │  │
│  │  └───────────┬────────────┘                                    │  │
│  │              │                                                 │  │
│  │    ┌─────────┼──────────┬──────────┬──────────┐               │  │
│  │    ▼         ▼          ▼          ▼          ▼               │  │
│  │  ┌──────┐ ┌──────┐ ┌────────┐ ┌───────┐ ┌────────┐          │  │
│  │  │Water │ │Sand  │ │Seaweed │ │ Fish  │ │Bubble  │          │  │
│  │  │Layer │ │Floor │ │Builder │ │Builder│ │Builder │          │  │
│  │  │Builder│ │Builder│ │        │ │       │ │        │          │  │
│  │  └──────┘ └──────┘ └────────┘ └───────┘ └────────┘          │  │
│  │                                                                │  │
│  │  Each builder writes drawing operations (drawOval, drawCircle, │  │
│  │  drawRect, drawLine) + animation expressions (sin, cos, time)  │  │
│  │  + sensor expressions (accelX, accelY) into the binary buffer  │  │
│  │                                                                │  │
│  │  Output: ByteArray (binary document, ~14KB)                    │  │
│  └────────────────────────────────────────────────────────────────┘  │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────┐
│                        DI LAYER (Hilt)                               │
│                                                                      │
│  AppModule  — binds repository, sensor provider, provides            │
│               SensorManager                                          │
│  DataModule — binds AquariumDataSource (swap point: mock → server)   │
└──────────────────────────────────────────────────────────────────────┘
```

### What is Remote Compose UI vs what is not

| Component | Remote Compose? | Description |
|-----------|:-:|---|
| `RemoteComposePlayer` | **Yes** | Android View that renders binary documents |
| `AquariumDocumentBuilder` | **Yes** | Creates the binary document using `RemoteComposeContext` |
| `WaterLayerBuilder`, `FishBuilder`, etc. | **Yes** | Write drawing ops + expressions into the binary buffer |
| `SensorVariableRegistry` | **Bridge** | Defines named float keys shared between creation (`USER:accelX`) and player (`accelX`) |
| `AquariumScreen` | No | Jetpack Compose UI that hosts the `RemoteComposePlayer` via `AndroidView` |
| `AquariumViewModel` | No | Standard MVVM — loads document, exposes sensor flow |
| `DeviceSensorDataProvider` | No | Reads accelerometer via `SensorManager` |
| `SensorDataMapper` | No | Normalizes raw sensor values to -1..1 with smoothing |
| Domain models & repository | No | Clean Architecture contracts |
| Hilt modules | No | Dependency injection wiring |

### Sensor bridge flow

```
Accelerometer (hardware)
    │
    ▼
SensorManager.onSensorChanged()
    │
    ▼
SensorDataMapper  ──────────────  normalizes to -1..1, low-pass filter
    │
    ▼
StateFlow<SensorData>
    │
    ▼
collectAsStateWithLifecycle()  ──  Compose recomposition on change
    │
    ▼
RemoteComposePlayer.setUserLocalFloat("accelX", value)
    │
    ▼
Remote Compose expression engine  ──  expressions like
    │                                  rf(324f) + sin(t * 0.4f) * 162f + accelX * 40f
    ▼                                  re-evaluate with new sensor value
Canvas redraws with shifted positions
```

## Build & Run

```bash
./gradlew assembleDebug            # Build debug APK
./gradlew :app:testDebugUnitTest   # Run unit tests
```

## Tech Stack

| Concern | Library | Version |
|---------|---------|---------|
| Remote UI | AndroidX Remote Compose | 1.0.0-alpha07 |
| UI | Jetpack Compose | BOM 2026.03.00 |
| DI | Hilt | 2.59.2 |
| Build | AGP 9.0.0 / Gradle 9.1.0 / Kotlin 2.1.10 | |
| Testing | JUnit 5 + MockK + Turbine | |
| Min SDK | 29 | |

## Swapping mock for a real server

Change one Hilt binding in `DataModule.kt`:

```kotlin
// From:
@Binds abstract fun bindAquariumDataSource(impl: MockAquariumDataSource): AquariumDataSource

// To:
@Binds abstract fun bindAquariumDataSource(impl: RemoteAquariumDataSource): AquariumDataSource
```

Where `RemoteAquariumDataSource` fetches the binary document bytes over HTTP.

## Lessons learned from Remote Compose alpha

1. **`ctx.buffer()` returns the full pre-allocated 1MB buffer** — always trim with `ctx.buffer().copyOf(ctx.bufferSize())`
2. **`ComponentWidth()`/`ComponentHeight()` expressions don't work reliably** — use hardcoded pixel dimensions matching the document size instead
3. **`setUserLocalFloat("name", value)` internally prepends `"USER:"`** — register named floats as `"USER:name"` on the creation side via `addNamedFloat("USER:name", default)`
4. **`RFloat.flush()` is essential** — break complex expression chains into intermediate flushed values to avoid buffer overflow
