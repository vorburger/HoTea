package ch.vorburger.hotea.minecraft.api;

import org.spongepowered.api.event.Event;
import org.spongepowered.api.plugin.PluginContainer;

/**
 * Represents all plugin state events, see sub interfaces and {@link HotPluginState}.
 */
public interface PluginEvent extends Event {

	/**
	 * Get the Plugin
	 *
	 * @return the {@link PluginContainer}
	 */
	PluginContainer getPluginContainer();

}
