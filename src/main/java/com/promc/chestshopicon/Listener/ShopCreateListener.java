package com.promc.chestshopicon.Listener;

import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Events.ItemParseEvent;
import com.Acrobot.ChestShop.Events.ItemStringQueryEvent;
import com.Acrobot.ChestShop.Events.ShopCreatedEvent;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import static com.promc.chestshopicon.ChestShopICON.containerSignMap;
import static com.promc.chestshopicon.ChestShopICON.display;
import static com.promc.chestshopicon.ChestShopICON.getXYZLoc;
import static com.promc.chestshopicon.ChestShopICON.signHoloMap;

public class ShopCreateListener implements Listener {

    @EventHandler
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
}
