package graph;

import graph.ui.GraphEditor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

import javax.vecmath.Point2d;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableUndirectedGraph;
import org.jgrapht.graph.SimpleGraph;

import utils.DRectangle;
import utils.Line;
import utils.Pair;
import utils.Triple;

/**
 * A convenience superclass because constructing these things is a bit of a pain.
 *
 */
public class Graph extends ListenableUndirectedGraph<Vertex, DefaultEdge> {

	private static final long serialVersionUID = 4301862979315775815L;

	public Graph() {
		super(new SimpleGraph<Vertex, DefaultEdge>(DefaultEdge.class));
	}
	
	public static Pair<Graph, DRectangle> loadFrom(Reader input) throws IOException {
		Scanner scanner = new Scanner(input);
		Graph graph = new Graph();
		List<Vertex> vertices = new ArrayList<Vertex>();
		try {
			DRectangle rect = new DRectangle(scanner.nextDouble(), scanner
					.nextDouble(), scanner.nextDouble(), scanner.nextDouble());
			int numVerts = scanner.nextInt();
			for (int i = 0; i < numVerts; i++) {
				double x = scanner.nextDouble();
				double y = scanner.nextDouble();
				Vertex v = new Vertex(x, y);
				vertices.add(v);
				graph.addVertex(v);
			}
			int numEdges = scanner.nextInt();
			for (int i = 0; i < numEdges; i++) {
				int a = scanner.nextInt();
				int b = scanner.nextInt();
				graph.addEdge(vertices.get(a-1), vertices.get(b-1));
			}
			return new Pair<Graph, DRectangle>(graph, rect);
		} catch (InputMismatchException e) {
			throw new IOException("Invalid file.");
		} catch (NoSuchElementException e) {
			throw new IOException("Invalid file.");
		}  catch (IndexOutOfBoundsException e) {
			throw new IOException("Invalid file.");
		}
	}
	
	public void saveTo(Writer output, DRectangle bounds) throws IOException {
		PrintWriter writer = new PrintWriter(output);
		writer.print(bounds.x);
		writer.print(" ");
		writer.print(bounds.y);
		writer.print(" ");
		writer.print(bounds.width);
		writer.print(" ");
		writer.print(bounds.height);
		writer.print("\n");
		writer.println(vertexSet().size());
		Map<Vertex, Integer> vMap = new HashMap<Vertex, Integer>();
		int i = 1;
		for (Vertex v : vertexSet()) {
			vMap.put(v, i);
			writer.print(v.getPoint().x);
			writer.print(" ");
			writer.print(v.getPoint().y);
			writer.print("\n");
			i++;
		}
		writer.println(edgeSet().size());
		for (DefaultEdge e : edgeSet()) {
			writer.print(vMap.get(getEdgeSource(e)));
			writer.print(" ");
			writer.print(vMap.get(getEdgeTarget(e)));
			writer.print("\n");
		}
		writer.close();
	}
	
	public Line lineForEdge(DefaultEdge edge) {
		return new Line(getEdgeSource(edge).getPoint(), getEdgeTarget(edge).getPoint());
	}
	
	public Vertex getOtherVertex(DefaultEdge edge, Vertex v) {
		if (getEdgeSource(edge) == v) {
			return getEdgeTarget(edge);
		} else {
			return getEdgeSource(edge);
		}
	}
	
	// Makes a deep copy of the graph; the new graph references none of the
	// same objects as the original. This is, in particular, useful for threads.
	// Really, we probably want some functional data structure that can store
	// updates to the graph, but whatever.
	public Graph deepCopy() {
		Graph graph = new Graph();
		Map<Vertex, Vertex> mapping = new HashMap<Vertex, Vertex>();
		for (Vertex v : vertexSet()) {
			Vertex vClone = new Vertex(v.getPoint());
			graph.addVertex(vClone);
			mapping.put(v, vClone);
		}
		for (DefaultEdge e : edgeSet()) {
			graph.addEdge(mapping.get(getEdgeSource(e)), mapping.get(getEdgeTarget(e)));
		}
		return graph;
	}
	
	public void removeIntersections(double distSqrEpsilon) {
		// begin: merge vertices that are close to each other
		HashSet<Vertex> vertexAgenda = new HashSet<Vertex>();
		for (Vertex v : vertexSet())
		{
			vertexAgenda.add(v);
		}

		while ( !vertexAgenda.isEmpty() )
		{
			Vertex next = vertexAgenda.iterator().next();
			for (Vertex v : this.vertexSet())
			{
				// if two DIFFERENT vertices are close to each other
				if ( !next.equals(v)
				&& next.getPoint().distanceSquared(v.getPoint()) <= distSqrEpsilon )
				{
					// merge together the two vertices: "next" and "v"
					for (DefaultEdge e: this.edgesOf(v))
					{
						this.addEdge(next,this.getOtherVertex(e,v));
					}
					// remove the merged vertex and edges that were incident on v
					this.removeVertex(v);
					vertexAgenda.remove(v);
				}
			}
			vertexAgenda.remove(next);
		}
		// end: merge vertices that are close to each other
		
		HashSet<DefaultEdge> agenda = new HashSet<DefaultEdge>();
		for (DefaultEdge e : edgeSet())
		{
			// copy the edges into an agenda
			agenda.add(e);
		}

		while ( !agenda.isEmpty() )
		{
			DefaultEdge next = agenda.iterator().next();
			
			Vertex v0 = this.getEdgeSource(next);
			Vertex v1 = this.getEdgeTarget(next);
			Line line1 = lineForEdge(next);

			ArrayList<Triple<Double,Point2d,DefaultEdge>> intersections
				= new ArrayList<Triple<Double,Point2d,DefaultEdge>>();

			// find intersections with other edges
			for (DefaultEdge e : agenda)
			{
				Vertex v2 = this.getEdgeSource(e);
				Vertex v3 = this.getEdgeTarget(e);

				if ( v0.equals(v2) || v0.equals(v3) || v1.equals(v2) || v1.equals(v3) )
					continue;

				Line line2 = lineForEdge(e);
				Point2d intersection = line1.intersects(line2, true);
				if ( intersection != null )
				{
					// if there was an intersection
					// and that intersection was not close to the edge target of "next"

					double distSqrToIntersection = v0.getPoint().distanceSquared(intersection);
					Triple<Double,Point2d,DefaultEdge> triple =
						new Triple<Double,Point2d,DefaultEdge>(distSqrToIntersection, intersection, e);
					intersections.add(triple);
				}
			}

			Collections.sort(intersections,
				new Comparator<Triple<Double,Point2d,DefaultEdge>>()
				{
					@Override
					public int compare(
							Triple<Double, Point2d, DefaultEdge> arg0,
							Triple<Double, Point2d, DefaultEdge> arg1) {
						if ( arg0.first() == arg1.first() )
							return 0;
						return arg0.first() < arg1.first() ? -1 : 1;
					}
				});

			int numIntersections = intersections.size();

			//if there are any intersections
			if ( numIntersections > 0 )
			{
				double distSqrToPrevIntersection = 0;
				Vertex prevIntersectionVertex = v0;
				int i;
				for (i = 0; i < numIntersections; i++)
				{
					double distSqrToIntersection = intersections.get(i).first();
					Point2d intersection = intersections.get(i).second();
					DefaultEdge intersectedEdge = intersections.get(i).third();
					System.out.println(intersection.distanceSquared(v1.getPoint()));
					if ( intersection.distanceSquared(v1.getPoint()) <= distSqrEpsilon )
					{
						// if this intersection is close to the edge target of "next"
						// then so are the rest of the intersections
						// if the rest of the intersections are close to the edge target of "next"
						// then just say that they intersect at the edge target
						// this is done in the for loop that follows
						System.out.println("too close");
						break;
					}
					if ( intersection.distanceSquared(this.getEdgeSource(intersectedEdge).getPoint())
						<= distSqrEpsilon )
					{
						// if the intersection is too close to the edge source of the intersected edge
						// then just say that the intersection was at the source
						DefaultEdge newEdge0 =
							this.addEdge(prevIntersectionVertex,this.getEdgeSource(intersectedEdge));

						if (newEdge0 != null) agenda.add(newEdge0);

						prevIntersectionVertex = this.getEdgeSource(intersectedEdge);
						distSqrToPrevIntersection = distSqrToIntersection;
						continue;
					}
					else if ( intersection.distanceSquared(this.getEdgeTarget(intersectedEdge).getPoint())
						<= distSqrEpsilon )
					{
						// if the intersection is too close to the edge target of the intersected edge
						// then just say that the intersection was at the source
						DefaultEdge newEdge0 =
							this.addEdge(prevIntersectionVertex,this.getEdgeTarget(intersectedEdge));

						if (newEdge0 != null) agenda.add(newEdge0);

						prevIntersectionVertex = this.getEdgeSource(intersectedEdge);
						distSqrToPrevIntersection = distSqrToIntersection;
						continue;
					}
					else if ( distSqrToIntersection > distSqrToPrevIntersection+distSqrEpsilon )
					{
						// if the intersection is not close enough to previous intersection
						// then the ith intersection is considered to be a different vertex
						Vertex intersectionVertex = new Vertex(intersection.x,intersection.y);
						this.addVertex(intersectionVertex);

						DefaultEdge newEdge0 = this.addEdge(prevIntersectionVertex,intersectionVertex);
						DefaultEdge newEdge1 =
							this.addEdge(this.getEdgeSource(intersectedEdge),intersectionVertex);
						DefaultEdge newEdge2 =
							this.addEdge(intersectionVertex,this.getEdgeTarget(intersectedEdge));
						
						if (newEdge0 != null) agenda.add(newEdge0);
						if (newEdge1 != null) agenda.add(newEdge1);
						if (newEdge2 != null) agenda.add(newEdge2);

						prevIntersectionVertex = intersectionVertex;
						distSqrToPrevIntersection = distSqrToIntersection;
					}
					else
					{
						// else the intersection is close enough to previous intersection
						// for both to be considered the same
						DefaultEdge newEdge0 =
							this.addEdge(
								this.getEdgeSource(intersectedEdge),prevIntersectionVertex);
						DefaultEdge newEdge1 =
							this.addEdge(
								prevIntersectionVertex,this.getEdgeTarget(intersectedEdge));

						if (newEdge0 != null) agenda.add(newEdge0);
						if (newEdge1 != null) agenda.add(newEdge1);
					}
					
					//remove the intersected edge because it is now replaced by two other edges
					agenda.remove(intersectedEdge);
					this.removeEdge(intersectedEdge);
				} // for (int i = 0; i < numIntersections; i++)

				for (; i < numIntersections; i++)
				{
					// if this intersection is close to the edge target of "next"
					// then so are the rest of the intersections
					// if the rest of the intersections are close to the edge target of "next"
					// then just say that they intersect at the edge target
					DefaultEdge newEdge0 = this.addEdge(this.getEdgeSource(intersections.get(i).third()),v1);
					DefaultEdge newEdge1 = this.addEdge(v1,this.getEdgeTarget(intersections.get(i).third()));

					if (newEdge0 != null) agenda.add(newEdge0);
					if (newEdge1 != null) agenda.add(newEdge1);
					//remove the intersected edge because it is now replaced by two other edges
					agenda.remove(intersections.get(i).third());
					this.removeEdge(intersections.get(i).third());
				}
				
				// if there was an intersection with "next" that was not at its edge source
				if ( prevIntersectionVertex != v0 )
				{
					// now create the last edge
					DefaultEdge newEdge0 = this.addEdge(prevIntersectionVertex,v1);
					if (newEdge0 != null) agenda.add(newEdge0);
					// remove the original edge
					this.removeEdge(next);
				}
			} // if ( numIntersections > 0 )
			
			// remove the edge for which we just found all the intersections
			agenda.remove(next);
		} // while ( !agenda.isEmpty() )
		
		
		// remove degree 0 vertices that are too close to an edge
		// that is, the vertex is basically on the edge, in which case
		// it's as if it wasn't even there
		vertexAgenda.clear();
		for (Vertex v : vertexSet())
		{
			vertexAgenda.add(v);
		}
		while ( !vertexAgenda.isEmpty() )
		{
			Vertex next = vertexAgenda.iterator().next();
			if ( this.degreeOf(next) == 0 )
			{
				DefaultEdge e =
					GraphEditor.getNearestEdge(next.getPoint(), distSqrEpsilon, this);
				if ( e != null )
				{
					assert ( false );
					this.removeVertex(next);
				}
			}
			vertexAgenda.remove(next);
		}

	}

	
	static class PairOfVertices extends Pair<Vertex,Vertex>
	{
		public PairOfVertices(Vertex v0, Vertex v1) {
			super(v0,v1);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object o)
		{
			return o instanceof Pair<?,?>
			&& this.first() == ((Pair<Vertex,Vertex>)o).first()
			&& this.second() == ((Pair<Vertex,Vertex>)o).second();
		}
		
		@Override
		public int hashCode()
		{
			int result = 37*17 + this.first().hashCode();
			return 37*result + this.second().hashCode();
		}
	}
	
	// only works for a graph with no intersecting edges
	public BoundaryHierarchyTree computeBoundaryHierarchyTree() {
		Set<PairOfVertices> pairs = new HashSet<PairOfVertices>();
		Map<PairOfVertices, OrientedLoop.EdgeContext> assignedEdges =
			new HashMap<PairOfVertices, OrientedLoop.EdgeContext>();

		for (DefaultEdge e : this.edgeSet())
		{
			pairs.add(new PairOfVertices(this.getEdgeSource(e),this.getEdgeTarget(e)));
			pairs.add(new PairOfVertices(this.getEdgeTarget(e),this.getEdgeSource(e)));
		} // for (DefaultEdge e : this.edgeSet())

		BoundaryHierarchyTree hierarchyTree = new BoundaryHierarchyTree();

		while ( !pairs.isEmpty() )
		{
			List<Vertex> polygon = new ArrayList<Vertex>();
			
			PairOfVertices nextPair =  pairs.iterator().next();
			PairOfVertices firstPair = nextPair;
			double sumTurnAngles = 0;

			do {
				polygon.add(nextPair.first());
				// remove the pair that we just pulled out
				boolean removed = pairs.remove(nextPair);
				assert removed;

				PairOfVertices pair = calculateNextLeftmostPair(nextPair);
				sumTurnAngles += Graph.calculateAngle(nextPair, pair);
				nextPair = pair;
			} while (! nextPair.equals(firstPair) );
			// If sumTurnAngles > 0, then counter-clockwise.
			OrientedLoop loop = new OrientedLoop(polygon, sumTurnAngles > 0);
			hierarchyTree.insert(loop);
			// Keep track of the edges inserted so far, and check if they need matching.
			for (OrientedLoop.EdgeContext edgeContext : loop.getEdgeContexts()) {
				assignedEdges.put(new PairOfVertices(edgeContext.getSource(), edgeContext.getTarget()), edgeContext);
				OrientedLoop.EdgeContext other =
					assignedEdges.get(new PairOfVertices(edgeContext.getTarget(), edgeContext.getSource()));
				if (other != null) {
					edgeContext.connect(other);
				}
			}
		} // while ( !pairs.isEmpty() )
		
		// Also put in the degree-zero vertices.
		for (Vertex v : vertexSet()) {
			if (degreeOf(v) == 0) {
				hierarchyTree.insert(new OrientedLoop(Arrays.asList(v), false));
			}
		}

		return hierarchyTree;
	}
	
	public PairOfVertices calculateNextLeftmostPair(PairOfVertices p)
	{
		double leftmostAngle = -4; // anything less than -Math.PI will work
		PairOfVertices leftmostPair = null;

		for (DefaultEdge e: this.edgesOf(p.second()))
		{
			PairOfVertices p2 =
				new PairOfVertices(p.second(),this.getOtherVertex(e,p.second()));
			double angle = calculateAngle(p,p2);
			if ( angle > leftmostAngle )
			{
				leftmostPair = p2;
				leftmostAngle = angle;
			}
		}

		return leftmostPair;
	}
	
	static double calculateAngle(Pair<Vertex,Vertex> p1, Pair<Vertex,Vertex> p2)
	{
		assert p1.second() == p2.first(); 
		// return negative PI if nextPair is the reverse of pair
		if ( p1.first() == p2.second() )
			return -Math.PI;
		double arcTanDiff =
			Math.atan2(
				p2.second().getPoint().y - p2.first().getPoint().y,
				p2.second().getPoint().x - p2.first().getPoint().x)
			- Math.atan2(
				p1.second().getPoint().y - p1.first().getPoint().y,
				p1.second().getPoint().x - p1.first().getPoint().x);
		
		if ( arcTanDiff >= Math.PI )
			return arcTanDiff - 2*Math.PI;
		if ( arcTanDiff < -Math.PI )
			return arcTanDiff + 2*Math.PI;
		return arcTanDiff;
	}
}
