/*
 * This file is part of Technic Launcher.
 * Copyright (C) 2013 Syndicate, LLC
 *
 * Technic Launcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Technic Launcher is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Technic Launcher.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.spoutcraft.launcher;

import net.technicpack.launchercore.exception.RestfulAPIException;
import net.technicpack.launchercore.install.AddPack;
import net.technicpack.launchercore.install.InstalledPack;
import net.technicpack.launchercore.install.InstalledPacks;
import net.technicpack.launchercore.install.PackRefreshListener;
import net.technicpack.launchercore.install.ResourceInstaller;
import net.technicpack.launchercore.install.User;
import net.technicpack.launchercore.install.Users;
import net.technicpack.launchercore.restful.PackInfo;
import net.technicpack.launchercore.restful.RestObject;
import net.technicpack.launchercore.restful.platform.PlatformPackInfo;
import net.technicpack.launchercore.restful.solder.FullModpacks;
import net.technicpack.launchercore.restful.solder.Solder;
import net.technicpack.launchercore.restful.solder.SolderConstants;
import net.technicpack.launchercore.restful.solder.SolderPackInfo;
import net.technicpack.launchercore.util.Utils;
import org.spoutcraft.launcher.entrypoint.SpoutcraftLauncher;
import org.spoutcraft.launcher.skin.LauncherFrame;

import javax.swing.JOptionPane;
import java.util.logging.Level;

public class Launcher implements PackRefreshListener {
	private static Launcher instance;
	private final LauncherFrame launcherFrame;
	private Users users;
	private InstalledPacks installedPacks;
	private InstallThread installThread;
	private ResourceInstaller assetInstaller;

	public Launcher() {
		if (Launcher.instance != null) {
			throw new IllegalArgumentException("You can't have a duplicate launcher");
		}

		instance = this;

		users = Users.load();

		if (users == null) {
			users = new Users();
		}

		installedPacks = InstalledPacks.load();

		if (installedPacks == null) {
			installedPacks = new InstalledPacks();
		}

		this.launcherFrame = new LauncherFrame();
		this.assetInstaller = new ResourceInstaller();

		loadDefaultPacks();
		loadInstalledPacks();
		loadForcedPack();
		installedPacks.add(new AddPack());

		Thread assets = new Thread("Assets Thread") {
			@Override
			public void run() {
				instance.updateAssets();
			}
		};

		Thread faces = new Thread("Faces Thread") {
			@Override
			public void run() {
				launcherFrame.updateFaces();
			}
		};

		Thread news = new Thread("News Thread") {
			@Override
			public void run() {
				launcherFrame.getNews().loadArticles();
			}
		};

		faces.start();
		news.start();
		assets.start();

		JOptionPane.showMessageDialog(launcherFrame, "Warning! This is an early beta of a complete back-end rewrite.\n" +
				"Your mod installs may be corrupted or worse.\n" +
				"Use at your own risk.\n\n" +
				"Known issue: Yogbox and Hack/Mine DO NOT WORK.",
				"Warning! Beta Build of Launcher!", JOptionPane.WARNING_MESSAGE);
	}

	private void updateAssets() {
		assetInstaller.updateResources();
	}

	private void loadInstalledPacks() {
		for (final InstalledPack pack : installedPacks.getPacks()) {
			if (pack.isPlatform()) {
				Thread thread = new Thread(pack.getName() + " Info Loading Thread") {
					@Override
					public void run() {
						try {
							String name = pack.getName();
							PlatformPackInfo platformPackInfo = PlatformPackInfo.getPlatformPackInfo(name);
							PackInfo info = platformPackInfo;
							if (platformPackInfo.hasSolder()) {
								SolderPackInfo solderPackInfo = SolderPackInfo.getSolderPackInfo(platformPackInfo.getSolder(), name);
								Solder solder = RestObject.getRestObject(Solder.class, platformPackInfo.getSolder());
								solder.setUrl(platformPackInfo.getSolder());
								solderPackInfo.setSolder(solder);
								info = solderPackInfo;
							}

							info.getLogo();
							info.getIcon();
							info.getBackground();
							pack.setInfo(info);
							pack.setRefreshListener(instance);
							launcherFrame.getSelector().redraw(false);
						} catch (RestfulAPIException e) {
							Utils.getLogger().log(Level.WARNING, "Unable to load platform pack " + pack.getName(), e);
						}
					}
				};
				thread.start();
			}
		}
	}

	private void loadForcedPack() {
		if (SpoutcraftLauncher.params != null && SpoutcraftLauncher.params.getSolderPack() != null) {
			final String solder = SpoutcraftLauncher.params.getSolderPack();
			Thread thread = new Thread("Forced Solder Thread - " + solder) {

				@Override
				public void run() {
					try {
						SolderPackInfo info = SolderPackInfo.getSolderPackInfo(solder);
						if (info == null) {
							throw new RestfulAPIException();
						}

						info.getLogo();
						info.getIcon();
						info.getBackground();

						InstalledPacks packs = Launcher.getInstalledPacks();
						if (packs.getInstalledPacks().containsKey(info.getName())) {
							InstalledPack pack = installedPacks.get(info.getName());
							pack.setInfo(info);
						} else {
							InstalledPack pack = new InstalledPack(info.getName(), true);
							pack.setRefreshListener(instance);
							pack.setInfo(info);
							packs.add(pack);
						}
					} catch (RestfulAPIException e) {
						Utils.getLogger().log(Level.WARNING, "Unable to load forced solder pack " + solder, e);
					}
				}
			};
			thread.start();
		}
	}

	private void loadDefaultPacks() {

		Thread thread = new Thread("Technic Solder Defaults") {
			@Override
			public void run() {
				int index = 0;

				InstalledPacks packs = Launcher.getInstalledPacks();
				try {
					FullModpacks technic = RestObject.getRestObject(FullModpacks.class, SolderConstants.getFullSolderUrl(SolderConstants.TECHNIC));
					Solder solder = new Solder(SolderConstants.TECHNIC, technic.getMirrorUrl());
					for (SolderPackInfo info : technic.getModpacks().values()) {
						String name = info.getName();
						info.setSolder(solder);
						if (packs.getInstalledPacks().containsKey(name)) {
							InstalledPack pack = installedPacks.get(info.getName());
							pack.setInfo(info);
						} else {
							InstalledPack pack = new InstalledPack(name, false);
							pack.setRefreshListener(instance);
							pack.setInfo(info);
							packs.add(pack);
						}
						packs.reorder(index, name);
						index++;
					}
					launcherFrame.getSelector().redraw(false);
				} catch (RestfulAPIException e) {
					Utils.getLogger().log(Level.WARNING, "Unable to load technic modpacks", e);
				}
			}
		};
		thread.start();
	}

	public static InstalledPacks getInstalledPacks() {
		return instance.installedPacks;
	}

	public static Launcher getInstance() {
		return instance;
	}

	public static void launch(User user, InstalledPack pack, String build) {
		instance.installThread = new InstallThread(user, pack, build, new LauncherHider(instance));
		instance.installThread.start();
	}

	public static boolean isLaunching() {
		return instance.installThread != null && !instance.installThread.isFinished();
	}

	public static LauncherFrame getFrame() {
		return instance.launcherFrame;
	}

	public static Users getUsers() {
		return instance.users;
	}

	@Override
	public void refreshPack(InstalledPack pack) {
		launcherFrame.getSelector().redraw(true);
	}
}
