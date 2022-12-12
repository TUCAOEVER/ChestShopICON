package com.promc.chestshopicon.Listener;

import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Events.ItemParseEvent;
import com.Acrobot.ChestShop.Events.ItemStringQueryEvent;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.Acrobot.ChestShop.Utils.uBlock;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;

import static com.promc.chestshopicon.ChestShopICON.containerSignMap;
import static com.promc.chestshopicon.ChestShopICON.display;
import static com.promc.chestshopicon.ChestShopICON.getXYZLoc;
import static com.promc.chestshopicon.ChestShopICON.signArrayList;
import static com.promc.chestshopicon.ChestShopICON.signHoloMap;

public class ChunkLoadListener implements Listener {
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

}
