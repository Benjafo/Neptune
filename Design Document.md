End Explorer Mod - Complete Design Document
Minecraft Version: 1.21
Document Version: 1.0
Last Updated: February 2026

Table of Contents

Executive Summary
Core Systems Overview
Economy & Progression
Relic System
Map System
Unlock Tree
Broker System
Challenges
Gear Sell Values
Technical Implementation Notes


Executive Summary
Concept: A progression-focused Minecraft mod that transforms End city exploration into a rewarding long-term gameplay loop through collection, unlocks, and meaningful player choice.
Target Audience: Players who enjoy exploration, collection, and gradual progression systems. Designed for players who already raid end cities but want more depth and purpose.
Core Loop:

Explore End cities and collect loot
Sell gear to broker for void essence currency
Find relics (120 collectibles across 5 rarity tiers)
Progress three-branch unlock tree with currency + milestones
Complete relic sets for permanent bonuses
Track exploration with grid-based map system
Complete 45 challenges
Reach endgame and prestige for continued progression

Progression Timeline:

Target completion: 175-200 gameplay sessions (~30 min each)
Early game: First 20 sessions (unlock basics, learn systems)
Mid game: Sessions 20-100 (steady progression, set completion)
Late game: Sessions 100-175 (high-tier unlocks, rare relics)
Endgame: Session 175+ (prestige, completionism)


Core Systems Overview
1. Broker
What: NPC entity that serves as the economic hub
Where: Spawns near main End island (0,0 coordinates) on first player visit
Function:

Buy player gear for void essence
Sell consumables and unlock permanent upgrades
Rotating stock system for special items

Properties:

Cannot be killed or moved
Unique (one per End dimension)
Persistent across sessions

2. Unlock Tree
Structure: 3 branches × 4 tiers + 1 endgame tier = 13 total unlocks
Branches:

Navigation: Maps, compasses, waypoints, exploration tools
Processing: Gear conversion, enchanting, crafting
Catalog: Relic tracking, set bonuses, collection rewards

Gating: Requires both void essence AND milestone achievements (cities marked, relics collected, challenges completed)
3. Map System
Type: Command-driven grid-based exploration tracker
Function:

Divide End into customizable grids
Track visited areas
Mark end city locations with notes
Visual overlay (minimap + full-screen)

Key Feature: Completely separate from vanilla maps, integrates with unlock tree
4. Relics
Total: 120 collectible items
Function:

Physical items that drop from end cities
Auto-register to permanent journal on pickup
Can be organized into sets (4 major, 8 minor)
Major sets grant powerful permanent bonuses

5. Challenges
Total: 45 achievements across 5 categories
Function: Gate certain unlocks, provide goals, track accomplishments
Categories: Exploration, Collection, Combat, Efficiency, Milestones

Economy & Progression
Currency: Void Essence
Implementation: Abstract currency (similar to XP)

Cannot be dropped or lost on death
Tracks current balance + lifetime earned separately
Earned by selling gear to broker
Spent on unlocks and consumables

Income
Average End City (no ship): ~105 essence

Diamond gear: ~47 essence
Iron gear: ~20 essence
Raw materials: ~20 essence
Shulker shells: 12 essence
Misc items: ~6 essence

End City with Ship: ~163 essence

Base city: ~105 essence
Elytra: +25 essence
Dragon head: +25 essence
Extra shells (~2): +8 essence

Session Income:

Typical 30-minute session: 3 cities
Average income: 100-150 essence per session

Spending
Total unlock costs: 10,375 essence
Estimated broker spending: 9,000-14,000 essence

Consumables (rockets, food, repairs): 5,000-8,000
Relic hints: 2,000-3,000
Rotating special items: 2,000-3,000

Total to completion: ~19,375-24,375 essence
Sessions to complete: 162-203 sessions (~175-200 target)
Sell Rate Modifier
Before Processing T1: 70% of base value
After Processing T1: 100% of base value
This creates a ~43% income boost when unlocking Processing T1, making it feel immediately impactful.

Relic System
Overview
Total Relics: 120
Organization: 4 major sets (60 relics) + 8 minor sets (40 relics) + 20 standalone
Implementation: Physical items that spawn in end city chests

Max one relic per end city structure
Spawns in random chest within structure
Auto-registers to journal when picked up
Can be sold, stored, or used for infusion/transmutation

Rarity Tiers & Drop Rates
TierCountAvg Cities Between FindsDrop Chance per CityCommon403 (range 2-4)~33%Uncommon355.5 (range 4-7)~18%Rare259.5 (range 7-12)~8%Very Rare1516 (range 12-20)~4%Legendary525 (with protection)~1%
Combined drop rate: ~64% of end cities contain a relic
Legendary Drop Mechanism
Base chance: 1% per city
Bad luck protection: Guaranteed drop by 150th city without finding that specific legendary
Counter: Resets per legendary relic (not cumulative across all 5)
Example: If player hasn't found Legendary #1 after 150 cities, it's guaranteed. If they then search for Legendary #2, counter starts fresh.
Relic Sets
Major Sets (15 relics each, grants permanent bonus)
Set NameThemeBonusThe BuildersAncient civilization artifacts+15% essence from all gear soldThe VoidDimension's nature, energy, mysteriesElytra durability drains 15% slowerThe InhabitantsEndermen, shulkers, dragonShulker status effect -20% duration, shulker bullets deal 25% less damageThe ExplorersThose who came beforeAll broker items 15% cheaper
Rarity Distribution per Major Set:

6 Common
5 Uncommon
3 Rare
1 Very Rare

Bonus Activation: Immediately upon collecting all 15 relics, regardless of unlock progression. Triggers achievement popup + chat message + journal entry.
Minor Sets (4-6 relics each, no gameplay reward)
Set NameSizeThemeVoid Constellations6Star patterns seen from the EndShulker Origins4How shulkers came to beDragon's Legacy5Relics connected to the dragonChorus Mutations4Strange chorus plant growthsEnd City Seals6Official markers from different citiesRitual Objects5Ceremonial itemsCartographer's Tools5Ancient mapping equipmentWarrior's Remnants5Ancient weapons and armor fragments
Total Minor Set Relics: 40

16 Common
15 Uncommon
6 Rare
3 Very Rare

Purpose: Provide collection goals and lore without gameplay bonuses. Completing minor sets counts toward Catalog branch unlock requirements.
Standalone Relics (20 total)
Rare curiosities that don't fit any set. Skew toward higher rarities:

0 Common
0 Uncommon
7 Rare
8 Very Rare
5 Legendary

Purpose: Ultimate collectibles, mysterious artifacts, endgame hunting targets.
Relic Values
Sell prices:
TierBase ValueWith Catalog T1 (+50%)Common2030Uncommon3552.5Rare5075Very Rare75112.5Legendary100150Duplicate (any tier)1522.5
Duplicate Handling
Three uses for duplicate relics:

Sell to broker: 15 essence (any rarity)
Relic Infusion (Processing T4):

Requires 20 duplicates of same relic
Choose one permanent buff: +2 max health OR +5% damage OR +5% speed
Repeatable (can infuse multiple times for stacking effects)


Transmutation (Catalog T4):

Convert duplicates toward missing relics
Expensive but helps completionists



Expected Collection Timeline
MilestoneExpected CitiesSessions (~3 cities/session)10 relics (common)~18-22~718 relics (+ uncommon)~50-60~1824 relics (+ rare)~110-130~4028 relics (+ very rare)~180-210~6530 relics (+ legendary)~230-280~80-9560 relics (for major sets)~200-250~70-8585 relics (Catalog T4 req)~270-320~95-110120 relics (completion)~500-600~175-200
NOTE: Specific names and lore for all 120 relics to be defined during implementation.

Map System
Core Concept
Grid-based exploration tracking system for the End dimension. Players create named maps with custom grid sizes, and the system tracks which grids have been visited and which contain marked end cities.
Grid System
Origin: Grid [0,0] contains world coordinates 0,0 (main End island)
Calculation: Grid coords = floor(x / gridSize), floor(z / gridSize)
Grid Size: Any positive integer set at map creation (examples: 200, 512, 1024)
Immutable: Grid size cannot be changed after map creation
Grid States
Two states per grid:

Unmarked: Default state, player hasn't confirmed city presence
Marked: Player manually confirmed end city location with command

Note: Original design included "visited" state for grids entered by player, but simplified to marked/unmarked only. Players must manually mark cities when visiting them.
Commands
Map Management:
/endmap create <name> <gridsize>
  - Creates new map with specified grid size
  - Example: /endmap create main_exploration 512

/endmap load <name>
  - Activates specified map for tracking
  - Only one map can be loaded at a time

/endmap unload
  - Deactivates current map

/endmap list
  - Shows all saved maps with stats (grids visited, cities marked)

/endmap delete <name>
  - Removes map (confirmation prompt recommended)
Marking:
/endmap mark [optional_note]
  - Marks current grid as containing end city
  - Optional note for personal reference
  - Behavior:
    * If unmarked + city detected → Success
    * If unmarked + no city → Warning: "No end city detected. Run again to confirm."
    * If already marked → Error: "This grid square is already marked."

/endmap unmark
  - Removes mark from current grid
  - Behavior:
    * If marked → Success
    * If unmarked → Error: "This grid square is not marked."

/endmap info
  - Shows current grid coords, marked status, notes if any
  - May show distance to nearest unmarked grid
Display:
/endmap show
  - Opens full-screen map view
  - Closes with same command, Escape key, or both

/endmap hide
  - Closes any map display

/endmap minimap
  - Toggles persistent corner minimap overlay

/endmap minimap size <n>
  - Sets minimap to display n×n grids (default 7, range 5-15)
  - Centered on player position
Visual Display
Color scheme:
StateColorAppearanceUnvisitedDark gray (#3a3a3a)Dim squareVisitedLight gray (#8a8a8a)Lighter squareMarked cityGold (#ffaa00)Bright with iconCurrent positionWhite borderHighlighted
Minimap (corner overlay):

Configurable n×n grid display (default 7×7)
Always centered on player
Shows immediate surroundings
Toggleable with command or keybind (default: M)

Full-screen map:

Larger view of entire mapped area
Grid coordinates labeled on edges
Stats display: grids visited, cities marked, completion %
Click marked squares to view notes
Toggleable with command or keybind (default: N)

Multiple Maps
Allowed: Players can create multiple maps with different grid sizes
Use case: Start with large grid (512) for broad coverage, create detailed map (128) for interesting regions
Limitation: Only one map can be loaded/active at a time
Independence: Each map tracks separately
Unlock Integration
UnlockEffectNavigation T1Unlocks entire map system (commands, full-screen view)Navigation T2Unlocks minimap displayNavigation T3Map shows "?" hints for likely cities in unexplored adjacent gridsCatalog T2Map tracks which cities had relics (visual indicator)

Unlock Tree
Structure
3 Branches × 4 Tiers + 1 Endgame = 13 Total Unlocks
Branch Philosophy

Navigation: Exploration tools, wayfinding, mapping enhancements
Processing: Economy, crafting, gear conversion
Catalog: Collection, relics, completionism rewards

Cross-Branch Dependencies
Forces players to engage with all systems rather than rushing one branch:
UnlockRequires from Other BranchNavigation T3Processing T2Navigation T4Catalog T2Processing T2Navigation T1Processing T3Catalog T2Processing T460 relics (Catalog progression)Catalog T2Navigation T1Catalog T3Processing T2EndgameAll three branches at T4

Navigation Branch
Tier 1: Cartographer's Basics
Cost: 75 essence
Requirements:

5 cities marked
15 grids visited
"First Steps" challenge (10 grids)

Unlocks:

Map system (all commands, full-screen map view)
/endmap info shows distance to nearest unmarked grid
Enables basic exploration tracking

Impact: Core quality-of-life improvement. Players who manually mapped before can now track digitally.

Tier 2: Wayfinder
Cost: 250 essence
Requirements:

20 cities marked
60 grids visited
"Wanderer" challenge (50 grids)

Unlocks:

Minimap display (corner overlay)
Compass item (craftable): Points to nearest unmarked city within loaded chunks
Persistent visual reference while exploring

Impact: Navigation convenience, reduced need to stop and check map.

Tier 3: Voidwalker
Cost: 600 essence
Requirements:

50 cities marked
150 grids visited
"No End in Sight" challenge (20,000 blocks from main island)
Processing T2

Unlocks:

Map shows "?" hints: Likely city locations in unvisited grids adjacent to visited ones
Void Compass (craftable): Points to nearest undiscovered city regardless of distance or chunk loading

Impact: Reduces empty exploration time, helps find cities in vast unexplored areas.

Tier 4: End Cartographer
Cost: 1200 essence
Requirements:

100 cities marked
300 grids visited
"City Conqueror" challenge (75 cities)
Catalog T2

Unlocks:

Waypoint system: Place up to 3 waypoint beacons anywhere in the End

Teleport between waypoints
Cost: Base 20 essence + 5 per 1000 blocks distance
Examples: 5,000 blocks = 45 essence; 10,000 blocks = 70 essence
Waypoints are placeable blocks, persist between sessions
Can break and relocate freely


Map shows city density heatmap for unexplored regions

Impact: Massive convenience for traversing vast distances. Expensive enough to matter, cheap enough to use regularly.

Processing Branch
Tier 1: Salvager
Cost: 75 essence
Requirements:

Sell 750 essence worth of gear (lifetime)
"Shulker Slayer" challenge (50 shulker kills)

Unlocks:

Gear sells at 100% value (before this: 70% rate)
Breakdown table block: Placeable block for selling items without visiting broker

Right-click with items to sell instantly
Same prices as broker
No inventory, immediate transaction
Can only have one placed at a time



Impact: Immediate ~43% income boost. Feels very impactful. Breakdown table adds huge convenience.

Tier 2: Enchantment Studies
Cost: 300 essence
Requirements:

Sell 2,500 essence worth of gear (lifetime)
15 relics collected
"Wealthy" challenge (1,000 essence lifetime)
Navigation T1

Unlocks:

Enchantment extraction: Break down enchanted gear to get "enchantment shards"
Shards can be applied to your own gear to add enchantments
New crafting system for custom gear

Impact: Transforms junk enchanted gear into useful resource. Enables building perfect loadouts.

Tier 3: Void Synthesis
Cost: 750 essence
Requirements:

Sell 6,000 essence worth of gear (lifetime)
Extract 50 enchantment shards
"Shulker Nemesis" challenge (500 shulker kills)
Catalog T2

Unlocks:

New crafting recipes using essence + shards:

Reinforced Elytra: Higher durability than normal
Void Pouch: Extra inventory space (like portable shulker box)
Ender Magnet: Items auto-collect from longer range



Impact: Unique utility items unavailable elsewhere. Quality-of-life improvements for extended exploration.

Tier 4: Master Artificer
Cost: 1,500 essence
Requirements:

Sell 12,000 essence worth of gear (lifetime)
Craft 10 void synthesis items
60 relics collected
"Rich" challenge (5,000 essence lifetime)

Unlocks:

Elytra boosters: Brief speed burst consumable, costs essence
Portable ender chest: Access ender chest from inventory without placing
Relic infusion: Combine 20 duplicates of same relic → permanent stat buff

Choose one: +2 max health OR +5% damage OR +5% speed
Repeatable (can do multiple times for different relics)
Extremely expensive but provides permanent character progression



Impact: Peak convenience + endgame character progression through relic infusion.

Catalog Branch
Tier 1: Collector
Cost: 75 essence
Requirements:

10 relics collected
"Relic Hunter" challenge (10 relics)

Unlocks:

Relic journal: UI showing all relics

Discovered relics displayed with name, lore, set affiliation
Undiscovered relics shown as silhouettes
Track collection progress per set


Relics sell for +50% value (20→30, 35→52.5, etc.)

Impact: Makes relic collection feel rewarding immediately. Journal provides clear goals.

Tier 2: Archivist
Cost: 300 essence
Requirements:

25 relics collected
25 cities marked
"Set Starter" challenge (complete 1 minor set)
Navigation T1

Unlocks:

Map integration: Marked cities that contained relics show special icon
Relic detector: Audio/particle cue when relic is present in current city (before opening chests)
Journal hints: Shows hints for unfound relics (biome, structure type, associated set)

Impact: Reduces frustration of searching for specific relics. Detector prevents missing relics.

Tier 3: Curator
Cost: 750 essence
Requirements:

50 relics collected
2 minor sets completed
"Relic Hoarder" challenge (60 relics)
Processing T2

Unlocks:

Display cases: Placeable blocks to showcase relics in base (cosmetic)
Set bonuses activate: Completing relic sets now grants permanent passive buffs

Note: Bonuses activate immediately when set is completed, regardless of whether this unlock has been purchased
This unlock simply reveals the existence of set bonuses and provides display cases


Region completion bonus: Fully explore 5×5 grid area → 200 essence reward

Requires all 25 grids visited AND all cities in region marked
Repeatable for different regions
Encourages thorough exploration



Impact: Makes set completion feel rewarding. Region bonus creates sub-goals during exploration.

Tier 4: Grand Archivist
Cost: 1,500 essence
Requirements:

85 relics collected
5 relic sets completed (any combination of major/minor)
100 cities marked
"Major Discovery" challenge (complete 1 major set)

Unlocks:

Relic transmutation: Convert duplicate relics toward missing ones

Expensive conversion rate
Helps completionists finish collections


Legendary relic quests: Broker provides hints/quests for legendary relic locations
Trophy room items: Cosmetic base decoration rewards

Impact: Enables collection completion for dedicated players. Legendary quests add endgame hunting goals.

Endgame: Void Master
Cost: 3,000 essence
Requirements:

All three branches at Tier 4
150 cities marked
110 relics collected
"Veteran Explorer" challenge (500 grids visited)
"Edge Walker" challenge (50,000 blocks from main island)
"Mogul" challenge (15,000 essence lifetime)
"True Master" challenge (all T4s unlocked)
"Grand Curator" challenge (all 4 major sets complete)

Unlocks:

Void flight: Creative-style flight in the End

Fuel cost or limited duration (to be determined during implementation)
Ultimate mobility for final exploration/completionism


Prestige system: Option to reset unlock tree

Reset all unlocks to T0
Keep all relics, challenges, and journal progress
Permanent bonus: +10% essence from all sources
Stacks with multiple prestiges (2x prestige = +20%, etc.)
Unlocks prestige challenges (harder versions of existing challenges)


Cosmetic rewards: Title, particle effects, trophy items
Access to prestige challenge system

Impact: Provides continued progression for completionists. Prestige system enables infinite replayability.
NOTE: Specific prestige challenges to be defined during implementation.

Cost Summary
BranchT1T2T3T4TotalNavigation7525060012002125Processing7530075015002625Catalog7530075015002625Endgame————3000Grand Total10,375

Broker System
Overview
The broker is an NPC entity that serves as the economic hub of the mod. It spawns near the main End island and provides:

Gear purchasing (player sells to broker for essence)
Consumable sales
Unlock purchases
Rotating special items

Broker Properties
Entity Type: Custom NPC
Spawn Location: Near main End island (0,0 coordinates) on first player visit
Spawn Trigger: Player enters the End for first time (or mod is added to existing world)
Invulnerability: Cannot be killed or damaged
Uniqueness: One broker per End dimension
Persistence: Does not despawn, remains at spawn location
Appearance & Dialogue: To be defined during implementation
Inventory Structure
The broker has three inventory categories with different availability rules:

Core Stock: Always available
Unlock-Gated Stock: Appears after specific unlocks
Rotating Stock: Limited selection that refreshes periodically


Core Stock (Always Available)
ItemCostEffectRocket bundle (64)120Bulk fireworks for elytra flightEnder pearl bundle (16)50Emergency mobility/escapeCooked food bundle (32)175Sustenance (expensive due to End scarcity)Basic repair kit30Restores 50% durability to one itemRecall pearl130One-use teleport to main End island (spawn)Bulk ender pearls (64)140Larger pearl bundle, better value per pearl
Design notes:

Rockets and food are expensive to encourage preparation
Recall pearl is safety net for dangerous situations
Basic repair kit is affordable entry point for repairs


Unlock-Gated Stock
Items that appear in broker inventory after unlocking specific tree tiers:
ItemCostUnlockEffectCity map150Navigation T1Reveals grid coordinates of nearest undiscovered cityAdvanced repair kit60Processing T1Restores 100% durability to one itemShulker shell (1)45Processing T2Buy shells directly (luxury pricing)Relic hint (common)120Catalog T1Grid coordinates of nearest city with unfound common relicRelic hint (uncommon)170Catalog T2Grid coordinates of nearest city with unfound uncommon relicRelic hint (rare)240Catalog T3Grid coordinates of nearest city with unfound rare relicRelic hint (very rare)320Catalog T4Grid coordinates of nearest city with unfound very rare relicEnchantment shards (5)75Processing T2Buy shards directly for enchantingVoid elytra220Navigation T3Emergency elytra, low durability (100)Pocket shulker120Processing T3Temporary inventory expansion, vanishes on death
Relic hint mechanics:

Points to nearest city containing unfound relic of specified tier
Provides grid coordinates (not exact world coords)
Only shows cities with relics you don't own yet
If no unfound relics of that tier exist, item doesn't appear in stock

Pocket shulker death behavior:

Item itself vanishes on death
All contents are lost
High risk, high reward for extended expeditions


Rotating Stock
Pool Size: 13 items
Visible at once: 4 items
Selection: Random, with slight weighting toward items player hasn't purchased
Refresh Timing (per-player):
Refreshes when either condition is met (whichever comes first):

Player has looted 3+ cities since last broker visit, OR
20 Minecraft days have elapsed since last refresh

Timer reset: After refresh occurs
Complete item pool:
ItemCostUnlockEffectEfficiency tome90Processing T2+1 Efficiency level to held tool for 1 hour, no cap, applies to one itemProtection tome90Processing T2+1 Protection level to held armor for 1 hour, no cap, applies to one itemDouble drop charm150Catalog T2Next relic found is duplicatedMystery relic box1000Catalog T3Random relic you don't own (common-rare only)Slow falling potion bundle (3)80NoneVoid safety, essential for dangerous situationsTotem of undying350NoneDeath preventionEnchanted golden apple200NoneEmergency panic buttonGolden carrot bundle (32)150NonePremium food alternativeEnder chest60NoneAccess storage anywhereNight vision potion bundle (3)60NoneSee in dark city interiorsScaffolding (64)45NoneEasy city navigationSpyglass30NoneScout cities from distanceShield40NoneBlock shulker bulletsBow + 64 arrows75NoneRanged shulker combat
Special mechanics:
Tome stacking:

Each tome buffs ONE held item (not all worn items)
Duration stacks, level does not
Example: Use 2 Efficiency tomes = +1 Efficiency for 2 hours (not +2 for 1 hour)
Must be applied separately to each armor piece if buffing full set

Mystery relic box edge case:

If player owns all common-rare relics, this item stops appearing in rotation
Prevents wasted purchases

Design intent:

Creates FOMO and urgency ("should I buy this now?")
Encourages checking broker regularly
Prevents stockpiling everything at once


Challenges
Overview
Total: 45 challenges across 5 categories
Visibility: All challenges visible from start with progress tracking
Function: Gate certain unlocks, provide goals, track accomplishments
UI: Achievement-style popups when completed + chat notifications
Categories

Exploration (11) - Distance, grids, cities
Collection (11) - Relics, sets
Combat (6) - Shulkers, endermen
Efficiency (6) - Speed, resource management
Milestones (11) - Progression markers


Exploration Challenges
ChallengeRequirementUnlock GateFirst StepsVisit 10 gridsNavigation T1WandererVisit 50 gridsNavigation T2PathfinderVisit 150 grids—Veteran ExplorerVisit 500 gridsEndgameInto the VoidTravel 10,000 blocks from main island—No End in SightTravel 20,000 blocks from main islandNavigation T3Edge WalkerTravel 50,000 blocks from main islandEndgameCity FinderMark 5 citiesNavigation T1City HunterMark 25 cities—City ConquerorMark 75 citiesNavigation T4CompletionistMark 150 citiesEndgame
Tracking:

Grids: Count unique grid coordinates entered
Distance: Track furthest distance from 0,0 coordinates
Cities marked: Count /endmap mark commands used


Collection Challenges
ChallengeRequirementUnlock GateRelic HunterCollect 10 relicsCatalog T1Relic SeekerCollect 30 relics—Relic HoarderCollect 60 relicsCatalog T3Relic MasterCollect 100 relics—Set StarterComplete 1 minor setCatalog T2Set CollectorComplete 4 minor sets—Set CompletionistComplete all minor sets—Major DiscoveryComplete 1 major setCatalog T4Grand CuratorComplete all major setsEndgameLucky FindFind a legendary relic—Legendary CollectorFind all 5 legendary relics—
Tracking:

Relics: Count unique relics acquired (duplicates don't count)
Sets: Track completion status of all sets


Combat Challenges
ChallengeRequirementUnlock GateShulker SlayerKill 50 shulkersProcessing T1Shulker HunterKill 200 shulkers—Shulker NemesisKill 500 shulkersProcessing T3Shell CollectorCollect 100 shulker shells—Bullet DodgerKill 10 shulkers without being hit—Enderman PacifistMark 10 cities without killing an enderman—
Tracking:

Kills: Standard kill counters
Shell collection: Count shells acquired
Bullet Dodger: Track shulker kills without taking levitation damage
Enderman Pacifist: Track enderman kills, reset counter when marking cities


Efficiency Challenges
ChallengeRequirementUnlock GateQuick LootFully loot a city in under 5 minutes—MarathonLoot 5 cities in one session without returning to main island—MinimalistLoot 3 cities with only elytra + rockets + food—WealthyAccumulate 1,000 essence (lifetime)Processing T2RichAccumulate 5,000 essence (lifetime)Processing T4MogulAccumulate 15,000 essence (lifetime)Endgame
Tracking:

Quick Loot: Timer starts when opening first chest, ends when all chests opened
Marathon: Session defined as continuous play without respawning at main island
Minimalist:

Challenge activates when entering city with only elytra + rockets + food
Can pick up loot during challenge
Must complete 3 cities in a row (returning to base resets counter)


Essence accumulation: Track lifetime earned (separate from current balance)


Milestone Challenges
ChallengeRequirementUnlock GateFirst SaleSell an item to the broker—Big SpenderSpend 500 essence at the broker—Regular CustomerSpend 2,000 essence at the broker—PatronSpend 10,000 essence at the broker—Fully EquippedPurchase every unlock-gated broker item at least once—Upgrade NoviceUnlock any Tier 2—Upgrade AdeptUnlock any Tier 3—Upgrade MasterUnlock any Tier 4—Jack of All TradesReach Tier 2 in all three branches—Well RoundedReach Tier 3 in all three branches—True MasterReach Tier 4 in all three branchesEndgame
Tracking:

All milestone challenges use standard progression tracking
No special mechanics required


Gear Sell Values
Overview
Gear value is calculated using: Base Material Value × Type Multiplier + Enchantments - Durability Penalty
This system creates predictable, scalable pricing while allowing for variation based on item condition and enchantments.

Base Material Values
MaterialBase ValueLeather2Gold3Chainmail4Iron2Diamond7Netherite18
Note: Iron is relatively cheap (fodder), Diamond is valuable, Netherite is premium

Type Multipliers
TypeMultiplierReasoningHelmet1.0xBase armor pieceChestplate1.6xMost protective, most resourcesLeggings1.4xMedium protectionBoots1.0xBase armor pieceSword1.2xPrimary weaponSpear1.2xPrimary weapon (1.21 addition)Pickaxe1.4xEssential tool, high durabilityAxe1.4xTool + weaponShovel0.8xLess essentialHoe0.6xLeast essential

Calculated Gear Values (Base, No Enchants, Full Durability)
Iron gear (base 2):
ItemValueHelmet2Chestplate3.2Leggings2.8Boots2Sword2.4Spear2.4Pickaxe2.8Axe2.8Shovel1.6Hoe1.2
Diamond gear (base 7):
ItemValueHelmet7Chestplate11.2Leggings9.8Boots7Sword8.4Spear8.4Pickaxe9.8Axe9.8Shovel5.6Hoe4.2
Netherite gear (base 18):
ItemValueHelmet18Chestplate28.8Leggings25.2Boots18Sword21.6Spear21.6Pickaxe25.2Axe25.2Shovel14.4Hoe10.8

Enchantment Values
Enchantments add bonus value on top of base item value.
Common enchantments (+0.5 per level):
Protection, Fire Protection, Blast Protection, Projectile Protection, Thorns, Unbreaking, Respiration, Aqua Affinity, Feather Falling, Depth Strider, Frost Walker, Soul Speed, Swift Sneak, Knockback
EnchantmentMax LevelMax BonusProtection IVIV+2Unbreaking IIIIII+1.5Feather Falling IVIV+2
Mid-tier enchantments (+1 per level):
Sharpness, Smite, Bane of Arthropods, Looting, Efficiency, Fortune, Fire Aspect, Lunge
EnchantmentMax LevelMax BonusSharpness VV+5Efficiency VV+5Looting IIIIII+3Fortune IIIIII+3
Valuable enchantments (+7 flat):
Mending, Silk Touch
EnchantmentMax LevelBonusMending II+7Silk Touch II+7
Curses (-3 flat):
Curse of Vanishing, Curse of Binding
EnchantmentMax LevelPenaltyCurse of Vanishing II-3Curse of Binding II-3
Enchanted books:
Sum all enchantment values on the book.
Example: Book with Sharpness V (+5) and Mending (+7) = 12 essence

Durability Modifier
Items with reduced durability sell for less:
Durability %ModifierExample: Diamond Sword (8.4 base)100%1.0x8.4 essence99-75%0.5x4.2 essence74-50%0.35x2.94 essence49-25%0.2x1.68 essence24-1%0.1x0.84 essence
Design intent: Heavily penalizes damaged gear to encourage either repairing or bringing fresh items.

Other Items (End City Loot)
ItemValueNotesGold ingot1Common materialIron ingot0.5Very commonDiamond2Valuable resourceEmerald2Trading resourceBeetroot seeds0WorthlessSaddle5Useful itemIron horse armor2Minor valueGold horse armor3Minor valueDiamond horse armor5Decent valueCopper horse armor1Low valueSpire Armor Trim5Exclusive cosmeticEnder pearl1UtilityChorus fruit0.1Nearly worthlessShulker shell4Valuable, farmableElytra25Premium itemDragon head25Premium trophy

Example Calculations
Diamond chestplate with Protection IV, Unbreaking III, full durability:

Base: 7 × 1.6 = 11.2
Enchants: (4 levels × 0.5) + (3 levels × 0.5) = 2 + 1.5 = 3.5
Durability: 1.0x
Total: 14.7 essence

Diamond pickaxe with Efficiency V, Fortune III, Mending, 80% durability:

Base: 7 × 1.4 = 9.8
Enchants: (5 levels × 1) + (3 levels × 1) + 7 (Mending) = 5 + 3 + 7 = 15
Durability: 0.5x
Total: (9.8 + 15) × 0.5 = 12.4 essence

Iron sword with Sharpness III, Curse of Vanishing, 50% durability:

Base: 2 × 1.2 = 2.4
Enchants: (3 levels × 1) - 3 (curse) = 3 - 3 = 0
Durability: 0.35x
Total: (2.4 + 0) × 0.35 = 0.84 essence


Technical Implementation Notes
Void Essence Implementation
Type: Abstract currency (similar to XP)
Properties:

Cannot be dropped or lost on death
Persists across sessions
Two separate trackers:

Current balance (for purchases)
Lifetime earned (for unlock/challenge requirements)



Technical approach:

Store as player NBT data
Sync to client for UI display
Server-authoritative for all transactions


Relic Implementation
Type: Physical items with custom properties
Behavior:

Drop from end city chests as items
Auto-register to permanent journal on pickup
Can be sold, stored, or used for infusion/transmutation
Journal tracks discovery permanently (separate from physical possession)

Technical approach:

Custom item with NBT data for rarity, set affiliation, lore
Global player data structure for journal (discovered relics)
Spawn system hooks into end city chest loot tables


Map System Implementation
Type: Server-side data structure with client-side rendering
Components:

Map data structure (per player, per map name)
Grid calculation system
Client-side renderer (minimap + full-screen)
Command interface

Technical approach:

Store map data as player NBT or separate file
Calculate grid coords on server, send updates to client
Client renders overlay using Minecraft's HUD system
Structure detection uses Minecraft's structure system


Broker Implementation
Type: Custom entity
Properties:

Spawns on first End entry (world-specific)
Cannot be killed (creative mode can't kill)
Does not despawn or move
Single instance per dimension

Technical approach:

Custom entity with GUI interface
Inventory system with three categories (core, unlock-gated, rotating)
Transaction logic server-side
Rotation timer per player (stored in player data)


Challenge Tracking
Type: Server-side achievement system
Components:

Progress trackers for each challenge type
Completion detection
Reward/unlock trigger system
UI notifications

Technical approach:

Store progress in player NBT data
Listen to relevant game events (kills, distance traveled, etc.)
Trigger unlock availability when challenge completed
Achievement-style toast notifications


Unlock Tree Implementation
Type: Progression system with dependency graph
Components:

Unlock state per player
Dependency checker
Purchase interface
Unlock effects

Technical approach:

Store unlocked tiers in player NBT
Check dependencies before allowing purchase
Apply unlock effects (enable features, modify values, etc.)
Integrate with broker UI


Death Mechanics
Standard Minecraft rules apply:

Items drop on death (unless voided)
Void deaths permanently lose items
No special death penalties from this mod
Void essence is never lost on death (abstract currency)

Special cases:

Pocket shulker vanishes with contents on death
No other mod-specific death mechanics


Multiplayer Compatibility
Design philosophy: Per-player progression
What's per-player:

Void essence balance + lifetime totals
Unlock tree state
Challenge completion
Relic collection + journal
Map data + marked cities
Broker rotation timing

What's shared:

Broker entity (same broker for all players)
Relic spawns in cities (first player to loot gets it)
End city structure generation

Technical considerations:

All player data stored per-UUID
Server authoritative for all progression
Sync relevant data to clients for UI
Handle race conditions for relic looting


Items Requiring Specific Implementation
Still needs definition:

Relic names and lore: All 120 relics need:

Display name
Lore text
Possibly custom texture/model
Set affiliation


Prestige challenges: Define specific challenges after observing base game balance
Broker entity appearance: Model, texture, idle animations, dialogue
Waypoint beacon appearance: Block model, texture, particles
Breakdown table appearance: Block model, texture, UI
Void synthesis items: Reinforced Elytra, Void Pouch, Ender Magnet

Mechanics
Models/textures
Crafting recipes


Processing T4 items: Elytra boosters, Portable ender chest

Specific mechanics
Consumption/usage


Display cases: Model, texture, placement rules
Tome items: Models, textures, usage interface
Custom compass items: Void Compass behavior, pointing logic


Appendix: Design Principles
These principles guided the design process:
1. Respect Player Time

No artificial grind walls
Clear goals and progress indicators
Meaningful rewards for time invested
Expensive items justified by value

2. Meaningful Choices

Unlock tree dependencies force engagement with all systems
Rotating broker stock creates urgency and priority decisions
Multiple paths to similar goals (sell vs. infuse duplicates)

3. Smooth Difficulty Curve

Early unlocks are affordable (75 essence)
Mid-game unlocks require both currency and exploration
Late game heavily emphasizes milestones over pure grinding
Endgame completion is challenging but achievable

4. Collection as Gameplay

Relics are physical items players interact with
Sets provide clear goals
Major set bonuses are impactful
Completion requires exploration, not just luck

5. Respect Vanilla Mechanics

Uses existing Minecraft systems where possible
No breaking of core gameplay loops
Vanilla items have value in the economy
Death mechanics unchanged

6. Multiplayer Friendly

Per-player progression prevents competition
Shared world state (broker, spawns)
No griefing opportunities
Cooperative-compatible


END OF DESIGN DOCUMENT



