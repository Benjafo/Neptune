Implementation Roadmap
Phase 1: Foundation (Economy Core) ✅
Goal: Establish the basic economic loop - earn and spend essence

Void Essence System

Abstract currency tracking (current + lifetime)
Player NBT storage
UI display (HUD element showing balance)
Test: Manual commands to add/subtract essence


Broker Entity

Entity spawning near main island
Basic interaction (right-click to open GUI)
Invulnerability
Persistence
Test: Broker spawns, can't be killed, GUI opens


Gear Sell Values + Selling

Value calculation system (material × type × enchants × durability)
Selling interface in broker GUI
Transaction logic
Test: Sell various items, verify essence gained matches calculations


Basic Broker Core Stock

Implement only the always-available items
Purchase interface
Item delivery to player
Test: Buy items, verify essence deducted, items received



Milestone: Basic loop works - kill shulkers, sell gear, buy consumables

Phase 2: Progression Framework ✅
Goal: Add the unlock tree and challenge tracking

Unlock Tree Structure

Data structure for unlocks (per player, per tier)
Dependency checking system
Purchase interface in broker GUI
Unlock state storage
Test: Manually grant requirements, verify unlocks become available/purchasable


Challenge Tracking System

Event listeners for all trackable actions
Progress storage (per player, per challenge)
Completion detection
UI for viewing challenges
Test: Perform actions, verify challenges track correctly


Integration: Challenges → Unlocks

Wire challenge completion to unlock availability
Test all unlock requirements
Test: Complete challenges, verify they gate unlocks correctly



Milestone: Full progression system works - challenges track, unlocks gate properly

Phase 3: First Major Feature (Map System) ✅
Goal: Implement the first unlock-gated feature to prove the system works

Map System Core

Command system (/endmap create/load/mark/etc)
Grid calculation
Data storage (maps per player)
Mark/unmark logic with city detection
Test: Create maps, mark cities, verify data persists


Map Visual Display

Full-screen map renderer
Minimap renderer
Keybinds
Visual styling (colors, icons)
Test: Display updates correctly, keybinds work


Map Unlock Integration

Lock map features behind Navigation unlocks
T1: full-screen, T2: minimap, T3: hints
Test: Verify features unlock at correct tiers



Milestone: Map system fully functional and properly gated

Phase 4: Collection System (Relics)
Goal: Add the collection gameplay loop

Relic Items

Custom items with NBT data (rarity, set, lore)
Relic journal data structure
Auto-registration on pickup
Test: Spawn relics manually, verify journal updates


Relic Spawning

Hook into end city loot tables
Rarity-based spawn logic
Bad luck protection for legendaries
One per structure limit
Test: Generate end cities, verify spawn rates match design


Relic Sets

Set tracking logic
Set completion detection
Set bonus application (major sets only)
Display UI
Test: Collect full sets, verify bonuses activate


Relic Journal UI

Display discovered/undiscovered relics
Show sets and progress
Silhouettes for unfound relics
Catalog unlock integration (hints at T2)
Test: UI shows correct state



Milestone: Relic system complete - spawn, collect, track, bonuses work

Phase 5: Unlock-Gated Broker Items
Goal: Populate the broker with items that appear after unlocking tiers

Unlock-Gated Stock

City maps
Relic hints (all tiers)
Repair kits
Shulker shells
Void elytra
Pocket shulker
Enchantment shards
Test: Unlock tiers, verify items appear in broker


Item Functionality

City map points to nearest unmarked city
Relic hints provide grid coordinates
Repair kits restore durability
Pocket shulker death behavior
Test: Use each item, verify effects



Milestone: All unlock-gated broker items functional

Phase 6: Advanced Unlock Features
Goal: Implement the more complex unlock rewards

Processing T1: Breakdown Table

Placeable block
Selling interface
One-at-a-time limit
Test: Place block, sell items remotely


Processing T2: Enchantment System

Gear breakdown → shards
Shard application to gear
Crafting recipes
Test: Extract shards, apply to gear


Processing T3: Void Synthesis

New crafting recipes (Reinforced Elytra, Void Pouch, Ender Magnet)
Item mechanics
Test: Craft and use each item


Processing T4: Relic Infusion

20 duplicate → stat bonus system
Bonus selection UI
Permanent stat application
Test: Infuse relics, verify stats change


Navigation T4: Waypoint System

Waypoint beacon block
Placement/removal
Teleportation with distance-based cost
3-waypoint limit
Test: Place waypoints, teleport, verify costs


Catalog T3: Region Completion

5×5 region detection
Completion check (all grids + all cities)
200 essence reward
Repeatability
Test: Complete regions, verify rewards



Milestone: All unlock features work correctly

Phase 7: Rotating Stock & Polish
Goal: Add dynamic elements and final features

Broker Rotating Stock

Rotation timing logic (3 cities OR 20 days)
Random selection (4 of 13)
Weighting for unseen items
All rotating items (tomes, charms, potions, etc.)
Test: Rotation refreshes correctly, timing works


Tome Mechanics

1-hour buff application
Single-item targeting
Duration stacking
Test: Use tomes, verify effects


Mystery Relic Box

Random relic selection (common-rare, unowned)
Edge case handling (disappears when complete)
Test: Buy boxes, verify relic selection



Milestone: All broker features complete

Phase 8: Endgame & Prestige
Goal: Final progression tier

Endgame Unlock: Void Master

All unlock requirements check
Void flight implementation
Cosmetic rewards (title, particles)
Test: Meet requirements, verify unlock


Prestige System

Reset unlock tree
Permanent +10% essence bonus
Keep relics/challenges
Stacking mechanic
Test: Prestige, verify reset + bonus


Prestige Challenges

Define specific challenges (deferred from design)
Implement tracking
Test: Complete prestige challenges



Milestone: Endgame complete

Phase 9: Polish & Balance
Goal: Final refinement

Relic Names & Lore

Name all 120 relics
Write lore text
Create textures/models if desired
Test: Visual/text review


Broker Dialogue & Appearance

Entity model/texture
Dialogue text
Idle animations
Test: Visual polish review


Balance Pass

Playtest full progression
Adjust costs/rates if needed
Verify ~175-200 session timeline
Test: Full playthrough from start


UI/UX Polish

Improve all UI elements
Better notifications
Clear feedback for all actions
Test: Usability testing



Milestone: Mod is feature-complete and polished

Summary Timeline
PhaseFocusEstimated ComplexityCan Test Independently?1FoundationMedium✓ Yes (basic loop)2ProgressionHigh✓ Yes (with manual setup)3Map SystemMedium✓ Yes (once unlocked)4RelicsHigh✓ Yes (spawn manually)5Unlock ItemsMedium✗ Needs Phase 26Advanced FeaturesHigh✗ Needs Phase 27Rotating StockLow✗ Needs Phase 18EndgameMedium✗ Needs everything9PolishLow✗ Needs everything

Key Principles:

Build foundation first - void essence and broker are prerequisites
Test incrementally - each phase should be testable
Dependencies flow forward - later phases depend on earlier ones
Defer polish - names, lore, visuals come last
Balance at the end - only tune numbers after full system works


Recommended approach: Build in order, complete each phase fully before moving to the next. Don't skip ahead even if features seem exciting - dependencies will cause problems.