package eu.arrowhead.DeviceRegistry;

import eu.arrowhead.common.ArrowheadMain;
import eu.arrowhead.common.misc.CoreSystem;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DeviceRegistryMain extends ArrowheadMain {
	private DeviceRegistryMain(String[] args) {
		Set<Class<?>> classes = new HashSet<>(Collections.singleton(DeviceRegistryResource.class));
		//String[] packages = { "eu.arrowhead.common" };
		String[] packages = { "eu.arrowhead.common", "eu.arrowhead.DeviceRegistry.filter" };
		init(CoreSystem.DEVICE_REGISTRY, args, classes, packages);

		listenForInput();
	}

	public static void main(String[] args) {
		new DeviceRegistryMain(args);
	}
}
