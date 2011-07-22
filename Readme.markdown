ObsidianDestroyer plugin v1.04<br>
by Pandemoneus<br>
https://github.com/Pandemoneus

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
EnabledFor.TNT - set true if TNT is allowed to destroy Obsidian (default: true)<br>
EnabledFor.Creepers - set true if Creepers are allowed to destroy Obsidian (default: false)<br>
EnabledFor.Ghasts - set true if Ghasts are allowed to destroy Obsidian (default: false)<br>
Durability.Enabled - set true if you want to use the durability feature (default: false)<br>
Durability.Amount - determines after how many TNT explosions in the radius the Obsidian block gets destroyed (default: 1)


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
