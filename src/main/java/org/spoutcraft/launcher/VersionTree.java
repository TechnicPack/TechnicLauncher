package org.spoutcraft.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * A tree that describes what versions of Minecraft can be patched between, to help
 * determine what intermediate patch steps need to be taken to patch between versions
 * for which there is no direct patch.
 *  
 * @author cheide
 */
public class VersionTree {
	
	/**
	 * A pairing of versions.  The first one is the version you start with, and the next one
	 * is the version you are trying to reach.
	 */
	public class VersionPair {
		public String ver_first;
		public String ver_next;
	}
	
	/**
	 * A node in the version tree.
	 */
	private class VersionNode {
		public String version;
		
		public VersionNode parent = null;
		public ArrayList<VersionNode> children = new ArrayList<VersionNode>();
		public boolean marked = false; // For the breadth-first search
	}
	
	/**
	 * Pairings of versions that can be patched, as read in from the external file.
	 */
	private ArrayList<VersionPair> file_pairs;
	
	/**
	 * Root node of the version tree.
	 */
	private VersionNode root = null;
	
	public VersionTree(String ver_file, String root_ver)
	{
		file_pairs = new ArrayList<VersionPair>();
		
		/**
		 * Read the version pairings from the file and set them aside.
		 */
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(ver_file));
			while(true)
			{
				String line = reader.readLine();
				if(line == null)
					break;
				if(line.startsWith("#"))
					continue;
				String[] vers = line.split(" ");
				if(vers.length == 2)
				{
					addFilePair(vers[0], vers[1]);
				}
			}
		}
		catch(Exception e)
		{
			
		}
		buildTree(root_ver);
		
	}
	
	private void addFilePair(String first_ver, String next_ver)
	{
		VersionPair vp = new VersionPair();
		vp.ver_first = first_ver;
		vp.ver_next = next_ver;
		file_pairs.add(vp);
	}
	
	/**
	 * Build the version tree from the pairings read in from the file.
	 * @param root_ver The version to put in the root of the tree.
	 */
	private void buildTree(String root_ver)
	{
		VersionNode vn = new VersionNode();
		vn.version = root_ver;
		root = vn;
		
		addChildNodes();
	}
	
	/**
	 * Add version pairs to the tree, starting at the root.
	 * @return Returns the number of nodes added to the tree.
	 */
	private int addChildNodes()
	{
		int added = visitNodeForAdd(root);
		return added;
	}
	
	/**
	 * If there are any pairings in the file data where the first version matches
	 * the current node version, make the paired versions children of this node.
	 * @param node The version being processed.
	 * @return Returns the number of nodes added to the tree so far.
	 */
	private int visitNodeForAdd(VersionNode node)
	{
		int added = 0;
		
		if(node.children.size() == 0)
		{
			// No children, so this is a leaf node we could add to
			for(VersionPair vp : file_pairs)
			{
				if(vp.ver_first.equals(node.version))
				{
					VersionNode vn = new VersionNode();
					vn.version = vp.ver_next;
					vn.parent = node;
					node.children.add(vn);
					added += 1;
				}
			}
		}
		
		for(VersionNode vn : node.children)
		{
			added += visitNodeForAdd(vn);
		}
		
		return added;
	}
	
	/**
	 * Find a path through the tree from the given version to the root.  For example, if the root version
	 * is 1.4 and you search for 1.1 and the path through the tree is 1.1 -> 1.2.3 -> 1.2.5 -> 1.3.2 -> 1.4,
	 * the result will be a list with four pairings: 1.4/1.3.2, 1.3.2/1.2.5, 1.2.5/1.2.3, and 1.2.3/1.1.
	 * The order is the route needed to get from the root to the node.
	 * 
	 * @param new_version The version for which we are trying to find a path.
	 * @return Returns a list of version pair strings which describe the path through the tree.
	 */
	public List<VersionPair> getVersionPath(String new_version)
	{
		List<VersionPair> l = new ArrayList<VersionPair>();
		
		VersionNode vn = findVersion(new_version);
		if(vn != null)
			addParentPath(l, vn);
		else
			l = null;
		
		return l;
	}
	
	/**
	 * Helper function for getVersionPath, recursing over the parent nodes so that we can
	 * add them to the list on the return path, to get them in the proper order and avoid
	 * having to reverse them later.
	 * 
	 * @param list The list of pairings built up so far.
	 * @param vn The version node currently being visited.
	 */
	private void addParentPath(List<VersionPair> list, VersionNode vn)
	{
		if(vn.parent != null)
		{
			addParentPath(list, vn.parent);
			
			VersionPair vp = new VersionPair();
			vp.ver_first = vn.parent.version;
			vp.ver_next = vn.version;
			list.add(vp);
		}
	}
	
	/**
	 * Setup function for the breadth-first search.
	 * @param vn All nodes below this one will be unmarked.
	 */
	private void unmarkTree(VersionNode vn)
	{
		vn.marked = false;
		for(VersionNode cvn : vn.children)
		{
			unmarkTree(cvn);
		}
	}
	
	/**
	 * Do a breadth-first search to find a path from the root version to the given version
	 * number, to minimize the number of patches that need to be applied.
	 * @param version The version number to look for.
	 * @return Returns the node, or null if none was found.
	 */
	private VersionNode findVersion(String version)
	{
		unmarkTree(root);
		Queue<VersionNode> q = new LinkedList<VersionNode>();
		q.add(root);
		root.marked = true;
		
		while(!(q.isEmpty()))
		{
			VersionNode vn = q.remove();
			if(vn.version.equals(version))
				return vn;
			for(VersionNode cvn : vn.children)
			{
				if(!cvn.marked)
				{
					cvn.marked = true;
					q.add(cvn);
				}
			}
		}
		
		return null;
	}

}
