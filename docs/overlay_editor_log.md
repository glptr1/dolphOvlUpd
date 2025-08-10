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

## Build
- 2025-08-10 16:04 — Debug build succeeded. APK artifacts:
  - Source: Source/Android/app/build/outputs/apk/debug/app-debug.apk
  - Copied as: Source/Android/app/build/outputs/apk/debug/dolphin-android-wii-overlay-editor-20250810-1604-e6ed939952-debug.apk
