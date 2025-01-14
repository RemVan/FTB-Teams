package dev.ftb.mods.ftbteams.client;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.*;
import dev.ftb.mods.ftblibrary.ui.input.MouseButton;
import dev.ftb.mods.ftblibrary.ui.misc.NordColors;
import dev.ftb.mods.ftbteams.data.ClientTeamManager;
import dev.ftb.mods.ftbteams.data.KnownClientPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.HashSet;
import java.util.Set;

import static dev.ftb.mods.ftblibrary.ui.misc.NordColors.*;

public abstract class BaseInvitationScreen extends BaseScreen implements InvitationSetup {
    protected final Set<KnownClientPlayer> available = new HashSet<>();
    protected final Set<GameProfile> invites = new HashSet<>();
    private Panel playerPanel;
    private Button executeButton;
    private Button closeButton;
    private final Component title;

    public BaseInvitationScreen(Component title) {
        this.title = title;

        ClientTeamManager.INSTANCE.knownPlayers.forEach((id, known) -> {
            if (shouldIncludePlayer(known)) {
                available.add(known);
            }
        });
    }

    protected abstract boolean shouldIncludePlayer(KnownClientPlayer player);

    protected abstract ExecuteButton makeExecuteButton();

    @Override
    public boolean onInit() {
        setWidth(200);
        setHeight(getScreen().getGuiScaledHeight() * 3 / 4);
        return true;
    }

    @Override
    public void drawBackground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
        GuiHelper.drawHollowRect(matrixStack, x, y, w, h, POLAR_NIGHT_0, true);
        POLAR_NIGHT_0.draw(matrixStack, x + 1, y + 1, w - 2, h - 2);
        POLAR_NIGHT_1.draw(matrixStack, x + playerPanel.posX, y + playerPanel.posY, playerPanel.width, playerPanel.height);
        POLAR_NIGHT_0.draw(matrixStack, x + 1, y + h - 20, w - 2, 18);
    }

    @Override
    public void drawForeground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
        theme.drawString(matrixStack, title, x + w / 2F, y + 5, SNOW_STORM_1, Theme.CENTERED);
    }

    @Override
    public void addWidgets() {
        add(closeButton = new SimpleButton(this, Component.translatable("gui.cancel"), Icons.CANCEL.withTint(SNOW_STORM_2), (simpleButton, mouseButton) -> closeGui()) {
            @Override
            public void draw(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
                drawIcon(matrixStack, theme, x, y, w, h);
            }
        });

        add(playerPanel = new PlayerButtonPanel());

        add(executeButton = makeExecuteButton());
    }

    @Override
    public void alignWidgets() {
        closeButton.setPosAndSize(width - 20, 2, 16, 16);
        playerPanel.setPosAndSize(2, 20, width - 4, height - 40);
        executeButton.setPosAndSize(60, height - 18, 80, 16);
    }

    @Override
    public boolean isInvited(GameProfile profile) {
        return invites.contains(profile);
    }

    @Override
    public void setInvited(GameProfile profile, boolean invited) {
        if (invited) {
            invites.add(profile);
        } else {
            invites.remove(profile);
        }
    }

    private class PlayerButtonPanel extends Panel {
        public PlayerButtonPanel() {
            super(BaseInvitationScreen.this);
        }

        @Override
        public void addWidgets() {
            if (available.isEmpty()) {
                add(new TextField(this).setText(Component.translatable("ftbteams.gui.no_players").withStyle(ChatFormatting.ITALIC)).addFlags(Theme.CENTERED));
            } else {
                available.forEach(player -> add(new InvitedButton(this, BaseInvitationScreen.this, player)));
            }
        }

        @Override
        public void alignWidgets() {
            align(new WidgetLayout.Vertical(2, 1, 2));
            widgets.forEach(w -> w.setX(4));
        }

        @Override
        public void drawBackground(PoseStack matrixStack, Theme theme, int x, int y, int w, int h) {
            NordColors.POLAR_NIGHT_2.draw(matrixStack, x, y, w, h);
        }
    }

    protected abstract class ExecuteButton extends NordButton {
        private final Component titleDark;
        private final Runnable callback;

        public ExecuteButton(Component txt, Icon icon, Runnable callback) {
            super(BaseInvitationScreen.this, txt, icon);
            this.titleDark = title.copy().withStyle(Style.EMPTY.withColor(POLAR_NIGHT_0.rgb()));
            this.callback = callback;
        }

        @Override
        public void onClicked(MouseButton button) {
            if (isEnabled()) callback.run();
        }

        @Override
        public Component getTitle() {
            return isEnabled() ? title : titleDark;
        }

        @Override
        public boolean renderTitleInCenter() {
            return true;
        }
    }
}
