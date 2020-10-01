package foldandcut;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

import straightskeleton.Corner;
import utils.DRectangle;
import utils.Line;


public class FoldAndCutPattern {
	private FoldAndCutProblem problem;
	private List<SkeletonOutput> outputs;
	private List<Perpendicular> perpendiculars;
	
	public FoldAndCutPattern(FoldAndCutProblem problem, List<SkeletonOutput> outputs) {
		this.problem = problem;
		this.outputs = outputs;
		this.perpendiculars = new ArrayList<Perpendicular>();
	}
	
	private static final class PerpendicularSource {
		SkeletonFace face;
		Point3d point;
		public PerpendicularSource(SkeletonFace face, Point3d point) {
			this.face = face;
			this.point = point;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((face == null) ? 0 : face.hashCode());
			result = prime * result
					+ ((point == null) ? 0 : point.hashCode());
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
			PerpendicularSource other = (PerpendicularSource) obj;
			if (face == null) {
				if (other.face != null)
					return false;
			} else if (!face.equals(other.face))
				return false;
			if (point == null) {
				if (other.point != null)
					return false;
			} else if (!point.equals(other.point))
				return false;
			return true;
		}
	}
	public void computePerpendiculars(DRectangle bounds) {
		Set<PerpendicularSource> computedPerpendiculars = new HashSet<PerpendicularSource>();
		
		for (SkeletonOutput sOutput : outputs) {
			for (SkeletonFace face : sOutput.getFaces().values()) {
				for (Point3d startPoint3d : face.getSkeletonFace().points.eIterator()) {
					tracePerpendicular(sOutput, bounds, computedPerpendiculars, face, startPoint3d);
				}
				for (Corner c : face.getSkeletonFace().definingCorners) {
					tracePerpendicular(sOutput, bounds, computedPerpendiculars, face, new Point3d(c));
				}
			}
		}
	}
	
	private void tracePerpendicular(SkeletonOutput sOutput, DRectangle bounds,
			Set<PerpendicularSource> computedPerpendiculars, SkeletonFace face,
			Point3d startPoint3d) {
		if (computedPerpendiculars.contains(new PerpendicularSource(face, startPoint3d)))
			return;
		// Trace a perpendicular starting from |startPoint| on |edge| of |face|.
		Point2d startPoint = sOutput.getInput().unperturb(startPoint3d);
		if (!bounds.contains(startPoint.x, startPoint.y))
			return;
		SkeletonFace.PerimeterPoint pe =
			face.followPerpendicular(startPoint);
		perpendiculars.add(new Perpendicular(new Line(startPoint, pe.point)));
		
		final int MAX_SEGMENTS = 40;
		// Continue tracing perpendiculars
		int i = 0;
		while (pe.asVertex == null) {
			Point2d prev = pe.point;
			if (!bounds.contains(prev.x, prev.y)) {
				System.err.println("Went off-paper. Stopping.");
				break;
			}
			pe = pe.continuePerpendicular();
			if (pe == null)
				break;
			perpendiculars.add(new Perpendicular(new Line(prev, pe.point)));
			// Limit the number.
			i++;
			if (i > MAX_SEGMENTS / 2) {
				System.err.println("Merp.\t" + pe.face.hashCode() + "\t" + pe.point);
				for (Point3d p : pe.face.getSkeletonFace().points.get(0)) { 
					System.err.println("\t" + p);
				}
			}
			if (i > MAX_SEGMENTS) {
				System.err.println("Had to stop perpendiculars!");
				break;
			}
		}
		
		// If we end on a vertex, don't go backwards.
		if (pe != null && pe.asVertex != null) {
			computedPerpendiculars.add(new PerpendicularSource(pe.face, pe.asVertex));
		}		
	}
	
	public List<SkeletonOutput> getOutputs() {
		return Collections.unmodifiableList(outputs);
	}
	
	public List<Perpendicular> getPerpendiculars() {
		return Collections.unmodifiableList(perpendiculars);
	}
	
	public FoldAndCutProblem getProblem() {
		return problem;
	}
}
