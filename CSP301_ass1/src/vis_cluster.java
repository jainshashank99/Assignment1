

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Stack;

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
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.DragControl;
import prefuse.controls.FocusControl;
import prefuse.controls.NeighborHighlightControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.controls.ZoomingPanControl;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.tuple.TupleSet;
import prefuse.demos.GraphView.FitOverviewListener;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.force.ForceSimulator;
import prefuse.util.ui.JForcePanel;
import prefuse.util.ui.JValueSlider;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;


public class vis_cluster extends JPanel {

	private static final String nodes = "graph.nodes";
	private static final String edges = "graph.edges";
	private static Graph graph;
	private static Graph graph2;
	private static Stack stack;

	private Visualization m_vis;

	public vis_cluster(Graph g, String label) {
		super(new BorderLayout());

		// create a new, empty visualization for our data
		m_vis = new Visualization();

		// --------------------------------------------------------------------
		// set up the renderers
		FinalRenderer r = new FinalRenderer();
		DefaultRendererFactory drf = new DefaultRendererFactory(r);
		m_vis.setRendererFactory(drf);
		
		// --------------------------------------------------------------------
		// register the data with a visualization

		// adds graph to visualization
		setGraph(g, label);

		// fix selected focus nodes
		TupleSet focusGroup = m_vis.getGroup(Visualization.FOCUS_ITEMS);
		focusGroup.addTupleSetListener(new TupleSetListener() {
			public void tupleSetChanged(TupleSet ts, Tuple[] add, Tuple[] rem) {
				for (int i = 0; i < rem.length; ++i)
					((VisualItem) rem[i]).setFixed(false);
				for (int i = 0; i < add.length; ++i) {
					((VisualItem) add[i]).setFixed(false);
					((VisualItem) add[i]).setFixed(true);
				}
				if (ts.getTupleCount() == 0) {
					ts.addTuple(rem[0]);
					((VisualItem) rem[0]).setFixed(false);
				}
				m_vis.run("draw");
			}
		});

		// --------------------------------------------------------------------
		// create actions to process the visual data
		int hops = 15;
		final GraphDistanceFilter filter = new GraphDistanceFilter(label, hops);

		int[] palette = { ColorLib.rgb(255, 150, 150),ColorLib.rgb(150, 255, 150) };
		DataColorAction fill = new DataColorAction("graph2.nodes", "value",
									Constants.NOMINAL, VisualItem.FILLCOLOR, palette);
		fill.add(VisualItem.FIXED, ColorLib.rgb(200, 0, 0));
		fill.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255, 200, 125));
		
		ColorAction edges = new ColorAction("graph2.edges",
				VisualItem.STROKECOLOR, ColorLib.gray(200));
		edges.add(VisualItem.HIGHLIGHT, ColorLib.rgb(0,0,0));

		ActionList draw = new ActionList();
		draw.add(filter);
		draw.add(fill);
		draw.add(edges);

		ActionList animate = new ActionList(Activity.INFINITY);
		animate.add(new ForceDirectedLayout("graph2", false));
		animate.add(fill);
		animate.add(edges);
		animate.add(new RepaintAction());

		// finally, we register our ActionList with the Visualization.
		// we can later execute our Actions by invoking a method on our
		// Visualization, using the name we've chosen below.
		m_vis.putAction("draw", draw);
		m_vis.putAction("layout", animate);
		m_vis.runAfter("draw", "layout");

		
		// --------------------------------------------------------------------
		// set up a display to show the visualization

		Display display = new Display(m_vis);
		display.setSize(1366, 768);
		display.pan(550, 350);
		display.setForeground(Color.GRAY);
		display.setBackground(Color.WHITE);

		// main display controls
		display.addControlListener(new FocusControl(1));
		display.addControlListener(new DragControl());
		display.addControlListener(new PanControl());
		display.addControlListener(new ZoomControl());
		display.addControlListener(new FinalControlListener_edited());
		display.addControlListener(new WheelZoomControl());
		display.addControlListener(new ZoomToFitControl());
		display.addControlListener(new NeighborHighlightControl());

		// overview display
		 Display overview = new Display(m_vis);
		 overview.setSize(290,290);
		 overview.addItemBoundsListener(new FitOverviewListener());

		display.setForeground(Color.GRAY);
		display.setBackground(Color.WHITE);

		
		// --------------------------------------------------------------------
		// launch the visualization

		// create a panel for editing force values
		ForceSimulator fsim = ((ForceDirectedLayout) animate.get(0)).getForceSimulator();
		JForcePanel fpanel = new JForcePanel(fsim);

		JPanel opanel = new JPanel();
		opanel.setBorder(BorderFactory.createTitledBorder("Overview"));
		opanel.setBackground(Color.WHITE);
		opanel.add(overview);

		final JValueSlider slider = new JValueSlider("Distance", 0, hops, hops);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				filter.setDistance(slider.getValue().intValue());
				m_vis.run("draw");
			}
		});
		slider.setBackground(Color.WHITE);
		slider.setPreferredSize(new Dimension(300, 30));
		slider.setMaximumSize(new Dimension(300, 30));

		Box cf = new Box(BoxLayout.Y_AXIS);
		cf.add(slider);
		cf.setBorder(BorderFactory.createTitledBorder("Connectivity Filter"));
		fpanel.add(cf);

		fpanel.add(opanel);

		fpanel.add(Box.createVerticalGlue());

		// create a new JSplitPane to present the interface
		JSplitPane split = new JSplitPane();
		split.setLeftComponent(display);
		split.setRightComponent(fpanel);
		split.setOneTouchExpandable(true);
		split.setContinuousLayout(false);
		split.setDividerLocation(1000);

		// now we run our action list
		m_vis.run("draw");

		add(split);
	}

	public void setGraph(Graph g, String label) {
		// update graph
		m_vis.removeGroup(label);
		VisualGraph vg = m_vis.addGraph(label, g);
		m_vis.setValue(edges, null, VisualItem.INTERACTIVE, Boolean.FALSE);
		VisualItem f = (VisualItem) vg.getNode(0);
		m_vis.getGroup(Visualization.FOCUS_ITEMS).setTuple(f);
		f.setFixed(false);
	}

	
	// ------------------------------------------------------------------------
	// Main and demo methods

	public static void main(String[] args) throws IOException {
		UILib.setPlatformLookAndFeel();

		//sets the data in the graph
		setUpData();
		JFrame frame = demo(graph2, "graph2");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public static void setUpData() throws IOException {
		graph = new Graph(true);
		graph.addColumn("id", Integer.class);
		graph.addColumn("color", String.class);
		graph.addColumn("label", String.class);
		graph.addColumn("value", Integer.class);
		graph.addColumn("source", String.class);
		graph.addColumn("correspond", Integer.class);

		FileInputStream fstream = new FileInputStream("polblogs.gml");
		DataInputStream data = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(data));
		br.readLine();
		br.readLine();
		br.readLine();
		String s = br.readLine();
		while (s.equals("  node [")) {
			Node n = graph.addNode();
			n.set("color", "white");
			Scanner sc = new Scanner(br.readLine());
			sc.next();
			n.set("id", sc.nextInt());
			String[] second_line = br.readLine().split("[\"]");
			n.set("label", second_line[1]);
			sc.close();
			sc = new Scanner(br.readLine());
			sc.next();
			n.set("value", sc.nextInt());
			String[] fourth_line = br.readLine().split("[\"]");
			n.set("source", fourth_line[1]);
			br.readLine();
			s = br.readLine();
			sc.close();
		}
		while (s.equals("  edge [")) {
			Scanner sc = new Scanner(br.readLine());
			sc.next();
			int first = sc.nextInt();
			sc.close();
			sc = new Scanner(br.readLine());
			sc.next();
			int second = sc.nextInt();
			graph.addEdge(first - 1, second - 1);
			br.readLine();
			s = br.readLine();
			sc.close();
		}
		br.close();
		int tot_nodes = graph.getNodeCount();

		stack = new Stack();

		for (int i = 0; i < tot_nodes; i++) {
			Node observe = graph.getNode(i);
			if (observe.get(1).equals("white")) {
				DFS(observe);
			}
		}

		Iterator it = graph.edges();
		while (it.hasNext()) {
			Edge e = (Edge) it.next();
			Node source = e.getSourceNode();
			Node target = e.getTargetNode();
			graph.removeEdge(e);
			graph.addEdge(target, source);
		}

		graph2 = new Graph();
		graph2.addColumn("id", Integer.class);
		graph2.addColumn("label", String.class);
		graph2.addColumn("value", Integer.class);
		graph2.addColumn("source", String.class);
		while (!(stack.isEmpty())) {
			Node observe = (Node) stack.pop();
			if (observe.get(1).equals("black")) {
				if ((int) observe.get(0) == 1) {
					Node n2 = graph2.addNode();
					n2.set("id", observe.get(0));
					n2.set("label", observe.get(2));
					n2.set("value", observe.get(3));
					n2.set("source", observe.get(4));
					observe.set("correspond", n2.getRow());
					DFS3(observe, n2);
				} else {
					DFS2(observe);
				}
			}
		}

	}

	public static void DFS(Node observe) {
		observe.set(1, "gray");

		Iterator it = observe.outNeighbors();
		while (it.hasNext()) {
			Node neighbour = (Node) it.next();
			if (neighbour.get(1).equals("white")) {
				DFS(neighbour);
			}
		}
		observe.set(1, "black");
		stack.push(observe);
		return;

	}

	public static void DFS2(Node observe) {
		observe.set(1, "gray");

		Iterator it = observe.outNeighbors();
		while (it.hasNext()) {
			Node neighbour = (Node) it.next();
			if (neighbour.get(1).equals("black")) {
				DFS2(neighbour);
			}
		}
		observe.set(1, "tatti");
		return;

	}

	public static void DFS3(Node observe, Node n) {
		observe.set(1, "gray");
		Iterator it = observe.outNeighbors();
		while (it.hasNext()) {
			Node neighbour = (Node) it.next();
			if (neighbour.get(1).equals("black")) {
				Node n2 = graph2.addNode();
				n2.set("id", neighbour.get(0));
				n2.set("label", neighbour.get(2));
				n2.set("value", neighbour.get(3));
				n2.set("source", neighbour.get(4));
				neighbour.set("correspond", n2.getRow());
				graph2.addEdge(n2, n);
				DFS3(neighbour, n2);
			} else if (neighbour.get(1).equals("white")
					|| neighbour.get(1).equals("gray")) {
				Node n2 = graph2.getNode((int) neighbour.get("correspond"));
				graph2.addEdge(n2, n);
			}
		}
		observe.set(1, "white");
		return;
	}

	public static JFrame demo(Graph g, String label) {
		final vis_cluster view = new vis_cluster(g, label);

		JFrame frame = new JFrame("Political Blogs - Strongly Connected Component");
		frame.setContentPane(view);
		frame.pack();
		frame.setVisible(true);

		frame.addWindowListener(new WindowAdapter() {
			public void windowActivated(WindowEvent e) {
				view.m_vis.run("layout");
			}

			public void windowDeactivated(WindowEvent e) {
				view.m_vis.cancel("layout");
			}
		});

		return frame;
	}
}