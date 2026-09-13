package com.zephyrhauler.client.gui.screen;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.menu.WindSensorMenu;
import com.zephyrhauler.network.WindSensorUpdatePayload;
import com.zephyrhauler.util.ZephyrWindSystem.WindDirection;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;

public class WindSensorScreen extends AbstractContainerScreen<WindSensorMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "textures/gui/wind_sensor.png");
    private final Map<WindDirection, Button> dirButtons = new HashMap<>();

    public WindSensorScreen(WindSensorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 166;
        this.imageWidth = 176;
    }

    private int getPower() { return this.menu.data.get(0); }
    private WindDirection getDirection() {
        int idx = this.menu.data.get(1);
        WindDirection[] dirs = WindDirection.values();
        return (idx >= 0 && idx < dirs.length) ? dirs[idx] : WindDirection.NORTH;
    }
    private boolean isLinked() { return this.menu.data.get(2) == 1; }
    private int getCooldown() { return this.menu.data.get(3); }

    @Override
    protected void init() {
        super.init();
        dirButtons.clear();

        double initialPower = (getPower() - 1) / 14.0D;
        this.addRenderableWidget(new PowerSlider(this.leftPos + 28, this.topPos + 25, 20, 60, initialPower));

        int gridX = this.leftPos + 58;
        int gridY = this.topPos + 25;
        int size = 20;

        addDirBtn(gridX, gridY, WindDirection.NORTH_WEST, "NW");
        addDirBtn(gridX + size, gridY, WindDirection.NORTH, "N");
        addDirBtn(gridX + size * 2, gridY, WindDirection.NORTH_EAST, "NE");

        addDirBtn(gridX, gridY + size, WindDirection.WEST, "W");
        addDirBtn(gridX + size * 2, gridY + size, WindDirection.EAST, "E");

        addDirBtn(gridX, gridY + size * 2, WindDirection.SOUTH_WEST, "SW");
        addDirBtn(gridX + size, gridY + size * 2, WindDirection.SOUTH, "S");
        addDirBtn(gridX + size * 2, gridY + size * 2, WindDirection.SOUTH_EAST, "SE");

        double initialCooldown = (getCooldown() - 1) / 59.0D;
        this.addRenderableWidget(new CooldownSlider(this.leftPos + 128, this.topPos + 25, 20, 60, initialCooldown));

        updateButtonStates();
    }

    private void addDirBtn(int x, int y, WindDirection dir, String label) {
        Button btn = Button.builder(Component.literal(label), button -> {
            sendUpdate(getPower(), getCooldown(), dir);
        }).bounds(x, y, 20, 20).build();

        this.addRenderableWidget(btn);
        dirButtons.put(dir, btn);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean linked = isLinked();
        WindDirection currentDir = getDirection();

        for (Map.Entry<WindDirection, Button> entry : dirButtons.entrySet()) {
            Button btn = entry.getValue();
            if (linked) btn.active = false;
            else btn.active = (entry.getKey() != currentDir);
        }
    }

    private void sendUpdate(int power, int cooldown, WindDirection dir) {
        PacketDistributor.sendToServer(new WindSensorUpdatePayload(this.menu.blockEntity.getBlockPos(), power, cooldown, dir.name()));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        WindDirection currentDir = getDirection();
        String dirLabel = switch (currentDir) {
            case NORTH -> "N"; case SOUTH -> "S"; case EAST -> "E"; case WEST -> "W";
            case NORTH_EAST -> "NE"; case NORTH_WEST -> "NW"; case SOUTH_EAST -> "SE"; case SOUTH_WEST -> "SW";
            default -> "-";
        };
        graphics.drawCenteredString(this.font, dirLabel, this.leftPos + 88, this.topPos + 50, 0x00FF00);

        graphics.drawCenteredString(this.font, Component.literal(getPower() + " Pwr").withStyle(ChatFormatting.RED), this.leftPos + 38, this.topPos + 88, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.literal(getCooldown() + "s").withStyle(ChatFormatting.YELLOW), this.leftPos + 138, this.topPos + 88, 0xFFFFFF);

        if (isLinked()) {
            int textBaseY = this.topPos + 106;

            graphics.drawCenteredString(this.font, Component.translatable("message.zephyr_hauler.sensor.locked").withStyle(ChatFormatting.RED), this.leftPos + imageWidth / 2, textBaseY, 0xFFFFFF);

            String nameStr = this.menu.blockEntity.getLinkedDockName();
            if (nameStr == null || nameStr.isEmpty()) nameStr = "Unknown Dock";

            BlockPos p = this.menu.blockEntity.getLinkedDockPos();
            String coordStr = "X: " + p.getX() + "  Y: " + p.getY() + "  Z: " + p.getZ();

            String dimStr = this.menu.blockEntity.getLinkedDockDimension();
            if (dimStr == null || dimStr.isEmpty()) dimStr = "overworld";
            else dimStr = dimStr.replace("minecraft:", "");

            graphics.drawCenteredString(this.font, Component.literal(nameStr).withStyle(ChatFormatting.GOLD), this.leftPos + imageWidth / 2, textBaseY + 12, 0xFFFFFF);
            graphics.drawCenteredString(this.font, Component.literal(coordStr).withStyle(ChatFormatting.GRAY), this.leftPos + imageWidth / 2, textBaseY + 24, 0xFFFFFF);
            graphics.drawCenteredString(this.font, Component.literal(dimStr).withStyle(ChatFormatting.DARK_AQUA), this.leftPos + imageWidth / 2, textBaseY + 36, 0xFFFFFF);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
    }

    private abstract class VerticalSlider extends AbstractSliderButton {
        public VerticalSlider(int x, int y, int width, int height, double value) {
            super(x, y, width, height, Component.empty(), value);
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFF000000);
            graphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1, 0xFF333333);

            int handleHeight = 8;
            int handleY = (int)(this.getY() + (this.height - handleHeight) * (1.0 - this.value));

            graphics.fill(this.getX(), handleY, this.getX() + this.width, handleY + handleHeight, 0xFF555555);
            graphics.fill(this.getX() + 1, handleY + 1, this.getX() + this.width - 1, handleY + handleHeight - 1, 0xFFAAAAAA);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            this.setValueFromMouse(mouseY);
        }

        @Override
        protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
            this.setValueFromMouse(mouseY);
            super.onDrag(mouseX, mouseY, dragX, dragY);
        }

        private void setValueFromMouse(double mouseY) {
            double d0 = 1.0 - ((mouseY - (double)(this.getY() + 4)) / (double)(this.height - 8));
            this.value = Math.max(0.0D, Math.min(1.0D, d0));
            this.updateMessage();
            this.applyValue();
        }
    }

    private class PowerSlider extends VerticalSlider {
        public PowerSlider(int x, int y, int width, int height, double initialValue) {
            super(x, y, width, height, initialValue);
        }
        @Override protected void updateMessage() {}
        @Override protected void applyValue() {}

        @Override
        public void onRelease(double mouseX, double mouseY) {
            super.onRelease(mouseX, mouseY);
            sendUpdate(1 + (int)(this.value * 14), getCooldown(), getDirection());
        }
    }

    private class CooldownSlider extends VerticalSlider {
        public CooldownSlider(int x, int y, int width, int height, double initialValue) {
            super(x, y, width, height, initialValue);
        }
        @Override protected void updateMessage() {}
        @Override protected void applyValue() {}

        @Override
        public void onRelease(double mouseX, double mouseY) {
            super.onRelease(mouseX, mouseY);
            sendUpdate(getPower(), 1 + (int)(this.value * 59), getDirection());
        }
    }
}