# Converter repository light cleanup — design

**Date:** 2026-08-07  
**Status:** Approved for planning  
**Scope:** App-wide `data` / `domain` / `presentation` package layout + converter data layer (single `:app` module)

## Goal

Rename feature `ui` packages to `presentation`, and align the converter feature with home/files by adding the missing `data` layer (`ConverterRepository`), without multi-module splits, use cases, or Android-free domain models.

## Current state

- `home` and `files` already expose `domain/repository` + `data/repository` + Hilt modules.
- `converter_screen` has domain models (`Extension`, `Encoder`, `Map`, …) but all conversion I/O lives in `ConverterViewModel` (storage check, cache copy, MediaStore output reservation, FFmpeg invocation).
- `player` remains UI + `core/player`; unchanged by this work.
- Domain APIs may continue to use Android types (`Uri`, `MediaFile`) — intentional for this light cleanup.

## Architecture

Rename feature `ui` packages to `presentation`. Add the missing converter `data` repository pair:

```
feature/home|files|converter_screen/{data,domain,presentation}
feature/player/presentation
```

**Dependency rule:** `presentation` → `domain` ← `data`. ViewModel depends on `ConverterRepository` only. `FfmpegNative` / MediaStore stay in the data impl (and existing `core/ffmpeg`).

Home and files keep existing repositories. Player stays presentation + `core/player`. Shared types: `MediaFile`, `FfmpegConversionCommand`.

**`ConversionState`:** lives in `domain/model`. The repository must not depend on `presentation`. `ConverterUiState` continues to hold a `ConversionState` field.

## Components and data flow

### ConverterRepository (domain)

Single responsibility: convert files with explicit settings.

```kotlin
suspend fun convert(
    files: List<MediaFile>,
    settings: ConversionSettings,
    onProgress: (ConversionState) -> Unit,
): Result<Int>  // success = converted count
```

`ConversionSettings` is a domain value object holding the resolved conversion parameters the ViewModel already computes from dropdowns (container format / MIME, encoder, CBR bitrate or VBR quality, sample rate, channel count, output extension). Exact field names follow existing domain model helpers (`containerFormat`, `ffmpegEncoder`, `bitsPerSecond`, etc.). Place it under `domain/model`.

Progress callbacks report in-flight stages only: `PreparingSpace`, `NamingFile`, `Converting`. Terminal outcomes use the `Result`: success → ViewModel sets `Completed(count)`, delays, then `Idle`; failure → ViewModel sets `Failed(message)`.

### ConverterRepositoryImpl (data)

Owns logic moved out of `ConverterViewModel`:

- Ensure enough storage for the batch
- Copy each input URI into app cache
- Reserve MediaStore output (Music → Download → Documents fallback for unresolved MIME types)
- Build `FfmpegConversionCommand` and call `FfmpegNative.convert`
- Mark output ready (`IS_PENDING = 0`) or delete failed pending outputs
- Invoke `onProgress` for PreparingSpace / NamingFile / Converting

Output folder name must stay `"ConverterPro"` so the files screen query continues to find outputs. The completed-state visibility delay stays in the ViewModel.

### ConverterViewModel (ui)

Keeps:

- Dropdown cascade (`Map`, extension / encoder / bitrate / sample rate / channel)
- Rename and output file-name updates
- Mapping selections → `ConversionSettings`
- Calling the repository, applying `onProgress` to `ConverterUiState`, and mapping `Result` to Completed (with delay) / Failed
- `dismissConversionError` → Idle

Drops direct use of `ContentResolver`, `StatFs`, cache file I/O, and `FfmpegNative` once the repository is wired.

### DI

Add `ConverterRepositoryModule` in `di/`, same `@Provides` + `@Singleton` pattern as `HomeRepositoryModule` / `FilesRepositoryModule`.

## Error handling

- Repository returns `Result.failure` with a clear message for storage, open-input, reserve-output, and FFmpeg failures.
- ViewModel maps failures to `ConversionState.Failed` and keeps existing dismiss → Idle UX.
- Batch semantics unchanged: if file *n* fails, delete that file’s pending output and fail the whole run. No new retry policy.

## Testing

- No new automated test suite in this pass.
- Manual verification:
  - Pick files → convert common formats (e.g. AAC) → appear on files screen
  - Niche containers still fall back to Download/Documents
  - Failed conversion still surfaces `ConversionState.Failed` and dismiss recovers

## Out of scope

- Gradle multi-module split
- Renaming `ui` → `presentation`
- Use-case classes
- Shared MediaStore helpers in `core`
- Player domain façade
- Making domain Android-free
- Behavior changes to conversion quality, formats, or player

## Success criteria

- `ConverterViewModel` no longer performs MediaStore/FFmpeg I/O directly
- Converter follows the same repository binding pattern as home/files
- Existing conversion UX and output locations behave as before
