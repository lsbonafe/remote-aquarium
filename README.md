# RemoteAquarium

An interactive neon aquarium Android app showcasing **AndroidX Remote Compose** — a server-driven UI framework that serializes drawing operations into compact binary documents rendered natively on Android.

18 neon fish swim with real physics (gravity, momentum, drag, wall bouncing, fish-to-fish collision). Tap to drop food — fish rotate to face their target, chase it down, and eat it. After eating they settle to face left or right based on their last heading. Bubbles rise and recycle. Seaweed sways. Waves ripple. Tilt your phone and everything reacts. Leave it still for 5 seconds and fish begin idle swimming on their own.

https://github.com/user-attachments/assets/739a3bef-7ff4-4238-aa39-7042a449c7c3

## What is Remote Compose?

[AndroidX Remote Compose](https://developer.android.com/jetpack/androidx/releases/compose-remote) (`androidx.compose.remote`) is an official AndroidX library (currently alpha) that lets you define UI on a server using Kotlin, serialize it into a binary format, and render it on Android without app updates.

Instead of JSON or XML, it captures **actual drawing operations** (ovals, circles, lines, gradients, animations) into a compact binary document that a player renders natively via Canvas.

## How it compares to other approaches

There are several ways to update UI without an app release. Each trades off different things:

| | WebView | OTA (CodePush) | JSON SDUI | DivKit | Remote Compose |
|---|---|---|---|---|---|
| **What the server sends** | HTML/CSS/JS | JS bundle | JSON component tree | JSON + expressions | Binary drawing ops + math expressions |
| **Rendering** | Web engine | Framework runtime | Native components | Built-in component set | Native Canvas |
| **Platforms** | All | RN / Flutter | Any (custom) | Android, iOS, Web, Flutter | Android only |
| **Custom visuals** | Unlimited | Unlimited | Limited to registered components | Built-in + extensions | Unlimited (Canvas primitives) |
| **Animations** | CSS / JS | Full framework | Client-side or Lottie embed | Transitions + Lottie/Rive | Math expressions evaluated per frame |
| **Data-reactive animations** | Yes (JS) | Yes | No | Partial (variable triggers) | Yes (expressions reference live named floats) |
| **Performance** | Web overhead | Good | Good | Good | Native Canvas |
| **Maturity** | Mature | Mature | Mature | Production (Yandex) | Alpha |

Remote Compose's unique position is at the **drawing-operation level** — the server doesn't describe components ("a button", "a card"), it describes **what to draw** ("an oval at x,y with this color") and **how to animate it** ("oscillate x using sin(time * speed + phase)"). Those animation expressions are evaluated every frame and can reference named variables the app updates in real-time.

The tradeoff is real: Android only, alpha stability, no component system, no visual editor. For forms and content screens, JSON SDUI or DivKit is more practical. Remote Compose shines when visuals go beyond what pre-built components can express.

## Why an aquarium?

The aquarium uses capabilities that are specific to Remote Compose's drawing-operation level:

- **Seaweed** sways using `sin(time * speed + phase)` expressions that also reference `accelX` — the sway reacts to phone tilt in real-time. A Lottie file could animate seaweed, but it couldn't make it respond to live sensor data.
- **Fish rotation** uses `cos²/sin²` dimension swapping in RFloat expressions, driven by angle floats the physics engine pushes 60 times per second. No pre-baked animation can depend on runtime physics output.
- **Waves** ripple with time-based sin expressions that shift with accelerometer tilt.

All of this lives in the binary document — changeable from a server without an app update. The app only provides the physics simulation and sensor data; the entire visual scene, including its animation behavior, is remote.

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
│  │                   │  │ idle swimming,     │──▶ setUserLocalFloat │ │
│  │  Flow<SensorData> │  │ food, rotation     │  │ pushes 185 floats│ │
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
│  │    ┌─────────┼──────────┬──────────┬──────────┬────────┐      │  │
│  │    ▼         ▼          ▼          ▼          ▼        ▼      │  │
│  │  ┌──────┐ ┌──────┐ ┌────────┐ ┌───────┐ ┌────────┐ ┌──────┐│  │
│  │  │Water │ │Sand  │ │Seaweed │ │ Fish  │ │Bubble  │ │ Food ││  │
│  │  │Layer │ │Floor │ │Builder │ │Builder│ │Builder │ │Builder││  │
│  │  └──────┘ └──────┘ └────────┘ └───────┘ └────────┘ └──────┘│  │
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
| `WaterLayerBuilder`, `FishBuilder`, `FoodBuilder`, etc. | **Yes** | Write drawing ops + expressions into the binary buffer |
| `NeonPalette` | **Yes** | Named color constants used by builders |
| `SensorVariableRegistry` | **Bridge** | Named float keys shared between creation (`USER:accelX`) and player (`accelX`) |
| `AquariumScreen` | No | Jetpack Compose UI that hosts the `RemoteComposePlayer` via `AndroidView` |
| `AquariumViewModel` | No | Loads document, runs physics, exposes flows |
| `AquariumPhysicsEngine` | No | Orchestrates per-frame physics pipeline via delegation |
| `FishMotion` | No | Fish forces: chase food, idle swim, or follow tilt |
| `FacingDirection` | No | Fish heading: face food, settle to nearest side, or return to default |
| `BubblePhysics` | No | Bubble lifecycle: rise with buoyancy, respawn at bottom |
| `FoodManager` | No | Spawns food on tap, tracks particles, handles eating |
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
    ├──▶ AquariumPhysicsEngine   ──  delegates to:
    │       │                        FishMotion (chase/idle/tilt forces)
    │       │                        FacingDirection (heading toward food)
    │       │                        BubblePhysics (rise + respawn)
    │       │                        FoodManager (spawn, sink, eat)
    │       ▼
    │    PhysicsState (18 fish + angles + 50 food + 6 bubbles)
    │       │
    │       ▼
    │    setUserLocalFloat("fish0X", 423f)  ──  185 floats
    │    setUserLocalFloat("fish0AC", 0.9f)     pushed per frame
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
