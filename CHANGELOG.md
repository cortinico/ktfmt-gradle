
# Change Log

This file follows [Keepachangelog](https://keepachangelog.com/) format.
Please add your entries according to this format.

## Unreleased

## Version 0.15.1 *(2023-10-31)*

- Kotlin version reverted to 1.9.10

## Version 0.15.0 *(2023-10-30)*

- Replace class loader isolation with process isolation (#206)
- Do not create tasks for aot srcset on Spring projects (#204)
- Kotlin to 1.9.20

## Version 0.14.0 *(2023-10-09)*

- Add support for Gradle Worker API and Classloader Isolation (#182)
- KtFmt to 0.46
- Kotlin to 1.9.10
- Gradle to 8.4
- AGP to 8.1.2

## Version 0.13.0 *(2023-07-15)*

- Handle implicit dependency error message for KMP project (#171)
- Make sure files in the `build/` folder are skipped (#170)
- Fix incompatibility with KSP (#167)
- The Plugin is now GPG signed (#166)
- KtFmt to 0.44
- Kotlin to 1.9.0
- Gradle to 8.2.1
- AGP to 8.0.2

## Version 0.12.0 *(2023-02-21)*

- KtFmt to 0.43
- Kotlin to 1.8.10
- Gradle to 8.0.1
- AGP to 7.4.1
- Fix bug with KMP tasks clashing with Android Tasks (#130)

## Version 0.11.0 *(2022-10-01)*

- KtFmt to 0.41 
- AGP to 7.3.0

## Version 0.10.0 *(2022-09-09)*

- Add support for Gradle Configuration Cache 
- KtFmt to 0.40

## Version 0.9.0 *(2022-08-29)*

- KtFmt to 0.39
- Gradle to 7.5.1
- Kotlin to 1.7.10
- AGP to 7.2.2
- Kotlinx Coroutines to 1.6.1
- Moved to Version Catalog

## Version 0.8.0 *(2022-02-19)*

- Set Kotlin jvmTarget to 11
- KtFmt to 0.32
- Kotlin to 1.5.31
- Gradle to 7.4

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
