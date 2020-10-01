package graph;

import graph.Graph.PairOfVertices;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import utils.Pair;

import junit.framework.Assert;
//import junit.framework.TestCase;

public class GraphTest {

	Graph g0;
	Graph g1;
	Graph g2;
	Graph g3;
	Graph g4;
	Graph g5;
	Graph g6;
	
	@Before public void setup() {

		// g0
		// *  *
		// |\/|
		// |/\|
		// *  *

		g0 = new Graph();
		Vertex v0 = new Vertex(-1,-1);
		Vertex v1 = new Vertex(-1,1);
		Vertex v2 = new Vertex(1,1);
		Vertex v3 = new Vertex(1,-1);
		g0.addVertex(v0);
		g0.addVertex(v1);
		g0.addVertex(v2);
		g0.addVertex(v3);
		g0.addEdge(v0, v1);
		g0.addEdge(v1, v3);
		g0.addEdge(v3, v2);
		g0.addEdge(v2, v0);
		

		// g1
		//    *  *
		//    |\/
		//    |/\
		// *  /  *
		//  \/|
		//  /\|
		// *  *
		
		g1 = new Graph();
		v0 = new Vertex(-2,-2);
		v1 = new Vertex(-2,0);
		v2 = new Vertex(0,-2);
		v3 = new Vertex(0,2);
		Vertex v4 = new Vertex(2,0);
		Vertex v5 = new Vertex(2,2);
		g1.addVertex(v0);
		g1.addVertex(v1);
		g1.addVertex(v2);
		g1.addVertex(v3);
		g1.addVertex(v4);
		g1.addVertex(v5);
		g1.addEdge(v0, v5);
		g1.addEdge(v1, v2);
		g1.addEdge(v2, v3);
		g1.addEdge(v3, v4);
		
		
		// g2
		// *  *  *
		//  \ | /
		//   \|/
		//   /|\
		//  / | \
		// *  *  *

		g2 = new Graph();
		v0 = new Vertex(-1,1);
		v1 = new Vertex(0,1);
		v2 = new Vertex(1,1);
		v3 = new Vertex(-1,-1);
		v4 = new Vertex(0,-1);
		v5 = new Vertex(1,-1);
		g2.addVertex(v0);
		g2.addVertex(v1);
		g2.addVertex(v2);
		g2.addVertex(v3);
		g2.addVertex(v4);
		g2.addVertex(v5);
		g2.addEdge(v0, v5);
		g2.addEdge(v1, v4);
		g2.addEdge(v2, v3);
		
		
		// g3
		//     *   *
		//    / \ / \
		//   /   /   \
		//  /   / \   \
		// *   *   *   *
		//  \   \ /   /
		//   \   /   /
		//    \ / \ /
		//     *   *
		
		g3 = new Graph();
		v0 = new Vertex(-1,2);
		v1 = new Vertex(-3,0);
		v2 = new Vertex(-1,-3);
		v3 = new Vertex(1,0);
		v4 = new Vertex(1,2);
		v5 = new Vertex(-1,0);
		Vertex v6 = new Vertex(1,-3);
		Vertex v7 = new Vertex(3,0);
		g3.addVertex(v0);
		g3.addVertex(v1);
		g3.addVertex(v2);
		g3.addVertex(v3);
		g3.addVertex(v4);
		g3.addVertex(v5);
		g3.addVertex(v6);
		g3.addVertex(v7);
		g3.addEdge(v0, v1);
		g3.addEdge(v1, v2);
		g3.addEdge(v2, v3);
		g3.addEdge(v3, v0);
		g3.addEdge(v4, v5);
		g3.addEdge(v5, v6);
		g3.addEdge(v6, v7);
		g3.addEdge(v7, v4);
		
		
		// g4
		//     *
		//      \
		//       \
		//      / \
		// *   /   *
		//  \ /
		//   \
		//    \
		//     *

		g4 = new Graph();
		v0 = new Vertex(-2,0);
		v1 = new Vertex(-1,-1);
		v2 = new Vertex(0,-2);
		v3 = new Vertex(0,2);
		v4 = new Vertex(1,1);
		v5 = new Vertex(2,0);
		g4.addVertex(v0);
		g4.addVertex(v1);
		g4.addVertex(v2);
		g4.addVertex(v3);
		g4.addVertex(v4);
		g4.addVertex(v5);
		g4.addEdge(v0, v2);
		g4.addEdge(v1, v4);
		g4.addEdge(v3, v5);
		
		
		// g5
		//   *
		//  /|\
		// * * *
		
		g5 = new Graph();
		v0 = new Vertex(0,1);
		v1 = new Vertex(-1,0);
		v2 = new Vertex(0,0);
		v3 = new Vertex(1,0);
		g5.addVertex(v0);
		g5.addVertex(v1);
		g5.addVertex(v2);
		g5.addVertex(v3);
		g5.addEdge(v0,v1);
		g5.addEdge(v0,v2);
		g5.addEdge(v0,v3);
	 }


	@Test public void testRemoveIntersections() {
		System.out.println(g0);
		Assert.assertEquals(g0.edgeSet().size(), 4);
		g0.removeIntersections(0);
		Assert.assertEquals(g0.edgeSet().size(), 6);
		
		
		Assert.assertEquals(g1.edgeSet().size(), 4);
		g1.removeIntersections(0);
		Assert.assertEquals(g1.edgeSet().size(), 10);

		
		int numDegreeTwo = 0;
		int numDegreeFour = 0;
		for (Vertex v: g1.vertexSet())
		{
			if ( g1.degreeOf(v) == 2 )
			{
				numDegreeTwo++;
				continue;
			}
			if ( g1.degreeOf(v) == 4 )
			{
				numDegreeFour++;
			}
		}
		Assert.assertEquals(numDegreeTwo, 2);
		Assert.assertEquals(numDegreeFour, 3);


		Assert.assertEquals(g2.edgeSet().size(), 3);
		g2.removeIntersections(0);
		Assert.assertEquals(g2.edgeSet().size(), 6);


		Assert.assertEquals(g3.edgeSet().size(), 8);
		g3.removeIntersections(0);
		Assert.assertEquals(g3.edgeSet().size(), 12);


		Assert.assertEquals(g4.edgeSet().size(), 3);
		g4.removeIntersections(0);
		Assert.assertEquals(g4.edgeSet().size(), 5);
		

		Assert.assertEquals(g5.edgeSet().size(), 3);
		g5.removeIntersections(0);
		Assert.assertEquals(g5.edgeSet().size(), 3);
		
		
	}


	@Test public void testCalculateAngle() {
		double d;
		
		d = Graph.calculateAngle(
			new Pair<Vertex,Vertex>(new Vertex(1,1),new Vertex(2,2)),
			new Pair<Vertex,Vertex>(new Vertex(2,2),new Vertex(1,1)));
		Assert.assertEquals(d, -Math.PI);
		
		d = Graph.calculateAngle(
			new Pair<Vertex,Vertex>(new Vertex(1,-1),new Vertex(2,-2)),
			new Pair<Vertex,Vertex>(new Vertex(2,-2),new Vertex(-1,1)));
		Assert.assertEquals(d, -Math.PI);
		
		d = Graph.calculateAngle(
			new Pair<Vertex,Vertex>(new Vertex(1,1),new Vertex(2,2)),
			new Pair<Vertex,Vertex>(new Vertex(2,2),new Vertex(2,3)));
		Assert.assertEquals(d, Math.PI/4);
		
		d = Graph.calculateAngle(
			new Pair<Vertex,Vertex>(new Vertex(1,1),new Vertex(2,2)),
			new Pair<Vertex,Vertex>(new Vertex(2,2),new Vertex(1,3)));
		Assert.assertEquals(d, Math.PI/2);

		d = Graph.calculateAngle(
			new Pair<Vertex,Vertex>(new Vertex(1,1),new Vertex(2,2)),
			new Pair<Vertex,Vertex>(new Vertex(2,2),new Vertex(-2,0)));
		Assert.assertTrue(d < Math.PI && d > 0);

		d = Graph.calculateAngle(
			new Pair<Vertex,Vertex>(new Vertex(1,1),new Vertex(2,2)),
			new Pair<Vertex,Vertex>(new Vertex(2,2),new Vertex(-2,-1)));
		Assert.assertTrue(d < Math.PI && d > 0);
		
		d = Graph.calculateAngle(
			new Pair<Vertex,Vertex>(new Vertex(1,-1),new Vertex(2,-2)),
			new Pair<Vertex,Vertex>(new Vertex(2,-2),new Vertex(2,-1)));
		Assert.assertEquals(d, Math.PI*3/4);
		
		Random r = new Random();
		r.setSeed(System.currentTimeMillis());
		
		d = Graph.calculateAngle(
			new Pair<Vertex,Vertex>(new Vertex(r.nextDouble(),r.nextDouble()),new Vertex(r.nextDouble(),r.nextDouble())),
			new Pair<Vertex,Vertex>(new Vertex(r.nextDouble(),r.nextDouble()),new Vertex(r.nextDouble(),r.nextDouble())));
		Assert.assertTrue(d < Math.PI && d >= -Math.PI);
		
	}


	@Test public void calculateNextLeftmostPair()
	{
		// g3
		//     0   4
		//    / \ / \
		//   /   /   \
		//  /   / \   \
		// 1   5   3   7
		//  \   \ /   /
		//   \   /   /
		//    \ / \ /
		//     2   6
		
		g3 = new Graph();
		Vertex v0 = new Vertex(-1,2);
		Vertex v1 = new Vertex(-3,0);
		Vertex v2 = new Vertex(-1,-3);
		Vertex v3 = new Vertex(1,0);
		Vertex v4 = new Vertex(1,2);
		Vertex v5 = new Vertex(-1,0);
		Vertex v6 = new Vertex(1,-3);
		Vertex v7 = new Vertex(3,0);
		g3.addVertex(v0);
		g3.addVertex(v1);
		g3.addVertex(v2);
		g3.addVertex(v3);
		g3.addVertex(v4);
		g3.addVertex(v5);
		g3.addVertex(v6);
		g3.addVertex(v7);
		g3.addEdge(v0, v1);
		g3.addEdge(v1, v2);
		g3.addEdge(v2, v3);
		g3.addEdge(v3, v0);
		g3.addEdge(v4, v5);
		g3.addEdge(v5, v6);
		g3.addEdge(v6, v7);
		g3.addEdge(v7, v4);
		
		g3.removeIntersections(0);
		PairOfVertices p = new PairOfVertices(v0,v1);
		p = g3.calculateNextLeftmostPair(p);
		Assert.assertEquals(p,new PairOfVertices(v1,v2));
		p = g3.calculateNextLeftmostPair(p);
		Assert.assertTrue(p.first() == v2);
		p = g3.calculateNextLeftmostPair(p);
		Assert.assertTrue(p.second() == v5);
		p = g3.calculateNextLeftmostPair(p);
		Assert.assertTrue(p.first() == v5);
		p = g3.calculateNextLeftmostPair(p);
		Assert.assertTrue(p.second() == v0);
		
		p = new PairOfVertices(v4,v7);
		p = g3.calculateNextLeftmostPair(p);
		Assert.assertEquals(p,new PairOfVertices(v7,v6));
		p = g3.calculateNextLeftmostPair(p);
		Assert.assertTrue(p.first() == v6);
		p = g3.calculateNextLeftmostPair(p);
		Assert.assertTrue(p.second() == v2);
		p = g3.calculateNextLeftmostPair(p);
		Assert.assertEquals(p, new PairOfVertices(v2,v1));
		p = g3.calculateNextLeftmostPair(p);
		Assert.assertEquals(p, new PairOfVertices(v1,v0));
		p = g3.calculateNextLeftmostPair(p);
		Assert.assertTrue(p.first() == v0);
		p = g3.calculateNextLeftmostPair(p);
		Assert.assertTrue(p.second() == v4);
		p = g3.calculateNextLeftmostPair(p);
		Assert.assertEquals(p, new PairOfVertices(v4,v7));
		p = g3.calculateNextLeftmostPair(p);
		Assert.assertEquals(p,new PairOfVertices(v7,v6));
	}


	@Test public void testComputeBoundaryHierarchyTree()
	{
		g3.removeIntersections(0);
		g3.computeBoundaryHierarchyTree();
	}
}
