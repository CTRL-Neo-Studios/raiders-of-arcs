# Raiders of ARCs (RoA)

A Minecraft 1.21.1 NeoForge gun mod inspired by ARC Raiders, featuring realistic weapon mechanics, attachment systems, and smooth animations powered by AzureLib.

## Project Overview

**Mod ID:** `roa`  
**Version:** 0.1.0-alpha  
**Minecraft Version:** 1.21.1  
**Mod Loader:** NeoForge 21.1.206
**Animation Library:** AzureLib

## Tech Stack & Dependencies

- **NeoForge 21.1.206** - Mod loader
- **AzureLib** - 3D model animation library
- **MixinExtras** - Enhanced mixin capabilities
- **Java 21** - Language version

## Core Design Philosophy

### Data-Driven Architecture
- All gun properties stored in **Data Components** (Minecraft 1.21's new system)
- Datapack-friendly for easy customization
- Network-synchronized for multiplayer compatibility
- Component-based approach for modularity

### Realistic FPS Mechanics
- **Client-side rendering** - Smooth FOV transitions, recoil, ADS
- **Server-authoritative gameplay** - All damage/firing handled server-side
- **No invulnerability frames** - Guns can rapid-fire without damage cooldowns
- **Physics-based projectiles** - Range-based gravity simulation

## Credits
Inspiration:
- ARC Raiders (game by Embark Studios)
- Simple Animated Guns (Fabric mod by elidhan) - Mixin patterns

Libraries:
- NeoForge Team
- AzureLib Team
- LlamaLad7 (MixinExtras)
