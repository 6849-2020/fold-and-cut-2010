// Modified by David Benjamin and Anthony Lee to:
// * Work with KD tree's new API.
// * Silence some Java warnings
package utils;

import edu.wlu.cs.levy.CG.KDTree;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author twak
 */
public abstract class Cluster1D <D>
{
    KDTree<D> kd = new KDTree<D>( 1 );
    MultiMap<Double, D> vals = new MultiMap<Double, D>();

    public Cluster1D(Iterable<D> stuff)
    {
        for (D d : stuff)
        {
            try
            {
                double val = getVal( d );
                kd.insert( new double[]
                        {
                            val
                        }, d );
                vals.put( val, d );
            }
            catch ( Throwable ex )
            {
                ex.printStackTrace();
            }
        }
    }

    public Set<D> getStuffBetween( double min, double max )
    {
        Set<D> out = new HashSet<D>();
        try
        {

            List<D> res = kd.range( new double[]
                    {
                        min
                    }, new double[]
                    {
                        max
                    } );
            for ( D o : res )
            {
//                double[] val = (double[]) o;
                out.add(o);
            }
        }
        catch ( Throwable ex )
        {
            ex.printStackTrace();
        }
        return out;
    }

    public abstract double getVal (D d);

    public Set<D> getNear( double val, double delta )
    {
        Set<D> out= new HashSet<D>();
        try
        {
            List<D> found = kd.range( new double[]{val - delta}, new double[]{val + delta} );
            for ( D o : found )
            {
                out.add(o);
            }
        }
        catch ( Throwable ex )
        {
            ex.printStackTrace();
        }
        return out;
    }
}
