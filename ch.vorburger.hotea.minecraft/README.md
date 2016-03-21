HOT Minecraft plugins
====

This is a Minecraft Server modu sing the Sponge modding API.

It allows other mods to be "hot reloaded" on changes in them.

Build this plugin by:

    cd ../ch.vorburger.hotea.minecraft.api
    mvn install
    cd ../ch.vorburger.hotea.minecraft
    ./gradlew shadowJar

Due to the rejection of https://github.com/SpongePowered/SpongeVanilla/pull/178, this mod requires a patched Sponge Vanilla to run, from one of the Hotea* branches of my fork of Sponge:

    git clone --recursive https://github.com/vorburger/SpongeVanilla.git
    cd SpongeVanilla
    git checkout Hotea_*
    cd SpongeCommon
    git checkout Hotea_*
    cd ..
    ./gradlew setupDecompWorkspace eclipse build --refresh-dependencies

You can copy ch.vorburger.hotea.minecraft/build/libs/ch.vorburger.hotea.minecraft-1.0.0-SNAPSHOT-all.jar
into mods/ of a SpongeVanilla install built by above, and it will "hot" (re)load plugins listed in (TODO Configuration),
in addition to the "regular" (non "hot") ones in mods/.

Have fun!  Star the repo if you find this useful.  Send PRs with improvements.

M.
