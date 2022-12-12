package com.promc.chestshopicon;

import com.promc.chestshopicon.Listener.*;
import me.arasple.mc.trhologram.api.TrHologramAPI;
import me.arasple.mc.trhologram.module.display.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;

public final class ChestShopICON extends JavaPlugin {

    public static final HashMap<Location, Hologram> signHoloMap = new HashMap<>();
    public static final HashMap<Location, Location> containerSignMap = new HashMap<>();
    public static final ArrayList<Sign> signArrayList = new ArrayList<>();

    @Override
    public void onEnable() {
        if (!Bukkit.getPluginManager().isPluginEnabled("ChestShop")) {
            error("ChestShop not found. Disabling...");
            this.setEnabled(false);
            return;
        }
        if (!Bukkit.getPluginManager().isPluginEnabled("TrHologram")) {
            error("TrHologram not found. Disabling...");
            this.setEnabled(false);
            return;
        }

        info("ChestShopICON Enabled! by TUCAOEVER");
        PluginManager manager = this.getServer().getPluginManager();
        manager.registerEvents(new ShopCreateListener(), this);
        manager.registerEvents(new ShopDestroyListener(), this);
        manager.registerEvents(new ChunkLoadListener(), this);
        manager.registerEvents(new ChunkUnloadListener(), this);
        manager.registerEvents(new ContainerListener(), this);

    }

    @Override
    public void onDisable() {
        info("Thanks for using ChestICON. by TUCAOEVER");
    }

    public void info(String info) {
        Bukkit.getLogger().info("[ChestShopICON] " + info);
    }

    public void error(String error) {
        Bukkit.getLogger().severe("[ChestShopICON] " + error);
    }

    public static Location getXYZLoc(Location loc) {
        return new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
    }

    public static Hologram display(Location location, ItemStack itemStack) {
        return TrHologramAPI
                .builder(location)
                .append(player -> itemStack)
                .build();
    }
}
