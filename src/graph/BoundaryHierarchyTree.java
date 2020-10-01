package graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.vecmath.Point2d;

public class BoundaryHierarchyTree {
	static final double INFINITY = 1000000.0;
	
	private Node root;
	
	static public class Node
	{
		private Node parent = null;
		private ArrayList<Node> children = new ArrayList<Node>();
		private OrientedLoop loop = null;
		private boolean isDummy = false;

		public Node(OrientedLoop l) {
			loop = l;
		}
		
		public boolean isDummy() {
			return isDummy;
		}
		
		public Node getParent() {
			return parent;
		}
		
		public List<Node> getChildren() {
			return Collections.unmodifiableList(children);
		}
		
		public OrientedLoop getLoop() {
			return loop;
		}
		
		public String toString() {
			return toString("");
		}
		public String toString(String indent) {
			String ans = "";
			ans += indent + "Node[" + loop + ",\n";
			String nextIndent = "  " + indent;
			for (Node child : children) {
				ans += child.toString(nextIndent);
			}
			ans += indent + "]\n";
			return ans;
		}
		
		public void insert(OrientedLoop A)
		{
			// check if a child bounds A
			int i;
			for (i = 0; i < children.size(); i++)
			{
				Node child = children.get(i);
				if ( A.isBoundedBy(child.loop) )
				{
					child.insert(A);
					return;
				}
				else if ( child.loop.isBoundedBy(A) )
				{
					break;
				}
			}
			
			
			//no child bounds A, but A might bound some children
			Node newNode = new Node(A);
			for (; i < children.size(); i++)
			{
				Node child = children.get(i);
				if ( child.loop.isBoundedBy(A) )
				{
					child.parent = newNode;
					newNode.children.add(child);
					this.children.remove(i);
					// reverse the increment i
					i--;
				}
			}

			// clear all of this's current children
			this.children.add(newNode);
			newNode.parent = this;
		}
		
	}
	
	public BoundaryHierarchyTree() {
		// Make a really big one at infinity
		root = new Node(new OrientedLoop(Arrays.asList(
				new Vertex(new Point2d(-INFINITY, -INFINITY)),
				new Vertex(new Point2d( INFINITY, -INFINITY)),
				new Vertex(new Point2d( INFINITY,  INFINITY)),
				new Vertex(new Point2d(-INFINITY,  INFINITY))),
				true));
		root.isDummy = true;
	}
	
	public Node getRoot() {
		return root;
	}
	
	public String toString() {
		if (root != null) {
			return root.toString();
		} else {
			return "<null tree>";
		}
	}
	
	public void insert(OrientedLoop A)
	{
		root.insert(A);
	}
	
}
