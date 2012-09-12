import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.Iterator;

import prefuse.data.Graph;
import prefuse.data.Node;

public class book_statistics {

	//function to get the highest degree of a specific type of node.
	
	public static void generate(Graph graph) {
		int totnodes = graph.getNodeCount();
		Node n = graph.getNode(0);
		Object l = n.get(0);
		for (int i = 0; i < totnodes; i++) {
			Node m = graph.getNode(i);
			if (n.getDegree() < m.getDegree()) {
				if ((m.get(2).equals("c"))) {
					l = m.get(0);
					n = m;
				}
			}
		}
		System.out.println(l);
		System.out.println(n.getDegree());
	}

	//Function to get the global clustering coefficient in pol_books 
	
	public static float clustered(Graph graph) {
		float tot_nodes = graph.getNodeCount();
		float triads = 0;
		Node[] adjacent;
		//getting the neighbors of a node
		for (int m = 0; m < tot_nodes; m++) {
			Iterator<?> it = graph.neighbors(graph.getNode(m));
			adjacent = new Node[50];
			int i = 0;
			while (it.hasNext()) {
				adjacent[i] = (Node) it.next();
				i++;
			}
			// checking whether there is an edge between any two neighbors
			for (int p = 0; p < i; p++) {
				for (int q = p + 1; q < i; q++) {
					if ((graph.getEdge(adjacent[p], adjacent[q]) != null)
							| (graph.getEdge(adjacent[q], adjacent[p]) != null)) {
						triads++;
					}
				}
			}
		}
		//obtaining number of triads and applying the formula for global clustering coefficient.
		triads = triads / 3;
		float cc = triads / (tot_nodes * (tot_nodes - 1) * (tot_nodes - 2) / 6);
		return cc;
	}

	public static void main(String[] args) throws FileNotFoundException,
			IOException {

		// Create the table for graph
		Graph graph = new Graph();
		graph.addColumn("id", Integer.class);
		graph.addColumn("label", String.class);
		graph.addColumn("value", String.class);

		// Reading the input file
		FileInputStream fstream = new FileInputStream("polbooks.gml");
		DataInputStream data = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(data));

		br.readLine();
		br.readLine();
		br.readLine();
		br.readLine();

		// Reading nodes
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

		// To check if graph is directed or undirected
		System.out.println("Is graph directed? " + graph.isDirected());

		// Total number of nodes
		int tot_nodes = graph.getNodeCount();
		System.out.println("\nTotal number of BOOKS (nodes): " + tot_nodes);

		// Number of different types of books
		int[] g = new int[3];
		for (int m = 0; m < tot_nodes; m++) {
			Node n = graph.getNode(m);
			Object ch = n.get(2);
			if (ch.equals("c"))
				g[0]++;
			else if (ch.equals("n"))
				g[1]++;
			else
				g[2]++;
		}
		System.out.println("	Conservative (purple nodes): " + g[0]);
		System.out.println("	Neutral (black nodes): " + g[1]);
		System.out.println("	Liberal (green nodes): " + g[2]);

		// Reading edges
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

		// Total number of edges
		float tot_edges = graph.getEdgeCount();
		System.out.println("\nTotal number of Edges: " + ((int) tot_edges));
		br.close();

		// All kinds of edges
		float[] h = new float[6];
		for (int m = 0; m < tot_edges; m++) {
			Node n1 = (graph.getEdge(m)).getSourceNode();
			Object ch1 = n1.get(2);
			Node n2 = (graph.getEdge(m)).getTargetNode();
			Object ch2 = n2.get(2);

			if (ch1.equals("c") && ch2.equals("c"))
				h[0]++;
			if (ch1.equals("n") && ch2.equals("n"))
				h[1]++;
			if (ch1.equals("l") && ch2.equals("l"))
				h[2]++;
			if (ch1.equals("c") && ch2.equals("n"))
				h[3]++;
			if (ch1.equals("n") && ch2.equals("l"))
				h[4]++;
			if (ch1.equals("l") && ch2.equals("c"))
				h[5]++;

		}
		System.out.println("	Conservative-Conservative: " + ((int) h[0]));
		System.out.println("	Neutral-Neutral: " + ((int) h[1]));
		System.out.println("	Liberal-Liberal: " + ((int) h[2]));
		System.out.println("	Conservative-Neutral: " + ((int) h[3]));
		System.out.println("	Neutral-Liberal: " + ((int) h[4]));
		System.out.println("	Liberal-Conservative: " + ((int) h[5]));

		// Polarization constant
		float pc = ((h[0] + h[2]) / tot_edges);
		System.out.println("\nPolarization Coefficient: " + pc);
		float ratio = ((h[0] + h[1] + h[2]) / tot_edges);
		System.out.println("Similar-linkage Coefficient: " + ratio);

		// Calculating the value of network average clustering coefficient

		float sum = 0;
		Node[] adjacent;
		for (int m = 0; m < tot_nodes; m++) {
			
			// Finding the neighbors of a node
			Iterator<?> it = graph.neighbors(graph.getNode(m));
			adjacent = new Node[50];
			int i = 0;
			while (it.hasNext()) {
				adjacent[i] = (Node) it.next();
				i++;
			}
			float triads = 0;
			// Calculating number of triads
			for (int p = 0; p < i; p++) {
				for (int q = p + 1; q < i; q++) {
					if ((graph.getEdge(adjacent[p], adjacent[q]) != null)
							|| (graph.getEdge(adjacent[q], adjacent[p]) != null)) {
						triads++;
					}
				}
			}
			sum += (2 * triads / (i * (i - 1)));
		}

		// Clustering coefficient
		float cc = sum / tot_nodes;
		System.out.println("Network Average Clustering Coefficient: " + cc);

		System.out.print("Global Clustering Coefficient: " + clustered(graph));

	}
}
