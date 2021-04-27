package de.ofterdinger.e4.keytool.internal.ui.util;

import org.eclipse.core.runtime.PlatformObject;

public class TreeChainObject extends PlatformObject {
	private TreeChainObject child;
	private String name;
	private TreeChainObject parent;

	public TreeChainObject(String name) {
		this.name = name;
	}

	public TreeChainObject getChild() {
		return this.child;
	}

	public String getName() {
		return this.name;
	}

	public TreeChainObject getParent() {
		return this.parent;
	}

	public boolean hasChild() {
		return (this.child != null);
	}

	public boolean hasParent() {
		return (this.parent != null);
	}

	public void setChild(TreeChainObject child) {
		this.child = child;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParent(TreeChainObject parent) {
		this.parent = parent;
	}

	@Override
	public String toString() {
		return this.name;
	}
}
