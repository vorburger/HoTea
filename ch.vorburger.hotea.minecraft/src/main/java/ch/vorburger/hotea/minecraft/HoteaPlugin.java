package ch.vorburger.hotea.minecraft;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginManager;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;

import ch.vorburger.hotea.HotClassLoader;
import ch.vorburger.hotea.HotClassLoaderBuilder;
import ch.vorburger.hotea.minecraft.Configuration.HotPluginsLocation;
import ch.vorburger.hotea.minecraft.api.HotPluginManager;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.DefaultObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

/**
 * Sponge plugin which manages the HOT reloading of other plug-ins.
 *
 * See https://github.com/SpongePowered/SpongeVanilla/pull/178 for some history.
 *
 * @author Michael Vorburger
 */
@Plugin(id = "ch.vorburger.hotea", name = "HOT Reload Plug-In", description = "Loads and reloads other plugins on changes; useful for development. Currently requires patched Sponge (with support for HotPluginManager)", version = "3.0.0-SNAPSHOT", authors = "Michael Vorburger.ch")
public class HoteaPlugin {

	// TODO Hot reload changes to the config file!
	// TODO make a public Service API, higher level than HotPluginManager, using which other plugins can register gradle projets as new plugins
	
	private @Inject Logger logger;
	private @Inject @DefaultConfig(sharedRoot = true) ConfigurationLoader<CommentedConfigurationNode> configManager;
	private @Inject DefaultObjectMapperFactory objectMapperFactory;

	private Configuration configuration;
	private HotPluginManager hotPluginManager;
	private List<HotClassLoader> hotClassLoaders = new ArrayList<>();

	private void loadConfiguration() {
		try {
			CommentedConfigurationNode node = configManager.load(ConfigurationOptions.defaults().setObjectMapperFactory(objectMapperFactory));
			configuration = node.getValue(TypeToken.of(Configuration.class));
			if (configuration == null) {
				configuration = new Configuration();
				configuration.hotPluginsLocations = new ArrayList<>(1);
				configuration.hotPluginsLocations.add(new HotPluginsLocation());
				configuration.hotPluginsLocations.get(0).classpathLocations = new ArrayList<>(1);
				objectMapperFactory.getMapper(Configuration.class).bind(configuration).serialize(node);
				configManager.save(node);
			}
		} catch (IOException | ObjectMappingException e) {
			throw new IllegalArgumentException("Could not load configuration" , e);
		}
	}
	
	private void loadHotPlugins() {
		closeHotClassLoaders();
		for (HotPluginsLocation hotPluginsLocation : configuration.hotPluginsLocations) {
			if (hotPluginsLocation.classpathLocations.isEmpty())
				continue;
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
