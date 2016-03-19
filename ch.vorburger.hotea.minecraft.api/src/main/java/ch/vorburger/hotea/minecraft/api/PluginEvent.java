package ch.vorburger.hotea.minecraft.api;

import org.spongepowered.api.event.game.state.GameStateEvent;
import org.spongepowered.api.plugin.PluginContainer;

/**
 * Represents all plugin state events, see sub interfaces and {@link PluginState}.
 */
public interface PluginEvent extends GameStateEvent {

	/**
	 * Get the Plugin
	 *
	 * @return the {@link PluginContainer}
	 */
	PluginContainer getPluginContainer();

}
