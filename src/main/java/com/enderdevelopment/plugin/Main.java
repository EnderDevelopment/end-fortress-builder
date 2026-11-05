package com.enderdevelopment.plugin;

import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        getCommand("endcastle").setExecutor(new EndCastleCommand());
        getCommand("endcastledecor").setExecutor(new EndCastleDecorCommand());
        getLogger().info("FuturisticEndCastle enabled - build the future castle with /endcastle");
    }

    @Override
    public void onDisable() {
        getLogger().info("FuturisticEndCastle disabled");
    }
}
