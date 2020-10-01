// Originated as straightskeleton/debug/Main.java
// Modified by David Benjamin and Anthony Lee to:
// * Rewrite to use our graph editor
// * Change the UI to inclue are menu
package foldandcut.ui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import utils.DRectangle;
import utils.Pair;
import graph.Graph;


public class Main extends javax.swing.JFrame {
	private static final long serialVersionUID = -2443364264240874941L;
	
	private Action newAction;
	private Action openAction;
	private Action saveAction;
	private Action saveAsAction;
	private Action exportAction;
	private Action closeAction;
	
	private Action drawSkeletonAction;
	private Action drawPerpendicularsAction;
	
	private FoldAndCutGraphEditor graphEditor;
	private File file;
	
	private boolean unsaved = false;
	
	public Main(File file) {
		Graph g = null;
		DRectangle bounds = null;
		if (file != null) {
			try {
				Pair<Graph, DRectangle> p = Graph.loadFrom(new BufferedReader(new FileReader(file)));
				g = p.first();
				bounds = p.second();
				setFile(file);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, e.getLocalizedMessage(), "Open Error", JOptionPane.ERROR_MESSAGE);
				file = null;
			}
		}
		setFile(file);
		
    	initActions();
        initComponents();

        graphEditor = new FoldAndCutGraphEditor(g, bounds);
        setContentPane(graphEditor);
        graphEditor.setup();
        graphEditor.addListener(new FoldAndCutGraphEditor.Listener() {
			@Override
			public void graphChanged() {
				unsaved = true;
				updateTitle();
				if (saveAction != null)
					saveAction.setEnabled(unsaved);
			}
		});
	}

	/** Creates new form Main */
    public Main() {
    	this(null);
    }
    
    private void setFile(File file) {
    	this.file = file;
    	updateTitle();
    }
    
    private void save() {
    	assert file != null;	
    	try {
    		graphEditor.graph.saveTo(new BufferedWriter(new FileWriter(file)), graphEditor.paperBounds);
        	
        	unsaved = false;
    		updateTitle();
    		if (saveAction != null)
    			saveAction.setEnabled(unsaved);
    	} catch (IOException e) {
    		JOptionPane.showMessageDialog(this, e.getLocalizedMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
    	}
    }
    
    private void exportToPDF(File file) {
    	try {
    		graphEditor.exportToPDF(new BufferedOutputStream(new FileOutputStream(file)));
    	} catch (IOException e) {
    		JOptionPane.showMessageDialog(this, e.getLocalizedMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
    	}
    }
    
    private void updateTitle() {
    	String title = (file != null ? file.getName() : "<untitled>");
    	if (unsaved)
    		title = "*" + title;
    	setTitle(title);
    }
    
    @SuppressWarnings("serial")
	private void initActions() {
    	newAction = new AbstractAction("New") {
    		{
    			putValue(MNEMONIC_KEY, KeyEvent.VK_N);
    			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
    		}
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new Main().setVisible(true);
			}
		};
		openAction = new AbstractAction("Open...") {
    		{
    			putValue(MNEMONIC_KEY, KeyEvent.VK_O);
    			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
    		}
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser(file);
				fc.setFileFilter(new FileNameExtensionFilter("Fold-and-cut file (*.fnc)", "fnc"));
				int result = fc.showOpenDialog(Main.this);
				if (result == JFileChooser.APPROVE_OPTION) {
					new Main(fc.getSelectedFile()).setVisible(true);
					if (file == null && !unsaved)
						dispose();
				}
			}
		};
		saveAction = new AbstractAction("Save") {
    		{
    			putValue(MNEMONIC_KEY, KeyEvent.VK_S);
    			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
    		}
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (file == null) {
					JFileChooser fc = new JFileChooser(file);
					fc.setFileFilter(new FileNameExtensionFilter("Fold-and-cut file (*.fnc)", "fnc"));
					int result = fc.showSaveDialog(Main.this);
					if (result == JFileChooser.APPROVE_OPTION) {
						setFile(fc.getSelectedFile());
						save();
					}
				} else {
					save();
				}
			}
		};
		saveAction.setEnabled(unsaved);
		saveAsAction = new AbstractAction("Save As...") {
    		{
    			putValue(MNEMONIC_KEY, KeyEvent.VK_A);
    		}
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser(file);
				fc.setFileFilter(new FileNameExtensionFilter("Fold-and-cut file (*.fnc)", "fnc"));
				int result = fc.showSaveDialog(Main.this);
				if (result == JFileChooser.APPROVE_OPTION) {
					setFile(fc.getSelectedFile());
					save();
				}
			}
		};
		exportAction = new AbstractAction("Export To PDF...") {
    		{
    			putValue(MNEMONIC_KEY, KeyEvent.VK_E);
    		}
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser(file);
				fc.setFileFilter(new FileNameExtensionFilter("PDF file", "pdf"));
				int result = fc.showSaveDialog(Main.this);
				if (result == JFileChooser.APPROVE_OPTION) {
					exportToPDF(fc.getSelectedFile());
				}
			}
		};
		closeAction = new AbstractAction("Close") {
    		{
    			putValue(MNEMONIC_KEY, KeyEvent.VK_C);
    			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
    		}
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO: Prompt "as you sure blah blah" dialog if not saved.
				dispose();
			}
		};
		
		drawSkeletonAction = new AbstractAction("Draw Straight Skeleton") {
			{
    			putValue(MNEMONIC_KEY, KeyEvent.VK_S);
    			putValue(SELECTED_KEY, true);
    		}
			@Override
			public void actionPerformed(ActionEvent arg0) {
				putValue(SELECTED_KEY, (getValue(SELECTED_KEY) != null) ? null : true);
				drawPerpendicularsAction.setEnabled(getValue(SELECTED_KEY) != null);
				graphEditor.setDrawSkeleton(getValue(SELECTED_KEY) != null);
			}
		};
		drawPerpendicularsAction = new AbstractAction("Draw Perpendiculars") {
			{
    			putValue(MNEMONIC_KEY, KeyEvent.VK_P);
    			putValue(SELECTED_KEY, true);
    		}
			@Override
			public void actionPerformed(ActionEvent arg0) {
				putValue(SELECTED_KEY, (getValue(SELECTED_KEY) != null) ? null : true);
				graphEditor.setDrawPerpendiculars(getValue(SELECTED_KEY) != null);
			}
		};
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setJMenuBar(makeMenuBar());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 686, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 502, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private JMenuBar makeMenuBar() {
    	JMenuBar menuBar = new JMenuBar();
    	JMenu menu;
    	
    	// File menu
    	menu = new JMenu("File");
    	menu.setMnemonic(KeyEvent.VK_F);
    	menu.add(new JMenuItem(newAction));
    	menu.add(new JMenuItem(openAction));
    	menu.addSeparator();
    	menu.add(new JMenuItem(saveAction));
    	menu.add(new JMenuItem(saveAsAction));
    	menu.add(new JMenuItem(exportAction));
    	menu.addSeparator();
    	menu.add(new JMenuItem(closeAction));
    	menuBar.add(menu);
    	
    	// View menu
    	menu = new JMenu("View");
    	menu.setMnemonic(KeyEvent.VK_V);
    	menu.add(new JCheckBoxMenuItem(drawSkeletonAction));
    	menu.add(new JCheckBoxMenuItem(drawPerpendicularsAction));
    	menuBar.add(menu);
    	
    	return menuBar;
    }

    /**
    * @param args the command line arguments
    */
    public static void main(final String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
            	if (args.length < 1)
            		new Main().setVisible(true);
            	else
            		new Main(new File(args[0])).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
