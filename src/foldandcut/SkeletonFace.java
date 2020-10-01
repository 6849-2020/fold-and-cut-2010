package foldandcut;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;

import straightskeleton.Corner;
import straightskeleton.Edge;
import straightskeleton.Output;
import utils.Loop;
import graph.OrientedLoop;
import graph.Vertex;


public final class SkeletonFace {
	// The SS face this guy wraps.
	private Output.Face ssFace;
	// TODO: AffineTransform is far too general. Write this by hand later?
	private AffineTransform toCanonical;
	private AffineTransform fromCanonical;
	private SkeletonInput skeletonInput;
	private SkeletonOutput skeletonOutput;
	// Sorted list of edges along the bottom of the face.
	private List<TransformedEdge> edgesBottom;
	// Sorted list of edges along the bottom of the face.
	private List<TransformedEdge> edgesTop;
	
	private class TransformedCutEdge {
		Edge edge;
		Point2d canonicalStart;
		Point2d canonicalEnd;
		
		private TransformedCutEdge(Edge edge) {
			this.edge = edge;
			this.canonicalStart = mapToCanonical(edge.start);
			this.canonicalEnd = mapToCanonical(edge.end);
		}
		
		//  Bleh.
		public boolean contains(Point2d p) {
			Point2d canonicalP = mapToCanonical(p);
			if (canonicalStart.x < canonicalEnd.x) {
				return canonicalStart.x <= canonicalP.x && canonicalP.x <= canonicalEnd.x; 
			} else {
				return canonicalEnd.x <= canonicalP.x && canonicalP.x <= canonicalStart.x;
			}		
		}
	}
	
	public class TransformedEdge implements Comparable<TransformedEdge> {
		Point2d canonicalStart;
		Point2d canonicalEnd;
		Output.SharedEdge edge;
		List<TransformedCutEdge> cutEdges;
		
		// The point with the lower x coordinate.
		Point2d canonical;
		
		private TransformedEdge(Output.SharedEdge edge) {
			this.edge = edge;
			this.canonicalStart = mapToCanonical(edge.getStart(ssFace));
			this.canonicalEnd = mapToCanonical(edge.getEnd(ssFace));
			this.cutEdges = new ArrayList<TransformedCutEdge>();
			Corner end = skeletonInput.getCornerMap().get(edge.getEnd(ssFace));
			Corner first = end;
			while (ssFace.definingCorners.contains(end)) {
				cutEdges.add(new TransformedCutEdge(end.nextL));
				end = end.nextL.end;
				if (first == end) {
					assert false; // This should never ever ever happen. I think.
					break;
				}
			}
		}

		public Edge getEdge(Point2d p) {
			for (TransformedCutEdge e : cutEdges) {
				if (e.contains(p))
					return e.edge;
			}
			return null;
		}
		
		public OrientedLoop.EdgeContext getEdgeContext(Point2d p) {
			Vertex vStart, vEnd;
			if (ssFace.isBottom(edge)) {
				// start/end are backwards.
				vStart = skeletonInput.getVertex(edge.getEnd(ssFace));
				vEnd = skeletonInput.getVertex(edge.getStart(ssFace));
			} else {
				// The obnoxious case.
				Edge e = getEdge(p);
				assert e != null;
				if (e == null)
					return null;
				vStart = skeletonInput.getPerturbationMap().get(e.start);
				vEnd = skeletonInput.getPerturbationMap().get(e.end);
				assert vStart != null;
				assert vEnd != null;
			}
			assert vStart != null;
			assert vEnd != null;
			assert vStart != vEnd;
			if (vStart == null || vEnd == null)
				return null;
			return skeletonInput.getEdgeContextFor(vStart, vEnd);
		}
		
		public Point3d asVertex(double x) {
			// Snapping epsilon
			final double EPSILON = 0.1;
			// Sigh, they might be flipped.
			if (canonicalStart.x < canonicalEnd.x) { 
				if (x+EPSILON > canonicalEnd.x) {
					return edge.getEnd(ssFace);
				}
				if (x-EPSILON < canonicalStart.x) {
					return edge.getStart(ssFace);
				}
			} else {
				if (x+EPSILON > canonicalStart.x) {
					return edge.getStart(ssFace);
				}
				if (x-EPSILON < canonicalEnd.x) {
					return edge.getEnd(ssFace);
				}				
			}
			return null;
		}
		
		public Point2d project(double x) {
			final double EPSILON = 0.0000001;  // Avoid divisions by zero.
			double y = canonicalStart.y;
			double xTot = canonicalEnd.x - canonicalStart.x;
			if (Math.abs(xTot) > EPSILON) {
				y += (x - canonicalStart.x) / xTot * (canonicalEnd.y - canonicalStart.y); 
			} else {
				System.err.println("Degenerate edge!");
			}
			return mapFromCanonical(new Point2d(x, y));
		}
		
		public Output.SharedEdge getSharedEdge() {
			return edge;
		}
		
		@Override
		public String toString() {
			return "CP[" + edge.getStart(ssFace) + " to " + edge.getEnd(ssFace) + ",\n\t(" + canonicalStart +" to " + canonicalEnd + ")]\n";
		}

		@Override
		public int compareTo(TransformedEdge other) {
			// Sort by x.
			double delta = canonicalStart.x - other.canonicalStart.x;
			// Grumble integers
			if (delta < 0) return -1;
			if (delta > 0) return 1;
			return 0;  // Should never happen, so probably not worth comparing by y?
		}
	}
	
	public SkeletonFace(SkeletonOutput output, Output.Face face) {
		ssFace = face;
		
		Edge definingEdge = face.edge;
		// Should be nearly zero. Assuming I'm doing this right.
		assert Math.abs(definingEdge.start.z) < 0.001;
		assert Math.abs(definingEdge.end.z) < 0.001;

		// Shape should be above, assuming counter-clockwise?
		fromCanonical = AffineTransform.getRotateInstance(
				definingEdge.end.x - definingEdge.start.x,
				definingEdge.end.y - definingEdge.start.y);
		try {
			toCanonical = fromCanonical.createInverse();
		} catch (NoninvertibleTransformException e) {
			// This should never happen.
			e.printStackTrace();
			assert false;
		}
		
		skeletonInput = output.getInput();
		skeletonOutput = output;
		
		// Compute the edges. We know the face is monotonic along the perpendicular of the defining edge.
		assert face.edges.size() == 1; // Should not have a hole.
		Loop<Output.SharedEdge> edges = face.edges.get(0);
		List<TransformedEdge> transformedEdges = new ArrayList<TransformedEdge>(edges.count());
		for (Output.SharedEdge edge : edges) {
			transformedEdges.add(new TransformedEdge(edge));
			// The format of these edges is dumb. Edges are counter-clockwise,
			// but vertices on edges are clockwise. For more fun, the whole
			// thing is wrong mirrored.
			// TODO: Check over the rest of this code with this in mind.
		}
		// Control points are apparently pretty backward.
		Collections.reverse(transformedEdges);
		int leftmost = 0;
		int rightmost = 0;
		for (int i = 1; i < transformedEdges.size(); i++) {
			if (transformedEdges.get(leftmost).compareTo(transformedEdges.get(i)) > 0)
				leftmost = i;
			if (transformedEdges.get(rightmost).compareTo(transformedEdges.get(i)) < 0)
				rightmost = i;
		}
		
		// Shape is counter-clockwise, so, starting from the leftmost going to
		// the rightmost, you get the bottom half. Then the top-half. I probably
		// actually want a Loop here, but I at least understand this thing,
		edgesBottom = new ArrayList<TransformedEdge>(); // TODO: figure out size.
		int i = leftmost;
		while (i != rightmost) {
			TransformedEdge te = transformedEdges.get(i);
			te.canonical = te.canonicalStart;
			edgesBottom.add(te);
			i++;
			if (i == transformedEdges.size())
				i = 0;
		}
		
		edgesTop = new ArrayList<TransformedEdge>(); // TODO: figure out size.
		i = rightmost;
		while (i != leftmost) {
			TransformedEdge te = transformedEdges.get(i);
			te.canonical = te.canonicalEnd;
			edgesTop.add(te);
			i++;
			if (i == transformedEdges.size())
				i = 0;
		}
		Collections.reverse(edgesTop);
		
		assert isSorted(edgesTop);
		assert isSorted(edgesBottom);
	}
	
	private static int projectToBumpyGround(List<TransformedEdge> edge, double x) {
		if (edge.isEmpty())
			return 0; // WHAT?
		// Check if we're way off-base?
		if (edge.get(0).canonical.x > x)
			return 0;
		if (edge.get(edge.size() - 1).canonical.x < x)
			return edge.size() - 1;
		int lo = 0;
		int hi = edge.size() - 1;
		while (lo < hi) {
			int mid = (lo + hi + 1) / 2;
			if (edge.get(mid).canonical.x > x)
				hi = mid-1;
			else
				lo = mid;
		}
		return lo;
	}
	
	private static boolean isSorted(List<TransformedEdge> list) {
		for (int i = 1; i < list.size(); i++) {
			if (list.get(i-1).compareTo(list.get(i)) > 0)
				return false;
		}
		return true;
	}
	
	public SkeletonInput getInput() {
		return skeletonInput;
	}	
	public SkeletonOutput getOutput() {
		return skeletonOutput;
	}
	
	public Output.Face getSkeletonFace() {
		return ssFace;
	}
	
	private Point2d mapFromCanonical(Point2d point) {
		// Ugh.
		Point2D p = fromCanonical.transform(new Point2D.Double(point.x, point.y), null);
		return new Point2d(p.getX(), p.getY());
	}
	
	private Point2d mapToCanonical(Tuple3d point) {
		return mapToCanonical(skeletonInput.unperturb(point));
	}
	
	private Point2d mapToCanonical(Point2d point) {
		// Ugh.
		Point2D p = toCanonical.transform(new Point2D.Double(point.x, point.y), null);
		return new Point2d(p.getX(), p.getY());
	}
	
	public static class PerimeterPoint {
		public TransformedEdge edge;
		public Point2d point;
		public Point3d asVertex;
		public SkeletonFace face;
		
		PerimeterPoint(TransformedEdge edge, Point2d point, Point3d asVertex, SkeletonFace face) {
			this.edge = edge;
			this.point = point;
			this.asVertex = asVertex;
			this.face = face;
		}
		
		public PerimeterPoint continuePerpendicular() {
			Output.Face otherSSFace = edge.getSharedEdge().getOther(face.ssFace);
			SkeletonFace otherFace = null;
			if (otherSSFace != null) {
				// The easy case. We stay within the same place.
				otherFace = face.skeletonOutput.getFaces().get(otherSSFace);
				assert otherFace != null;
			} else {
				OrientedLoop.EdgeContext ec = edge.getEdgeContext(point);
				assert ec != null;
				if (ec == null)
					return null;
				ec = ec.getOtherSide();
				if (ec == null)
					return null;
				// Finally. Found the next skeleton.
				SkeletonInput otherInput = face.skeletonInput.parent.loopToInput.get(ec.getLoop());
				assert otherInput != null;
				if (otherInput == null)
					return null;
				Edge edge = otherInput.edgeContextToEdge.get(ec);
				assert edge != null;
				if (edge == null)
					return null;
				SkeletonOutput otherOutput = otherInput.output;
				otherFace = otherOutput.getFaceForCutEdge(edge);
				if (otherFace == null) {
					// We should only run off the screen at infinity.
					assert this.face.skeletonInput.getBHTNode().isDummy();
					return null;
				}
			}
			
			if (otherFace == null) {
				// We should only run off the screen at infinity.
				assert this.face.skeletonInput.getBHTNode().isDummy();
				return null;
			}
			return otherFace.followPerpendicular(this.point);
		}
	}
	
	public PerimeterPoint followPerpendicular(Point2d point) {
		PerimeterPoint toTop = followPerpendicularTo(point, edgesTop);
		PerimeterPoint toBottom = followPerpendicularTo(point, edgesBottom);
		
		double topDist = toTop.point.distance(point);
		double bottomDist = toBottom.point.distance(point);
		if (topDist < bottomDist) {
			assert topDist < 0.1;
			return toBottom;
		} else {
			assert bottomDist < 0.1;
			return toTop;
		}
	}
	
	private PerimeterPoint followPerpendicularTo(Point2d point, List<TransformedEdge> side) {
		Point2d pointCanonical = mapToCanonical(point);
		int index = projectToBumpyGround(side, pointCanonical.x);
		TransformedEdge edge = side.get(index);
		Point3d asVertex = edge.asVertex(pointCanonical.x);
		if (asVertex == null) {
			return new PerimeterPoint(edge, edge.project(pointCanonical.x), null, this);
		} else {
			return new PerimeterPoint(edge, skeletonInput.unperturb(asVertex), asVertex, this);
		}
	}
}
