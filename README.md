# Bowser's Fury — Minecraft Java 1.21.11 (Forge)

A single Forge mod project built around an ocean boss encounter inspired by the Fury Bowser battles. The project includes the boss entity, custom model and texture, combat AI, attack items, progression materials, the Giga Bell transformation, recipes, natural ocean spawning, and Spanish/English names.

## Target

- Minecraft Java: **1.21.11**
- Minecraft Forge: **61.2.4** (recommended Forge build for this Minecraft version)
- Java: **21, 64-bit JDK**
- Mod ID: `bowsersfury`

## Gameplay included

### Fury Bowser

- Custom oversized 3D model and dedicated 128×128 texture.
- 1,250 health, heavy armor, strong melee hits, knockback resistance, and fireball attacks.
- Water navigation and rare natural spawning in ocean biomes.
- Black squid-ink and purple dragon-breath particles circling the water surface to create the Fury-goop arrival effect.
- Long-range fireballs and falling pointed-dripstone barrages aimed at the player.
- Enrages below half health, accelerating its attack rhythm.
- Drops Fury Shards, Fury Scales, and a chance of an Ember Core when defeated.

### Progression and equipment

- **Fury Shard:** ammunition for the three ranged weapons.
- **Fury Scale:** advanced crafting material, also made from four Fury Shards.
- **Ember Core:** rare crafting component.
- **Fury Launcher:** entry weapon.
- **Inferno Cannon:** stronger weapon; costs two Fury Shards per shot.
- **Tidal Cannon:** strongest weapon; costs three Fury Shards per shot.
- All launchers fire projectiles, have durability and a cooldown, and deal increased damage to Fury Bowser.

### Giga Bell

Using the Giga Bell temporarily increases player scale and attack damage, restores health and hunger, and lasts 45 seconds. The bell has three uses and a long cooldown.

### Spawn options

- Natural spawn: very rare in ocean biomes.
- Creative item: **Fury Bowser Summoner**. Use it on a block to summon the boss.
- Command: `/summon bowsersfury:fury_bowser ~ ~ ~`

## Crafting

- Fury Scale: 2×2 Fury Shards.
- Fury Launcher: iron ingot, crossbow, Fury Shards, blaze rod.
- Inferno Cannon: Fury Launcher, Ember Core, Fury Scale, iron blocks, blaze rod.
- Tidal Cannon: Inferno Cannon, diamonds, Fury Scales, Fury Shards, nautilus shell.
- Giga Bell: gold blocks, Ember Core, Fury Scale.

## Build and install

This is the complete **single-mod source project**. The JAR must be compiled on a machine with internet access so Gradle can download ForgeGradle and Minecraft dependencies; this workspace could not reach the Forge Maven repository, so no compiled JAR is claimed here.

1. Install a 64-bit Java 21 JDK.
2. Install Minecraft Forge 1.21.11 / 61.2.4 in your launcher.
3. Open this project folder in IntelliJ IDEA (Gradle project) or run `gradle build` from a terminal with Gradle 8.14+ installed.
4. After a successful build, copy `build/libs/bowsersfury-1.0.0.jar` into `.minecraft/mods/`.
5. Launch the Forge 1.21.11 profile.

## Important testing note

The full source and assets are included, but compilation and in-game playtesting were not possible in this environment because the Forge Maven host was unreachable. In particular, confirm the build with Forge 61.2.4 before using it in a valuable world. Back up worlds before testing any new mod.
