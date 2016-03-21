package ch.vorburger.hotea.minecraft;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginManager;

import com.google.inject.Inject;

import ch.vorburger.hotea.HotClassLoader;
import ch.vorburger.hotea.HotClassLoaderBuilder;
import ch.vorburger.hotea.minecraft.Configuration.HotPluginsLocation;
import ch.vorburger.hotea.minecraft.api.HotPluginManager;

/**
 * Sponge plugin which manages the HOT reloading of other plug-ins.
 *
 * See https://github.com/SpongePowered/SpongeVanilla/pull/178 for some history.
 *
 * @author Michael Vorburger
 */
@Plugin(id = "ch.vorburger.hotea", name = "HOT Reload Plug-In", description = "Loads and reloads other plugins on changes; useful for development. Currently requires patched Sponge (with support for HotPluginManager)", version = "3.0.0-SNAPSHOT", authors = "Michael Vorburger.ch")
public class HoteaPlugin {

	// TODO remove v0.0.1 hard-coding below, and instead use proper configuration file.  Hot reload changes to the config file.
	// TODO make a public Service API, higher level than HotPluginManager, using which other plugins can register gradle projets as new plugins
	
	private @Inject Logger logger;
	// private @Inject Game game;
	private HotPluginManager hotPluginManager;
	private Configuration configuration;
	private List<HotClassLoader> hotClassLoaders = new ArrayList<>();

	private void loadConfiguration() {
		configuration = new Configuration();
		configuration.hotPluginsLocations = new ArrayList<>(1);
		configuration.hotPluginsLocations.add(new HotPluginsLocation());
		configuration.hotPluginsLocations.get(0).classpathLocations = new ArrayList<>(4);
		configuration.hotPluginsLocations.get(0).classpathLocations.add("/home/vorburger/dev/Minecraft/ch.vorburger.hotea/ch.vorburger.hotea.minecraft.example/target/classes");
//		configuration.hotPluginsLocations.get(0).classpathLocations.add("/home/vorburger/.gradle/caches/modules-2/files-2.1/org.eclipse.xtend/org.eclipse.xtend.lib.macro/2.9.0/7f8080053283efec4e6af4eb92a7f496430696c4/org.eclipse.xtend.lib.macro-2.9.0.jar");
//		configuration.hotPluginsLocations.get(0).classpathLocations.add("/home/vorburger/.gradle/caches/modules-2/files-2.1/org.eclipse.xtend/org.eclipse.xtend.lib/2.9.0/8d462b90999f1820405f7a549d08917b95d663c6/org.eclipse.xtend.lib-2.9.0.jar");
//		configuration.hotPluginsLocations.get(0).classpathLocations.add("/home/vorburger/.gradle/caches/modules-2/files-2.1/org.eclipse.xtext/org.eclipse.xtext.xbase.lib/2.9.0/d58aae519ab9e235c45ea0e76aac38f6088de400/org.eclipse.xtext.xbase.lib-2.9.0.jar");
	}
	
	private void loadHotPlugins() {
		closeHotClassLoaders();
		for (HotPluginsLocation hotPluginsLocation : configuration.hotPluginsLocations) {
			ClassLoader parentClassLoader = HoteaPlugin.class.getClassLoader(); // or, but NOT org.spongepowered.api.plugin.Plugin, that's another one that leads to a java.lang.LinkageError: loader constraint violation: loader (instance of sun/misc/Launcher$AppClassLoader) previously initiated loading for a different type with name "org/slf4j/Logger"
			try {
				HotClassLoaderBuilder builder = new HotClassLoaderBuilder().setParentClassLoader(parentClassLoader);
				for (String classpathLocation : hotPluginsLocation.classpathLocations)
					builder.addClasspathEntry(classpathLocation);
				hotClassLoaders.add(builder.addListener(new HoteaListener(hotPluginManager)).build());
			} catch (Exception e) {
				logger.error("HotClassLoaderBuilder failed", e);
			}
		}
	}

	private void closeHotClassLoaders() {
		hotClassLoaders.forEach(hcl -> hcl.close());
		hotClassLoaders.clear();
	}

	@Listener
	public void onServerStarting(GameStartingServerEvent event) {
		//Optional<HotPluginManager> optHotPluginManager = Sponge.getServiceManager().provide(HotPluginManager.class);
		PluginManager spongePluginManager = Sponge.getPluginManager();
		if (!(spongePluginManager instanceof HotPluginManager)) {
			throw new IllegalStateException("This plugin only works on a Sponge server which can offer a HotPluginManager");
		}
		hotPluginManager = (HotPluginManager) spongePluginManager;

		loadConfiguration();
		loadHotPlugins();
	}

	@Listener
	public void onServerStopping(GameStoppingServerEvent event) {
		// NOTE: We only close the Hotea HCL, but do not stop the Sponge plugins, because Sponge will do that itself during a "real" shutdown
		closeHotClassLoaders();
	}

}
