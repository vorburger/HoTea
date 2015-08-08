package ch.vorburger.hotea.minecraft;

import java.io.File;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.state.ServerStartingEvent;
import org.spongepowered.api.event.state.ServerStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.plugin.PluginManager;

import com.google.inject.Inject;

import ch.vorburger.hotea.HotClassLoader;
import ch.vorburger.hotea.HotClassLoader.Listener;
import ch.vorburger.hotea.HotClassLoaderBuilder;

/**
 * See https://github.com/SpongePowered/SpongeVanilla/pull/178. 
 */
@Plugin(id = "HoTea1", name = "Java HOT Reload Plug-In", version = "1.0.0-SNAPSHOT")
public class HoteaPlugin implements Listener {

	protected @Inject Logger logger;
	protected @Inject PluginManager pluginManager;
	
	private HotClassLoader hcl;
	private Set<PluginContainer> plugins = Collections.emptySet();

	// TODO remove v0.0.1 hard-coding below, and instead add commands such as:
	// 	 /plugin register <directory>
	//   /plugin unregister <name>
	
	@Subscribe
	public void onServerStarting(ServerStartingEvent event) {
		File dir = new File("/home/vorburger/dev/Minecraft/Sponge/workspace/HotSpongePlugin/target/classes");
		try {
			// TODO Doc
			ClassLoader parentClassLoader = Plugin.class.getClassLoader();
			hcl = new HotClassLoaderBuilder().setParentClassLoader(parentClassLoader).addClasspathEntry(dir).addListener(this).build();
		} catch (Exception e) {
			logger.error("HotClassLoaderBuilder failed", e);
		}
	}

	@Override
	public void onReload(ClassLoader newClassLoader) throws Throwable {
		unloadPlugins();
		plugins = pluginManager.loadPlugins((URLClassLoader) newClassLoader);
	}

	protected void unloadPlugins() {
		for (PluginContainer plugin : plugins) {
			pluginManager.unloadPlugin(plugin);
		}
	}

	@Subscribe
	public void onServerStopping(ServerStoppingEvent event) {
		unloadPlugins();
		hcl.close();
	}

}
