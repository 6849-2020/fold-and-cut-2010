// Modified by David Benjamin and Anthony Lee to:
// * Silence some Java warnings
package utils;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author twak
 */
public class It<E> implements Iterable<E>
{
    List<E> stuff = new ArrayList<E>();

    public It (E...ees)
    {
        for (E e : ees)
            stuff.add(e);
    }

    public Iterator<E> iterator()
    {
        return stuff.iterator();
    }

}
