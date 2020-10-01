package foldandcut;

import graph.BoundaryHierarchyTree;
import graph.OrientedLoop;
import graph.Vertex;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;

import straightskeleton.Corner;
import straightskeleton.Edge;
import straightskeleton.Machine;
import straightskeleton.Skeleton;
import utils.LoopL;

/**
 * Represents an input to the straight skeleton problem. One polygon with some
 * number of holes. The holes do not intersect, and neither does the boundary.
 */
public class SkeletonInput {
	private LoopL<Edge> edges;
	private Map<Corner, Vertex> perturbationMap;
	private BoundaryHierarchyTree.Node bhtNode;
	// The straight-skeleton copies things. Floating-point equality comparison
	// is Bad, but I think unavoidable here.
	private Map<Tuple3d, Corner> cornerMap;
	// Oh, whatever.
	private static class VP {
		Vertex a;
		Vertex b;
		public VP(Vertex a, Vertex b) { this.a = a; this.b = b; }
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((a == null) ? 0 : a.hashCode());
			result = prime * result + ((b == null) ? 0 : b.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			VP other = (VP) obj;
			if (a == null) {
				if (other.a != null)
					return false;
			} else if (!a.equals(other.a))
				return false;
			if (b == null) {
				if (other.b != null)
					return false;
			} else if (!b.equals(other.b))
				return false;
			return true;
		}
	}
	private Map<VP, OrientedLoop.EdgeContext> edgeMap;
	
	public FoldAndCutProblem parent; // Bah.
	SkeletonOutput output; // BAAAH
	Map<OrientedLoop.EdgeContext, Edge> edgeContextToEdge;
	
	/**
	 * Returns the node as a straight skeleton shape; the current boundary
	 * with children removed as holes. Epsilon is the amount to perturb the
	 * shape by.
	 */
	public SkeletonInput(BoundaryHierarchyTree.Node node, double epsilon) {
		edgeMap = new HashMap<VP, OrientedLoop.EdgeContext>();
		
		assert node.getLoop().isInside();
		LoopL<Edge> face = new LoopL<Edge>();
        // controls the gradient of the edge
        Machine machine = new Machine(Math.PI/4);
        perturbationMap = new HashMap<Corner, Vertex>();
        edgeContextToEdge = new HashMap<OrientedLoop.EdgeContext, Edge>();

        // Add the boundary itself.
        face.add(node.getLoop().asStraightSkeletonLoop(machine, epsilon, perturbationMap, edgeContextToEdge));
        rememberLoopEdges(node.getLoop());
        
        // And then each of the holes.
        for (BoundaryHierarchyTree.Node child : node.getChildren()) {
        	assert !child.getLoop().isInside();
        	if (child.getLoop().isInside()) {
        		// Should not happen!
        		continue;
        	}
        	face.add(child.getLoop().asStraightSkeletonLoop(machine, epsilon, perturbationMap, edgeContextToEdge));
        	rememberLoopEdges(child.getLoop());
        }
        
		this.edges = face;
		this.bhtNode = node;
		this.cornerMap = new HashMap<Tuple3d, Corner>();
		for (Corner c : this.perturbationMap.keySet()) {
			this.cornerMap.put(new Point3d(c), c);
		}
	}
	
	private void rememberLoopEdges(OrientedLoop loop) {
		// This function is such a nasty hack.
		for (int i = 0; i < loop.getVertices().size(); i++) {
			int j = i+1;
			if (j == loop.getVertices().size())
				j = 0;
			edgeMap.put(new VP(loop.getVertices().get(i), loop.getVertices().get(j)), loop.getEdgeContexts().get(i));
		}
	}
	
	public OrientedLoop.EdgeContext getEdgeContextFor(Vertex a, Vertex b) {
		return edgeMap.get(new VP(a, b));
	}

	public BoundaryHierarchyTree.Node getBHTNode() {
		return bhtNode;
	}
	
	public LoopL<Edge> getEdges() {
		return edges;
	}
	
	public Map<Corner, Vertex> getPerturbationMap() {
		return Collections.unmodifiableMap(perturbationMap);
	}
	
	public Map<Tuple3d, Corner> getCornerMap() {
		return Collections.unmodifiableMap(cornerMap);
	}
	
	public Vertex getVertex(Tuple3d point) {
		Corner c = getCornerMap().get(point);
		if (c != null) {
			return getPerturbationMap().get(c);
		}
		return null;
	}
	
	public Point2d unperturb(Tuple3d point) {
		Corner c = getCornerMap().get(point);
		if (c != null) {
			// If it's a corner, perturb it first.
			return getPerturbationMap().get(c).getPoint();
		}
		return new Point2d(point.x, point.y);
	}
	
	public SkeletonOutput computeStraightSkeleton() {
		Skeleton skeleton = new Skeleton(getEdges(), true);
		skeleton.skeleton();
		output = new SkeletonOutput(this, skeleton.output);
		return output;
	}
}
