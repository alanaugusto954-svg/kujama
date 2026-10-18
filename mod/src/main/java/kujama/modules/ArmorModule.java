package kujama.modules;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import kujama.HudModule;
import net.minecraft.world.item.Items;

public class ArmorModule extends HudModule {
    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };

    public ArmorModule() { super("Armadura", Items.IRON_CHESTPLATE, 4, 124); }

    @Override
    public void render(GuiGraphics g) {
        if (mc.player == null) return;
        for (int i = 0; i < SLOTS.length; i++) {
            ItemStack s = mc.player.getItemBySlot(SLOTS[i]);
            if (s.isEmpty()) continue;
            g.renderItem(s, x, y + i * 18);
            if (s.getMaxDamage() > 0) text(g, String.valueOf(s.getMaxDamage() - s.getDamageValue()), 20, i * 18 + 4);
        }
    }
}
