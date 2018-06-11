package eu.arrowhead.SystemRegistry;

import eu.arrowhead.common.ArrowheadMain;
import eu.arrowhead.common.misc.CoreSystem;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SystemRegistryMain extends ArrowheadMain {
	private SystemRegistryMain(String[] args) {
		Set<Class<?>> classes = new HashSet<>(Collections.singleton(SystemRegistryResource.class));
		//String[] packages = { "eu.arrowhead.common" };
		String[] packages = { "eu.arrowhead.common", "eu.arrowhead.SystemRegistry.filter" };
		init(CoreSystem.SYSTEM_REGISTRY, args, classes, packages);

		listenForInput();
	}

	public static void main(String[] args) {
		new SystemRegistryMain(args);
	}

}
