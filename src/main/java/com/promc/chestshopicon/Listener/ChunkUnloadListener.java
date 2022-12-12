package com.promc.chestshopicon.Listener;

import com.Acrobot.ChestShop.Utils.uBlock;
import me.arasple.mc.trhologram.module.display.Hologram;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import static com.promc.chestshopicon.ChestShopICON.*;

public class ChunkUnloadListener implements Listener {

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
}
