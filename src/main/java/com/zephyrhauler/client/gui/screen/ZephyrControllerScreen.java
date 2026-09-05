package com.zephyrhauler.client.gui.screen;

import com.zephyrhauler.ZephyrHauler;
import com.zephyrhauler.component.DockEntry;
import com.zephyrhauler.component.ZephyrDataComponents;
import com.zephyrhauler.menu.ZephyrControllerMenu;
import com.zephyrhauler.network.ControllerActionPayload;
import com.zephyrhauler.network.DockStatusRequestPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class ZephyrControllerScreen extends AbstractContainerScreen<ZephyrControllerMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ZephyrHauler.MOD_ID, "textures/gui/zephyr_controller.png");

    private int selectedIndex = -1;
    private int scrollOffset = 0;
    private int pendingDeleteIndex = -1;
    private int deleteConfirmTimer = 0;

    private Button btnLink;
    private Button btnReset;
    private Button btnRefresh;

    private int currentDockStatus = -1;
    private String feedbackMessage = "";
    private int feedbackTimer = 0;

    public ZephyrControllerScreen(ZephyrControllerMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageHeight = 166;
        this.imageWidth = 176;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 6;
        this.inventoryLabelY = 73;

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        this.btnLink = this.addRenderableWidget(Button.builder(Component.translatable("gui.zephyr_hauler.button.link"), button -> executeAction(0))
                .bounds(x - 117, y + 118, 110, 20).build());

        this.btnReset = this.addRenderableWidget(Button.builder(Component.translatable("gui.zephyr_hauler.button.reset"), button -> executeAction(1))
                .bounds(x - 117, y + 141, 110, 20).build());

        this.btnRefresh = this.addRenderableWidget(Button.builder(Component.literal("⟳"), button -> executeRefresh())
                .bounds(x - 24, y + 4, 14, 14)
                .tooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("gui.zephyr_hauler.button.refresh")))
                .build());

        updateButtonStates();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (this.deleteConfirmTimer > 0) {
            this.deleteConfirmTimer--;
            if (this.deleteConfirmTimer <= 0) {
                this.pendingDeleteIndex = -1;
            }
        }
    }

    private void updateButtonStates() {
        boolean hasSelection = this.selectedIndex >= 0;
        this.btnLink.active = hasSelection;
        this.btnReset.active = hasSelection;
    }

    public void receiveStatusUpdate(int status, String message) {
        if (status != -1) {
            this.currentDockStatus = status;
        }
        if (message != null && !message.isEmpty()) {
            this.feedbackMessage = message;
            this.feedbackTimer = 60;

            if (message.contains("success")) {
                this.minecraft.player.playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
            } else if (message.contains("error")) {
                this.minecraft.player.playSound(net.minecraft.sounds.SoundEvents.VILLAGER_NO, 1.0f, 1.0f);
            }
        }
    }

    private void executeAction(int actionType) {
        ItemStack controllerStack = this.minecraft.player.getMainHandItem();
        if (!(controllerStack.getItem() instanceof com.zephyrhauler.item.ZephyrControllerItem)) controllerStack = this.minecraft.player.getOffhandItem();

        if (controllerStack.getItem() instanceof com.zephyrhauler.item.ZephyrControllerItem) {
            List<DockEntry> docks = controllerStack.getOrDefault(ZephyrDataComponents.SAVED_DOCKS.get(), List.of());
            if (this.selectedIndex >= 0 && this.selectedIndex < docks.size()) {
                DockEntry selectedDock = docks.get(this.selectedIndex);
                PacketDistributor.sendToServer(new ControllerActionPayload(actionType, selectedDock.dockId(), selectedDock.pos(), selectedDock.name()));
            }
        }
    }

    private void executeRefresh() {
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new com.zephyrhauler.network.RefreshDocksPayload(true));

        this.currentDockStatus = -1;
        this.feedbackMessage = Component.translatable("message.zephyr_hauler.controller.refreshing").getString();
        this.feedbackTimer = 40;

        ItemStack controllerStack = this.minecraft.player.getMainHandItem();
        if (!(controllerStack.getItem() instanceof com.zephyrhauler.item.ZephyrControllerItem)) controllerStack = this.minecraft.player.getOffhandItem();

        if (controllerStack.getItem() instanceof com.zephyrhauler.item.ZephyrControllerItem) {
            List<DockEntry> docks = controllerStack.getOrDefault(ZephyrDataComponents.SAVED_DOCKS.get(), List.of());
            if (this.selectedIndex >= 0 && this.selectedIndex < docks.size()) {
                DockEntry selectedDock = docks.get(this.selectedIndex);
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(new com.zephyrhauler.network.DockStatusRequestPayload(selectedDock.dockId(), selectedDock.pos()));
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        ItemStack controllerStack = this.minecraft.player.getMainHandItem();
        if (!(controllerStack.getItem() instanceof com.zephyrhauler.item.ZephyrControllerItem)) controllerStack = this.minecraft.player.getOffhandItem();

        if (controllerStack.getItem() instanceof com.zephyrhauler.item.ZephyrControllerItem) {
            List<DockEntry> docks = controllerStack.getOrDefault(ZephyrDataComponents.SAVED_DOCKS.get(), List.of());
            int maxScroll = Math.max(0, docks.size() - 6);

            if (maxScroll > 0) {
                if (scrollY > 0) {
                    this.scrollOffset = Math.max(0, this.scrollOffset - 1);
                } else if (scrollY < 0) {
                    this.scrollOffset = Math.min(maxScroll, this.scrollOffset + 1);
                }
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        ItemStack controllerStack = this.minecraft.player.getMainHandItem();
        if (!(controllerStack.getItem() instanceof com.zephyrhauler.item.ZephyrControllerItem)) controllerStack = this.minecraft.player.getOffhandItem();

        if (controllerStack.getItem() instanceof com.zephyrhauler.item.ZephyrControllerItem) {
            List<DockEntry> docks = controllerStack.getOrDefault(ZephyrDataComponents.SAVED_DOCKS.get(), List.of());
            int limit = Math.min(docks.size() - this.scrollOffset, 6);

            for (int i = 0; i < limit; i++) {
                int actualIndex = i + this.scrollOffset;
                int itemX = x - 115;
                int itemY = y + 23 + (i * 14);
                int deleteX = itemX + 90;

                if (mouseX >= deleteX && mouseX <= deleteX + 9 && mouseY >= itemY - 1 && mouseY <= itemY + 8) {
                    if (this.pendingDeleteIndex == actualIndex) {
                        PacketDistributor.sendToServer(new com.zephyrhauler.network.RemoveDockPayload(actualIndex));

                        if (this.selectedIndex == actualIndex) {
                            this.selectedIndex = -1;
                            this.currentDockStatus = -1;
                            this.feedbackMessage = "";
                        } else if (this.selectedIndex > actualIndex) {
                            this.selectedIndex--;
                        }

                        this.pendingDeleteIndex = -1;
                        this.updateButtonStates();
                        this.minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    } else {
                        this.pendingDeleteIndex = actualIndex;
                        this.deleteConfirmTimer = 60;
                        this.minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.2F));
                    }
                    return true;
                }

                if (mouseX >= itemX - 2 && mouseX <= itemX + 85 && mouseY >= itemY - 2 && mouseY <= itemY + 12) {
                    this.pendingDeleteIndex = -1;

                    if (this.selectedIndex != actualIndex) {
                        this.selectedIndex = actualIndex;
                        this.currentDockStatus = -1;
                        this.feedbackMessage = "";
                        PacketDistributor.sendToServer(new DockStatusRequestPayload(docks.get(actualIndex).dockId(), docks.get(actualIndex).pos()));
                    }
                    this.updateButtonStates();
                    this.minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        ItemStack controllerStack = this.minecraft.player.getMainHandItem();
        if (!(controllerStack.getItem() instanceof com.zephyrhauler.item.ZephyrControllerItem)) controllerStack = this.minecraft.player.getOffhandItem();

        if (controllerStack.getItem() instanceof com.zephyrhauler.item.ZephyrControllerItem) {
            List<DockEntry> docks = controllerStack.getOrDefault(ZephyrDataComponents.SAVED_DOCKS.get(), List.of());

            graphics.fill(x - 122, y, x - 2, y + 166, 0x99000000);

            graphics.drawString(this.font, Component.translatable("gui.zephyr_hauler.saved_docks").withStyle(ChatFormatting.GOLD), x - 115, y + 6, 0xFFFFFF);
            graphics.fill(x - 118, y + 17, x - 6, y + 18, 0xFF555555);

            int limit = Math.min(docks.size() - this.scrollOffset, 6);
            for (int i = 0; i < limit; i++) {
                int actualIndex = i + this.scrollOffset;
                int itemX = x - 115;
                int itemY = y + 23 + (i * 14);

                if (actualIndex == this.selectedIndex) {
                    graphics.fill(itemX - 2, itemY - 2, itemX + 85, itemY + 11, 0x66AAAAAA);
                    graphics.drawString(this.font, Component.literal("> ").withStyle(ChatFormatting.YELLOW).append(Component.literal(docks.get(actualIndex).name()).withStyle(ChatFormatting.WHITE)), itemX, itemY, 0xFFFFFF);
                } else {
                    if (mouseX >= itemX - 2 && mouseX <= itemX + 85 && mouseY >= itemY - 2 && mouseY <= itemY + 11) {
                        graphics.fill(itemX - 2, itemY - 2, itemX + 85, itemY + 11, 0x33FFFFFF);
                    }
                    graphics.drawString(this.font, Component.literal("- ").withStyle(ChatFormatting.GRAY).append(Component.literal(docks.get(actualIndex).name()).withStyle(ChatFormatting.WHITE)), itemX, itemY, 0xFFFFFF);
                }

                int deleteX = itemX + 90;
                boolean hoveringDelete = mouseX >= deleteX && mouseX <= deleteX + 9 && mouseY >= itemY - 1 && mouseY <= itemY + 8;

                if (actualIndex == this.pendingDeleteIndex) {
                    int confirmColor = (this.deleteConfirmTimer % 20 > 10) ? 0xFFFF3333 : 0xFFCC0000;
                    graphics.fill(deleteX, itemY - 1, deleteX + 9, itemY + 8, hoveringDelete ? 0xFFFF5555 : confirmColor);

                    graphics.drawString(this.font, "✓", deleteX + 1, itemY - 2, 0xFFFFFF);
                } else {
                    graphics.fill(deleteX, itemY - 1, deleteX + 9, itemY + 8, hoveringDelete ? 0xFFFF5555 : 0xAAFF0000);
                    graphics.drawString(this.font, "X", deleteX + 2, itemY, 0xFFFFFF);
                }
            }

            if (docks.size() > 6) {
                int scrollBarX = x - 6;
                int scrollBarY = y + 21;
                int scrollBarHeight = 14 * 6;
                graphics.fill(scrollBarX, scrollBarY, scrollBarX + 2, scrollBarY + scrollBarHeight, 0xFF555555);
                int thumbHeight = Math.max(10, scrollBarHeight * 6 / docks.size());
                int thumbY = scrollBarY + (int) ((scrollBarHeight - thumbHeight) * ((float) this.scrollOffset / (docks.size() - 6)));
                graphics.fill(scrollBarX, thumbY, scrollBarX + 2, thumbY + thumbHeight, 0xFFCCCCCC);
            }

            if (this.selectedIndex >= 0) {
                MutableComponent statusText = Component.translatable("gui.zephyr_hauler.status.calculating").withStyle(ChatFormatting.GRAY);
                if (currentDockStatus == 0) { statusText = Component.translatable("gui.zephyr_hauler.status.free").withStyle(ChatFormatting.GREEN); }
                else if (currentDockStatus == 1) { statusText = Component.translatable("gui.zephyr_hauler.status.occupied").withStyle(ChatFormatting.RED); }
                else if (currentDockStatus == 2) { statusText = Component.translatable("gui.zephyr_hauler.status.inaccessible").withStyle(ChatFormatting.GRAY); }

                graphics.drawString(this.font, Component.translatable("gui.zephyr_hauler.status.prefix").append(" ").append(statusText), x - 117, y + 107, 0xFFFFFF);
            }

            if (this.feedbackTimer > 0 && !this.feedbackMessage.isEmpty()) {
                this.feedbackTimer--;

                Component msgComp = Component.translatable(this.feedbackMessage);
                int msgWidth = this.font.width(msgComp);
                int centerX = this.width / 2;
                int msgY = y - 18;

                graphics.fill(centerX - (msgWidth / 2) - 6, msgY - 3, centerX + (msgWidth / 2) + 6, msgY + 11, 0xCC000000);

                int textColor = this.feedbackMessage.contains("error") ? 0xFF5555 : 0xFFFF55;
                graphics.drawCenteredString(this.font, msgComp, centerX, msgY, textColor);
            }
        }
    }
}