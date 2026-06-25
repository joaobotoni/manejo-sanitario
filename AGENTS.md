# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run a single unit test class
./gradlew test --tests "com.omni.container.ExampleUnitTest"

# Clean build
./gradlew clean assembleDebug
```

## Architecture Overview

This is a single-Activity Android app (Java, minSdk 24, targetSdk 36) for livestock sanitary management ("Manejo Sanitário"). The entry point is `MainActivity`, which immediately hosts a Fragment and delegates all logic there.

**Layer structure:**

- `data/entities/` — Room `@Entity` classes: `Protocolo` (vaccination/treatment protocol header) and `ProtocoloItem` (individual steps within a protocol). Both implement `Parcelable`. Table names follow the `xgp_` prefix convention.
- `data/dao/` — Room `@Dao` interfaces (`ProtocoloDao`, `ProtocoloItemDao`) with synchronous query methods (no LiveData/Flow — queries must be called off the main thread).
- `data/AppDatabase` — Singleton Room database (`Sample.db`). Seeds initial protocol data in the `onCreate` callback.
- `data/Converters` — `@TypeConverter` for `Date` ↔ `Long` used by Room.
- `ui/states/` — UI state classes (`ProtocoloUiState`, `ProtocoloItemUiState`, `ProtocoloItemAplicacaoUiState`, `OrigemItem` enum). These are Parcelable view-models without lifecycle awareness.
- `ui/adapters/` — RecyclerView/ArrayAdapter adapters for displaying protocols and medication items.
- `ui/fragments/` — All UI logic lives here, not in `MainActivity`.

**Threading pattern:** Each Fragment contains private static inner `Executor` and `Data` classes that wrap a `ThreadPoolExecutor` + main-thread `Handler`. The `Executor.execute()` methods accept a DAO extractor function and a query function, run them on the pool, then `post()` the result back to the main thread. The `cancelled` flag prevents callbacks from running after `onDestroyView`. Fragments null out all view references in `onDestroyView` and call `executor.close()`.

**Fragment navigation:** Navigation is done manually via `FragmentManager.beginTransaction()`. `ManejoSanitarioFragment` is the main screen; `ConsultaMedicamentoFragment` is pushed onto the back stack when searching for individual medications.

**Domain language:** The codebase is in Portuguese. `Protocolo` = protocol, `Item` = protocol step, `Aplicacao` = application/administration of a medication, `Manejo Sanitário` = sanitary/health management, `Prazo de Carência` = withdrawal period.

## Key Design Notes

- No ViewModel or LiveData — threading is handled manually via the inner `Executor`/`Data` pattern duplicated across fragments. If extracting this pattern, do so without breaking the cancellation contract.
- Room DAOs use synchronous methods; never call them on the main thread.
- `OrigemItem` enum distinguishes whether a medication item came from a `PROTOCOLO` (preset protocol) or was added individually (`AVULSO`).
- The `status_app` field on `Protocolo` uses `'A'` (ativo/active). The `ativo` field uses `'S'`/`'N'` (sim/não).
- `ConsultaMedicamentoFragment` is a scaffold — its `init()`, `setupViews()`, and `setupClickListeners()` methods are empty stubs.