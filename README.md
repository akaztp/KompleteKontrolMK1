# KK1 Splits — Komplete Kontrol MK1 for Bitwig Studio

A Bitwig Studio controller extension for the first-generation **Native Instruments Komplete Kontrol S-series keyboards (S25, S49, S61, S88 MK1)**. The keyboard plays through **your templates in NI's Controller Editor**, with their splits, MIDI channels, transposition and pedals. The extension gives **each MIDI channel its own Bitwig input** and **lights the keys in your templates' zone colours**. On top of that comes the full MK1 support from [DrivenByMoss](https://github.com/git-moss/DrivenByMoss): display, knobs, transport, mixer, device parameters and browser.

> **Credits.** This project is a derivative of the Komplete Kontrol MK1 driver in [DrivenByMoss](https://github.com/git-moss/DrivenByMoss) by **Jürgen Moßgraber** ([mossgrabers.de](https://www.mossgrabers.de)), licensed under the LGPL-3.0. All of the original MK1 functionality (transport, mixer, device parameters, browser, display and USB handling) is his work. If this extension is useful to you, please consider [supporting DrivenByMoss](https://www.mossgrabers.de/Software/Bitwig/Bitwig.html).

## Features

Everything the DrivenByMoss MK1 driver does with the display, knobs and buttons, plus:

- **Your NI templates play the keys**: the keyboard applies the template selected in Controller Editor, with its key zones, MIDI channels, transposition, pedals, pitch bend and modulation. **Preset Up/Down** switch templates as usual.
- **One Bitwig input per MIDI channel**: `KK1 Ch 1` to `KK1 Ch 16`, so each zone of a template can play its own track. `KK1 Keyboard` receives every channel.
- **Key lights for your templates**: the extension reads your templates from Controller Editor and lights each key in its zone's colour, brighter while held. It follows Preset Up/Down and shows the template's name and zones on the display.

## Installation

1. Download `KompleteKontrolMK1.bwextension` from the [Releases](https://github.com/akaztp/KompleteKontrolMK1/releases) page (or [build it](#building-from-source)).
2. Copy it to `Documents\Bitwig Studio\Extensions` (macOS: `~/Documents/Bitwig Studio/Extensions`).
3. In Bitwig: **Settings → Controllers → Add Controller → Native Instruments → Komplete Kontrol Sxx mk I (KK1 Splits)**.
4. The NI hardware drivers and NI's Controller Editor must be installed, as for DrivenByMoss. Set up your templates in Controller Editor, then close it before you play in Bitwig.

You can keep DrivenByMoss installed for your other controllers. **Remove DrivenByMoss's own "Komplete Kontrol Sxx mk I" controller from the list**, because both would try to take over the keyboard's USB connection.

## How it works with your templates

The keyboard plays its keys as set by the template selected in NI's Controller Editor. A template can split the keyboard into zones, send each zone on its own MIDI channel, transpose it, and set up the pedals, pitch bend and modulation. The keyboard applies the template itself, also while the extension runs. When it is switched on, it returns to the template that was selected last.

The extension leaves all of this to the template. It passes on what the template sends, draws the key lights, and handles the display, knobs, transport and the other buttons like DrivenByMoss.

### Playing tracks

The extension provides 17 note inputs, which receive notes, pedals, footswitch, pitch bend, modulation and aftertouch unchanged:

- `KK1 Keyboard` receives everything the keyboard sends, on every MIDI channel. Tracks set to *All Ins* receive it.
- `KK1 Ch <n>` receives what the template sends on MIDI channel n. These inputs are not part of *All Ins*, so a track on *All Ins* doesn't play the keys twice.

To play one zone of a template on a track:

1. Set the track's **input** to `KK1 Ch <n>`, where n is the zone's MIDI channel (for example the bass track to `KK1 Ch 2` and the piano track to `KK1 Ch 3`).
2. Set the track's **monitoring** to **On** so it plays without being armed.

As with any MIDI keyboard, a pedal only reaches the inputs of its own MIDI channel, plus `KK1 Keyboard`. For the footswitch to sustain several zones, the template has to send it on each of their channels.

An input that no track listens to may stay inactive in Bitwig's input list, even while you play.

### Switching templates

**Preset Up/Down** switch your templates as usual: Preset Down selects the next template in Controller Editor's list and Preset Up the previous one, wrapping around at the ends. The keyboard does the switching; the extension follows, and the display shows the template's name and its zones.

### Key lights

Once the extension takes over the display, the keyboard stops drawing the template's lights itself. So the extension reads your templates from the file in which Controller Editor keeps them (`C:\ProgramData\Native Instruments\Controller Editor\KompleteKontrol.ncs`), and lights each key in its zone's colour, brighter while held. The colours are tuned by eye on an S88 to look like the ones in Controller Editor's colour picker. How bright the keys are while not held is set in **Keyboard → Unheld Key Brightness**. Keys outside every zone, or in a zone coloured black (the picker's last colour), stay dark.

- The keyboard's **Octave −/+** buttons shift the notes the keys play, while the zones and their lights stay on the same keys.
- If the file can't be read, Bitwig shows a notification when the extension starts, and the extension leaves the lights alone.
- **When Bitwig closes**, the zones stay lit in their colours, but the lights don't follow Preset Up/Down or the keys until the keyboard is switched off and on, or Controller Editor is opened.

### Which template is active

The keyboard doesn't tell the extension which template it plays, and Controller Editor's file only names the template that was selected when Controller Editor last saved it. So the extension follows your Preset Up/Down presses and remembers the template between sessions. If Controller Editor saved its file since the extension last read it, the extension starts with the template named in the file.

If the lights show another template than the keyboard plays, for example after you switched templates while Bitwig was closed, choose the right one in **Keyboard → Active Template**.

### After changing your templates

Close Controller Editor and restart the extension (or Bitwig), so it reads the templates again and takes the keyboard back.

### On the keyboard

| Control | Function |
|---|---|
| **Keys, wheels, pedals** | As set in the template. |
| **Preset Up / Preset Down** | The keyboard switches templates, the extension follows. The LEDs are lit if there is more than one template. |
| **Octave − / +** | The keyboard shifts the notes. The display shows the new range. |
| **Scale**, **Instance** | No function. |
| **All other buttons, knobs and the display** | As in DrivenByMoss's MK1 driver. |

## Settings reference

All of these are in Bitwig under **Settings → Controllers → (this controller)**:

- **Keyboard**
  - *Active Template*: the template which the keyboard plays, numbered as in Controller Editor's list. The extension follows Preset Up/Down; choose the right one here if the key lights show another template than the keyboard plays. Only shown if the templates could be read.
  - *Unheld Key Brightness*: how bright the key lights are while the keys are not held, relative to held keys. 100% lights them as bright as held keys (so held keys no longer stand out), 0% leaves them dark so only held keys light up. The default is 50%. At low values, colours with a weak red, green or blue part may shift a little, since the keys take only 128 steps per part.
- **Diagnostics**
  - *Log to Console*: prints to Bitwig's control script console the USB connection, the note inputs, where the templates were read from and the active template. After that, it prints each template change with its zones, each incoming note and CC with its MIDI channel and the inputs it reaches, and changes of the keyboard's status bytes, such as an octave shift. It also prints the USB reports the keyboard sends, and whether the reports the extension sends to it go through. See [Troubleshooting](#troubleshooting).
- The DrivenByMoss MK1 settings for playing, transport and workflow.

## Status

Version 1.0.0 is tested on an S88 MK1 with Bitwig Studio 6.1: the templates' notes reach the note inputs of their MIDI channels, Preset Up/Down switch templates, and the display, knobs and transport work. Not tested yet:

- The key lights: held keys, following Preset Up/Down, and the zones while the keyboard is octave-shifted.
- The pedals through the note inputs.
- The S25, S49 and S61. The extension assumes that their left-most key plays C2 on the S25 and C1 on the S49 and S61 when not octave-shifted (A-1 on the S88, with C3 = MIDI note 60). If that is wrong, the zones light up an octave away from the keys they play.
- Controller Editor's file on macOS: its location there is a guess.

Feedback and issues are welcome.

## Troubleshooting

- **A track stays silent**: its input must be `KK1 Keyboard`, *All Ins*, or the `KK1 Ch <n>` input of the MIDI channel that the template sends on, with monitoring **On**. With *Log to Console* on, every key and pedal press is printed with its MIDI channel and the inputs it reaches. If nothing arrives at all, check that the controller's MIDI input is set to the keyboard's MIDI port (`Komplete Kontrol - 1`) in **Settings → Controllers**.
- **The key lights show another template than the keyboard plays**: choose the right one in *Keyboard → Active Template*. The extension only sees Preset Up/Down while it runs. See [Which template is active](#which-template-is-active).
- **The key lights don't change at all, or Bitwig says it could not read the templates**: the extension couldn't read Controller Editor's file and leaves the lights alone. The notification and the console show the file it tried and why it failed. After closing Bitwig, switch the keyboard off and on, or open Controller Editor, to get the template's own lights back.
- **Display, buttons and lights don't work**: with *Log to Console* on, the console shows the USB connection state. Make sure no other software uses the keyboard (DrivenByMoss's own MK1 controller, NI's Komplete Kontrol app, Controller Editor), then restart Bitwig.
- **Anything else**: turn on **Diagnostics → Log to Console**, then open Bitwig's control script console (search the command palette for *Show Control Script Console*). Turning the log on prints the USB connection, the note inputs, the template file and the active template. After that, every key and pedal press is printed as it arrives, for example `Note on C3 (60) velocity 100 on MIDI channel 2 -> KK1 Keyboard, KK1 Ch 2`. Template changes and changes of the keyboard's status bytes, such as an octave shift, are printed too. Errors such as `Crash during flush` also appear there.

## Building from source

Requires Windows PowerShell. The scripts put a portable JDK 21 (Eclipse Temurin) and Maven into the git-ignored `.tools` folder, and keep Maven's cache there too, so nothing is installed system-wide.

```powershell
powershell -ExecutionPolicy Bypass -File scripts\setup-tools.ps1   # once: downloads JDK + Maven into .tools
powershell -ExecutionPolicy Bypass -File scripts\build.ps1          # builds target\KompleteKontrolMK1.bwextension
powershell -ExecutionPolicy Bypass -File scripts\build.ps1 -Install # builds and copies it to Bitwig's Extensions folder
```

The Bitwig Controller API reference ships with Bitwig Studio: `C:\Program Files\Bitwig Studio\resources\doc\control-surface\api\index.html`.

### Project layout

| Path | Contents |
|---|---|
| `src/main/java/pt/ztp/kontrol1` | The MK1 controller, derived from DrivenByMoss's `controller/ni/kontrol/mki` |
| `src/main/java/pt/ztp/kontrol1/template` | New: reading the templates of NI's Controller Editor, and the key lights for them |
| `src/main/java/pt/ztp/kontrol1/NoteInputs.java` | New: the note input of each MIDI channel |
| `src/main/java/de/mossgrabers/framework`, `.../bitwig/framework` | The DrivenByMoss framework, copied with a few small patches (see [UPSTREAM.md](UPSTREAM.md)) |
| `maven-local-repository` | purejavahidapi, which is not on Maven Central (copied from DrivenByMoss) |

## License

[LGPL-3.0](LICENSE), like DrivenByMoss.

- Original code: © 2017–2026 Jürgen Moßgraber.
- Modifications and additions: © 2026 akaztp.
- Bundled libraries:
  - [purejavahidapi](https://github.com/nyholku/purejavahidapi): BSD-3-Clause.
  - [JNA](https://github.com/java-native-access/jna): Apache-2.0 / LGPL-2.1.

*Native Instruments, Komplete Kontrol and Bitwig Studio are trademarks of their respective owners. This project is not affiliated with or endorsed by them.*
