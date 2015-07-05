package ch.vorburger.hotea.minecraft2;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.state.ServerStartingEvent;
import org.spongepowered.api.event.state.ServerStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.event.EventManager;
import org.spongepowered.common.Sponge;
import org.spongepowered.vanilla.plugin.VanillaPluginContainer;
import org.spongepowered.vanilla.plugin.VanillaPluginManager;

import com.google.inject.Inject;

import ch.vorburger.hotea.HotClassLoader;
import ch.vorburger.hotea.HotClassLoader.Listener;
import ch.vorburger.hotea.HotClassLoaderBuilder;

/**
 * HOT reloading other plug-ins.
 * 
 * Not an ideal implementation, would be better to have this in the core platform, but see https://github.com/SpongePowered/SpongeVanilla/pull/178.
 *  
 * @author Michael Vorburger
 */
@Plugin(id = "ch.vorburger.hotea.minecraft.HoteaPlugin", name = "Java HOT Reload Plug-In", version = "1.0.0-SNAPSHOT")
public class HoteaPlugin2 implements Listener {
	
	private @Inject Logger logger;
	// protected @Inject Injector injector;
	protected @Inject Game game;
	private @Inject VanillaPluginManager pluginManager;
	private @Inject EventManager eventManager; // TODO once Sponge allows extensible Guice configuration: private @Inject SinglePluginEventManager singlePluginEventManager;
	
	private HotClassLoader hcl;
	private Set<PluginContainer> pluginContainers;

	// TODO remove v0.0.1 hard-coding below, and instead add commands such as:
	// 	 /plugin register <directory>
	//   /plugin unregister <name>
	
	@Subscribe
	public void onServerStarting(ServerStartingEvent event) {
		File dir = new File("/home/vorburger/dev/Minecraft/SwissKnightMinecraft/SpongePowered/MyFirstSpongePlugIn/bin");
		try {
			ClassLoader parentClassLoader = Plugin.class.getClassLoader();
			hcl = new HotClassLoaderBuilder().setParentClassLoader(parentClassLoader).addClasspathEntry(dir).addListener(this).build();
		} catch (Exception e) {
			logger.error("HotClassLoaderBuilder failed", e);
		}
	}

	@Override
	public void onReload(ClassLoader newClassLoader) throws Throwable {
		// Impl as in org.spongepowered.vanilla.plugin.VanillaPluginManager.loadPlugins(Object, Iterable<String>)
		unloadPlugins();
		URLClassLoader urlClassLoader = (URLClassLoader) newClassLoader;
		String source = Arrays.toString(urlClassLoader.getURLs());
		Set<String> pluginClassNames = PluginScanner.scanClassPath(urlClassLoader);
		pluginContainers = new HashSet<>(pluginClassNames.size());
		for (String pluginClassName : pluginClassNames) {
            try {
				Class<?> pluginClass = newClassLoader.loadClass(pluginClassName);
				// Object plugin = injector.getInstance(pluginClass);
	            VanillaPluginContainer container = new VanillaPluginContainer(pluginClass);
	            registerPlugin(container);	
	            Object plugin = container.getInstance();
				eventManager.register(container, plugin);
				callStartedLifecycleEvents(container);
				pluginContainers.add(container);
	            logger.info("HOT Loaded plugin: {} {} (from {})", container.getName(), container.getVersion(), source);
	        } catch (Throwable e) {
	        	logger.error("HOT Failed to load plugin: {} (from {})", pluginClassName, source, e);
	        }
		}
	}

	// TODO Once Sponge allows extensible Guice configuration, put following methods into a VanillaPluginManager subclass (and correctly Guice register it)
	
	private void registerPlugin(VanillaPluginContainer container) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method method = pluginManager.getClass().getDeclaredMethod("registerPlugin", PluginContainer.class);
		method.setAccessible(true);
		method.invoke(pluginManager, container);
	}

	private void unregisterPlugin(PluginContainer plugin) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		removeFromPrivateMap(pluginManager, "plugins", plugin.getId());
		removeFromPrivateMap(pluginManager, "pluginInstances", plugin.getInstance());
	}

	private void removeFromPrivateMap(Object object, String fieldName, Object mapKey) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = object.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		@SuppressWarnings("rawtypes")
		Map map = (Map) field.get(pluginManager);
		map.remove(mapKey);
	}
	
	@Subscribe
	public void onServerStopping(ServerStoppingEvent event) {
		// Not needed, as normal notification flow will already do this, as the plugin is "normally" registered:
		// unloadPlugins();
		hcl.close();
	}

	private void unloadPlugins() {
		if (pluginContainers == null)
			return;
		for (PluginContainer container : pluginContainers) {
            unloadPlugin(container);
            callStopingLifecycleEvents(container);
            logger.info("HOT Unloaded plugin: {} {}", container.getName(), container.getVersion());
		}
		pluginContainers = null;
	}

	private boolean unloadPlugin(PluginContainer container) {
    	if (!pluginManager.isLoaded(container.getId())) {
            Sponge.getLogger().error("HOT Failed to unload plugin, as it wasn't loaded: {}", container);
            return false;
    	}
        try {
			unregisterPlugin(container);
			eventManager.unregister(container.getInstance());
			eventManager.unregisterPlugin(container);
			return true;
        } catch (Throwable e) {
        	logger.error("HOT Failed to load unplugin: {}", container.getId(), e);
        	return false;
        }
	}

	private void callStartedLifecycleEvents(PluginContainer plugin) {
		// TODO This cannot work like this, yet... ;-(
		// singlePluginEventManager.post(plugin, SpongeEventFactory.createState(ServerStartingEvent.class, game));
	}

	private void callStopingLifecycleEvents(PluginContainer plugin) {
		// singlePluginEventManager.post(plugin, SpongeEventFactory.createState(ServerStoppingEvent.class, game));
	}

}
