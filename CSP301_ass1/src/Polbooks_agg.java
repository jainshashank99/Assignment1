import java.awt.Color;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import javax.swing.JFrame;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.RandomLayout;
import prefuse.activity.Activity;
import prefuse.controls.DragControl;
import prefuse.controls.FocusControl;
import prefuse.controls.NeighborHighlightControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.render.DefaultRendererFactory;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;

public class Polbooks_agg {
	private static Graph graph;
	private static Visualization vis;
	private static Display display;

	public static void main(String[] argv) throws IOException {
		setUpData();
		setUpVisualization();
		setUpRenderers();
		setUpActions();
		setUpDisplay();
		// The following is standard java.awt.
		// A JFrame is the basic window element in awt.
		// It has a menu (minimize, maximize, close) and can hold
		// other gui elements.
		// Create a new window to hold the visualization.
		// We pass the text value to be displayed in the menubar to the
		// constructor.
		JFrame frame = new JFrame("prefuse example");
		// Ensure application exits when window is closed
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// The Display object (d) is a subclass of JComponent, which
		// can be added to JFrames with the add method.
		frame.add(display);
		// Prepares the window.
		frame.pack();
		// Shows the window.
		frame.setVisible(true);
		// We have to start the ActionLists that we added to the visualization
		vis.run("color");
		vis.run("layout");
	}

	private static void setUpData() throws IOException {
		graph = new Graph();
		graph.addColumn("id", Integer.class);
		graph.addColumn("label", String.class);
		graph.addColumn("value", String.class);
		graph.addColumn("degree", Integer.class);

		FileInputStream fstream = new FileInputStream("polbooks.gml");
		DataInputStream data = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(data));

		br.readLine();
		br.readLine();
		br.readLine();
		br.readLine();
		String s = br.readLine();
		while (s.equals("  node")) {
			Node n = graph.addNode();
			br.readLine();
			Scanner sc = new Scanner(br.readLine());
			sc.next();
			n.set("id", sc.nextInt());
			String[] second_line = br.readLine().split("[\"]");
			n.set("label", second_line[1]);
			String[] third_line = br.readLine().split("[\"]");
			n.set("value", third_line[1]);
			br.readLine();
			s = br.readLine();
			sc.close();
		}
		while (s.equals("  edge")) {
			br.readLine();
			Scanner sc = new Scanner(br.readLine());
			sc.next();
			int first = sc.nextInt();
			sc.close();
			sc = new Scanner(br.readLine());
			sc.next();
			int second = sc.nextInt();
			graph.addEdge(first, second);
			br.readLine();
			s = br.readLine();
			sc.close();
		}
		br.close();
		
		for(int i=0;i<graph.getNodeCount();i++) {
			Node n = graph.getNode(i);
			n.set("degree",n.getDegree());
		}

	}

	public static void setUpVisualization() {
		// -- 2. the visualization ------------------------------------------
		// We must first creat the Visualization object.
		vis = new Visualization();
		// Now we add our previously created Graph object to the visualization.
		// The graph gets a textual label so that we can refer to it later
		// on.
		vis.add("graph", graph);
	}

	public static void setUpRenderers() {
		// -- 3. the renderers and renderer factory -------------------------
		// Create a default ShapeRenderer
		FinalRenderer_books r = new FinalRenderer_books();
		// create a new DefaultRendererFactory
		// This Factory will use the ShapeRenderer for all nodes.
		vis.setRendererFactory(new DefaultRendererFactory(r));
	}

	public static void setUpActions() {
		int[] palette = { ColorLib.rgb(244,164,96),ColorLib.rgb(0,191,255), ColorLib.rgb(0,255,0) };
		int[] palette2 = { ColorLib.rgb(255,0,0),ColorLib.rgb(0,0,255), ColorLib.rgb(0,100,0) };
		DataColorAction fill = new DataColorAction("graph.nodes", "value",
									Constants.NOMINAL, VisualItem.FILLCOLOR, palette);
		ColorAction nStroke = new ColorAction("graph.nodes", VisualItem.STROKECOLOR);
        nStroke.setDefaultColor(ColorLib.gray(100));
		DataColorAction fill2 = new DataColorAction("graph.nodes", "value",
									Constants.NOMINAL, VisualItem.FILLCOLOR, palette2);
		fill.add(VisualItem.FIXED, ColorLib.rgb(139,137,112));
		fill.add(VisualItem.HIGHLIGHT, fill2);
		
		ColorAction edges = new ColorAction("graph.edges",
				VisualItem.STROKECOLOR, ColorLib.gray(200));
		edges.add(VisualItem.HIGHLIGHT, ColorLib.rgb(0,0,0));

		ActionList draw = new ActionList();
		draw.add(fill);
		draw.add(nStroke);
		draw.add(new ColorAction("graph.nodes", VisualItem.TEXTCOLOR, ColorLib.rgb(0,0,0)));
		draw.add(edges);

		ActionList animate = new ActionList();
		animate.add(new FinalDecoratorLayout("nodedec"));
		animate.add(new RandomLayout("graph"));
		animate.add(fill);
		animate.add(edges);
		animate.add(new RepaintAction());

		// finally, we register our ActionList with the Visualization.
		// we can later execute our Actions by invoking a method on our
		// Visualization, using the name we've chosen below.
		vis.putAction("draw", draw);
		vis.putAction("layout", animate);
		vis.runAfter("draw", "layout");
	}

	public static void setUpDisplay() {
		display = new Display(vis);
		display.setSize(1366, 768);
		display.pan(550, 350);
		display.setForeground(Color.GRAY);
		display.setBackground(Color.WHITE);

		// main display controls
		display.addControlListener(new FocusControl(1));
		display.addControlListener(new DragControl());
		display.addControlListener(new PanControl());
		display.addControlListener(new ZoomControl());
		display.addControlListener(new FinalControlListener());
		display.addControlListener(new WheelZoomControl());
		display.addControlListener(new ZoomToFitControl());
		display.addControlListener(new NeighborHighlightControl());

		display.setForeground(Color.GRAY);
		display.setBackground(Color.DARK_GRAY);
	}
}