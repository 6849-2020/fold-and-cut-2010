package graph;

import javax.vecmath.Point2d;

/***
 * A wrapper over Point2d that deals with equals and such correctly.
 *
 */
public class Vertex {
	private Point2d point;
	
	public Vertex() {
		point = new Point2d();
	}
	
	public Vertex(double x, double y) {
		point = new Point2d(x, y);
	}
	
	public Vertex(Point2d p) {
		this();
		setPoint(p);
	}
	
	public Point2d getPoint() {
		return point;
	}
	
	public void setPoint(Point2d p) {
		point.set(p);
	}
	
	@Override
	public Vertex clone() {
		return new Vertex(getPoint());
	}

	@Override
	public String toString() {
		return "V[" + point + "]";
	}
}
