package graph;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.graph.DefaultEdge;

public class Simplify {
	public static Graph simplifyGraph(Graph input, double epsilon) {
		Map<Vertex, Vertex> toSnappedVertex = new HashMap<Vertex, Vertex>();
		Graph graph = new Graph();

		// Bleh, stupid O(N^2) thing.
		for (Vertex v : input.vertexSet()) {
			Vertex snap = null;
			for (Vertex other : graph.vertexSet()) {
				if (v.getPoint().distance(other.getPoint()) < epsilon) {
					snap = other;
					break;
				}
			}
			if (snap == null) {
				snap = v.clone();
				graph.addVertex(snap);
			}
			toSnappedVertex.put(v, snap);
		}
		
		for (DefaultEdge e : input.edgeSet()) {
			Vertex source = toSnappedVertex.get(input.getEdgeSource(e));
			Vertex target = toSnappedVertex.get(input.getEdgeTarget(e));
			if (source != target)
				graph.addEdge(source, target);
		}
		
		return graph;
	}
}
