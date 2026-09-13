package com.zephyrhauler.client.gui.screen;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.menu.ZephyrAutoStationMenu;
import com.zephyrhauler.network.ToggleAutoRepairPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class ZephyrAutoStationScreen extends AbstractContainerScreen<ZephyrAutoStationMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "textures/gui/zephyr_auto_station.png");
    private Button toggleBtn;

    public ZephyrAutoStationScreen(ZephyrAutoStationMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 176;
        this.imageHeight = 184;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 5;
        this.inventoryLabelY = 92;

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        this.toggleBtn = this.addRenderableWidget(Button.builder(Component.literal(""), button -> {
            PacketDistributor.sendToServer(new ToggleAutoRepairPayload(this.menu.blockEntity.getBlockPos()));
        }).bounds(x + 150, y + 18, 16, 16).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        if (this.menu.isAutoRepairEnabled()) {
            this.toggleBtn.setMessage(Component.literal("ON").withStyle(net.minecraft.ChatFormatting.GREEN));
        } else {
            this.toggleBtn.setMessage(Component.literal("OFF").withStyle(net.minecraft.ChatFormatting.RED));
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }
}