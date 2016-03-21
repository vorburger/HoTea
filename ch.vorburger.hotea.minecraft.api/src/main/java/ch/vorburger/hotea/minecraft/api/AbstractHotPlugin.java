package ch.vorburger.hotea.minecraft.api;

import java.util.Optional;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;

public abstract class AbstractHotPlugin {

	abstract protected void onLoaded();

	abstract protected void onStop();


	// Hotea lifecycle

	@Listener
	public final void onPluginLoaded(PluginLoadedEvent event) {
		Optional<?> plugin = event.getPluginContainer().getInstance();
		if (!plugin.isPresent())
			return;
		if (plugin.get() == this)
			onLoaded();
	}

	@Listener
	public final void onPlugUnloading(PluginUnloadingEvent event) {
		Optional<?> plugin = event.getPluginContainer().getInstance();
		if (!plugin.isPresent())
			return;
		if (plugin.get() == this)
			onStop();
	}


	// Regular Sponge lifecycle

	@Listener
	public final void onServerStarting(GameStartingServerEvent event) {
		onLoaded();
	}

	@Listener
	public final void onServerStopping(GameStoppingServerEvent event) {
		onStop();
	}

}
