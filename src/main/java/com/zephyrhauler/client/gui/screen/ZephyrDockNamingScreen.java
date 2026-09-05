package com.zephyrhauler.client.gui.screen;

import com.zephyrhauler.ZephyrHauler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public class ZephyrDockNamingScreen extends Screen {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "textures/gui/dock_naming.png");

    private final int imageWidth = 176;
    private final int imageHeight = 90;

    private EditBox nameField;
    private Button saveButton;
    private final BlockPos dockPos;
    private final String currentName;

    public ZephyrDockNamingScreen(BlockPos pos, String currentName) {
        super(Component.translatable("gui.zephyr_hauler.dock_naming.title"));
        this.dockPos = pos;
        this.currentName = currentName;
    }

    @Override
    protected void init() {
        super.init();

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        this.nameField = new EditBox(this.font, x + 28, y + 35, 120, 16, Component.translatable("gui.zephyr_hauler.dock_naming.field_placeholder"));
        this.nameField.setMaxLength(32);

        this.nameField.setFocused(true);
        this.nameField.setResponder(text -> {
            if (this.saveButton != null) {
                this.saveButton.active = !text.trim().isEmpty();
            }
        });
        this.addRenderableWidget(this.nameField);
        this.setFocused(this.nameField);
        this.saveButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.zephyr_hauler.button.save"), button -> this.saveAndClose())
                .bounds(x + 58, y + 60, 60, 20).build());

        this.saveButton.active = !this.nameField.getValue().trim().isEmpty();
    }

    private void saveAndClose() {
        String newDockName = this.nameField.getValue().trim();

        if (!newDockName.isEmpty()) {
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                    new com.zephyrhauler.network.SaveDockNamePayload(this.dockPos, newDockName)
            );
        }
        this.minecraft.setScreen(null);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (this.saveButton != null && this.saveButton.active) {
                this.saveAndClose();
                this.minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        guiGraphics.drawCenteredString(this.font, this.title, x + (this.imageWidth / 2), y + 7,  0xffffff);

        String displayCurrent = this.currentName != null && !this.currentName.isEmpty() ? this.currentName : Component.translatable("gui.zephyr_hauler.dock_naming.unnamed").getString();
        Component currentText = Component.translatable("gui.zephyr_hauler.dock_naming.current_name").append(Component.literal(displayCurrent).withStyle(ChatFormatting.YELLOW));
        guiGraphics.drawCenteredString(this.font, currentText, x + (this.imageWidth / 2), y + 20, 0xAAAAAA);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0x66000000);
    }
}