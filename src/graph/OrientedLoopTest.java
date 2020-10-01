package graph;

import java.util.Arrays;

import javax.vecmath.Point2d;

import junit.framework.Assert;

import org.junit.Test;


public class OrientedLoopTest {
	@Test public void testContainedIn() {
		//
		//       A____
		//       |\_  \__
		//       |\ D    \_
		//       | \EF     \
		//       B----------C
		//
		Vertex a = new Vertex(new Point2d(0, 10));
		Vertex b = new Vertex(new Point2d(0, 0));
		Vertex c = new Vertex(new Point2d(10, 0));
		
		Vertex d = new Vertex(new Point2d(3, 4));
		Vertex e = new Vertex(new Point2d(3, 3));
		Vertex f = new Vertex(new Point2d(4, 3));

		Vertex g = new Vertex(new Point2d(103.1, 103.9));
		Vertex h = new Vertex(new Point2d(103.1, 103.1));
		Vertex i = new Vertex(new Point2d(103.9, 103.1));
		
		OrientedLoop acbOuter = new OrientedLoop(Arrays.asList(a, c, b), false);		
		OrientedLoop defInner = new OrientedLoop(Arrays.asList(d, e, f), true);
		OrientedLoop aedInner = new OrientedLoop(Arrays.asList(a, e, d), true);
		OrientedLoop abcadfeInner = new OrientedLoop(
				Arrays.asList(a, b, c, a, d, f, e), true);
		OrientedLoop ghiInner = new OrientedLoop(Arrays.asList(g, h, i), true);
		OrientedLoop gihOuter = new OrientedLoop(Arrays.asList(g, i, h), false);
		
		Assert.assertFalse(acbOuter.isBoundedBy(defInner));
		Assert.assertFalse(acbOuter.isBoundedBy(aedInner));
		Assert.assertFalse(acbOuter.isBoundedBy(abcadfeInner));
		Assert.assertFalse(acbOuter.isBoundedBy(ghiInner));
		Assert.assertFalse(acbOuter.isBoundedBy(gihOuter));
		
		Assert.assertTrue(defInner.isBoundedBy(acbOuter));
		Assert.assertFalse(defInner.isBoundedBy(aedInner));
		Assert.assertFalse(defInner.isBoundedBy(abcadfeInner));
		Assert.assertFalse(defInner.isBoundedBy(ghiInner));
		Assert.assertFalse(defInner.isBoundedBy(gihOuter));
				
		Assert.assertTrue(aedInner.isBoundedBy(acbOuter));
		Assert.assertFalse(aedInner.isBoundedBy(defInner));
		Assert.assertFalse(aedInner.isBoundedBy(abcadfeInner));
		Assert.assertFalse(aedInner.isBoundedBy(ghiInner));
		Assert.assertFalse(aedInner.isBoundedBy(gihOuter));
			
		Assert.assertTrue(abcadfeInner.isBoundedBy(acbOuter));
		Assert.assertFalse(abcadfeInner.isBoundedBy(defInner));
		Assert.assertFalse(abcadfeInner.isBoundedBy(aedInner));
		Assert.assertFalse(abcadfeInner.isBoundedBy(ghiInner));
		Assert.assertFalse(abcadfeInner.isBoundedBy(gihOuter));
		
		Assert.assertFalse(ghiInner.isBoundedBy(acbOuter));
		Assert.assertFalse(ghiInner.isBoundedBy(defInner));
		Assert.assertFalse(ghiInner.isBoundedBy(aedInner));
		Assert.assertFalse(ghiInner.isBoundedBy(abcadfeInner));
		Assert.assertTrue(ghiInner.isBoundedBy(gihOuter));

		Assert.assertFalse(gihOuter.isBoundedBy(acbOuter));
		Assert.assertFalse(gihOuter.isBoundedBy(defInner));
		Assert.assertFalse(gihOuter.isBoundedBy(aedInner));
		Assert.assertFalse(gihOuter.isBoundedBy(abcadfeInner));
		Assert.assertFalse(gihOuter.isBoundedBy(ghiInner));
	}
}
