# TenshiLib [![](http://cf.way2muchnoise.eu/full_312040_Forge_%20.svg)![](http://cf.way2muchnoise.eu/versions/312040.svg)](https://www.curseforge.com/minecraft/mc-mods/tenshilib) [![](http://cf.way2muchnoise.eu/full_552662_Fabric_%20.svg)![](http://cf.way2muchnoise.eu/versions/552662.svg)](https://www.curseforge.com/minecraft/mc-mods/tenshilib-fabric) [![Discord](https://img.shields.io/discord/790631506313478155?color=0a48c4&label=discord)](https://discord.gg/8Cx26tfWNs)

Library mod for my other projects.

```gradle
repositories {
    maven {
        name = "Flemmli97"
        url "https://gitlab.com/api/v4/projects/21830712/packages/maven"
    }
}

dependencies {    
    //Fabric==========    
    modImplementation("io.github.flemmli97:tenshilib:${minecraft_version}-${flan_version}:${mod_loader}")
    
    //Forge==========    
    compile fg.deobf("io.github.flemmli97:tenshilib:${minecraft_version}-${flan_version}:${mod_loader}")
}
```
