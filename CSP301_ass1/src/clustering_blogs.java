import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Stack;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

//finds strongly connected components
public class clustering_blogs {

	private static Graph graph;
	private static Stack stack;

	public static void main(String[] args) throws IOException {
		setUpData();

	}

	public static void setUpData() throws IOException {
		graph = new Graph(true);
		graph.addColumn("id", Integer.class);
		graph.addColumn("color", String.class);
		graph.addColumn("label", String.class);
		graph.addColumn("value", Integer.class);
		graph.addColumn("source", String.class);

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
		
		while (!(stack.isEmpty())) {
			Node observe = (Node) stack.pop();
			if (observe.get(1).equals("black")) {
				DFS2(observe);
				System.out.println();
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
		int indx = (int) observe.get(0);
		System.out.print(" " + indx);
		Iterator it = observe.outNeighbors();
		while (it.hasNext()) {
			Node neighbour = (Node) it.next();
			if (neighbour.get(1).equals("black")) {
				DFS2(neighbour);
			}
		}
		observe.set(1, "white");
		return;

	}
}

