package org.spoutcraft.launcher;

import net.technicpack.launchercore.launch.MinecraftExitListener;
import net.technicpack.launchercore.launch.MinecraftProcess;

class LauncherHider implements MinecraftExitListener {
	private final Launcher instance;

	public LauncherHider(Launcher instance) {
		this.instance = instance;
	}

	@Override
	public void onMinecraftExit(MinecraftProcess process) {
		Launcher.getFrame().setVisible(true);
	}
}