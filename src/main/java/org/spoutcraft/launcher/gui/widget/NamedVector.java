package org.spoutcraft.launcher.gui.widget;

import java.util.Vector;

@SuppressWarnings({"rawtypes", "serial"})
class NamedVector extends Vector {
	private String name;

	public NamedVector(String name) {
		this.name = name;
	}

        @SuppressWarnings("unchecked")
	public NamedVector(String name, Object elements[]) {
		this.name = name;
		for (int i = 0, n = elements.length; i < n; i++) {
			add(elements[i]);
		}
	}

	public String toString() {
		return "[" + name + "]";
	}
}