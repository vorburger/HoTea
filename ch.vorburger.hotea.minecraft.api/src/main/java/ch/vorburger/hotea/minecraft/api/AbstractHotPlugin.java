package ch.vorburger.hotea.minecraft.api;

import org.spongepowered.api.event.GameEvent;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.state.ServerStartingEvent;
import org.spongepowered.api.event.state.ServerStoppingEvent;

public abstract class AbstractHotPlugin {

	abstract protected void onLoaded(GameEvent event);
	
	abstract protected void onStop(GameEvent event);

	
	// Hotea lifecycle

	@Subscribe
	public void onPluginLoaded(PluginLoadedEvent event) {
		if (event.getPluginContainer().getInstance() == this)
			onLoaded(event);
	}
	
	@Subscribe
	public void onPlugUnloading(PluginUnloadingEvent event) {
		if (event.getPluginContainer().getInstance() == this)
			onStop(event);
	}

	
	// Regular Sponge lifecycle

	@Subscribe
	public void onServerStarting(ServerStartingEvent event) {
		onLoaded(event);
	}

	@Subscribe
	public void onServerStopping(ServerStoppingEvent event) {
		onStop(event);
	}

}
