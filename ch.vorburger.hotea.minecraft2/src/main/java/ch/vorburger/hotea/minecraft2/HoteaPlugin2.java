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
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.SpongeEventFactoryUtils;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.server.plugin.VanillaPluginContainer;
import org.spongepowered.server.plugin.VanillaPluginManager;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

import ch.vorburger.hotea.HotClassLoader;
import ch.vorburger.hotea.HotClassLoaderBuilder;
import ch.vorburger.hotea.minecraft.api.PluginEvent;
import ch.vorburger.hotea.minecraft.api.PluginLoadedEvent;
import ch.vorburger.hotea.minecraft.api.PluginUnloadingEvent;

/**
 * HOT reloading other plug-ins.
 *
 * Not an ideal implementation, would be better to have this in the core platform, but see https://github.com/SpongePowered/SpongeVanilla/pull/178.
 *
 * @author Michael Vorburger
 */
@Plugin(id = "HoTea", name = "Java HOT Reload Plug-In", version = "1.0.0-SNAPSHOT")
public class HoteaPlugin2 implements ch.vorburger.hotea.HotClassLoader.Listener {

	private @Inject Logger logger;
	// protected @Inject Injector injector;
	protected @Inject Game game;
	private @Inject VanillaPluginManager pluginManager;
	private @Inject EventManager eventManager;

	private HotClassLoader hcl;
	private Set<PluginContainer> pluginContainers;

	// TODO remove v0.0.1 hard-coding below, and instead either add commands such as below, or (better?) configuration file, and public Service API:
	// 	 /plugin register <directory>
	//   /plugin unregister <name>

	@org.spongepowered.api.event.Listener
	public void onServerStarting(GameStartingServerEvent event) {
		File dir = new File("/home/vorburger/dev/Minecraft/SwissKnightMinecraft/SpongePowered/MyFirstSpongePlugIn/bin");
		try {
			ClassLoader parentClassLoader = Plugin.class.getClassLoader();
			hcl = new HotClassLoaderBuilder().setParentClassLoader(parentClassLoader)
					.addClasspathEntry(dir)
					.addClasspathEntry("/home/vorburger/.gradle/caches/modules-2/files-2.1/org.eclipse.xtend/org.eclipse.xtend.lib.macro/2.9.0/7f8080053283efec4e6af4eb92a7f496430696c4/org.eclipse.xtend.lib.macro-2.9.0.jar")
					.addClasspathEntry("/home/vorburger/.gradle/caches/modules-2/files-2.1/org.eclipse.xtend/org.eclipse.xtend.lib/2.9.0/8d462b90999f1820405f7a549d08917b95d663c6/org.eclipse.xtend.lib-2.9.0.jar")
					.addClasspathEntry("/home/vorburger/.gradle/caches/modules-2/files-2.1/org.eclipse.xtext/org.eclipse.xtext.xbase.lib/2.9.0/d58aae519ab9e235c45ea0e76aac38f6088de400/org.eclipse.xtext.xbase.lib-2.9.0.jar")
					.addListener(this).build();
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
				eventManager.registerListeners(container, plugin);
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

	@org.spongepowered.api.event.Listener
	public void onServerStopping(GameStoppingServerEvent event) {
		// Not needed, as normal notification flow will already do this, as the plugin is "normally" registered:
		// unloadPlugins();
		hcl.close();
	}

	private void unloadPlugins() {
		if (pluginContainers == null)
			return;
		for (PluginContainer container : pluginContainers) {
			callStopingLifecycleEvents(container);
			unloadPlugin(container);
			logger.info("HOT Unloaded plugin: {} {}", container.getName(), container.getVersion());
		}
		pluginContainers = null;
	}

	private boolean unloadPlugin(PluginContainer container) {
		if (!pluginManager.isLoaded(container.getId())) {
			logger.error("HOT Failed to unload plugin, as it wasn't loaded: {}", container);
			return false;
		}
		try {
			unregisterPlugin(container);
			eventManager.unregisterListeners(container.getInstance());
			eventManager.unregisterPluginListeners(container);
			return true;
		} catch (Throwable e) {
			logger.error("HOT Failed to load unplugin: {}", container.getId(), e);
			return false;
		}
	}

	private void callStartedLifecycleEvents(PluginContainer pluginContainer) {
		eventManager.post(createPluginEvent(PluginLoadedEvent.class, game, pluginContainer));
	}

	private void callStopingLifecycleEvents(PluginContainer pluginContainer) {
		eventManager.post(createPluginEvent(PluginUnloadingEvent.class, game, pluginContainer));
	}

	private <T extends PluginEvent> T createPluginEvent(Class<T> type, Game game, PluginContainer pluginContainer) {
		Map<String, Object> values = Maps.newHashMapWithExpectedSize(2);
		values.put("game", game);
		values.put("pluginContainer", pluginContainer);
		return SpongeEventFactoryUtils.createEventImpl(type, values);
	}
}
