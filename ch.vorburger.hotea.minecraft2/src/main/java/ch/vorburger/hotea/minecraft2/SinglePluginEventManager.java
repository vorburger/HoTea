package ch.vorburger.hotea.minecraft2;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.spongepowered.api.event.Event;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.common.event.RegisteredHandler;
import org.spongepowered.common.event.SpongeEventManager;

public class SinglePluginEventManager extends SpongeEventManager {

	@Inject
	public SinglePluginEventManager(PluginManager pluginManager) {
		super(pluginManager);
	}

	public boolean post(PluginContainer plugin, Event event) {
        List<RegisteredHandler<?>> handlers = getHandlerCache(event).getHandlers();
		List<RegisteredHandler<?>> filteredHandlers = handlers.stream().filter(h -> h.getPlugin().equals(plugin)).collect(Collectors.toList());
		return post(event, filteredHandlers);		
	}

}
