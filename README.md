# Data SIM Switcher (Root Only)

A lightweight, purely root-based Quick Settings tile to switch the default mobile data SIM on dual-SIM Android devices. 

Unlike other implementations that rely on Shizuku and persistent daemons, this app uses `libsu` to spawn a Root IPC service for the exact millisecond the tile is tapped. It's incredibly fast, survives reboots reliably, and uses zero background battery.

## Features
* **Zero Daemons:** Uses on-demand Magisk/KernelSU root access.
* **No Permissions Required:** Bypasses Android's `READ_PHONE_STATE` restrictions entirely by doing all calculation logic natively in the root daemon.
* **Backward Compatible:** Uses an internal reflection engine instead of hard-linked AIDL files, making it immune to hidden API signature changes across Android versions (Supports Android 7+).
* **Smart Toggling:** Automatically scans active slots, switches to the next one, and enables the cellular data on the new SIM.

## Setup & Installation
1. Install the APK.
2. Open the app once to grant Superuser privileges via Magisk/KernelSU or manually granting root in your root managers.
3. Pull down your Quick Settings panel, tap edit, and add the "Switch SIM" tile.

## Dependencies
* [libsu](https://github.com/topjohnwu/libsu) - Android Root IPC
* Jetpack Compose - UI

## Credit
Architecture inspired by the reflection engine found in [Mygod/DataSimTile](https://github.com/Mygod/DataSimTile).
