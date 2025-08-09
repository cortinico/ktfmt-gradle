# Change Log

This file follows [Keepachangelog](https://keepachangelog.com/) format.
Please add your entries according to this format.

## Unreleased
- Add support for android projects with the `com.android.kotlin.multiplatform.library` plugin (#422)

- Add `ktfmt.useClassloaderIsolation` property to toggle between processIsolation and classloaderIsolation for the Gradle Worker (#427)
- Kotlin to 2.1.21
- Gradle to 8.14.1
- AGP to 8.10.1
- KtFmt to 0.55

## Version 0.22.0 _(2025-02-06)_
- Add tasks `ktfmtCheckScripts` and `ktfmtFormatScripts` to check and format the `.kts` files in the project folder. (#382)
- Remove transitive ktfmt dependencies from the plugin to avoid conflicts with the project dependencies. (#394, #181)
- Change output format of ktfmtCheck task
- Add output file for ktfmtFormat task
- Improve logging messages from ktfmt tasks
- Kotlin to 2.1.10
- Gradle to 8.12.1
- AGP to 8.8.0
- KtFmt to 0.54

## Version 0.21.0 _(2024-11-01)_
- KtFmt to 0.53
- AGP to 8.7.1
- Gradle to 8.10.2

## Version 0.20.1 _(2024-08-26)_
- Set 'manageTrailingCommas' to 'true' for kotlinLangStyle to align with ktfmt 0.52

## Version 0.20.0 _(2024-08-25)_

- Add new regex property to exclude sourceSets from analysis (#328)
- Fix custom KtFmt tasks not compatible with configuration cache (#290)
- Fix custom source directories not correctly recognized (#292)
- Fix mixed up task descriptions (#342)
- KtFmt to 0.52
- AGP to 8.5.2
- Gradle to 8.9

## Version 0.19.0 _(2024-07-03)_

- Remove dropboxStyle since it is no longer supported by ktfmt. Use kotlinLangStyle() instead
- Fix task caching for ktfmtCheckTask when project has multiple source sets (#289)
- Kotlin to 2.0.0
- KtFmt to 0.51
- AGP to 8.4.2
- Gradle to 8.8

## Version 0.18.0 _(2024-04-13)_

- Make `KtfmtCheckTask` cacheable
- Add support for `manageTrailingCommas` and enables it by default for googleStyle
- Gradle to 8.7
- AGP to 8.3.2
- Kotlin to 1.9.23

## Version 0.17.0 _(2024-01-31)_

- KTFMT to 0.47
- Kotlin to 1.9.22
- AGP to 8.2.2

## Version 0.16.0 _(2023-12-19)_

- Adapt the plugin to work with KSP 1.9.21
- Remove legacy `kotlin-compiler-embeddable` dependency to prevent potential Kotlin version conflicts
- Fix bug with Gradle Configuration Cache by always creating working dir if it's not existing
- Kotlin to 1.9.21
- AGP to 8.2.0
- Gradle to 8.5

## Version 0.15.1 _(2023-10-31)_

- Kotlin version reverted to 1.9.10

## Version 0.15.0 _(2023-10-30)_

- Replace class loader isolation with process isolation (#206)
- Do not create tasks for aot srcset on Spring projects (#204)
- Kotlin to 1.9.20

## Version 0.14.0 _(2023-10-09)_

- Add support for Gradle Worker API and Classloader Isolation (#182)
- KtFmt to 0.46
- Kotlin to 1.9.10
- Gradle to 8.4
- AGP to 8.1.2

## Version 0.13.0 _(2023-07-15)_

- Handle implicit dependency error message for KMP project (#171)
- Make sure files in the `build/` folder are skipped (#170)
- Fix incompatibility with KSP (#167)
- The Plugin is now GPG signed (#166)
- KtFmt to 0.44
- Kotlin to 1.9.0
- Gradle to 8.2.1
- AGP to 8.0.2

## Version 0.12.0 _(2023-02-21)_

- KtFmt to 0.43
- Kotlin to 1.8.10
- Gradle to 8.0.1
- AGP to 7.4.1
- Fix bug with KMP tasks clashing with Android Tasks (#130)

## Version 0.11.0 _(2022-10-01)_

- KtFmt to 0.41
- AGP to 7.3.0

## Version 0.10.0 _(2022-09-09)_

- Add support for Gradle Configuration Cache
- KtFmt to 0.40

## Version 0.9.0 _(2022-08-29)_

- KtFmt to 0.39
- Gradle to 7.5.1
- Kotlin to 1.7.10
- AGP to 7.2.2
- Kotlinx Coroutines to 1.6.1
- Moved to Version Catalog

## Version 0.8.0 _(2022-02-19)_

- Set Kotlin jvmTarget to 11
- KtFmt to 0.32
- Kotlin to 1.5.31
- Gradle to 7.4

## Version 0.7.0 _(2021-08-30)_

- KtFmt to 0.28
- Gradle to 7.2
- Kotlin to 1.5.21
- Android Gradle Plugin to 4.2.2
- Android Ktfmt Tasks will not have `JavaSource` as postfix anymore
- Tasks should fallback to extension value before defaults

## Version 0.6.0 _(2021-05-20)_

- `--include-only` now works with paths that are file-separator independent.
- Added support for Gradle 7.0+
- Added task ordering between `ktfmt*` tasks and `compileKotlin` tasks. This fix the correctness warning introduced with
  Gradle 7.0

### Dependencies Update

- Gradle to `7.0.2`
- KtFmt to `0.24`

## Version 0.5.0 _(2021-03-20)_

### New features

- Added `--include-only` flag to support pre-commit hooks
- Improved error reporting for scenarios where KtFmt fail to parse
- Added example on how to apply the plugin to the `buildSrc` project

### Dependencies Update

- KtFmt to `0.22`
- Coroutines to `1.4.3`
- AGP to `4.1.3`
- Gradle to `6.8.3`

## Version 0.4.0 _(2021-02-27)_

### New features

- Added the `kotlinLangStyle()` function to the `ktfmt{}` extension. This will allow you to apply a format that attempts
  to reflect the [official kotlin convetions](https://kotlinlang.org/docs/coding-conventions.html).

### Dependencies Update

- Ktfmt to 0.21

## Version 0.3.0 _(2021-02-23)_

### Dependencies Update

- Ktfmt to 0.20
- Gradle to 6.8.2
- AGP to 4.1.2

## Version 0.2.0 _(2020-12-24)_

### Added

- Added the `googleStyle()` function to the `ktfmt{}` extension to configure indents at `2`.

### Dependencies Update

- Ktfmt to 0.19
- Kotlin to 1.4.21
- kotlinx-coroutines to 1.4.2

## Version 0.1.0 _(2020-12-22)_

That's the first version of `ktfmt-gradle`
