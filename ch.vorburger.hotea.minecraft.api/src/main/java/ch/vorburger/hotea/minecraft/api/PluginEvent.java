package ch.vorburger.hotea.minecraft.api;

import org.spongepowered.api.event.GameEvent;
import org.spongepowered.api.plugin.PluginContainer;

/**
 * Represents all plugin state events, see sub interfaces and {@link PluginState}.
 */
public interface PluginEvent extends GameEvent {
	
	/**
	 * Get the Plugin
	 * 
	 * @return the {@link PluginContainer}
	 */
	PluginContainer getPluginContainer();

}
