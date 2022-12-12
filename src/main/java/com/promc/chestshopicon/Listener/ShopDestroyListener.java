package com.promc.chestshopicon.Listener;

import com.Acrobot.ChestShop.Events.ShopDestroyedEvent;
import me.arasple.mc.trhologram.module.display.Hologram;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static com.promc.chestshopicon.ChestShopICON.containerSignMap;
import static com.promc.chestshopicon.ChestShopICON.getXYZLoc;
import static com.promc.chestshopicon.ChestShopICON.signHoloMap;

public class ShopDestroyListener implements Listener {
    @EventHandler
    public void shopDestroy(ShopDestroyedEvent event) {
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
}
