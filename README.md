# 🪓 LivingTools

> **Un plugin de Minecraft (Spigot 1.20.4) que da vida a las herramientas.**  
> Crea herramientas con alma propia: suben de nivel, hablan, tienen personalidad, pelean jefes y dejan un legado.

---

## 📦 Instalación

1. Descargar el `.jar` desde [Releases](https://github.com/Viega095/LivingTools/releases)
2. Colocar en la carpeta `plugins/` del servidor
3. Reiniciar el servidor
4. Las recetas y configuraciones se generan automáticamente

**Requisitos:**
- Spigot / Paper `1.20.4`
- Java 17+

---

## ✨ Características Principales

### 🔧 Herramientas Vivientes
- Craftea herramientas especiales con recetas personalizadas
- Cada herramienta tiene **nombre, nivel, XP, personalidad y humor**
- Suben de nivel con el uso: minería y combate
- Al llegar al nivel 200 pueden hacer **Prestige** para reiniciarse con bonus permanentes

### 🧠 Sistema de Personalidad
Cada herramienta tiene una de **4 personalidades** que determina su diálogo y comportamiento:
- **Aggressive** — Ama el combate, reacciona a tormentas, frases de batalla
- **Wise** — Reflexiva, da consejos al minar y subir de nivel
- **Lazy** — Se queja de todo, pero funciona
- **Cheerful** — Siempre positiva y animada

El **humor** varía entre -10 y 10 según el uso. Una herramienta triste rinde menos.

### ⚔️ Sistema de Habilidades
Árbol de habilidades desbloqueables por nivel:
- Habilidades de minería: Fortune Boost, Speed Mining, Vein Miner
- Habilidades de combate: Critical Strike, Life Steal, Chain Attack
- Habilidades de utilidad: Auto Smelt, Silk Touch Plus, Night Vision

### 🏆 Sistema de Reputación
La herramienta gana **títulos** según sus acciones:
| Título | Condición |
|--------|-----------|
| The Fighter | 10+ mobs |
| The Hunter | 100+ mobs |
| The Destroyer | 200+ mobs |
| The Butcher | 5+ jugadores |
| The Warrior | 20+ jugadores |
| The Legend | Prestige I |
| The Undying | Prestige III |
| The Guardian | 50+ acciones pacíficas |

Al ganar un nuevo título: **pantalla de título + partículas doradas + sonido de logro**.

### 🎯 Sistema de Hitos (18 hitos)
Hitos por categoría con **bonus de XP al completarlos**:
- Niveles: 10, 25, 50, 100, 200
- Mobs: 10, 50, 250, 1000, 5000
- Bloques: 100, 1000, 10000
- Prestige: I, III
- PvP y Pacíficos

### 🌦️ Bonificaciones de Clima
| Condición | Bonus | Herramientas |
|-----------|-------|--------------|
| Lluvia | +15% XP | Picos, Palas, Azadas |
| Noche | +20% XP | Espadas, Hachas |
| Tormenta | +25% XP | Todas |
| Amanecer/Atardecer | +5% XP | Todas |

### 🍖 Sistema de Alimentación
Cada herramienta tiene un nivel de **saciedad (0-10)** que afecta el XP:
- Saciedad ≥ 9: **+5% XP**
- Saciedad 1-3: **-7% XP**
- Muerto de hambre: **-10% XP**

Cada tipo de herramienta prefiere una comida diferente (picos → piedra/minerales, espadas → carne).

### ⚗️ Mesa de Ensamblaje (AssemblyGUI)
Craftea items especiales colocando una **Smithing Table sobre un Iron Block** y haciendo clic derecho:
- **LivingTools** (herramientas base)
- **Artefactos** con habilidades especiales
- **Reliquias de Jefes** con efectos pasivos permanentes

### 🔮 Sistema de Runas
Coloca runas en tu herramienta para potenciarla:
- **Sapientia** — +20% XP
- **Fortitudo** — +Fuerza en combate
- **Velocitas** — +Velocidad de minería
- Y muchas más...

### 👹 Sistema de Jefes
6 jefes únicos con sus arenas personalizadas:

| Jefe | Arena | Reliquia |
|------|-------|---------|
| Living Boss | Arena Volcánica | — |
| Seraphim | Fortaleza Celeste | Seraphim Feather |
| Titan | Arena de Titán | Titan Shard |
| Dryad | Bosque Encantado (radio 18) | Dryad Heartwood |
| Wyrm | Desierto de Arenas Rojas | Wyrm Core |
| Leviathan | Arena Submarina | Leviathan Core |

### 💎 Reliquias de Jefe
Equipar una reliquia en el inventario activa efectos pasivos permanentes:
- **Dryad Relic** — Regeneración + Fuerza en biomas de bosque
- **Wyrm Relic** — Resistencia al fuego permanente, velocidad en desierto, -75% daño de lava
- **Leviathan Relic** — Respiración acuática + Gracia del Delfín + Visión nocturna bajo el agua

### 🏛️ Sistema de Legado
Al hacer Prestige, el jugador deja un "legado" para la siguiente herramienta del mismo tipo:
- Legado I: +10% XP
- Legado II: +20% XP
- Legado III (máximo): +30% XP

### 📖 HistoryGUI
`/livingtool history` — abre una GUI de 54 slots con:
- Identidad (nombre, material, personalidad, título)
- Progresión (nivel, XP, prestige, legado)
- Estadísticas de combate y minería
- XP por bioma (Nether, Océano, Cielo)

---

## 📋 Comandos

| Comando | Descripción |
|---------|-------------|
| `/livingtool` | Abre el dashboard de la herramienta en mano |
| `/livingtool history` | Abre el historial visual |
| `/livingtool stats` | Ver árbol de habilidades |
| `/livingtool help` | Guía de uso |
| `/livingtool feed` | Alimentar la herramienta |
| `/livingtool top` | Leaderboard del servidor |

### Comandos de Admin (`/livingtool admin`)
| Comando | Descripción |
|---------|-------------|
| `spawnboss <TIPO>` | Spawna un jefe |
| `spawnarena <TIPO>` | Crea la arena de un jefe |
| `setlevel <N>` | Setea el nivel de la herramienta en mano |
| `reset` | Reinicia la herramienta |
| `multiplier <N> [MINING\|COMBAT\|ALL]` | Cambia el multiplicador de XP |

**Tipos de boss/arena:** `LIVING`, `SERAPHIM`, `TITAN`, `DRYAD`, `WYRM`, `LEVIATHAN`

---

## 🗂️ Estructura del Proyecto

```
src/main/java/com/livingtools/
├── LivingToolsPlugin.java          # Entry point
├── commands/
│   ├── LivingToolCommand.java      # Comando principal
│   └── AdminCommand.java           # Comandos de admin
├── data/
│   ├── LivingTool.java             # Lógica central de la herramienta
│   ├── ToolData.java               # Storage PDC de stats
│   └── LivingArmor.java            # Armaduras vivientes
├── gui/
│   ├── DashboardGUI.java           # Panel principal (54 slots)
│   ├── HistoryGUI.java             # Historial de la herramienta
│   ├── ImprovedSkillTreeGUI.java   # Árbol de habilidades
│   ├── AssemblyGUI.java            # Mesa de ensamblaje
│   ├── BossForgeGUI.java           # Forja de objetos de jefe
│   └── ...
├── listeners/
│   ├── ExperienceListener.java     # XP de minería y combate
│   ├── GUIListener.java            # Manejador de GUIs
│   ├── ReputationListener.java     # Sistema de títulos
│   └── ...
├── manager/
│   ├── MilestoneManager.java       # 18 hitos de progresión
│   ├── WeatherBonusManager.java    # Bonus de clima y hora
│   ├── RelicEffectManager.java     # Efectos pasivos de reliquias
│   ├── LegacyManager.java          # Sistema de legado post-prestige
│   ├── ForestArenaManager.java     # Arena de la Dryad
│   ├── DesertArenaManager.java     # Arena del Wyrm
│   ├── OceanArenaManager.java      # Arena del Leviathan
│   ├── BossAbilityManager.java     # Habilidades de los jefes
│   ├── FeedingManager.java         # Sistema de alimentación
│   ├── DialogueManager.java        # Diálogos por personalidad
│   └── ...
└── runes/
    ├── RuneType.java               # Tipos de runas
    └── RuneManager.java            # Gestión de runas
```

---

## 🔮 Roadmap — Próximas Mejoras Planeadas

> Las siguientes funcionalidades están diseñadas y planeadas para futuras versiones:

### 🎯 v1.1 — Retos Diarios
- **Sistema de Daily Challenges**: Cada día 3 tareas aleatorias para la herramienta
  - Ejemplos: "Mina 50 minerales de diamante", "Mata 20 esqueletos", "Sobrevive una tormenta"
  - Recompensas: XP bonus, Geodes Rúnicas, fragmentos de reliquia
  - Las tareas varían según la categoría de herramienta (espada → tareas de combate)

### ✨ v1.1 — Trails de Partículas por Nivel
- Herramientas nivel 50+: trail sutil de partículas al caminar
- Nivel 100+: trail más pronunciado con color según personalidad
- Nivel 200 (pre-prestige): aura completa con partículas únicas
- Prestige I+: efecto especial dorado en la herramienta

### 📢 v1.1 — Notificaciones de Servidor
- Broadcast cuando alguien hace **Prestige** por primera vez: `☆ [Jugador] ha ascendido con [Herramienta]!`
- Broadcast al matar un **boss nuevo**: `[Jugador] ha derrotado al Leviathan por primera vez!`
- Broadcast al completar hitos de nivel 200: pantalla para todo el servidor

### 🏅 v1.2 — Leaderboard Global
- `/livingtool top` — GUI con Top 10 en categorías:
  - Nivel más alto
  - Más mobs eliminados
  - Más prestige
  - Más hitos completados
- Actualización en tiempo real, persiste entre reinicios

### 📜 v1.2 — Naming Ceremony
- La primera vez que usas una herramienta viviente, se abre una GUI para ponerle nombre
- El nombre queda grabado como "Nombre del Artesano" en el lore permanentemente
- Bonus de +5% XP durante las primeras 24h de juego (luna de miel)

### 🎁 v1.2 — Daily Login Bonus
- Primera vez que usas cada herramienta en el día: mensaje especial y XP bonus
- Streak de días consecutivos: multiplica el bonus (max ×3 en día 7+)
- La herramienta comenta sobre cuánto tiempo pasó desde el último uso

### 💎 v1.3 — Cristalización de Reliquias
- Las reliquias pueden mejorar de nivel I → II → III con materiales raros
- Cada nivel mejora el efecto pasivo (ej: Dryad I → Regen I, Dryad III → Regen III + Absorción)
- Requiere un nuevo item crafteable: "Cristal de Ascensión"

### 🌍 v1.3 — Eventos de Mundo
- **Noche Corrupta**: mobs más fuertes, +50% XP, mayor chance de drops especiales
- **Marea Alta**: el Leviathan aparece en cualquier océano cercano
- **Tormenta de Runas**: mayor chance de geodes y runas al minar

### 💾 v1.4 — Persistencia en Base de Datos
- Guardar estadísticas de herramientas en YAML/SQLite
- Los datos del leaderboard y hitos persisten entre reinicios del servidor
- Estadísticas históricas por jugador

### 🔧 v1.4 — Optimización de Rendimiento
- Cache de `LivingTool` por jugador (evitar crear objetos en cada evento)
- Throttle dinámico de partículas según TPS del servidor
- Pool de objetos reutilizables para operaciones frecuentes

---

## 🛠️ Compilación

```bash
git clone https://github.com/Viega095/LivingTools.git
cd LivingTools
mvn clean package -DskipTests
```

El `.jar` se genera en `target/livingtools-1.0-BETA.jar`.

---

## 📄 Licencia

MIT License — libre uso, modificación y distribución.

---

## 👤 Autor

**Viega095** — [GitHub](https://github.com/Viega095)

*Plugin desarrollado con asistencia de Google Antigravity AI*
