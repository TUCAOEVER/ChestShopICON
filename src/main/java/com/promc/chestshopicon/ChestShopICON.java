package com.promc.chestshopicon;

import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.ChestShop.Events.ShopCreatedEvent;
import com.Acrobot.ChestShop.Events.ShopDestroyedEvent;
import com.Acrobot.ChestShop.Utils.uBlock;
import me.arasple.mc.trhologram.api.TrHologramAPI;
import me.arasple.mc.trhologram.module.display.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public final class ChestShopICON extends JavaPlugin implements Listener {

    private HashMap<Location, Hologram> signHoloMap = new HashMap<>();
    private HashMap<Location, Location> containerSignMap = new HashMap<>();

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().isPluginEnabled("ChestShop")) {
            this.getServer().getPluginManager().registerEvents(this, this);
        } else {
            this.setEnabled(false);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void info(String info) {
        Bukkit.getLogger().info("[ChestShopICON] " + info);
    }

    @EventHandler
    public void shopCreate(ShopCreatedEvent event) {
        if (event.getContainer() != null) {
            Location displayLocation = event.getContainer().getLocation().add(0.5, 2, 0.5);
            String itemCode = event.getSignLine((short) 3);
            ItemStack displayStack = MaterialUtil.getItem(itemCode);
            // Put container location and sign location into hashmap
            containerSignMap.put(event.getContainer().getLocation(), event.getSign().getLocation());
            // Link sign location with holo object
            signHoloMap.put(event.getSign().getLocation(), display(displayLocation, displayStack));
        }
    }

    @EventHandler
    public void shopDelete(ShopDestroyedEvent event) {
        // Get holo object from sign map
        Hologram holo = signHoloMap.get(event.getSign().getLocation());
        if (holo != null) {
            holo.destroy();
            signHoloMap.remove(event.getSign().getLocation());
            // Container is not broken before the shop was destroyed
            if (event.getContainer() != null) {
                containerSignMap.remove(event.getContainer().getLocation());
            }
        }
    }

    @EventHandler
    public void containerBreak(BlockBreakEvent event) {
        if (event.getBlock().getType().equals(Material.CHEST)) {
            Location signLoc = containerSignMap.get(event.getBlock().getLocation());
            Hologram holo = signHoloMap.get(signLoc);
            if (holo != null) {
                containerSignMap.remove(event.getBlock().getLocation());
                holo.destroy();
            }
        }
    }

    @EventHandler
    public void containerRecover(BlockPlaceEvent event) {
        if (event.getBlock().getType().equals(Material.CHEST)) {
            // Find nearby shop sign
            Sign sign = uBlock.findAnyNearbyShopSign(event.getBlock());
            // Found shop sign
            if (sign != null) {
                String itemCode = sign.getLine(3);
                ItemStack displayStack = MaterialUtil.getItem(itemCode);
                Location signLoc = sign.getLocation();
                Location containerLoc = event.getBlock().getLocation();
                // Regenerate holo
                containerSignMap.put(containerLoc, signLoc);
                display(containerLoc.add(0.5, 2, 0.5), displayStack);
            }
        }
    }


    public Hologram display(Location location, ItemStack itemStack) {
        return TrHologramAPI
                .builder(location)
                .append(player -> itemStack).build();
    }
}
