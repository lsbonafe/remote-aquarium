# RemoteAquarium

Interactive aquarium app showcasing AndroidX Remote Compose (server-driven UI). Fish, bubbles, water, and seaweed are rendered from a Remote Compose binary document. The aquarium reacts to phone accelerometer — tilt the device and water/fish shift like a real aquarium.

## Build & Test

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew :app:testDebugUnitTest # Run unit tests (25 tests)
```

## Architecture

Clean Architecture with package-level separation:

```
com.remoteaquarium/
├── domain/                  # Pure Kotlin contracts
│   ├── model/               # AquariumDocument, SensorData, SensorVariableNames
│   ├── repository/          # AquariumRepository interface
│   └── usecase/             # GetAquariumSceneUseCase
├── data/                    # Implementation layer
│   ├── datasource/          # AquariumDataSource interface + MockAquariumDataSource
│   ├── repository/          # AquariumRepositoryImpl
│   └── document/            # Remote Compose document builders
│       ├── AquariumDocumentBuilder  # Orchestrator — registers sensor vars, layers builders
│       ├── WaterLayerBuilder        # Blue gradient + animated waves + accel offset
│       ├── FishBuilder              # 6 fish with time-based swimming + sensor reactivity
│       ├── BubbleBuilder            # 12 rising bubbles with wobble
│       ├── SeaweedBuilder           # 8 swaying stalks
│       ├── SandFloorBuilder         # Sandy bottom with pebbles and coral
│       └── SensorVariableRegistry   # Named float constants (accelX, accelY)
├── presentation/            # Android UI layer
│   ├── AquariumActivity     # Fullscreen immersive mode
│   ├── AquariumViewModel    # Loads document + exposes sensor data
│   ├── AquariumScreen       # Hosts RemoteComposePlayer via AndroidView
│   └── sensor/              # Accelerometer integration
│       ├── SensorDataProvider       # Interface for sensor data flow
│       ├── DeviceSensorDataProvider # SensorManager + accelerometer listener
│       └── SensorDataMapper         # Normalizes raw values to -1..1
└── di/                      # Hilt modules
    ├── AppModule            # Binds repository, sensor provider, provides SensorManager
    └── DataModule           # Binds data source (swap point for mock → server)
```

## Key Design Decisions

- **Remote Compose procedural API** (`RemoteComposeContext`) used for document creation — this is the same API a real server would use (plain JVM, no Android SDK dependency)
- **`RFloat.flush()`** used to break complex expression chains that exceed buffer limits
- **`AndroidView` + `RemoteComposePlayer`** used instead of `RemoteDocumentPlayer` composable for direct control over `setUserLocalFloat()` sensor injection
- **Sensor bridge pattern**: Accelerometer → `SensorDataMapper` → `StateFlow<SensorData>` → `collectAsStateWithLifecycle()` → `RemoteComposePlayer.setUserLocalFloat()`

## Swapping Mock for Real Server

Change one Hilt binding in `DataModule.kt`:
```kotlin
@Binds
abstract fun bindAquariumDataSource(impl: RemoteAquariumDataSource): AquariumDataSource
```
Where `RemoteAquariumDataSource` fetches bytes from HTTP and wraps in `AquariumDocument`.

## Tech Stack

- Kotlin 2.2.10 (AGP built-in), AGP 9.1.0, Gradle 9.3.1
- AndroidX Remote Compose 1.0.0-alpha07
- Jetpack Compose (BOM 2026.03.00)
- Hilt 2.59.2
- JUnit 5 + MockK + Turbine
- Min SDK 29, Target SDK 35
