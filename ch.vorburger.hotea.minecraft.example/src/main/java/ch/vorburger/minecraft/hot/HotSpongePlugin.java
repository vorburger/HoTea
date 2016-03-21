package ch.vorburger.minecraft.hot;

import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

@Plugin(id = "ch.vorburger.minecraft.hot.example", name = "HOT Sponge Plug-In", version = "1.0")
public class HotSpongePlugin {

	@Inject	Game game;
	@Inject PluginContainer plugin;
	@Inject Logger logger;

	Optional<CommandMapping> commandMapping = Optional.empty();

	@Listener
	public void onPluginLoaded(GameStartingServerEvent event) {
		logger.info("I'm loaded! ;)");

		CommandSpec myCommandSpec = CommandSpec.builder()
			    .description(Text.of("Hello World Command"))
			    .executor(new HelloWorldCommand())
			    .build();
		commandMapping = game.getCommandManager().register(plugin, myCommandSpec, "hello");
	}

	@Listener
	public void onPluginUnloading(GameStoppingServerEvent event) {
		if (commandMapping.isPresent()) {
			game.getCommandManager().removeMapping(commandMapping.get());
		}
	}

}
