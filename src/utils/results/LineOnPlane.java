// Modified by David Benjamin and Anthony Lee to:
// * Silence a Java warning
package utils.results;

import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;

/**
 * A result of an intersection that is really a line
 * @author twak
 */
public class LineOnPlane extends Point3d
{
	private static final long serialVersionUID = 1L;
	public Tuple3d direction;
    public double distance;
    
    public LineOnPlane (Tuple3d start, Tuple3d direction, double distance)
    {
        super (start);
        this.direction = direction;
        this.distance = distance;
    }
}
