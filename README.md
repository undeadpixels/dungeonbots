# DungeonBots
The DungeonBots game

#### Build status:
[![BuildStatus](https://travis-ci.com/cyanpelican/DungeonBots.svg?token=e8xGyuEnZZSykp8ymA16&branch=master)](https://travis-ci.com/cyanpelican/DungeonBots/builds)

#### Import/run in Eclipse:

1. File > Import … “gradle project”
2. Once it imports, go into `DungeonBots-desktop` > `src/main/java` > `com.undead\_pixels.dungeon\_bots.desktop` > `DesktopLauncher.java`
3. “Run as” > Java Application. It will fail.
4. Run Configurations > DesktopLauncher > Arguments > Working directory. Change to `${workspace\_loc:DungeonBots-core}/assets`
5. Try running again.

### Alternate Flow Run with Gradle
1. In command line navigate to Project directory (should contain gradlew executable)
2. Run command ``` ./gradlew run ```
