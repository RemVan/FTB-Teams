package dev.ftb.mods.ftbteams;

import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.utils.EnvExecutor;
import dev.ftb.mods.ftbteams.client.FTBTeamsClient;
import dev.ftb.mods.ftbteams.data.FTBTeamsCommands;
import dev.ftb.mods.ftbteams.data.TeamBase;
import dev.ftb.mods.ftbteams.data.TeamManager;
import dev.ftb.mods.ftbteams.event.TeamCollectPropertiesEvent;
import dev.ftb.mods.ftbteams.event.TeamEvent;
import dev.ftb.mods.ftbteams.event.TeamManagerEvent;
import dev.ftb.mods.ftbteams.net.FTBTeamsNet;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FTBTeams {
	public static final String MOD_ID = "ftbteams";
	public static final String MOD_NAME = "FTB Teams";
	public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);
	public static FTBTeamsCommon PROXY;

	public FTBTeams() {
		LifecycleEvent.SERVER_BEFORE_START.register(this::serverAboutToStart);
		CommandRegistrationEvent.EVENT.register(this::registerCommands);
		LifecycleEvent.SERVER_STOPPED.register(this::serverStopped);
		LifecycleEvent.SERVER_LEVEL_SAVE.register(this::worldSaved);
		TeamEvent.COLLECT_PROPERTIES.register(this::teamConfig);
		PlayerEvent.PLAYER_JOIN.register(this::playerLoggedIn);
		PlayerEvent.PLAYER_QUIT.register(this::playerLoggedOut);
		FTBTeamsNet.init();
		PROXY = EnvExecutor.getEnvSpecific(() -> FTBTeamsClient::new, () -> FTBTeamsCommon::new);
	}


	private void serverAboutToStart(MinecraftServer server) {
		TeamManager.INSTANCE = new TeamManager(server);
		TeamManagerEvent.CREATED.invoker().accept(new TeamManagerEvent(TeamManager.INSTANCE));
		TeamManager.INSTANCE.load();
	}

	private void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection selection) {
		new FTBTeamsCommands().register(dispatcher);
	}

	private void serverStopped(MinecraftServer server) {
		TeamManagerEvent.DESTROYED.invoker().accept(new TeamManagerEvent(TeamManager.INSTANCE));
		TeamManager.INSTANCE = null;
	}

	private void worldSaved(ServerLevel level) {
		if (TeamManager.INSTANCE != null) {
			TeamManager.INSTANCE.saveNow();
		}
	}

	private void teamConfig(TeamCollectPropertiesEvent event) {
		event.add(TeamBase.DISPLAY_NAME);
		event.add(TeamBase.DESCRIPTION);
		event.add(TeamBase.COLOR);
		event.add(TeamBase.FREE_TO_JOIN);
		event.add(TeamBase.MAX_MSG_HISTORY_SIZE);
	}

	private void playerLoggedIn(ServerPlayer player) {
		if (TeamManager.INSTANCE != null) {
			TeamManager.INSTANCE.playerLoggedIn(player, player.getUUID(), player.getScoreboardName());
		}
	}

	private void playerLoggedOut(ServerPlayer player) {
		if (TeamManager.INSTANCE != null) {
			TeamManager.INSTANCE.playerLoggedOut(player);
		}
	}

	public void setup() {
	}
}
