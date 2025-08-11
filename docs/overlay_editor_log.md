# Android Wii Overlay Editor – Dev Log

This log tracks incremental changes for the custom Wii overlay editor.

## 2025-08-10

- Added data models and JSON storage for custom overlay elements (Button/D-Pad/Joystick) under `overlay/editor`.
- Created `OverlayEditorDialog` with basic UI to add elements and choose per-game vs global save.
- Integrated dialog launch from Edit Layout on Wii; added in-memory accumulation and JSON save.
- Fixed a build issue by deleting a manually-added view binding file and switching the dialog to inflate views directly.
- Added string resources for the dialog and localized the layout.

### Update
- Integrated custom overlay rendering in `InputOverlay` for Wii overlays. Elements are drawn alongside built-ins.
- Enabled editing for custom elements: drag to move and auto-save to per-game JSON.
- Added delete via double-tap while in edit mode for custom items.

Planned next:
- Render custom elements in `InputOverlay.refreshControls()` for Wii by converting models to drawables.
- Support drag-move/resize/delete for custom elements in edit mode and auto-save to JSON.
- Expand mapping options (axes, special actions like toggle orientation / change extensions).
- Hook per-game/global load on game start and refresh overlay.

## 2025-08-10 (later)
- Editor no longer auto-opens when entering Edit Layout; a new menu item opens it on demand.
- Editor: added Mapping Type selector (Control vs Action). First action supported: toggle Sideways/Upright.
- Add now persists immediately (per-game/global via checkbox) and refreshes the overlay; new elements appear.
- Built new debug APK: Source/Android/app/build/outputs/apk/debug/dolphin-android-wii-overlay-editor-20250810-1700-e6ed939952-debug.apk

### 2025-08-10 (even later)
- Editor Mapping (Wii Remote): expanded the selectable inputs to include Wiimote D-Pad, Nunchuk C/Z, and Classic Controller buttons/triggers (A/B/X/Y/Plus/Minus/Home/ZL/ZR/L/R).
- Mapping Type: added Motion Simulation action entries (Wiimote and Nunchuk Shake/Swing/Tilt variants). Note: these are UI options; runtime triggers will require native wiring not yet exposed via InputOverrider.
- Action buttons now show F-number labels for clarity (F1, F2, …) and renumber when items are added/removed.
- New actions implemented: Toggle IR Recenter, Cycle IR Mode (apply immediately to pointer).
- Build: app-debug assembled and copied as Source/Android/app/build/outputs/apk/debug/dolphin-android-wii-overlay-editor-20250810-1930-22266b376d-debug.apk

### 2025-08-10 (night)
- Fix: custom CONTROL-mapped buttons now use their exact controlId only (no fallback to stock A). This enables Nunchuk C/Z and Classic buttons to work correctly.
- Mapping Type: added Save State (Quick) and Load State (Quick). Wired to NativeLibrary.SaveState(9,false) and NativeLibrary.LoadState(9).
- Motion Simulation entries remain UI-only for now; wiring next.
- Build: Source/Android/app/build/outputs/apk/debug/dolphin-android-wii-overlay-editor-20250810-1949-22266b376d-debug.apk

### 2025-08-10 (late night)
- Visuals: improved appearance inference for non‑Wiimote controls in custom buttons:
  - Nunchuk C/Z and Classic A/B/X/Y/ZL/ZR/L/R now render with proper icons instead of defaulting to Wiimote A.
- Fallback: when no specific icon is available, use a neutral, theme‑agnostic generic circular button (generated at runtime) instead of Wiimote A.
- Build: app-debug assembled successfully. APK at Source/Android/app/build/outputs/apk/debug/app-debug.apk

### 2025-08-10 (scope tighten)
- Editor scope: limit mappings to Wiimote only; remove Joystick option; keep Button and D‑Pad.
- Motion: drop all motion actions except Wiimote Shake X/Y/Z; wire these to IMU overrides.
- Visuals: remove any remaining non‑Wiimote icon inference in runtime; non‑Wiimote appearances now always use the neutral generic fallback.
- Build: debug build OK after scope changes.

## Build
- 2025-08-10 16:04 — Debug build succeeded. APK artifacts:
  - Source: Source/Android/app/build/outputs/apk/debug/app-debug.apk
  - Copied as: Source/Android/app/build/outputs/apk/debug/dolphin-android-wii-overlay-editor-20250810-1604-e6ed939952-debug.apk

## 2025-08-11

- IR joystick: reworked pointer to velocity-based control at 60 Hz, added PREVENT_RECENTER during activity and delayed recenter (~8s) after inactivity. Inversion finalized per user; left unchanged.
- Editor polish: touch routing prioritizes custom items when overlapping; autosave/load for per-game and global layouts. Added quick actions: Toggle Sideways, Toggle IR Recenter, Cycle IR Mode, Save/Load State (slot 9).
- Wiimote-only scope: removed non-Wiimote mappings from selection lists; Wiimote D-Pad removed from custom mapping. Fixed Wiimote A icon fallback.
- Shake iterations (IMU spoofing): implemented horizontal-dominant alternating pattern (~7 Hz) with gyro assist for both tap burst (~500ms) and hold cycling. Build succeeded with these changes.
- Native Wiimote Shake override bridge: added direct override channels and wiring end-to-end.
  - Core C++:
    - InputOverrider: new control IDs WIIMOTE_SHAKE_X/Y/Z (62–64). Shifted NUNCHUK_IMU_ACCEL_* to 65–67. Mapped WiimoteEmu::Wiimote::SHAKE_GROUP X/Y/Z to these IDs.
    - WiimoteEmu: added SHAKE_GROUP constant. Declared and implemented EmulateShake overload accepting InputOverrideFunction to apply overrides to ControllerEmu::Shake before dynamics.
    - StepDynamics now calls the override-aware EmulateShake with the Wiimote input override function.
  - Android Kotlin:
    - InputOverrider.ControlId updated to include WIIMOTE_SHAKE_X/Y/Z and shifted Nunchuk IMU IDs.
    - InputOverlay: on shake action, sets direct Shake overrides each half-cycle (X with small Z assist) alongside IMU pulses for reliability. Added Toast/log on activation.
- Tap + Hold behavior: shake now triggers immediately on tap (short burst) and continues as long as the button is held; stops on release.
- Builds:
  - :app:assembleDebug — PASS
  - :app:externalNativeBuildDebug — PASS (C++ changes compiled)

Artifacts:
- Latest debug APK will be produced with timestamped name after build (see below).
