package com.promc.chestshopicon;

import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.Breeze.Utils.NameUtil;
import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Events.ItemParseEvent;
import com.Acrobot.ChestShop.Events.ItemStringQueryEvent;
import com.Acrobot.ChestShop.Events.ShopCreatedEvent;
import com.Acrobot.ChestShop.Events.ShopDestroyedEvent;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.Acrobot.ChestShop.Utils.uBlock;
import me.arasple.mc.trhologram.api.TrHologramAPI;
import me.arasple.mc.trhologram.module.display.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;

public final class ChestShopICON extends JavaPlugin implements Listener {

    private final HashMap<Location, Hologram> signHoloMap = new HashMap<>();
    private final HashMap<Location, Location> containerSignMap = new HashMap<>();
    private final ArrayList<Sign> signArrayList = new ArrayList<>();

    @Override
    public void onEnable() {
        if (!Bukkit.getPluginManager().isPluginEnabled("ChestShop")) {
            error("ChestShop not found. Disabling...");
            this.setEnabled(false);
        } else if (!Bukkit.getPluginManager().isPluginEnabled("TrHologram")) {
            error("TrHologram not found. Disabling...");
            this.setEnabled(false);
        } else {
            info("ChestShopICON Enabled! by TUCAOEVER");
            this.getServer().getPluginManager().registerEvents(this, this);
        }
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

    @EventHandler
    public void loadDisplay(ChunkLoadEvent event) {
        // 如果是新生成的区块则不进行检测
        if (event.isNewChunk()) {
            return;
        }
        Chunk chunk = event.getChunk();
        for (BlockState blockState : chunk.getTileEntities()) {
            if (blockState instanceof Sign) {
                Sign sign = (Sign) blockState;
                if (ChestShopSign.isValid(sign)) {
                    signArrayList.add(sign);
                }
            }
        }
        for (Sign sign :
                signArrayList) {
            Container container = uBlock.findConnectedContainer(sign);
            if (container != null) {
                Location containerLoc = getXYZLoc(container.getLocation());
                Location signLoc = getXYZLoc(sign.getLocation());

                // 新建一个物品名称处理事件
                ItemStringQueryEvent queryEvent = new ItemStringQueryEvent(null);
                // 事件内物品名称设置
                queryEvent.setItemString(sign.getLine(3));
                // 调用事件
                ChestShop.callEvent(queryEvent);
                // 处理完物品名称获取新的物品名称(内部名称)
                String material = queryEvent.getItemString();
                // 新建一个物品处理事件 将物品名称处理事件处理过后的文件放进此事件中
                ItemParseEvent parseEvent = new ItemParseEvent(material);
                // 调用事件
                ChestShop.callEvent(parseEvent);
                // 返回物品处理事件处理完成的物品
                ItemStack displayStack = parseEvent.getItem();
                // 如果邻近的商店还没有设置商品则可能会出现异常 与此同时
                // 物品处理事件返回的物品可能为空
                // 如果返回的物品为空 则跳过此循环
                if (displayStack == null) {
                    continue;
                }
                Location displayLocation = container.getLocation().add(0.5, 2, 0.5);
                if (!containerSignMap.containsKey(containerLoc)) {
                    // 将容器与商店牌子关联
                    containerSignMap.put(containerLoc, signLoc);
                    // 将商店牌子与全息关联
                    signHoloMap.put(signLoc, display(displayLocation, displayStack));
                }
            }
        }
    }

    @EventHandler
    public void unloadDisplay(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        // 遍历所有已被加载的商店告示牌
        for (Sign sign : signArrayList) {
            Location signLoc = sign.getLocation();
            Chunk signChunk = signLoc.getChunk();
            // 如果商店牌子的区块与卸载的区块相同
            if (signChunk == chunk) {
                // 从已加载的商店牌子列表里移除目标商店牌子
                signArrayList.remove(sign);
                // 获取该商店牌子关联的箱子
                Container container = uBlock.findConnectedContainer(sign);
                if (container != null) {
                    if (containerSignMap.containsKey(container.getLocation())) {
                        // 通过商店牌子位置获取全息对象
                        Hologram holo = signHoloMap.get(signLoc);
                        if (holo != null) {
                            // 摧毁全息对象
                            holo.destroy();
                        }
                        // 移除此箱子和商店牌子的关联
                        containerSignMap.remove(container.getLocation());
                        // 移除此商店牌子位置对应的全息对象
                        signHoloMap.remove(signLoc);
                    }
                }
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void shopCreate(ShopCreatedEvent event) {
        if (event.getContainer() != null) {
            Location displayLocation = event.getContainer().getLocation().add(0.5, 2, 0.5);
            Sign sign = event.getSign();
            // 新建一个物品名称处理事件
            ItemStringQueryEvent queryEvent = new ItemStringQueryEvent(null);
            // 事件内物品名称设置
            queryEvent.setItemString(event.getSignLine((short) 3));
            // 调用事件
            ChestShop.callEvent(queryEvent);
            // 处理完物品名称获取新的物品名称(内部名称)
            String material = queryEvent.getItemString();
            // 新建一个物品处理事件 将物品名称处理事件处理过后的文件放进此事件中
            ItemParseEvent parseEvent = new ItemParseEvent(material);
            // 调用事件
            ChestShop.callEvent(parseEvent);
            // 返回物品处理事件处理完成的物品
            ItemStack displayStack = parseEvent.getItem();

            Location containerLoc = getXYZLoc(event.getContainer().getLocation());
            Location signLoc = getXYZLoc(event.getSign().getLocation());
            if (!containerSignMap.containsKey(containerLoc)) {
                // 将容器与商店牌子关联
                containerSignMap.put(containerLoc, signLoc);
                // 将商店牌子与全息关联
                signHoloMap.put(signLoc, display(displayLocation, displayStack));
            }

        }
    }

    @EventHandler
    public void shopDelete(ShopDestroyedEvent event) {
        // 在删除商店时商店牌子存在关联箱子 即 全息正常显示
        if (containerSignMap.containsValue(event.getSign().getLocation())) {
            if (event.getContainer() != null) {
                Location containerLoc = getXYZLoc(event.getContainer().getLocation());
                Location signLoc = getXYZLoc(event.getSign().getLocation());

                // 获取全息对象
                Hologram holo = signHoloMap.get(signLoc);
                if (holo != null) {
                    holo.destroy();
                }

                containerSignMap.remove(containerLoc);
                signHoloMap.remove(signLoc);
            }
        }
    }

    @EventHandler
    public void containerBreak(BlockBreakEvent event) {
        if (event.getBlock().getType().equals(Material.CHEST)) {
            Location blockLoc = getXYZLoc(event.getBlock().getLocation());
            // 如果此箱子和商店牌子关联
            if (containerSignMap.containsKey(blockLoc)) {
                // 获取箱子所对应的商店牌子位置
                Location signLoc = containerSignMap.get(blockLoc);
                // 通过商店牌子位置获取全息对象
                Hologram holo = signHoloMap.get(signLoc);
                if (holo != null) {
                    // 摧毁全息对象
                    holo.destroy();
                }
                // 移除此箱子和商店牌子的关联
                containerSignMap.remove(blockLoc);
                // 移除此商店牌子位置对应的全息对象
                signHoloMap.remove(signLoc);
            }

        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void containerRecover(BlockPlaceEvent event) {
        if (event.getBlock().getType().equals(Material.CHEST)) {
            // 找到最近的商店牌子
            Sign sign = uBlock.getConnectedSign(event.getBlock());
            // 如果找到附近有商店牌子
            if (sign != null) {
                // 新建一个物品名称处理事件
                ItemStringQueryEvent queryEvent = new ItemStringQueryEvent(null);
                // 事件内物品名称设置
                queryEvent.setItemString(sign.getLine(3));
                // 调用事件
                ChestShop.callEvent(queryEvent);
                // 处理完物品名称获取新的物品名称(内部名称)
                String material = queryEvent.getItemString();
                // 新建一个物品处理事件 将物品名称处理事件处理过后的文件放进此事件中
                ItemParseEvent parseEvent = new ItemParseEvent(material);
                // 调用事件
                ChestShop.callEvent(parseEvent);
                // 返回物品处理事件处理完成的物品
                ItemStack displayStack = parseEvent.getItem();
                // 如果邻近的商店还没有设置商品则可能会出现异常 与此同时 物品处理事件返回的物品可能为空
                if (displayStack == null) {
                    return;
                }
                // 获取商店牌子的位置
                Location signLoc = getXYZLoc(sign.getLocation());
                // 获取箱子的位置
                Location containerLoc = getXYZLoc(event.getBlock().getLocation());
                // 如果该商店牌子没有和任何箱子关联 即 不存在全息对象的情况下
                if (!containerSignMap.containsValue(signLoc)) {
                    /*
                      设置新的全息显示坐标 此处不能用 containerLoc
                      因为 containerLoc 在HashMap中是一个对象,对其使用add方法后
                      其内部的数值会随之变动,即HashMap中的对象会随着对象的变化随时变化
                      并不像是Skript内,存储的是一个不会变动的常量
                     */
                    Location displayLocation = event.getBlock().getLocation().add(0.5, 2, 0.5);
                    // 将该商店牌子与箱子关联
                    containerSignMap.put(containerLoc, signLoc);
                    // 将全息与商店牌子关联并创建全息对象
                    signHoloMap.put(signLoc, display(displayLocation, displayStack));
                }

            }
        }
    }


    public Hologram display(Location location, ItemStack itemStack) {
        return TrHologramAPI
                .builder(location)
                .append(player -> itemStack)
                .build();
    }

    public Location getXYZLoc(Location loc) {
        return new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
    }
}
