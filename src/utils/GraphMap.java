// Modified by David Benjamin and Anthony Lee to:
// * Silence Java warnings
package utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author twak
 */
public class GraphMap <E>
{
    public Map <E, List<E>> map = new LinkedHashMap<E, List<E>>();

    public void add (E a, E b)
    {
        addEntry( a, b );
        addEntry( b, a );
    }

    private void addEntry (E a, E b)
    {
        List<E> res = map.get( a );
        if (res == null)
        {
            res = new ArrayList<E>();
            map.put( a, res);
        }
        
        if (!res.contains( b ))
            res.add(b);
    }

    public List<E> get (E a)
    {
        return map.get( a );  
    }

    public void clear()
    {
        map.clear();
    }

    public void addEntriesFrom ( GraphMap<E> otherMap )
    {
        if ( otherMap == this )
            return; // done!

        for ( Map.Entry<E,List<E>> e : otherMap.map.entrySet () )
        {
            Map.Entry<E,List<E>> entry = e;
            for ( E dest : entry.getValue () )
                addEntry ( entry.getKey (), dest );
        }
    }

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        for (E e : map.keySet())
        {
            sb.append( e +" |||   " );
            for (E ee : map.get( e ))
                sb.append( ee +"," );
            sb.append( "\n");
        }
        return sb.toString();
    }

    public void remove( E a, E b )
    {
        remove_ (a,b);
        remove_ (b,a);
    }

    void remove_( E a, E b )
    {
        List<E> e = get( a );
        if (e == null)
            return;
        e.remove( b );
        if (e.isEmpty())
            map.remove( a );
    }
}
