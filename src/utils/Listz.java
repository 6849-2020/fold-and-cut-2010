// Modified by David Benjamin and Anthony Lee to:
// * Silence Java warnings
package utils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author twak
 */
public class Listz {
    public static <T> List<T> union (List<T> a, List<T> b)
    {
        List<T> out = new ArrayList<T>();
        for (T oa : a)
            if (b.contains (oa))
                out.add(oa);
        return out;
    }
}
