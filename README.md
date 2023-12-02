# TenshiLib
[![](http://cf.way2muchnoise.eu/full_312040_Forge_%20.svg)![](http://cf.way2muchnoise.eu/versions/312040.svg)](https://www.curseforge.com/minecraft/mc-mods/tenshilib)  
[![](http://cf.way2muchnoise.eu/full_552662_Fabric_%20.svg)![](http://cf.way2muchnoise.eu/versions/552662.svg)](https://www.curseforge.com/minecraft/mc-mods/tenshilib-fabric)  
[![](https://img.shields.io/modrinth/dt/P2rffivS?logo=modrinth&label=Modrinth)![](https://img.shields.io/modrinth/game-versions/P2rffivS?logo=modrinth&label=Latest%20for)](https://modrinth.com/mod/tenshilib)  
[![Discord](https://img.shields.io/discord/790631506313478155?color=0a48c4&label=discord)](https://discord.gg/8Cx26tfWNs)

Library mod for my other projects.

To use this mod as a dependency add the following snippet to your build.gradle:  
```groovy
repositories {
    maven {
        name = "Flemmli97"
        url "https://gitlab.com/api/v4/projects/21830712/packages/maven"
    }
}

dependencies {    
    //Fabric/Loom==========    
    modImplementation("io.github.flemmli97:tenshilib:${minecraft_version}-${mod_version}-${mod_loader}")
    
    //Forge==========    
    compile fg.deobf("io.github.flemmli97:tenshilib:${minecraft_version}-${mod_version}-${mod_loader}")
}
```
