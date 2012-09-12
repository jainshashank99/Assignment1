import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Scanner;

import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

public class blog_statistics {
	
	// getting the node of highest degree
	public static void generate(Graph graph) {
		int totnodes = graph.getNodeCount();
		Node n = graph.getNode(0);
		for (int i = 0; i < totnodes; i++) {
			Node m = graph.getNode(i);
			if (n.getDegree() < m.getDegree()) {
				if ((m.get(2).equals(1))) {
					n = m;
				}
			}
		}
		// System.out.println(l + " liberally " + n.getDegree());
	}

	public static void graphstat(Graph graph) {

		// To check if graph is directed or undirected
		System.out.println("Is graph directed? " + graph.isDirected());

		// Total number of nodes
		int tot_nodes = graph.getNodeCount();
		System.out.println("\nTotal number of BLOGS (nodes): " + tot_nodes);

		// Number of different types of books
		int[] g = new int[2];
		for (int m = 0; m < tot_nodes; m++) {
			Node n = graph.getNode(m);
			Object ch = n.get(2);
			if (ch.equals(0))
				g[0]++;
			else if (ch.equals(1))
				g[1]++;

		}
		System.out.println("	Conservative (purple nodes): " + g[1]);
		System.out.println("	Liberal (green nodes): " + g[0]);

		float tot_edges = graph.getEdgeCount();
		System.out.println("\nTotal number of READERS (edges): " + tot_edges);

		// All kinds of edges
		float[] h = new float[4];
		for (int m = 0; m < tot_edges; m++) {
			Node n1 = (graph.getEdge(m)).getSourceNode();
			Object ch1 = n1.get(2);
			Node n2 = (graph.getEdge(m)).getTargetNode();
			Object ch2 = n2.get(2);

			if (ch1.equals(0) && ch2.equals(0))
				h[0]++;
			if (ch1.equals(1) && ch2.equals(1))
				h[1]++;
			if (ch1.equals(0) && ch2.equals(1))
				h[2]++;
			if (ch1.equals(1) && ch2.equals(0))
				h[3]++;

		}
		System.out.println("	Liberal-Liberal: " + h[0]);
		System.out.println("	Conservative-Conservative: " + h[1]);
		System.out.println("	Liberal-Conservative: " + h[2]);
		System.out.println("	Conservative-Liberal: " + h[3]);

		// Polarization constant
		float pc = ((h[0] + h[1]) / tot_edges);
		System.out.println("\nPolarization Coefficient: " + pc + "\n\n");
	}
	
	// function for calculating triads
	public static void triads(Graph graph) {

		// Total number of edges

		int triads = 0;
		int[] type_triads = { 0, 0, 0, 0 }; // ccc,lll,ccl,llc
		Iterator<?> ofEdges = graph.edges();
		while (ofEdges.hasNext()) {
			Edge e = (Edge) ofEdges.next();
			Node source = e.getSourceNode();
			Node target = e.getTargetNode();
			Iterator<?> ofthird = target.outNeighbors();
			while (ofthird.hasNext()) {
				Node third = (Node) ofthird.next();
				Iterator<?> thirdNeigh = third.outNeighbors();
				while (thirdNeigh.hasNext()) {
					Node check = (Node) thirdNeigh.next();
					if (check.equals(source)) {
						triads++;
						if ((int) source.get(2) == 1
								&& (int) target.get(2) == 1
								&& (int) third.get(2) == 1) {
							type_triads[0]++;
						} else if ((int) source.get(2) == 0
								&& (int) target.get(2) == 0
								&& (int) third.get(2) == 0) {
							type_triads[1]++;
						} else if ((int) source.get(2) == 0
								&& (int) target.get(2) == 1
								&& (int) third.get(2) == 1) {
							type_triads[2]++;
						} else if ((int) source.get(2) == 1
								&& (int) target.get(2) == 0
								&& (int) third.get(2) == 1) {
							type_triads[2]++;
						} else if ((int) source.get(2) == 1
								&& (int) target.get(2) == 1
								&& (int) third.get(2) == 0) {
							type_triads[2]++;
						} else if ((int) source.get(2) == 1
								&& (int) target.get(2) == 0
								&& (int) third.get(2) == 0) {
							type_triads[3]++;
						} else if ((int) source.get(2) == 0
								&& (int) target.get(2) == 1
								&& (int) third.get(2) == 0) {
							type_triads[3]++;
						} else if ((int) source.get(2) == 0
								&& (int) target.get(2) == 0
								&& (int) third.get(2) == 1) {
							type_triads[3]++;
						}

					}
				}

			}
		}

		System.out.println("Cliques : " + (triads) / 3);
		System.out.println("C-C-C : " + type_triads[0] / 3);
		System.out.println("L-L-L : " + type_triads[1] / 3);
		System.out.println("C-C-L : " + type_triads[2] / 3);
		System.out.println("L-L-C : " + type_triads[3] / 3);

	}

	// function for calculating global clustering coefficient
	public static void global(Graph graph) {
		float tot_nodes = graph.getNodeCount();
		float triads = 0;
		Node[] adjacent;
		for (int m = 0; m < tot_nodes; m++) {
			Iterator<?> it = graph.neighbors(graph.getNode(m));
			adjacent = new Node[500];
			int i = 0;
			while (it.hasNext()) {
				adjacent[i] = (Node) it.next();
				i++;
			}
			for (int p = 0; p < i; p++) {
				Iterator<?> it2 = graph.outNeighbors(adjacent[p]);
				while (it2.hasNext()) {
					Node check = (Node) it2.next();
					if ((graph.getEdge(check, (graph.getNode(m))) != null)
							|| (graph.getEdge((graph.getNode(m)), check) != null)) {
						triads++;
					}
				}
			}
		}
		triads = triads / 3;
		float cc = triads / (tot_nodes * (tot_nodes - 1) * (tot_nodes - 2) / 6);
		System.out.println("\nGlobal Clustering Coefficient: " + cc);

	}

	// Function for calculating network average clustering coefficient
	public static void nacc(Graph graph) {
		float tot_nodes = graph.getNodeCount();
		float sum = 0;
		Node[] adjacent;
		for (int m = 0; m < tot_nodes; m++) {
			// getting neighbors of a node
			Iterator<?> it = graph.neighbors(graph.getNode(m));
			adjacent = new Node[500];
			int i = 0;
			while (it.hasNext()) {
				adjacent[i] = (Node) it.next();
				i++;
			}
			float triads = 0;
			// checking triad formation
			for (int p = 0; p < i; p++) {
				for (int q = p + 1; q < i; q++) {
					if ((graph.getEdge(adjacent[p], adjacent[q]) != null)
							|| (graph.getEdge(adjacent[q], adjacent[p]) != null)) {
						triads++;
					}
				}
			}
			// System.out.println(sum);
			if (i > 1) {
				sum += (2 * triads / (i * (i - 1)));
			}
		}
		float cc = sum / (2 * tot_nodes);
		System.out.println("Network Average Clustering Coefficient: " + cc);
	}

	public static void main(String[] args) throws FileNotFoundException,
			IOException {

		Graph graph = new Graph(true);
		graph.addColumn("id", Integer.class);
		graph.addColumn("label", String.class);
		graph.addColumn("value", Integer.class);
		graph.addColumn("source", String.class);
		
		//	reading file
		FileInputStream fstream = new FileInputStream("polblogs.gml");
		DataInputStream data = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(data));
		br.readLine();
		br.readLine();
		br.readLine();
		String s = br.readLine();
		
		// reading nodes
		while (s.equals("  node [")) {
			Node n = graph.addNode();
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

		// reading edges
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
		System.out.println("Graph Made!!");

		 graphstat(graph);
		 triads(graph);
		global(graph);
		nacc(graph);

	}

}
