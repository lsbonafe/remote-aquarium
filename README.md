# RemoteAquarium

An interactive neon aquarium Android app showcasing **AndroidX Remote Compose** — a server-driven UI framework that serializes drawing operations into compact binary documents rendered natively on Android.

18 neon fish swim with physics-based movement (gravity, momentum, drag, wall bouncing, fish-to-fish collision). Bubbles rise and recycle. Seaweed sways. Waves ripple. Tilt your phone and everything reacts — fish drift toward the lowest point, bounce off screen edges, and settle naturally.

## What is Remote Compose?

[AndroidX Remote Compose](https://developer.android.com/jetpack/androidx/releases/compose-remote) (`androidx.compose.remote`) is an official AndroidX library (currently alpha) that lets you define UI on a server using Kotlin, serialize it into a binary format, and render it on Android without app updates.

Instead of JSON or XML, it captures **actual drawing operations** (ovals, circles, lines, gradients, animations) into a compact binary document that a player renders natively via Canvas.

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
│  │ immersive mode   │   │ Runs physics     │   │ pushes positions  │ │
│  │                  │   │ Exposes flows    │   │ to player         │ │
│  └─────────────────┘   └──────┬───────────┘   └────────┬──────────┘ │
│                               │                        │            │
│            ┌──────────────────┤                        │            │
│            │                  │                        │            │
│  ┌─────────▼────────┐  ┌─────▼──────────────┐  ┌──────▼───────────┐ │
│  │  SensorDataProvider│  │ PhysicsEngine      │  │RemoteComposePlayer│ │
│  │                   │  │                    │  │  ┄┄┄┄┄┄┄┄┄┄┄┄┄  │ │
│  │  Accelerometer    │  │ Per-fish gravity,  │  │  REMOTE COMPOSE  │ │
│  │  + SensorMapper   │  │ drag, collision,   │  │  UI              │ │
│  │  + EMA smoothing  │──▶ wall bounce,       │  │                  │ │
│  │                   │  │ idle swimming      │──▶ setUserLocalFloat │ │
│  │  Flow<SensorData> │  │                    │  │ pushes 28 floats │ │
│  └───────────────────┘  │ Flow<PhysicsState> │  │ per frame        │ │
│                         └────────────────────┘  └──────────────────┘ │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
                               │
                    GetAquariumSceneUseCase(screenWidth, screenHeight)
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
│  │  ┌────────────────────────┐  ┌──────────────┐                 │  │
│  │  │ AquariumDocumentBuilder │  │  DrawingDsl   │                 │  │
│  │  │ Orchestrator            │  │  rect/oval/   │                 │  │
│  │  │ Registers named floats  │  │  circle/line/ │                 │  │
│  │  │ Delegates to builders   │  │  fish          │                 │  │
│  │  └───────────┬────────────┘  └──────────────┘                 │  │
│  │              │                                                 │  │
│  │    ┌─────────┼──────────┬──────────┬──────────┐               │  │
│  │    ▼         ▼          ▼          ▼          ▼               │  │
│  │  ┌──────┐ ┌──────┐ ┌────────┐ ┌───────┐ ┌────────┐          │  │
│  │  │Water │ │Sand  │ │Seaweed │ │ Fish  │ │Bubble  │          │  │
│  │  │Layer │ │Floor │ │Builder │ │Builder│ │Builder │          │  │
│  │  └──────┘ └──────┘ └────────┘ └───────┘ └────────┘          │  │
│  │                                                                │  │
│  │  Supporting: NeonPalette (colors), AquariumLayout (constants)  │  │
│  │                                                                │  │
│  │  Builders use declarative DSL: fish(), circle(), line(), etc.  │  │
│  │  Specs declare fractions, resolve() computes pixel positions   │  │
│  │  Document sized to actual screen via displayMetrics            │  │
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
| `DrawingDsl` | **Yes** | Declarative extension functions (`fish()`, `circle()`, `line()`, etc.) over imperative RC API |
| `WaterLayerBuilder`, `FishBuilder`, etc. | **Yes** | Write drawing ops + expressions into the binary buffer |
| `NeonPalette` | **Yes** | Named color constants used by builders |
| `SensorVariableRegistry` | **Bridge** | Named float keys shared between creation (`USER:accelX`) and player (`accelX`) |
| `AquariumScreen` | No | Jetpack Compose UI that hosts the `RemoteComposePlayer` via `AndroidView` |
| `AquariumViewModel` | No | Loads document, runs physics, exposes flows |
| `AquariumPhysicsEngine` | No | App-side physics: gravity, drag, collision, idle swimming |
| `DeviceSensorDataProvider` | No | Reads accelerometer via `SensorManager` |
| `SensorDataMapper` | No | Normalizes raw sensor values to -1..1 with EMA smoothing |
| Domain models & repository | No | Clean Architecture contracts |
| Hilt modules | No | Dependency injection wiring |

### How physics and rendering connect

```
Accelerometer (hardware)
    │
    ▼
SensorManager.onSensorChanged()
    │
    ▼
SensorDataMapper  ───────────────  normalizes to -1..1, EMA smoothing
    │
    ├──▶ AquariumPhysicsEngine   ──  per-fish: gravity from tilt, drag,
    │       │                        wall bounce, fish-to-fish collision,
    │       │                        idle swimming when still for 5s
    │       ▼
    │    PhysicsState (18 fish + 6 bubble positions)
    │       │
    │       ▼
    │    setUserLocalFloat("fish0X", 423f)  ──  28 position floats
    │    setUserLocalFloat("fish0Y", 891f)      pushed per frame
    │
    └──▶ setUserLocalFloat("accelX", 0.3f)  ──  for waves + seaweed
            │
            ▼
    RemoteComposePlayer renders document with current values
```

### Idle swimming

When no tilt change is detected for 5 seconds, fish transition from physics-reactive mode to organic swimming — each fish has a unique sin/cos pattern with different speed and phase. The blend is gradual over 2 seconds. Any tilt change instantly switches back to physics mode.

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

## Lessons learned from Remote Compose alpha07

1. **`ctx.buffer()` returns the full pre-allocated 1MB buffer** — always trim with `ctx.buffer().copyOf(ctx.bufferSize())`
2. **`ComponentWidth()`/`ComponentHeight()` expressions don't work reliably** — pass actual screen dimensions as plain `Float` instead
3. **`setUserLocalFloat("name", value)` internally prepends `"USER:"`** — register named floats as `"USER:name"` on the creation side via `addNamedFloat("USER:name", default)`
4. **`RFloat.flush()` is essential** — break complex expression chains into intermediate flushed values to avoid buffer overflow
5. **`setRootContentBehavior()` crashes the player** — operation ID 65 is not recognized by the player; build the document at actual screen size instead
6. **Document size must match screen** — pass `displayMetrics.widthPixels/heightPixels` to the builder; hardcoded dimensions cause gaps on different devices
7. **`accelY` is useless in document expressions** — raw value is ~0.55 constant (gravity); only works app-side after subtracting the rest baseline
8. **Delegate draw calls across methods works fine** — builder objects can call DSL functions on the shared `RemoteComposeContext` as long as dimensions are plain `Float`, not `RFloat` expressions
