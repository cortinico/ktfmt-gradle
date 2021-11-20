# Change Log

This file follows [Keepachangelog](https://keepachangelog.com/) format.
Please add your entries according to this format.

## Unreleased

- KtFmt to 0.30

## Version 0.7.0 *(2021-08-30)*

- KtFmt to 0.28
- Gradle to 7.2
- Kotlin to 1.5.21
- Android Gradle Plugin to 4.2.2
- Android Ktfmt Tasks will not have `JavaSource` as postfix anymore
- Tasks should fallback to extension value before defaults

## Version 0.6.0 *(2021-05-20)*

- `--include-only` now works with paths that are file-separator independent.
- Added support for Gradle 7.0+
- Added task ordering between `ktfmt*` tasks and `compileKotlin` tasks. This fix the correctness warning introduced with Gradle 7.0

### Dependencies Update

- Gradle to `7.0.2`
- KtFmt to `0.24`

## Version 0.5.0 *(2021-03-20)*

### New features

- Added `--include-only` flag to support pre-commit hooks
- Improved error reporting for scenarios where KtFmt fail to parse
- Added example on how to apply the plugin to the `buildSrc` project

### Dependencies Update

- KtFmt to `0.22`
- Coroutines to `1.4.3`
- AGP to `4.1.3`
- Gradle to `6.8.3`

## Version 0.4.0 *(2021-02-27)*

### New features

- Added the `kotlinLangStyle()` function to the `ktfmt{}` extension. This will allow you to apply a format that attempts to reflect the [official kotlin convetions](https://kotlinlang.org/docs/coding-conventions.html).

### Dependencies Update

- Ktfmt to 0.21

## Version 0.3.0 *(2021-02-23)*

### Dependencies Update

- Ktfmt to 0.20
- Gradle to 6.8.2
- AGP to 4.1.2

## Version 0.2.0 *(2020-12-24)*

### Added

- Added the `googleStyle()` function to the `ktfmt{}` extension to configure indents at `2`.

### Dependencies Update

- Ktfmt to 0.19
- Kotlin to 1.4.21
- kotlinx-coroutines to 1.4.2 

## Version 0.1.0 *(2020-12-22)*

That's the first version of `ktfmt-gradle`
