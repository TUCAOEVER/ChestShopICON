package com.promc.chestshopicon.Listener;

import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Events.ItemParseEvent;
import com.Acrobot.ChestShop.Events.ItemStringQueryEvent;
import com.Acrobot.ChestShop.Utils.uBlock;
import me.arasple.mc.trhologram.module.display.Hologram;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import static com.promc.chestshopicon.ChestShopICON.*;

public class ContainerListener implements Listener {
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

    @EventHandler
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
}
