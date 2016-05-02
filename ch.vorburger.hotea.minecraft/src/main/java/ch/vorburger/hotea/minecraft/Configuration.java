package ch.vorburger.hotea.minecraft;

import java.util.List;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable class Configuration {

	@Setting List<HotPluginsLocation> hotPluginsLocations;

	@ConfigSerializable static class HotPluginsLocation {
		@Setting String eclipseDotClasspathFileLocation;
		@Setting List<String> classpathLocations;
	}
}
