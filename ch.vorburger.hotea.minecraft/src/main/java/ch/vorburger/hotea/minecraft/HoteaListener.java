package ch.vorburger.hotea.minecraft;

import java.net.URLClassLoader;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;

import ch.vorburger.hotea.HotClassLoader.Listener;
import ch.vorburger.hotea.minecraft.api.HotPluginManager;
import ch.vorburger.hotea.minecraft.api.HotPluginManager.HotPlugins;

public class HoteaListener implements Listener {

	private final HoteaPlugin hoteaPlugin;
	private final HotPluginManager pluginManager;
	private HotPlugins hotPlugins = null;

	public HoteaListener(HotPluginManager hotPluginManager, HoteaPlugin hoteaPlugin) {
		this.pluginManager = hotPluginManager;
		this.hoteaPlugin = hoteaPlugin;
	}

	@Override
	public void onReload(ClassLoader newClassLoader) throws Throwable {
		runOnMainServerThread(() -> {
			unload();
			hotPlugins = pluginManager.loadPlugins((URLClassLoader) newClassLoader);
		});
	}

	void unloadOnMainServerThread() {
		runOnMainServerThread(() -> {
			unload();
		});

	}
	private void unload() {
		if (hotPlugins != null) {
			pluginManager.unloadPlugins(hotPlugins);
		}
	}
	
	private void runOnMainServerThread(Runnable run) {
		Scheduler scheduler = Sponge.getScheduler();
		Task.Builder taskBuilder = scheduler.createTaskBuilder();
		taskBuilder.execute(run).name("HOT Plugin reload/unload").submit(hoteaPlugin);
	}
}
