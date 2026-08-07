# Data / Domain / Presentation Refactor — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans or superpowers:subagent-driven-development. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Align every feature with `data` / `domain` / `presentation` packages and add the missing converter `data` layer (`ConverterRepository`).

**Architecture:** Keep a single `:app` module and feature packages. Rename all `feature/*/ui` packages to `presentation`. Move conversion I/O out of `ConverterViewModel` into `ConverterRepository` + `ConverterRepositoryImpl`. Move `ConversionState` into converter `domain`. Player has presentation only (shared `core/player`); home/files already have data+domain.

**Tech Stack:** Kotlin, Jetpack Compose, Hilt, MediaStore, FFmpeg JNI (`FfmpegNative`)

## Global Constraints

- Output folder name remains `"ConverterPro"` (files screen query depends on it).
- No Gradle multi-module split.
- No use-case classes.
- Conversion behavior and batch failure semantics stay unchanged.
- Android types (`Uri`, `Context`) may appear in domain repository signatures (light cleanup).

## File structure (target)

```
feature/home/{data,domain,presentation}
feature/files/{data,domain,presentation}
feature/converter_screen/{data,domain,presentation}
feature/player/presentation
di/ConverterRepositoryModule.kt
```

---

### Task 1: Rename `ui` → `presentation` across features

**Files:**
- Move every file under `feature/*/ui/` to `feature/*/presentation/`
- Update package declarations and imports (including `AppNavGraph.kt`)

- [ ] **Step 1: Move directories**

```powershell
$base = "E:\ConverterPro\app\src\main\java\com\arman\dev\converterpro\feature"
@("home","files","converter_screen","player") | ForEach-Object {
  Move-Item "$base\$_\ui" "$base\$_\presentation"
}
```

- [ ] **Step 2: Replace package/import strings**

In all `.kt` files under `app/src/main/java`, replace:
- `.feature.home.ui` → `.feature.home.presentation`
- `.feature.files.ui` → `.feature.files.presentation`
- `.feature.converter_screen.ui` → `.feature.converter_screen.presentation`
- `.feature.player.ui` → `.feature.player.presentation`

- [ ] **Step 3: Commit**

```bash
git add -A app/src/main/java
git commit -m "refactor: rename feature ui packages to presentation"
```

---

### Task 2: Converter domain — `ConversionState`, `ConversionSettings`, `ConverterRepository`

**Files:**
- Create: `feature/converter_screen/domain/model/ConversionState.kt`
- Create: `feature/converter_screen/domain/model/ConversionSettings.kt`
- Create: `feature/converter_screen/domain/repository/ConverterRepository.kt`
- Delete: `feature/converter_screen/presentation/ConversionState.kt` (after move)
- Modify: `ConverterUiState.kt` import for `ConversionState`

**Interfaces:**
- Produces:

```kotlin
// ConversionState.kt
sealed interface ConversionState {
    data object Idle : ConversionState
    data object PreparingSpace : ConversionState
    data object NamingFile : ConversionState
    data object Converting : ConversionState
    data class Completed(val convertedFileCount: Int) : ConversionState
    data class Failed(val message: String) : ConversionState
}

// ConversionSettings.kt — resolved params only (no dropdown enums required by data)
data class ConversionSettings(
    val outputExtension: String,
    val containerFormat: String,
    val mimeType: String?,
    val encoder: String,
    val bitrateBitsPerSecond: Int?,
    val qualityScale: Float?,
    val sampleRateHz: Int?,
    val channelCount: Int?,
)

// ConverterRepository.kt
interface ConverterRepository {
    suspend fun convert(
        files: List<MediaFile>,
        settings: ConversionSettings,
        onProgress: (ConversionState) -> Unit,
    ): Result<Int>
}
```

- [ ] **Step 1: Add the three domain files; remove old `presentation/ConversionState.kt`; fix imports**
- [ ] **Step 2: Commit**

```bash
git commit -m "refactor(converter): move ConversionState and add repository contract"
```

---

### Task 3: `ConverterRepositoryImpl` + Hilt module

**Files:**
- Create: `feature/converter_screen/data/repository/ConverterRepositoryImpl.kt`
- Create: `di/ConverterRepositoryModule.kt`

**Interfaces:**
- Consumes: `ConverterRepository`, `ConversionSettings`, `ConversionState`, `FfmpegNative`, `MediaFile`
- Produces: working impl bound in Hilt

Move logic from current ViewModel: storage check, cache copy, MediaStore reserve (Music→Download→Documents), FFmpeg convert, mark ready / delete on failure. Progress: `PreparingSpace` / `NamingFile` / `Converting` only. Return `Result.success(count)` or `Result.failure`.

- [ ] **Step 1: Implement repository + module**
- [ ] **Step 2: Commit**

```bash
git commit -m "feat(converter): add ConverterRepository data layer"
```

---

### Task 4: Slim `ConverterViewModel`

**Files:**
- Modify: `feature/converter_screen/presentation/ConverterViewModel.kt`

Inject `ConverterRepository` instead of `@ApplicationContext`. Keep dropdown cascade and rename logic. `convert()` builds `ConversionSettings` via existing `FfmpegSettings` helpers, calls repo, maps `Result` to `Completed` (+ delay) / `Failed`.

- [ ] **Step 1: Rewrite convert path to use repository**
- [ ] **Step 2: Commit**

```bash
git commit -m "refactor(converter): ViewModel uses ConverterRepository"
```

---

### Task 5: Verify build

- [ ] **Step 1: Compile**

```bash
.\gradlew.bat :app:compileDebugKotlin
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 2: Fix any broken imports (e.g. missing `ReusableText` if already broken)**
- [ ] **Step 3: Final commit if fixes needed

---

## Spec coverage

| Spec item | Task |
|-----------|------|
| presentation package naming | 1 |
| ConversionState in domain | 2 |
| ConverterRepository API | 2 |
| ConverterRepositoryImpl + MediaStore/FFmpeg | 3 |
| DI module | 3 |
| ViewModel keeps dropdowns, uses repo | 4 |
| Output folder `ConverterPro` | 3 |
| Manual/build verification | 5 |
