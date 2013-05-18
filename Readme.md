ObsidianDestroyer plugin v3.1<br>
by Pandemoneus<br>
https://github.com/Pandemoneus<br>

Official Maintained<br>
https://github.com/drtshock/ObsidianDestroyer

How to install:
----------------
1. Copy 'ObsidianDestroyer.jar' into your 'plugins/' folder.
2. Load your server
3. Edit the newly created 'config.yml' in 'plugins/ObsidianDestroyer/' and set your preferences

How to uninstall:
-----------------
1. Delete the folder called 'ObsidianDestroyer' in plugins/.
2. Delete 'ObsidianDestroyer.jar'

Options:
-----------------
Radius - determines how far around the origin of the explosion Obsidian can be destroyed (default: 3) WARNING: High values probably cause lag<br>
CheckItemId -  The id of the item you use to check durability (default: 38 [rose])<br>
IgnoreCancel - Force ObsidianDestroyer to handle canceled explosions.. (default: false)<br>
EnabledFor.TNT - set true if TNT is allowed to destroy Obsidian (default: true)<br>
EnabledFor.Cannons - set true if you would like the cannons plugin to break obsidian (default: false) https://github.com/DerPavlov/Cannons/<br>
EnabledFor.Creepers - set true if Creepers are allowed to destroy Obsidian (default: false)<br>
EnabledFor.Ghasts - set true if Ghasts are allowed to destroy Obsidian (default: false)<br>
Durability.Enabled - set true if you want to use the durability feature (default: false)<br>
Durability.Obsidian - determines how many explosions in the radius the Obsidian block needs to be destroyed (default: 3)<br>
Durability.EnchantmentTable - determines how many explosions in the radius the EnchantmentTable block needs to be destroyed (default: 2)<br>
Durability.EnderChest - determines how many explosions in the radius the EnderChest block needs to be destroyed (default: 5)<br>
Durability.Anvil - determines how many explosions in the radius the Anvil block needs to be destroyed (default: 2)<br>
Durability.Bedrock.Enabled - set to true if explosions are allowed to break Bedrock (default: false)<br>
Durability.Bedrock.Durability - determines how many explosions in the radius the Bedrock block needs to be destroyed (default: 100)<br>
Durability.ResetEnabled - set true if you want to reset the durability back to max after a certain time has passed (default: true)<br>
Durability.ResetTime - time in milliseconds that has to pass before the durability is reset (default: '600000' (10 minutes))<br>
Durability.UseTimerSafety - Use if your are experiencing crashes due to the server running out of memory (default: false)<br>
Durability.SystemMinMemory - Amount of RAM (in MB) reserved for the server.  The plugin will lose some functionality if free server memory is below this amount (default: 80)<br>
Blocks.ChanceToDrop - set the chance to drop an Obsidian block when it was blown up, set to 1.0 to always drop a block (default: 0.7 (70%))<br>
FluidsProtectObsidian - set to false if you want Obsidian to be able to hit by explosions from within water (default: true)<br>
Explosions.BypassAllFluidProtection - Allows explosions from within a liquid to destroy the liquid, and the surrounding blocks (default: false)<br>
Explosions.TNTCannonsProtected - Protects "TNT Cannons" from destroying themselves when used (default: true)<br>
DisabledOnWorlds - A list of worlds that the plugin will not be used. (default: [])<br>

Permission nodes:
-----------------
obsidiandestroyer.help //makes help command available<br>
obsidiandestroyer.config.reload //makes reload command available<br>
obsidiandestroyer.config.info //makes info command available<br>
obsidiandestroyer.durability.reset //makes reset command available

Commands:
-----------------
obsidiandestroyer (alias: od) - shows the help<br>
obsidiandestroyer reload (alias: od reload) - reloads the plugin<br>
obsidiandestroyer info (alias: od info) - shows the currently loaded config<br>
obsidiandestroyer reset (alias: od reset) - resets all currently saved obsidian durabilities
