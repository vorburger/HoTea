package ch.vorburger.hotea.minecraft.api;

import org.spongepowered.api.event.GameEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;

public abstract class AbstractHotPlugin {

	abstract protected void onLoaded(GameEvent event);

	abstract protected void onStop(GameEvent event);


	// Hotea lifecycle

	@Listener
	public final void onPluginLoaded(PluginLoadedEvent event) {
		if (event.getPluginContainer().getInstance() == this)
			onLoaded(event);
	}

	@Listener
	public final void onPlugUnloading(PluginUnloadingEvent event) {
		if (event.getPluginContainer().getInstance() == this)
			onStop(event);
	}


	// Regular Sponge lifecycle

	@Listener
	public final void onServerStarting(GameStartingServerEvent event) {
		onLoaded(event);
	}

	@Listener
	public final void onServerStopping(GameStoppingServerEvent event) {
		onStop(event);
	}

}
