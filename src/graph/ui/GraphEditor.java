// Originated as straightskeleton/ui/PointEditor.java
// Modified by David Benjamin and Anthony Lee to:
// * Rewrite to use our graph classes
// * Add more features to the editor

package graph.ui;

import graph.Graph;
import graph.Vertex;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.GraphVertexChangeEvent;
import org.jgrapht.graph.DefaultEdge;

import utils.ConsecutivePairs;
import utils.Line;
import utils.Pair;
import utils.PanMouseAdaptor;

/**
 *
 * @author twak
 * @author davidben
 */
public class GraphEditor extends JComponent
{
	private static final long serialVersionUID = 8233547330280053499L;

	public Graph graph = new Graph();

    public DefaultEdge currentEdge = null;

    public PanMouseAdaptor ma;

    boolean paintGrid = false;

    protected Point2d dragEdgeStartPoint = new Point2d();
    protected Point2d draggedEdgeSource = new Point2d();
    protected Point2d draggedEdgeTarget = new Point2d();
    
    protected Vertex selectedVertex = null;
    boolean draggingVertex = false;
    
    public GraphEditor ()
    {
    	this(null);
    }
    
    public GraphEditor(Graph g) {
    	graph = (g == null) ? new Graph() : g;
    	setBackground( Color.white );
    }
    
    protected void createInitial()
    {
        List<Vertex> loop = Arrays.asList(
        		
                new Vertex(new Point2d (0,190)),
                new Vertex(new Point2d (-40,275)),
                new Vertex(new Point2d (-35,340)),
                new Vertex(new Point2d (30, 345)),
                new Vertex(new Point2d (40, 290))
                
        );        
        for (Vertex v : loop) {
        	graph.addVertex(v);
        }
        for ( Pair<Vertex, Vertex> pair : new ConsecutivePairs<Vertex>(loop, true )) {
            graph.addEdge(pair.first(), pair.second());
        }
    }

    private void viewCenter()
    {
    	double minX = 100000, minY = 100000, maxX = -100000, maxY = -100000;
        for (Vertex v : graph.vertexSet()) {
        	minX = Math.min(minX, v.getPoint().x);
        	minY = Math.min(minY, v.getPoint().y);
        	maxX = Math.max(maxX, v.getPoint().x);
        	maxY = Math.max(maxY, v.getPoint().y);
        }
        centerView( new Point((int) ((minX + maxX)/2.0), (int) ((minY + maxY)/2.0)));
    }


    public void centerView(Point o)
    {
        ma.center(new Point2d (o.x, o.y));
    }


    public void setup()
    {

        MouseAdapter ap = new EditorMouseAdapter(); // hard works happens in here

         ma = new PanMouseAdaptor( this ); // pan/scan convertor
         ma.button = MouseEvent.BUTTON3;

        // createInitial();

        addMouseListener( ap );
        addMouseMotionListener( ap );
        addMouseWheelListener( ap );

        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
            }
        });
        
        // Whenever the graph changes, repaint
        graph.addGraphListener(new GraphListener<Vertex, DefaultEdge>() {
			@Override
			public void vertexRemoved(GraphVertexChangeEvent<Vertex> arg0) {
				repaint();
			}
			
			@Override
			public void vertexAdded(GraphVertexChangeEvent<Vertex> arg0) {
				repaint();	
			}
			
			@Override
			public void edgeRemoved(GraphEdgeChangeEvent<Vertex, DefaultEdge> arg0) {
				repaint();
			}
			
			@Override
			public void edgeAdded(GraphEdgeChangeEvent<Vertex, DefaultEdge> arg0) {
				repaint();
			}
		});

        viewCenter();
    }

    protected boolean allowWeld( Vertex vertex )
    {
    	return graph.degreeOf(vertex) <= 2;
    }

    public boolean weld( Vertex vertex )
    {
        if (!allowWeld( vertex ))
            return false;

        Set<DefaultEdge> neighbors = graph.edgesOf(vertex);
        if (neighbors.size() == 2) {
        	Iterator<DefaultEdge> iter = neighbors.iterator();
        	Vertex a = graph.getOtherVertex(iter.next(), vertex);
        	Vertex b = graph.getOtherVertex(iter.next(), vertex);
        	graph.addEdge(a, b);
        }
        graph.removeVertex(vertex);
        return true;
    }

    public void addBetween( DefaultEdge edge, Point l )
    {
        Vertex mid = new Vertex(new Point2d( l.x, l.y ));
        Vertex source = graph.getEdgeSource(edge);
        Vertex target = graph.getEdgeTarget(edge);
        
        graph.removeEdge(edge);
        graph.addVertex(mid);
        graph.addEdge(source, mid);
        graph.addEdge(mid, target);
    }

    /**
     * Someone is dragging, but didn't start inside a loop...
     */
    public void painting( Point2d location, Point2d offset , MouseEvent evt)
    {
    }

    @Override
    public void paint( Graphics g )
    {
        Graphics2D g2 = (Graphics2D)g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setColor( getBackground() );
        g2.fillRect( 0,0,getWidth(), getHeight());

        AffineTransform at = g2.getTransform(), old = new AffineTransform(at);
        g2.setTransform( at );

        paintPointEditor( g2 );

        g2.setTransform( old );
    }


    public void paintPointEditor(Graphics2D g2) {
        // override me!

        g2.setColor(Color.red);
        for (DefaultEdge edge : graph.edgeSet()) {
        	g2.drawLine(
        			ma.toX(graph.getEdgeSource(edge).getPoint().x),
        			ma.toY(graph.getEdgeSource(edge).getPoint().y),
        			ma.toX(graph.getEdgeTarget(edge).getPoint().x),
        			ma.toY(graph.getEdgeTarget(edge).getPoint().y));
        }

        g2.setColor(Color.orange);
        for (Vertex v : graph.vertexSet())
            drawPixel( g2, v.getPoint() );
    }

    private Point2d doSnap(Vertex v, Point2d loc )
    {
        double tol = 10;
        return new Point2d ( 
                Math.round ( loc.x / tol ) * tol,
                Math.round ( loc.y / tol ) * tol);
    }

    /**
     *
     * @param loc is the location in world space.
     * @param inside did they click inside another bar-loop?
     */
    public void createSection (Point loc, boolean inside)
    {
        // override me! - event when user adds a line segment
//        createCircularPoints( 3, loc.x, loc.y, 30 );
    }

    public void movePoint(Vertex v, Point2d location, MouseEvent evt)
    {
        // override me! - request to move a point
    	v.setPoint(location);
    }

    public void moveEdge(MouseEvent e)
    {
    	// override me! - request to move a point
    }
    
    public boolean doSnap()
    {
        return true;
    }

    public void releasePoint(Vertex v, MouseEvent evt)
    {
        //override! we've stopped dragging a point, update something!
    }

    protected void drawPixel( Graphics g, double i, double j )
    {
        g.fillRect( ma.toX( i ) - 2, ma.toY( j ) - 2, 5, 5 );
    }

    protected void drawPixel( Graphics g, Point p )
    {
        drawPixel( g, p.x , p.y );
    }

    protected void drawPixel( Graphics g, Point3d p )
    {
        drawPixel( g, p.x, p.y );
    }

    protected void drawPixel( Graphics g, Point2d p)
    {
        drawPixel( g, p.x, p.y );
    }

    protected void drawLine (Graphics g, Line line )
    {
            g.drawLine( ma.toX( line.start.x ), ma.toY( line.start.y ), ma.toX( line.end.x), ma.toY( line.end.y ) );
    }

    protected void drawLine (Graphics g, double x, double y, double x2, double y2)
    {
        g.drawLine( ma.toX( x ), ma.toY( y ), ma.toX( x2 ), ma.toY( y2 ) );
    }

    protected void drawLine (Graphics g, Point3d start, Point3d end )
    {
            g.drawLine(
                ma.toX( start.x ),
                ma.toY( start.y ),
                ma.toX( end.x ),
                ma.toY( end.y ) );
    }

    protected void drawPoly (Graphics g, int[] xes, int[] yes)
    {
        g.fillPolygon( xes, yes, xes.length );
    }

    /**
     * @param destination point we're testing against
     * @param max maximum distance - else reports null
     * @return
     */
    public DefaultEdge getNearestEdge( Point destination, double max )
    { return getNearestEdge (new Point2d (destination.x, destination.y), max, this.graph); }
    public static DefaultEdge getNearestEdge(Point2d destination, double max, Graph graph)
    {
        DefaultEdge out = null;
        double best = max;
     
        for (DefaultEdge edge : graph.edgeSet()) {
        	double dist = graph.lineForEdge(edge).project(destination, true).distance(destination);
        	if (dist < best) {
        		out = edge;
        		best = dist;
        	}
        }
        return out;
    }

    protected void handleMousePressedEvent(MouseEvent e)
    {
    	// do nothing
    	// Overridden by subclasses
	}
    protected void handleMouseDraggedEvent(MouseEvent e)
    {
    	// do nothing
    	// Overridden by subclasses
	}
    protected void handleMouseReleasedEvent(MouseEvent e)
    {
    	// do nothing
    	// Overridden by subclasses
	}
    
    private class EditorMouseAdapter extends MouseAdapter
    {
        public void mousePressed( MouseEvent e )
        {
            if ( e.getButton() == ma.button )
                return;

            // this just lets us return, and always repaint
            mousePressed_( e );
        }

        public void mousePressed_( MouseEvent e )
        {
//tag            draggedVertex = null;
        	Vertex newlySelectedVertex = null;
            //selectedVertex = null;
//            draggingVertex = false;

            Point loc = new Point( (int) ma.fromX( e.getPoint().x ), (int) ma.fromY( e.getPoint().y ) );
            
            Point2d ept = new Point2d( ma.fromX( e.getPoint().x ), ma.fromY( e.getPoint().y ) );
            
            for (Vertex v : graph.vertexSet()) {
                // to screenspace!
                double dist = ma.to( v.getPoint() ).distanceSq( e.getPoint() );
                if ( dist < 100 )
                {
                	// if v is closer to the location of the mouse press than v,
                	// then update
                    if ( newlySelectedVertex == null
                    || dist <= newlySelectedVertex.getPoint().distance( ept ) )
                    	newlySelectedVertex = v;
//tag                    draggedVertex = v;
                }
            }
            
            if ( newlySelectedVertex != null )
            {
                if ( (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) > 0 )
                {
                    if (!weld( newlySelectedVertex )) {
                    	graph.removeVertex(newlySelectedVertex);
                    }
                    selectedVertex = null;
                    return;
                }
                else if ( draggingVertex )
                {
                    repeat( ept, e );
                    return;
                }
                else if ( (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) > 0 )
                {
                	// if a vertex was also selected at the time,
                	// then he also wants to create an edge
                	if ( selectedVertex != null && selectedVertex != newlySelectedVertex )
                	{
                		graph.addEdge(selectedVertex,newlySelectedVertex);
                		return;
                	}
                }
                else
                {
                	// the user wants to drag the vertex
                	selectedVertex = newlySelectedVertex;
                	draggingVertex = true;
                	repaint();
                    return;
                }
            }

            DefaultEdge selected = null;

            selected = getNearestEdge( ept, ma.fromZoom( 10 ), graph );

            if ( selected != null )
            {
                // an edge has been selected - are we adding a point or selecting the edge
                if ( (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) > 0 )
                {
                    addBetween( selected, loc );
                    return;
                }
                else if ( (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) > 0 )
                {
               		currentEdge = null;
                	graph.removeEdge(selected);
                }
                else
                {
                    edgeSelected.edgeSelected( currentEdge = selected );
                    dragEdgeStartPoint = new Point2d();
                    dragEdgeStartPoint.set( ept );
                    Point2d selectedSource = graph.getEdgeSource(selected).getPoint();
                    draggedEdgeSource.set(selectedSource.x,selectedSource.y);
                    Point2d selectedTarget = graph.getEdgeTarget(selected).getPoint();
                    draggedEdgeTarget.set(selectedTarget.x,selectedTarget.y);
                }
            }
            else
            {
            	if ( (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) > 0 )
                {
            		// the user wants to add a vertex to the graph
            		Vertex newVertex = new Vertex(ept);
                	graph.addVertex(newVertex);
                	// if a vertex was also selected at the time,
                	// then he also wants to create an edge
                	if ( selectedVertex != null )
                	{
                		graph.addEdge(selectedVertex,newVertex);
                	}
            		selectedVertex = newVertex;
                    return;
                }
            }

           	selectedVertex = null;
           	repaint();

            
            // no edge selected, how about entire loops
/*
             for ( Loop<Bar> loop : graph )

                if ( containsLoop( graph, ept ) )
                    draggedBarSet = loop;

            if ( draggedBarSet != null )
                return;


            if ( e.getButton() == MouseEvent.BUTTON1 && e.isControlDown() )
            {
                createSection( loc, contains( graph, ept ) );
                repaint();
                return;
            }
*/
            //painting( ept, new Point2d( 0, 0 ), e ); // this is a new-ish call!
            
           	handleMousePressedEvent(e);
        }

		public void repeat( Point2d e, MouseEvent evt )
        {
            if ( selectedVertex != null && draggingVertex )
            {
                if ( doSnap() && (evt.getModifiersEx() & MouseEvent.ALT_DOWN_MASK) > 0)
                {
                    e = doSnap(selectedVertex, e);
                }
              	movePoint(selectedVertex, e, evt );
               	repaint();
            }
        }

        @Override
        public void mouseDragged( MouseEvent e )
        {
            if (e.getButton() == ma.button)
                return;

            if ( dragEdgeStartPoint != null )
            {
            	moveEdge(e);
            }
            
            Point2d loc = ma.from( e.getPoint() );
            repeat( loc, e );
            
            handleMouseDraggedEvent(e);
        }

        @Override
        public void mouseReleased( MouseEvent e )
        {
            painting(null,null, e);

            if (e.getButton() == ma.button)
                return;
            
            if (selectedVertex != null)
                releasePoint (selectedVertex, e );
            draggingVertex = false;
            
            dragEdgeStartPoint = null;
            
            
            handleMouseReleasedEvent(e);
        }
    }

    public interface EdgeSelected
    {
        public void edgeSelected(DefaultEdge edge);
    }
    
    public EdgeSelected edgeSelected = new EdgeSelected()
    {
        public void edgeSelected(DefaultEdge edge)
        {
            return;
        }
    };

    @Override
    public String getToolTipText( MouseEvent event )
    {
        return ( ma.fromX( event.getX() ) + "," + ma.fromY( event.getY() ) );
    }
}
