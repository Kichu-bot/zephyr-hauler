package com.zephyrhauler.client.gui.screen;

import com.zephyrhauler.network.HubMonitorPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ZephyrHubMonitorScreen extends Screen {

    private final HubMonitorPayload payload;
    private final int ITEMS_PER_PAGE = 5;
    private int currentPage = 0;

    private Button prevButton;
    private Button nextButton;

    private final int panelWidth = 230;
    private final int panelHeight = 195;

    public ZephyrHubMonitorScreen(HubMonitorPayload payload) {
        super(Component.translatable("gui.zephyr_hauler.hub_monitor.title"));
        this.payload = payload;
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - panelWidth) / 2;
        int y = (this.height - panelHeight) / 2;

        this.prevButton = this.addRenderableWidget(Button.builder(Component.literal("<"), button -> {
            this.currentPage--;
            this.updateButtons();
        }).bounds(x + 10, y + 165, 20, 20).build());

        this.nextButton = this.addRenderableWidget(Button.builder(Component.literal(">"), button -> {
            this.currentPage++;
            this.updateButtons();
        }).bounds(x + 200, y + 165, 20, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> this.minecraft.setScreen(null))
                .bounds(x + 65, y + 165, 100, 20).build());

        this.updateButtons();
    }

    private void updateButtons() {
        int maxPages = (int) Math.ceil((double) this.payload.docks().size() / ITEMS_PER_PAGE);
        this.prevButton.active = this.currentPage > 0;
        this.nextButton.active = this.currentPage < maxPages - 1;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        int x = (this.width - panelWidth) / 2;
        int y = (this.height - panelHeight) / 2;

        guiGraphics.fill(x, y, x + panelWidth, y + panelHeight, 0xDD000000);
        guiGraphics.fill(x, y, x + panelWidth, y + 25, 0x88000000);

        guiGraphics.drawCenteredString(this.font, this.payload.hubName(), this.width / 2, y + 8, 0xFFFFFF);

        String totalDocksText = Component.translatable("gui.zephyr_hauler.hub_monitor.total_docks", this.payload.docks().size()).getString();
        guiGraphics.drawString(this.font, totalDocksText, x + 10, y + 8, 0xAAAAAA);

        List<HubMonitorPayload.DockInfo> docks = this.payload.docks();
        int start = this.currentPage * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, docks.size());

        if (docks.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, Component.translatable("gui.zephyr_hauler.hub_monitor.no_docks"), this.width / 2, y + 80, 0x555555);
        } else {
            for (int i = start; i < end; i++) {
                HubMonitorPayload.DockInfo info = docks.get(i);
                int rowY = y + 35 + ((i - start) * 22);

                int colorBox = 0xFF00FF00;
                Component statusComponent = Component.translatable("gui.zephyr_hauler.status.free");
                ChatFormatting statusColor = ChatFormatting.GREEN;

                if (info.status() == 1) {
                    colorBox = 0xFFFFFF00;
                    statusComponent = Component.translatable("gui.zephyr_hauler.status.reserved");
                    statusColor = ChatFormatting.YELLOW;
                } else if (info.status() == 2) {
                    colorBox = 0xFFFF0000;
                    statusComponent = Component.translatable("gui.zephyr_hauler.status.occupied");
                    statusColor = ChatFormatting.RED;
                } else if (info.status() == 3) {
                    colorBox = 0xFF555555;
                    statusComponent = Component.translatable("gui.zephyr_hauler.status.inaccessible");
                    statusColor = ChatFormatting.DARK_GRAY;
                }

                guiGraphics.fill(x + 10, rowY + 1, x + 18, rowY + 9, colorBox);
                guiGraphics.drawString(this.font, info.name(), x + 25, rowY, 0xFFFFFF);
                String cleanStatusText = statusComponent.getString().replaceAll("[⚪🟢🟡🔴]", "").trim();
                guiGraphics.drawString(this.font, Component.literal(cleanStatusText).withStyle(statusColor), x + 25, rowY + 10, 0xFFFFFF);
                String coords = "[" + info.pos().getX() + ", " + info.pos().getY() + ", " + info.pos().getZ() + "]";
                int coordsWidth = this.font.width(coords);
                guiGraphics.drawString(this.font, coords, x + 220 - coordsWidth, rowY + 5, 0x888888);

                guiGraphics.fill(x + 10, rowY + 20, x + 220, rowY + 21, 0x33FFFFFF);
            }
        }

        int maxPages = Math.max(1, (int) Math.ceil((double) docks.size() / ITEMS_PER_PAGE));
        String pageText = Component.translatable("gui.zephyr_hauler.hub_monitor.page", (this.currentPage + 1), maxPages).getString();
        guiGraphics.drawCenteredString(this.font, pageText, this.width / 2, y + 150, 0xAAAAAA);

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