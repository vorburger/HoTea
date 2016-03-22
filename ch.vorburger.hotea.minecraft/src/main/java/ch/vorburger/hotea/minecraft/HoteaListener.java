package ch.vorburger.hotea.minecraft;

import java.net.URLClassLoader;

import ch.vorburger.hotea.HotClassLoader.Listener;
import ch.vorburger.hotea.minecraft.api.HotPluginManager;
import ch.vorburger.hotea.minecraft.api.HotPluginManager.HotPlugins;

public class HoteaListener implements Listener {

	private final HotPluginManager pluginManager;
	private HotPlugins hotPlugins = null;

	public HoteaListener(HotPluginManager hotPluginManager) {
		this.pluginManager = hotPluginManager;
	}

	@Override
	public void onReload(ClassLoader newClassLoader) throws Throwable {
		unload();
		hotPlugins = pluginManager.loadPlugins((URLClassLoader) newClassLoader);
	}

	void unload() {
		if (hotPlugins != null) {
			pluginManager.unloadPlugins(hotPlugins);
		}
	}
	
}
