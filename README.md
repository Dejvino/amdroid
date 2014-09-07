Amdroid
=======

Ampache client for Android. Allows streaming music from any Ampache server.

![](https://raw.githubusercontent.com/Dejvino/amdroid/master/screenshot_01.png)

## History
This is a fork of the official "amdroid-h" repo.
Since there was no activity around this project in any way since 2010,
I decided to kick-start this again.

## Major Changes (WIP)
* **Reworked UI**. The original design looked very outdated. This puts off many potential users.
* **Upgraded Android APIs**. The aim is to drop all the deprecated dependencies and components.
* **Async Network/IO**. Everything UI-unrelated has to be moved out of the main (UI) thread. This brings app responsiveness and better UX.
* **Robust application**. Exceptions get handled properly. The app needs to be reliable.

