
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


public class Political_Books extends JPanel {

	private static final String nodes = "graph.nodes";
	private static final String edges = "graph.edges";
	private static Graph graph;//graph in which main data is stored

	private Visualization m_vis;//visualisation od graph

	public Political_Books(Graph g, String label) {
		super(new BorderLayout());

		// create a new, empty visualization for our data
		m_vis = new Visualization();

		
		// --------------------------------------------------------------------
		// set up the renderers
		LabelRenderer tr = new LabelRenderer("id");
		tr.setRoundedCorner(8, 8);
		m_vis.setRendererFactory(new DefaultRendererFactory(tr));

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

		int hops = 10;
		final GraphDistanceFilter filter = new GraphDistanceFilter(label, hops);

		//action for coloring the nodes created
		int[] palette = { ColorLib.rgb(244,164,96),ColorLib.rgb(0,191,255), ColorLib.rgb(0,255,0) };
		int[] palette2 = { ColorLib.rgb(255,0,0),ColorLib.rgb(0,0,255), ColorLib.rgb(0,100,0) };
		DataColorAction fill = new DataColorAction("graph.nodes", "value",
									Constants.NOMINAL, VisualItem.FILLCOLOR, palette);
		DataColorAction fill2 = new DataColorAction("graph.nodes", "value",
									Constants.NOMINAL, VisualItem.FILLCOLOR, palette2);
		//color of fixed nodes decided
		fill.add(VisualItem.FIXED, ColorLib.rgb(139,137,112));
		//color of highlighted neighbours decided
		fill.add(VisualItem.HIGHLIGHT, fill2);
		
		//color of edges decided
		ColorAction edges = new ColorAction("graph.edges",
				VisualItem.STROKECOLOR, ColorLib.gray(200));
		edges.add(VisualItem.HIGHLIGHT, ColorLib.rgb(0,0,0));

		//actionlist for coloring actions created
		ActionList draw = new ActionList();
		draw.add(filter);
		draw.add(fill);
		draw.add(new ColorAction(nodes, VisualItem.TEXTCOLOR, ColorLib.rgb(0,0,0)));
		draw.add(edges);

		//action list for layout added
		ActionList animate = new ActionList(Activity.INFINITY);
		//id written on the nodes
		animate.add(new FinalDecoratorLayout("nodedec"));
		//Foece directed layout given to the graph to see clustering
		animate.add(new ForceDirectedLayout("graph", false));
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
		display.setSize(1366, 768);//size of display given
		display.pan(550, 350);//origin of graph shifted
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

		// overview display, a small visualization in side panel
		 Display overview = new Display(m_vis);//vis added to display
		 overview.setSize(290,290);///size given
		 overview.addItemBoundsListener(new FitOverviewListener());

		display.setForeground(Color.GRAY);
		display.setBackground(Color.WHITE);

		// --------------------------------------------------------------------
		// launch the visualization

		// create a panel for editing force values
		ForceSimulator fsim = ((ForceDirectedLayout) animate.get(1)).getForceSimulator();
		JForcePanel fpanel = new JForcePanel(fsim);

		JPanel opanel = new JPanel();
		opanel.setBorder(BorderFactory.createTitledBorder("Overview"));
		opanel.setBackground(Color.WHITE);
		opanel.add(overview);

		//slider added to see nodes at given distance from the selected node
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

		//slider of distance added to fpanel
		Box cf = new Box(BoxLayout.Y_AXIS);//box of slider created
		cf.add(slider);
		cf.setBorder(BorderFactory.createTitledBorder("Connectivity Filter"));
		fpanel.add(cf);

		fpanel.add(opanel);

		fpanel.add(Box.createVerticalGlue());

		// create a new JSplitPane to present the interface
		JSplitPane split = new JSplitPane();
		split.setLeftComponent(display);//left part is main display
		split.setRightComponent(fpanel);//right part is fpanel which contains different controls
		split.setOneTouchExpandable(true);
		split.setContinuousLayout(false);
		split.setDividerLocation(1000);//divider location set

		// now we run our action list
		m_vis.run("draw");//coloring actions called on visualization 

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

		//sets data in the graph
		setUpData();
		//frame created and displayed
		JFrame frame = demo(graph, "graph");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public static void setUpData() throws IOException {
		//columns in graph added
		graph = new Graph();
		graph.addColumn("id", Integer.class);
		graph.addColumn("label", String.class);
		graph.addColumn("value", String.class);

		//file read using Filereader and BufferedReader
		FileInputStream fstream = new FileInputStream("polbooks.gml");
		DataInputStream data = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(data));

		br.readLine();
		br.readLine();
		br.readLine();
		br.readLine();
		String s = br.readLine();
		//nodes added
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
		//edges added
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

	}

	public static JFrame demo(Graph g, String label) {
		final Political_Books view = new Political_Books(g, label);

		JFrame frame = new JFrame("Political Books");
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