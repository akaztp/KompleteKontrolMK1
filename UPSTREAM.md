# Upstream: DrivenByMoss

This project contains code from [DrivenByMoss](https://github.com/git-moss/DrivenByMoss) by Jürgen Moßgraber (LGPL-3.0).

- **Upstream commit**: `f9aca50b97efb9d5273e1384b697677b55e309d9` (2026-11-05, version 26.7.0)
- **Required Bitwig API**: 21

## What was taken

| Upstream path | Here | Notes |
|---|---|---|
| `src/main/java/de/mossgrabers/framework` | same | Unchanged, except the patches below |
| `src/main/java/de/mossgrabers/bitwig/framework` | same | Unchanged, except the patches below |
| `src/main/java/de/mossgrabers/controller/ni/kontrol/mki` | `src/main/java/pt/ztp/kontrol1` | Renamed package, modified (each file is marked "Modified by akaztp"). `mode/ScaleMode.java` and `command/trigger/ScaleButtonCommand.java` are removed, because the NI template plays the keys |
| `src/main/java/de/mossgrabers/bitwig/controller/ni/kontrol/mki` | `src/main/java/pt/ztp/kontrol1/bitwig` | Renamed package, modified |
| `src/main/resources/devices` | same | Unchanged |
| `maven-local-repository/purejavahidapi` | same | Unchanged |

No other controllers are included.

## Local patches to the framework

Every patch is marked with a `Local patch (KompleteKontrolMK1)` comment.

1. `framework/view/ColorView.java`: casts to `AbstractParameterMode` instead of the Push controller's `DeviceLayerDetailsMode`, so the Push code isn't needed.
2. `framework/view/sequencer/AbstractDrumView.java`: uses the literal values of Maschine's `COLOR_GREEN` (30) and `COLOR_LIME_LO` (33), so the Maschine code isn't needed.
3. `framework/daw/midi/INoteInput.java` and `bitwig/framework/midi/NoteInputImpl.java`: two new methods, `sendRawMidiEvent` and `setIncludeInAllInputs`.

## Updating to a newer DrivenByMoss

1. Clone DrivenByMoss at the new version.
2. Replace `src/main/java/de/mossgrabers/framework` and `src/main/java/de/mossgrabers/bitwig/framework` with the new versions, then re-apply the patches above. Search the new code for `de.mossgrabers.controller` to find any new dependencies on other controllers.
3. Diff the upstream `controller/ni/kontrol/mki` folder between the old and new commits, and port relevant changes into `pt/ztp/kontrol1`.
4. Check `pom.xml` (Bitwig API version, JNA version) against upstream, then build and run the tests.
5. Update the commit hash above.
