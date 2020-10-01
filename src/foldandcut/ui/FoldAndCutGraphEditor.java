// Originated as straightskeleton/debug/WeightedPointEditor
// Modified by David Benjamin and Anthony Lee to:
// * Rewrite to use our graph classes
// * Rewrite to display fold-and-cut
package foldandcut.ui;

import foldandcut.FoldAndCutPattern;
import foldandcut.FoldAndCutProblem;
import foldandcut.Perpendicular;
import foldandcut.SkeletonOutput;
import graph.BoundaryHierarchyTree;
import graph.Graph;
import graph.Simplify;
import graph.Vertex;
import graph.ui.GraphEditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.GraphVertexChangeEvent;
import org.jgrapht.graph.DefaultEdge;

import pdf.PDFContentStream;
import pdf.PDFSimpleDocument;
import straightskeleton.Output;
import straightskeleton.Output.Face;
import straightskeleton.debug.DebugDevice;
import utils.DRectangle;
import utils.Loop;
import utils.LoopL;

public class FoldAndCutGraphEditor extends GraphEditor {

	private static final long serialVersionUID = -5398085729794484919L;

	private boolean changed = true;
	
	private boolean busy = false;
	private FoldAndCutPattern pattern = null;
	public DRectangle paperBounds = new DRectangle(-500, -500, 1000, 1000);
	private DRectangle prevPaperBounds =
		new DRectangle(paperBounds.x,paperBounds.y,paperBounds.width,paperBounds.height);
	
	protected boolean draggingPaperBound[] = {false,false,false,false}; //left,right,bottom,top
	
	private boolean drawSkeleton = true;
	private boolean drawPerpendiculars = true;
	
	private List<Listener> listeners = new ArrayList<Listener>();
	
	public FoldAndCutGraphEditor(Graph g, DRectangle bounds) {
		super(g);
		if (bounds != null)
			paperBounds.setFrom(bounds);
	}
	
	public interface Listener {
		public void graphChanged();
	}
	
	public void addListener(Listener l) {
		listeners.add(l);
	}
	
	private void setChanged() {
		changed = true;
		for (Listener l : listeners) {
			l.graphChanged();
		}
	}
	
	public void setDrawSkeleton(boolean value) {
		if (drawSkeleton == value) return;
		drawSkeleton = value;
		setChanged();
		repaint();
	}
	
	public void setDrawPerpendiculars(boolean value) {
		if (drawPerpendiculars == value) return;
		drawPerpendiculars = value;
		setChanged();
		repaint();
	}
	
	public void exportToPDF(OutputStream stream) throws IOException {
		// TODO: What should be the default page size?
		// For now, let's just make the largest coordinate 8 inches?
		PDFContentStream cs = new PDFContentStream();
		
		// TODO: Combine all these transforms into one cm command.
		int width, height;
		double scale;
		if (paperBounds.width < paperBounds.height) {
			height = 8 * 72;
			scale = height / paperBounds.height;
			width = (int) (scale * paperBounds.width);
		} else {
			width = 8 * 72;
			scale = width / paperBounds.width;
			height = (int) (scale * paperBounds.height);
		}
		// Scale the coordinate system appropriately.
		cs.applyTransform(AffineTransform.getScaleInstance(scale, scale));
		// Now, translate it so the box is in the right spot.
		cs.applyTransform(AffineTransform.getTranslateInstance(-paperBounds.x, -paperBounds.y));
		// Finally, flip the coordinate system.
		cs.applyTransform(AffineTransform.getScaleInstance(1, -1)); // Odd. Apparently we don't need to translate?
		cs.applyTransform(AffineTransform.getTranslateInstance(0, - 2 * paperBounds.y - paperBounds.height));
		
		if (drawSkeleton && pattern != null) {
			// Draw the straight-skeleton.
			cs.setStrokeColor(Color.red);
			for (SkeletonOutput output : pattern.getOutputs()) {
				for (Face face : output.getOutput().faces.values()) {
					for (Loop<Point3d> loop : face.getLoopL()) {
						boolean first = true;
						for (Point3d p : loop) {
							Point2d pp = output.getInput().unperturb(p);
							if (first) {
								cs.moveTo(pp);
								first = false;
							} else {
								cs.lineTo(pp);
							}
						}
						cs.closeAndStroke();
					}
				}
			}
			if (drawPerpendiculars) {
				// Draw the perpendiculars too.
				cs.setStrokeColor(Color.blue);
				for (Perpendicular perp : pattern.getPerpendiculars()) {
					cs.moveTo(perp.getLine().start);
					cs.lineTo(perp.getLine().end);
					cs.stroke();
				}
			}
		}
		
		// Draw the cut edges.
		cs.setStrokeColor(Color.black);
        for (DefaultEdge edge : graph.edgeSet()) {
        	cs.moveTo(graph.getEdgeSource(edge).getPoint());
        	cs.lineTo(graph.getEdgeTarget(edge).getPoint());
        }
		cs.stroke();
		
		PDFSimpleDocument.writeSinglePage(
				stream,
				width, height,
				cs.toString());
	}
	
	public void setup() {
		super.setup();
		graph.addGraphListener(new GraphListener<Vertex, DefaultEdge>() {
			@Override
			public void vertexRemoved(GraphVertexChangeEvent<Vertex> arg0) {
				setChanged();
			}
			
			@Override
			public void vertexAdded(GraphVertexChangeEvent<Vertex> arg0) {
				setChanged();
			}
			
			@Override
			public void edgeRemoved(GraphEdgeChangeEvent<Vertex, DefaultEdge> arg0) {
				setChanged();
			}
			
			@Override
			public void edgeAdded(GraphEdgeChangeEvent<Vertex, DefaultEdge> arg0) {
				setChanged();
			}
		});
	}
	
    public void movePoint(Vertex v, Point2d location, MouseEvent evt) {
    	if (v == null)
    		return;
    	setChanged();
        super.movePoint(v, location, evt);
    }

    public void moveEdge(MouseEvent e)
    {
	    Point2d ept = new Point2d(ma.fromX(e.getPoint().x), ma.fromY(e.getPoint().y));
	    double dx = ept.x-dragEdgeStartPoint.x;
	    double dy = ept.y-dragEdgeStartPoint.y;
		graph.getEdgeSource(currentEdge).getPoint().set(draggedEdgeSource.x+dx,draggedEdgeSource.y+dy);
		graph.getEdgeTarget(currentEdge).getPoint().set(draggedEdgeTarget.x+dx,draggedEdgeTarget.y+dy);
		setChanged();
		repaint();
	}
    
    public void paintPointEditor(Graphics2D g2)
    {
        scheduleStraightSkeletons();
        g2.setClip(ma.to(paperBounds));
        if (pattern != null && drawSkeleton) {
        	for (SkeletonOutput output : pattern.getOutputs()) {
        		paintSkeleton(g2, output);
        	}
        	if (drawPerpendiculars) {
        		for (Perpendicular perpendicular : pattern.getPerpendiculars()) {
        			g2.setColor(Color.blue);
        			drawLine(g2, perpendicular.getLine());
        		}
        	}
        }
        g2.setClip(null);
        paintCutEdges(g2);
        // Draw the paper bounds.
        g2.setColor(Color.green);
        g2.draw(ma.to(paperBounds));
    }
    
    private void paintCutEdges(Graphics2D g2) {
        // override me!

        g2.setColor(Color.black);
        for (DefaultEdge edge : graph.edgeSet()) {
        	g2.drawLine(
        			ma.toX(graph.getEdgeSource(edge).getPoint().x),
        			ma.toY(graph.getEdgeSource(edge).getPoint().y),
        			ma.toX(graph.getEdgeTarget(edge).getPoint().x),
        			ma.toY(graph.getEdgeTarget(edge).getPoint().y));
        }

        for (Vertex v : graph.vertexSet())
        	if ( v != this.selectedVertex )
        	{
        		drawPixel( g2, v.getPoint() );        		
        	}
        	else
        	{
        		g2.setColor(Color.blue);
        		drawPixel( g2, v.getPoint() );
            	g2.setColor(Color.black);
        	}
    }
    
    private static FoldAndCutProblem getFoldAndCutProblem(Graph g) {
    	final double EPSILON = 0.01;
    	
    	// First, we simplify the graph.
    	g = Simplify.simplifyGraph(g, EPSILON);
    	// Next, we handle intersection edges.
    	g.removeIntersections(EPSILON);
    	// While we're at it, simplify again.
    	g = Simplify.simplifyGraph(g, EPSILON);
    	System.err.println(g);
    	// Now, we find faces.
    	BoundaryHierarchyTree tree = g.computeBoundaryHierarchyTree();
    	
    	System.err.println(tree);
    	return new FoldAndCutProblem(tree, EPSILON);
    }
    
    private void scheduleStraightSkeletons() {
    	if (!changed || busy)
    		return;
    	busy = true;
    	changed = false;
    	
    	if (graph.vertexSet().size() == 0 || !drawSkeleton) {
    		// Special-case this to hide the dummy box.
    		pattern = null;
    		busy = false;
    		return;
    	}
    	
    	// Make a copy of the graph for the thread.
    	final Graph graphCopy = graph.deepCopy();
    	final boolean drawPs = drawPerpendiculars;
    	new Thread() {
    		@Override
    		public void run() {
    			FoldAndCutPattern pattern = null;
    			try {
    		    	FoldAndCutProblem problem = getFoldAndCutProblem(graphCopy);
    				DebugDevice.reset();
    				pattern = problem.computePattern();
    				if (drawPs)
    					pattern.computePerpendiculars(paperBounds);
    			} finally {
    				final FoldAndCutPattern fPattern = pattern;
    				SwingUtilities.invokeLater(new Runnable() {
    					@Override
    					public void run() {
    						FoldAndCutGraphEditor.this.busy = false;
    						FoldAndCutGraphEditor.this.pattern = fPattern;
    						FoldAndCutGraphEditor.this.repaint();
    					}
    				});
    			}
    		};
    	}.start();
    }
    
    private void paintSkeleton(Graphics2D g2, SkeletonOutput sOutput) {
    	Output output = sOutput.getOutput();
    	
        if ( output != null && output.faces != null )
        {
            g2.setStroke( new BasicStroke( 1 ) );

            for ( Face face : output.faces.values() ) {
                LoopL<Point3d> loopl = face.getLoopL();
                
                /**
                 * First loop is the outer. Most skeleton faces will only have this.
                 * Second+ loops are the holes in the face (if you need this, you're
                 * a long way down a rabbit hole)
                 */
                for ( Loop<Point3d> loop : loopl )
                {
                    Polygon pg = new Polygon();
                    for ( Point3d p : loop ) {
                    	Point2d pp = sOutput.getInput().unperturb(p);
                        pg.addPoint( ma.toX( pp.x ), ma.toY( pp.y ) );
                    }

                    if ( pg.npoints > 2 ) {
                        g2.setColor( Color.red );
                        g2.drawPolygon( pg );
                    }
                }
            }
        }
    }

    protected void handleMousePressedEvent(MouseEvent e)
    {
        Point screenSpaceTopLeft = ma.to(new Point2d(paperBounds.x,paperBounds.y));
        Point screenSpaceBottomRight =
        	ma.to(new Point2d(paperBounds.x+paperBounds.width,paperBounds.y+paperBounds.height));

        if ( e.getPoint().y >= screenSpaceTopLeft.y - 10
        && e.getPoint().y <= screenSpaceBottomRight.y + 10 )
        {
        	if ( Math.abs(e.getPoint().x-screenSpaceTopLeft.x) < 10 )
        	{
        		// dragging left
        		draggingPaperBound[0] = true;
        	}
        	else if ( Math.abs(e.getPoint().x-screenSpaceBottomRight.x) < 10 )
        	{
        		// dragging right
        		draggingPaperBound[1] = true;
        	}
        }

        if ( e.getPoint().x >= screenSpaceTopLeft.x - 10
        && e.getPoint().x <= screenSpaceBottomRight.x + 10 )
        {
        	if ( Math.abs(e.getPoint().y-screenSpaceTopLeft.y) < 10 )
	        {
        		// if dragging the top boundary
	        	draggingPaperBound[3] = true;
	        }
        	else if ( Math.abs(e.getPoint().y-screenSpaceBottomRight.y) < 10 )
        	{
    			// if dragging the bottom boundary
    			draggingPaperBound[2] = true;
        	}
        }
	}
    
    protected void handleMouseDraggedEvent(MouseEvent e)
    {
    	if ( draggingPaperBound[0] )
    	{
           	paperBounds.x = 
        		Math.min(ma.fromX(e.getPoint().x),
        			ma.fromX(ma.toX(prevPaperBounds.width+prevPaperBounds.x)-1));
        	paperBounds.width = prevPaperBounds.width+prevPaperBounds.x-paperBounds.x;
        	setChanged();
        	repaint();
    	}
    	else if ( draggingPaperBound[1] )
    	{
    		// right
           	paperBounds.width =
        		Math.max(ma.fromX(e.getPoint().x)-paperBounds.x,
        			ma.fromX(ma.toX(prevPaperBounds.x)+2)-prevPaperBounds.x);
        	setChanged();
        	repaint();
    	}
    	
    	if ( draggingPaperBound[2] )
    	{
    		// bottom
        	paperBounds.height =
        		Math.max(ma.fromY(e.getPoint().y)-paperBounds.y,
        			ma.fromY(ma.toY(prevPaperBounds.y)+2)-prevPaperBounds.y);
        	setChanged();
        	repaint();
    	}
    	else if ( draggingPaperBound[3] )
    	{
    		paperBounds.y = 
        		Math.min(ma.fromY(e.getPoint().y),
        			ma.fromY(ma.toY(prevPaperBounds.height+prevPaperBounds.y)-1));
        	paperBounds.height = prevPaperBounds.height+prevPaperBounds.y-paperBounds.y;
        	setChanged();
        	repaint();
    	}
	}
    
    protected void handleMouseReleasedEvent(MouseEvent e)
    {
    	draggingPaperBound[0] = false;
    	draggingPaperBound[1] = false;
    	draggingPaperBound[2] = false;
    	draggingPaperBound[3] = false;
    	prevPaperBounds.setFrom(paperBounds);
	}
    
}
