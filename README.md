# Nubia Toolkit

Nubia Toolkit is an Xposed/LSPosed module designed for Nubia RedMagic devices to unlock hidden features, remove limitations, and enhance the user experience of the compiled system apps like Game Space and Game Assist.

## Features

*   **Super Resolution Unlock:** Forces the "Superior Pic Quality" (Super Resolution) feature to be enabled and visible, bypassing device model checks.
*   **Watermark Limit Removal:** Removes the length restriction for custom watermark text in the camera app.
*   **No Kill Logic:** Prevents the system from aggressively killing background apps (configurable "No Kill" mode).
*   **Game Space Enhancements:** Hooks into Game Space and Game Assist to enable features on unsupported devices or modify their behavior.
*   **Widget Modifications:** Customizations and fixes for Game Widgets (Fan, FPS, Health).

## Requirements

*   **Rooted Nubia RedMagic Device:** This module requires Root access to function.
*   **LSPosed Framework:** You must have LSPosed (Zygisk or Riru) installed and active.
*   **Android 12+:** Targeted for recent Nubia OS versions (MyOS/RedMagic OS).

## Installation

1.  Download the latest release APK.
2.  Install the APK on your device.
3.  Open the **LSPosed Manager** app.
4.  Enable the **Nubia Toolkit** module.
5.  Select the recommended scope (System Framework, Game Space, Game Assist, etc. - usually pre-configured).
6.  Reboot your device to apply changes.
7.  Open the **Nubia Toolkit** app to configure specific settings.

## Usage

*   Open the app to toggle specific tweaks.
*   Some features (like "No Kill") may require granting Root permissions to the app itself for additional control.
*   "Force Stop on Apply": Use this option in the settings to restart targeted apps (Game Space, etc.) so hooks can take effect immediately without a full reboot.

## Disclaimer

This software is provided "as is", without warranty of any kind. Modifying system behavior carries risks. The developer is not responsible for any bootloops, data loss, or bricked devices. Always backup your data before installing Xposed modules.

## License

See the [LICENSE](LICENSE) file for details.
