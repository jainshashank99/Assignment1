import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.filter.GraphDistanceFilter;
import prefuse.action.layout.CircleLayout;
import prefuse.action.layout.RandomLayout;
import prefuse.action.layout.graph.ForceDirectedLayout;
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
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.io.CSVTableReader;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.PrefuseLib;
import prefuse.util.force.ForceSimulator;
import prefuse.util.ui.JForcePanel;
import prefuse.util.ui.JValueSlider;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.data.io.TableReader;

public class RandomPoliticalBooks {
	private static Graph graph;
	private static Random rand;
	private static Visualization vis;
	private static Display d;

	public static void main(String[] argv) throws IOException {
		setUpData();
		setUpVisualization();
		setUpRenderers();
		setUpActions();
		setUpDisplay();
		
		JFrame frame = new JFrame("Political Books");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(d);
		frame.pack();
		frame.setVisible(true);
		vis.run("color");
		vis.run("layout");
	}
	
	public static void setUpData() throws IOException{
		graph = new Graph();
		graph.addColumn("id", Integer.class);
		graph.addColumn("label", String.class);
		graph.addColumn("value", String.class);
		
		FileInputStream fstream = new FileInputStream("polbooks.gml");
        DataInputStream data = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(data));
        
        br.readLine();
        br.readLine();
        br.readLine();
		br.readLine();
		String s = br.readLine();
		while(s.equals("  node")){
			Node n = graph.addNode();
			br.readLine();
			Scanner sc = new Scanner(br.readLine());
			sc.next();
			n.set("id",sc.nextInt());
			String[] second_line = br.readLine().split("[\"]");
			n.set("label", second_line[1]);
			String[] third_line = br.readLine().split("[\"]");
			n.set("value", third_line[1]);
			br.readLine();
			s=br.readLine();
		}
		Random rand = new Random();
		for(int i = 0; i < 441; i++)
		{
			int first = rand.nextInt(105);
			int second = rand.nextInt(105);
			graph.addEdge(first, second);
		}
        
	}

	// -- 2. the visualization --------------------------------------------
	public static void setUpVisualization() {
		vis = new Visualization();
		vis.add("graph", graph);
	}

	// -- 3. the renderers and renderer factory ---------------------------
	private static void setUpRenderers() {
		FinalRenderer r = new FinalRenderer();
		DefaultRendererFactory drf = new DefaultRendererFactory(r);
		drf.add(new InGroupPredicate("nodedec"), new LabelRenderer("id"));
		vis.setRendererFactory(drf);
		final Schema DECORATOR_SCHEMA = PrefuseLib.getVisualItemSchema();
		DECORATOR_SCHEMA.setDefault(VisualItem.INTERACTIVE, false);
		DECORATOR_SCHEMA.setDefault(VisualItem.TEXTCOLOR,ColorLib.rgb(0, 0, 0));
		DECORATOR_SCHEMA.setDefault(VisualItem.FONT,FontLib.getFont("Tahoma", 10));
		vis.addDecorators("nodedec", "graph.nodes", DECORATOR_SCHEMA);
	}

	public static void setUpActions() {
		
		//int hops = 30;
        //final GraphDistanceFilter filter = new GraphDistanceFilter("graph", hops);
		
		int[] palette = { ColorLib.rgb(255, 150, 150), ColorLib.rgb(150, 150, 255),ColorLib.rgb(150, 255, 150) };
		DataColorAction fill = new DataColorAction("graph.nodes", "value",Constants.NOMINAL, VisualItem.FILLCOLOR, palette);
		fill.add(VisualItem.FIXED, ColorLib.rgb(200,0,0));
        fill.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255,200,125));
		ColorAction edges = new ColorAction("graph.edges",VisualItem.STROKECOLOR, ColorLib.gray(200));
		
		ActionList color = new ActionList();
		//color.add(filter);
		color.add(fill);
		color.add(edges);
		
		ActionList layout = new ActionList(Activity.INFINITY);
		layout.add(new FinalDecoratorLayout("nodedec"));
		layout.add(new ForceDirectedLayout("graph", true));
		layout.add(fill);
		layout.add(new RepaintAction());
		
		vis.putAction("color", color);
		vis.putAction("layout", layout);
		//vis.runAfter("color", "layout");
		
		/*ForceSimulator fsim = ((ForceDirectedLayout) layout.get(0)).getForceSimulator();
        JForcePanel fpanel = new JForcePanel(fsim);
        
        final JValueSlider slider = new JValueSlider("Distance", 0, hops, hops);
        slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                filter.setDistance(slider.getValue().intValue());
                vis.run("color");
            }
        });
        slider.setBackground(Color.WHITE);
        slider.setPreferredSize(new Dimension(300,30));
        slider.setMaximumSize(new Dimension(300,30));
        
        Box cf = new Box(BoxLayout.Y_AXIS);
        cf.add(slider);
        cf.setBorder(BorderFactory.createTitledBorder("Connectivity Filter"));
        fpanel.add(cf);

        //fpanel.add(opanel);
        
        fpanel.add(Box.createVerticalGlue());
        
        // create a new JSplitPane to present the interface
        JSplitPane split = new JSplitPane();
        split.setLeftComponent(d);
        //split.setRightComponent(fpanel);
        split.setOneTouchExpandable(true);
        split.setContinuousLayout(false);
        split.setDividerLocation(700);
        
        // now we run our action list
        //vis.run("color");
        
        //add(split);*/
	}

	public static void setUpDisplay() {
		d = new Display(vis);
		d.setSize(700, 700);
		d.pan(350, 350);
		d.setForeground(Color.GRAY);
        d.setBackground(Color.WHITE);
		// We use the addControlListener method to set up interaction.
		// The DragControl is a built in class for manually moving
		// nodes with the mouse.
		d.addControlListener(new DragControl());
		// Pan with left-click drag on background
		d.addControlListener(new PanControl());
		// Zoom with right-click drag
		d.addControlListener(new ZoomControl());
		d.addControlListener(new FinalControlListener());
		
		d.addControlListener(new FocusControl(1));
        d.addControlListener(new WheelZoomControl());
        d.addControlListener(new ZoomToFitControl());
        d.addControlListener(new NeighborHighlightControl());
	}
}