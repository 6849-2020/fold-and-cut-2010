package graph;

import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import straightskeleton.Corner;
import straightskeleton.Edge;
import straightskeleton.Machine;
import utils.ConsecutivePairs;
import utils.ConsecutiveTriples;
import utils.Loop;
import utils.Loopable;
import utils.Pair;
import utils.Triple;

public final class OrientedLoop {
	private boolean inside; // true if the loop is counter clockwise
	private List<Vertex> vertices;
	private List<EdgeContext> edgeContexts;
	
	public final class EdgeContext {
		private int index;
		private EdgeContext otherSide;
		
		private EdgeContext(int index) {
			this.index = index;
		}
		
		@Override
		public String toString() {
			return "EdgeContext [" + getSource() + " -> " + getTarget() + "]";
		}

		public Vertex getSource() {
			return vertices.get(index);
		}
		
		public Vertex getTarget() {
			int next = index + 1;
			if (next == vertices.size())
				next = 0;
			return vertices.get(next);
		}
		
		public OrientedLoop getLoop() {
			return OrientedLoop.this;
		}
		
		public EdgeContext getOtherSide() {
			return otherSide;
		}
		
		public void connect(EdgeContext other) {
			assert this.otherSide == null;
			assert other.otherSide == null;
			assert this.getTarget() == other.getSource();
			assert this.getSource() == other.getTarget();
			this.otherSide = other;
			other.otherSide = this;
		}
	}
	
	public OrientedLoop(List<Vertex> vertices, boolean isInside) {
		this.vertices = Collections.unmodifiableList(new ArrayList<Vertex>(vertices));
		this.inside = isInside;
		this.edgeContexts = new ArrayList<EdgeContext>(this.vertices.size());
		for (int i = 0; i < this.vertices.size(); i++) {
			this.edgeContexts.add(new EdgeContext(i));
		}
		this.edgeContexts = Collections.unmodifiableList(this.edgeContexts);
	}
	
	public List<EdgeContext> getEdgeContexts() {
		return this.edgeContexts;
	}
	
	/** Returns true if the loop is oriented inwards. */
	public boolean isInside() {
		return inside;
	}

	/** Returns the list of vertices. */
	public List<Vertex> getVertices() {
		return vertices;  // already unmodifiable
	}
	
	@Override
	public String toString() {
		return "OrientedLoop [" + (inside ? "inside" : "outside") + ", " + vertices + "]";
	}

	private Path2D.Double pathCache = null;
	
	/** Returns a Path2D.Double which represents this path. */
	private Path2D.Double asPath() {
		if (pathCache == null) {
			pathCache = new Path2D.Double(Path2D.WIND_EVEN_ODD, this.vertices
					.size());
			boolean started = false;
			for (Vertex v : this.vertices) {
				if (!started) {
					started = true;
					pathCache.moveTo(v.getPoint().x, v.getPoint().y);
				} else {
					pathCache.lineTo(v.getPoint().x, v.getPoint().y);
				}
			}
		}
		return pathCache;
	}
	
	/** Assumptions: loops were obtained by walking loops, graph has been simplified. */
	public boolean isBoundedBy(OrientedLoop other) {
		// Pick a vertex.
		Vertex sample = this.getVertices().iterator().next();
		for (Vertex v : other.getVertices()) {
			if (sample == v) {
				assert other.isInside() || this.isInside();
				return !other.isInside();
			}
		}
		// Not degenerate.
		return other.asPath().contains(sample.getPoint().x, sample.getPoint().y);
	}
	
	private static void addPerturbedVertex(Vertex point, Corner corner,
			                               List<Corner> perturbedVertices,
			                               Map<Corner, Vertex> perturbationSave) {
		perturbedVertices.add(corner);
		perturbationSave.put(corner, point);
	}

	public Loop<Edge> asStraightSkeletonLoop(Machine machine, double epsilon,
			Map<Corner, Vertex> perturbationSave,
			Map<EdgeContext, Edge> edgeSave) {
		// First start the straight-skeleton because it can't handle
		// degeneracies.
		List<Corner> perturbedVertices = new ArrayList<Corner>();
		Map<Corner, EdgeContext> cornerToEdgeContext = new HashMap<Corner, EdgeContext>();
		if (getVertices().size() == 1) {
			// The easy case.
			Vertex vertex = getVertices().get(0);
			Point2d center = vertex.getPoint();
			double epsSqrt2 = epsilon * Math.sqrt(2.0);
			center.x += epsSqrt2;
			center.y += epsSqrt2;
			addPerturbedVertex(vertex, new Corner(center), perturbedVertices, perturbationSave);

			center.y -= 2 * epsilon;
			addPerturbedVertex(vertex, new Corner(center), perturbedVertices, perturbationSave);

			center.x -= 2 * epsilon;
			addPerturbedVertex(vertex, new Corner(center), perturbedVertices, perturbationSave);

			center.y += 2 * epsilon;
			addPerturbedVertex(vertex, new Corner(center), perturbedVertices, perturbationSave);
		} else {
			Iterable<Triple<EdgeContext, EdgeContext, EdgeContext>> triples;
			if (getVertices().size() == 2) {
				// Want 121 and 212.
				triples = new ConsecutiveTriples<EdgeContext>(Arrays.asList(
						getEdgeContexts().get(0), getEdgeContexts().get(1),
						getEdgeContexts().get(0), getEdgeContexts().get(1)), false);
			} else {
				triples = new ConsecutiveTriples<EdgeContext>(getEdgeContexts(), true);
			}
			for (Triple<EdgeContext, EdgeContext, EdgeContext> triple : triples) {
				Vector2d edge1 = new Vector2d(triple.second().getSource().getPoint());
				edge1.sub(triple.first().getSource().getPoint());

				Vector2d edge2 = new Vector2d(triple.third().getSource().getPoint());
				edge2.sub(triple.second().getSource().getPoint());

				Vector2d normal1 = new Vector2d(-edge1.y, edge1.x);
				Vector2d normal2 = new Vector2d(-edge2.y, edge2.x);
				normal1.normalize();
				normal2.normalize();

				if (triple.first().getSource() == triple.third().getSource()) {
					Vector2d base = new Vector2d(edge1);
					base.normalize();
					
					Point2d p1 = new Point2d(normal1);
					p1.add(base);
					p1.x *= epsilon; p1.y *= epsilon;
					p1.add(triple.second().getSource().getPoint());
					
					Point2d p2 = new Point2d(normal2);
					p2.add(base);
					p2.x *= epsilon; p2.y *= epsilon;
					p2.add(triple.second().getSource().getPoint());
					
					addPerturbedVertex(triple.second().getSource(), new Corner(p1), perturbedVertices, perturbationSave);
					Corner p2C = new Corner(p2);
					addPerturbedVertex(triple.second().getSource(), p2C, perturbedVertices, perturbationSave);
					cornerToEdgeContext.put(p2C, triple.second());
				} else {
					// Add the normals because their angles range from -PI to
					// PI, so it's the right direction.
					Vector2d perturbed = new Vector2d(normal1);
					perturbed.add(normal2);
					perturbed.normalize();

					double angle = edge1.angle(edge2);
					double length = 1.0 / Math.cos(angle / 2.0);
					perturbed.x *= length; perturbed.y *= length;
					perturbed.x *= epsilon; perturbed.y *= epsilon;
					
					perturbed.add(triple.second().getSource().getPoint());
					Corner c = new Corner(perturbed);
					addPerturbedVertex(triple.second().getSource(), c, perturbedVertices, perturbationSave);
					cornerToEdgeContext.put(c, triple.second());
				}
			}
		}
		
		// Okay, /now/ convert it to a loop.
		Loop<Edge> outerLoop = new Loop<Edge>();
		for (Pair<Corner, Corner> pair : new ConsecutivePairs<Corner>(
				perturbedVertices, true)) {
			Edge e = new Edge(pair.first(), pair.second(), Math.PI / 4);
			e.machine = machine;
			outerLoop.append(e);
			EdgeContext ec = cornerToEdgeContext.get(pair.first());
			if (ec != null)
				edgeSave.put(ec, e);
		}
		// the points defining the start and end of a loop must be the same
		// object
		for (Loopable<Edge> le : outerLoop.loopableIterator())
			le.get().end = le.getNext().get().start;
		return outerLoop;
	}
}
