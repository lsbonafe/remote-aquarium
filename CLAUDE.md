# RemoteAquarium

Interactive neon aquarium app showcasing AndroidX Remote Compose (server-driven UI). The entire visual scene is rendered from a Remote Compose binary document. 18 fish and 6 bubbles react to phone tilt with real physics (gravity, momentum, drag, wall bouncing, fish-to-fish collision, idle swimming). Tap to drop food — fish rotate to face their target, chase it, open their mouth to swallow it, and settle facing left or right.

## Build & Test

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew :app:testDebugUnitTest # Run unit tests
adb install app/build/outputs/apk/debug/app-debug.apk  # Install on device
```

## Architecture Principles

- **Clean Architecture**: domain/data/presentation layers with strong interface contracts
- **SOLID**: single responsibility per class, dependency inversion via interfaces, open for extension
- **TDD**: tests written alongside implementation, full unit test coverage
- **Modular**: each layer connects through interfaces — data source is swappable (mock → server) via one Hilt binding
- **Declarative**: drawing DSL (`fish()`, `circle()`, `line()`, `rect()`) over imperative RC API; specs declare fractions, `resolve()` computes positions; colors via `NeonPalette`; physics constants named

## Package Structure

```
com.remoteaquarium/
├── domain/                     # Pure Kotlin — no Android imports
│   ├── model/                  # AquariumDocument, SensorData, SensorVariableNames
│   ├── repository/             # AquariumRepository (interface)
│   └── usecase/                # GetAquariumSceneUseCase
├── data/                       # Implementation layer
│   ├── datasource/             # AquariumDataSource (interface) + MockAquariumDataSource
│   ├── repository/             # AquariumRepositoryImpl
│   └── document/               # Remote Compose binary document creation
│       ├── AquariumDocumentBuilder   # Orchestrator — registers named floats, layers builders
│       ├── DrawingDsl                # Declarative extensions: fish(), rotatedFish(), circle(), line(), rect(), oval()
│       ├── NeonPalette               # Named color constants (CYAN, MAGENTA, HOT_PINK, etc.)
│       ├── AquariumLayout            # Shared layout constants (sand top fraction)
│       ├── WaterLayerBuilder         # Dark gradient + animated neon waves
│       ├── FishBuilder               # 18 neon fish with rotation at physics-driven positions
│       ├── BubbleBuilder             # 6 neon bubbles at physics-driven positions
│       ├── FoodBuilder               # Up to 50 food particles with glow (tap-to-feed)
│       ├── SeaweedBuilder            # 8 swaying stalks (spec/resolve pattern)
│       ├── SandFloorBuilder          # Dark floor with neon pebbles and coral (spec/resolve pattern)
│       └── SensorVariableRegistry    # Named float key constants (USER: prefix convention)
├── presentation/               # Android UI layer
│   ├── AquariumActivity              # Fullscreen immersive mode, @AndroidEntryPoint
│   ├── AquariumViewModel             # Loads document, runs physics, exposes flows
│   ├── AquariumScreen                # Hosts RemoteComposePlayer, pushes 203 named floats per frame
│   ├── AquariumUiState               # Sealed interface: Loading, Ready, Error
│   ├── sensor/                       # Accelerometer integration
│   │   ├── SensorDataProvider        # Interface: Flow<SensorData>, start(), stop()
│   │   ├── DeviceSensorDataProvider  # SensorManager + TYPE_ACCELEROMETER listener
│   │   └── SensorDataMapper          # Normalizes raw values to -1..1 with EMA smoothing
│   └── physics/                      # App-side physics simulation
│       ├── AquariumPhysicsEngine     # Orchestrates per-frame pipeline via delegation
│       ├── FishMotion                # Fish forces: chase food, idle swim, or follow tilt
│       ├── FacingDirection           # Fish heading: face food, or settle to nearest side (left/right)
│       ├── BubblePhysics             # Bubble lifecycle: rise with buoyancy, respawn at bottom
│       ├── FoodManager               # Spawns food on tap, tracks particles, handles eating
│       ├── MouthAnimation            # Mouth open/close: snap open on eat, gradually close
│       ├── IdleDetector              # Detects no-tilt for 5s, blends to idle swimming
│       ├── CollisionResolver         # Fish-to-fish collision separation + velocity exchange
│       ├── PhysicsWorld              # World boundaries, clamp and bounce
│       └── FishConfigs               # Initial positions, drag, gravity per fish tier
└── di/                         # Hilt DI modules
    ├── AppModule                     # Binds repository, sensor provider, provides SensorManager
    └── DataModule                    # Binds data source (swap point: mock → server)
```

## Remote vs Local Boundary

**Remote (binary document — can change from server without app update):**
- All visuals: fish shapes/colors/sizes, background, sand, seaweed, bubbles, waves, food particles
- Animation expressions: seaweed sway (sin/cos + time), wave animation
- Fish rotation rendering (rotatedFish DSL using cos/sin named floats)
- Where to draw elements relative to named float positions

**Local (baked into APK):**
- Physics simulation: FishMotion (forces), FacingDirection (heading), BubblePhysics (lifecycle), FoodManager (spawn/eat)
- Sensor pipeline: accelerometer reading, normalization, smoothing
- The bridge: `setUserLocalFloat()` pushing 203 values per frame to the player
- Architecture glue: Hilt, ViewModel, Activity, Compose hosting

**Contract between remote and local:** string-named float variables (`USER:` prefix on creation side, bare name on player side). Document declares them with `addNamedFloat()`, app pushes with `setUserLocalFloat()`. No schema, no type safety — just string matching.

## Key Lessons (Remote Compose alpha07)

1. **`ctx.buffer()` returns the full 1MB pre-allocated buffer** — always trim: `ctx.buffer().copyOf(ctx.bufferSize())`
2. **`ComponentWidth()`/`ComponentHeight()` don't work** — pass actual screen dimensions as plain `Float` instead
3. **`setUserLocalFloat("name", v)` internally prepends `"USER:"`** — register as `addNamedFloat("USER:name", default)` on creation side
4. **`RFloat.flush()` is essential** for complex expression chains to avoid buffer overflow
5. **Builder delegation works** — draw calls across method boundaries are fine as long as dimensions are plain `Float`, not `RFloat` expressions
6. **`setRootContentBehavior()` crashes the player** — operation ID 65 not recognized; build document at actual screen size instead
7. **Document must match screen size** — pass `displayMetrics` dimensions to builder; hardcoded sizes cause gaps on different devices
8. **`accelY` is useless in document expressions** — raw value ~0.55 constant (gravity); only works app-side after subtracting rest baseline
9. **Particle system API exists but didn't work** — `createParticles`/`particlesLoop` rendered but never updated; physics runs app-side instead

## Swapping Mock for Real Server

Change one binding in `di/DataModule.kt`:
```kotlin
@Binds
abstract fun bindAquariumDataSource(impl: RemoteAquariumDataSource): AquariumDataSource
```
Where `RemoteAquariumDataSource` fetches `ByteArray` over HTTP and wraps in `AquariumDocument`.

## Tech Stack

- Kotlin 2.1.10 (via AGP built-in) / AGP 9.0.0 / Gradle 9.1.0
- AndroidX Remote Compose 1.0.0-alpha07
- Jetpack Compose (BOM 2026.03.00)
- Hilt 2.59.2 (KSP 2.1.10-1.0.31)
- JUnit 5 + MockK + Turbine for testing
- Min SDK 29, Target SDK 35

## Code Standards

- Domain layer must remain Android-free (pure Kotlin only)
- All implementation plans must include testing coverage strategy
- After code changes, evaluate whether test coverage needs updating
- Tests follow TDD: write test, then implement
- Prefer interfaces at layer boundaries for testability and swappability
- Drawing code uses declarative DSL — no direct `writer.rcPaint.setColor().commit()` in builders
- Colors via `NeonPalette` constants — no hex literals in builders
- Layout specs declare fractions, `resolve()` computes pixel positions — no inline `w * 0.42f` math in draw calls
- Physics constants named in companion object — no magic numbers
