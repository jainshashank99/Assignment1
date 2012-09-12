
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
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.PrefuseLib;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;

public class Political_blogs {

	private static Graph graph;//Graph in which the main data will be stored
	private static Visualization vis;//visualization the created data
	private static Display d;//display on which vis is added

	public static void main(String[] args) throws IOException {
		setUpData();//called to create graph by reading given file
		setUpVisualization();//creating visualization of created graph
		setUpRenderers();//render option for nodes
		setUpActions();//coloring and layout actions performed
		setUpDisplay();//Display created, vis added and controls added

		//creating a frame on which display will be shown
		JFrame frame = new JFrame("Political Books");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(d);
		frame.pack();
		frame.setVisible(true);
		vis.run("color");
		vis.run("layout");
	}

	public static void setUpData() throws IOException {
		//creating table in which graph will be stored 
		graph = new Graph(true);
		graph.addColumn("id", Integer.class);
		graph.addColumn("label", String.class);
		graph.addColumn("value", Integer.class);
		graph.addColumn("source", String.class);

		//reading file from polblogs using Filereader and Buffered Reader
		FileInputStream fstream = new FileInputStream("polblogslhjk.gml");
		DataInputStream data = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(data));
		br.readLine();
		br.readLine();
		br.readLine();
		String s = br.readLine();
		//adding nodes
		while (s.equals("  node [")) {
			Node n = graph.addNode();
			Scanner sc = new Scanner(br.readLine());
			sc.next();
			n.set("id", sc.nextInt());
			String[] second_line = br.readLine().split("[\"]");
			n.set("label", second_line[1]);
			sc = new Scanner(br.readLine());
			sc.next();
			n.set("value", sc.nextInt());
			String[] fourth_line = br.readLine().split("[\"]");
			n.set("source", fourth_line[1]);
			br.readLine();
			s = br.readLine();
		}
		//adding edges
		while (s.equals("  edge [")) {
			Scanner sc = new Scanner(br.readLine());
			sc.next();
			int first = sc.nextInt();
			sc = new Scanner(br.readLine());
			sc.next();
			int second = sc.nextInt();
			graph.addEdge(first-1, second-1);
			br.readLine();
			s = br.readLine();
		}

	}

	// -- 2. the visualization --------------------------------------------
	public static void setUpVisualization() {
		vis = new Visualization();
		vis.add("graph", graph);
	}

	// -- 3. the renderers and renderer factory ---------------------------
	private static void setUpRenderers() {
		//to give circular shape to the nodes
		FinalRenderer r = new FinalRenderer();
		DefaultRendererFactory drf = new DefaultRendererFactory(r);
		vis.setRendererFactory(drf);
		
	}

	public static void setUpActions() {
		
		int[] palette = { ColorLib.rgb(255, 150, 150),ColorLib.rgb(150, 255, 150) };
		//coloring on basis of whether the node is liberal or conservative
		DataColorAction fill = new DataColorAction("graph.nodes", "value",Constants.NOMINAL, VisualItem.FILLCOLOR, palette);
		//adding colors for highlighting
		fill.add(VisualItem.FIXED, ColorLib.rgb(200,0,0));
        fill.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255,200,125));
        //coloring the edges
		ColorAction edges = new ColorAction("graph.edges",VisualItem.STROKECOLOR, ColorLib.gray(200));
		
		//creating actionlist for adding color actions
		ActionList color = new ActionList();
		color.add(fill);
		color.add(edges);
		long b = 20000;
		//creating action list for adding layout actions
		ActionList layout = new ActionList(b);
		//Force Directed layout given to Political blogs to see clustering
		layout.add(new ForceDirectedLayout("graph", false));
		layout.add(fill);
		layout.add(new RepaintAction());
		
		//ading actions to visualisation
		vis.putAction("color", color);
		vis.putAction("layout", layout);
		
	}

	public static void setUpDisplay() {
		//vis added to display
		d = new Display(vis);
		d.setSize(1366, 768);//setting size
		d.pan(550, 350);//making the origin of the graph somewhere in the middle of the screen
		d.setForeground(Color.GRAY);
        d.setBackground(Color.WHITE);
		
        //different controls added to display
		d.addControlListener(new DragControl());
		d.addControlListener(new PanControl());
		d.addControlListener(new ZoomControl());		
		d.addControlListener(new FocusControl(1));
        d.addControlListener(new WheelZoomControl());
        d.addControlListener(new ZoomToFitControl());
        d.addControlListener(new NeighborHighlightControl());
	}

}

