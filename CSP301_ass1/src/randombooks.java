import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;

import prefuse.data.Graph;
import prefuse.data.Node;

// generates random graphs for histograms

public class randombooks {

	
	// finds global clustering coefficient (same as in book statistics)
	public static float global(Graph graph) {
		float tot_nodes = graph.getNodeCount();
		float triads = 0;
		Node[] adjacent;

		for (int m = 0; m < tot_nodes; m++) {
			Iterator<?> it = graph.neighbors(graph.getNode(m));
			adjacent = new Node[50];
			int i = 0;
			while (it.hasNext()) {
				adjacent[i] = (Node) it.next();
				i++;
			}

			for (int p = 0; p < i; p++) {
				for (int q = p + 1; q < i; q++) {
					if ((graph.getEdge(adjacent[p], adjacent[q]) != null)
							| (graph.getEdge(adjacent[q], adjacent[p]) != null)) {
						triads++;
					}
				}
			}
		}
		triads = triads / 3;
		float cc = triads / (tot_nodes * (tot_nodes - 1) * (tot_nodes - 2) / 6);
		return cc;
	}

	
	// generates polarization coefficient
	public static float pcgenerate(Graph graph) {
		float tot_edges = graph.getEdgeCount();
		System.out.println("\nTotal number of READERS (edges): " + tot_edges);

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

		// Polarization constant
		float pc = ((h[0] + h[2]) / tot_edges);
		// System.out.println("\nPolarization Coefficient: " + pc);
		return pc;
	}

	
	// generates nacc for random graph (same as in book statistics)
	public static float clustered(Graph graph) {
		float tot_nodes = graph.getNodeCount();
		float sum = 0;
		Node[] adjacent;

		for (int m = 0; m < tot_nodes; m++) {
			Iterator<?> it = graph.neighbors(graph.getNode(m));
			adjacent = new Node[50];
			int i = 0;
			while (it.hasNext()) {
				adjacent[i] = (Node) it.next();
				i++;
			}

			for (int p = 0; p < i; p++) {
				float triads = 0;
				for (int q = p + 1; q < i; q++) {
					if ((graph.getEdge(adjacent[p], adjacent[q]) != null)
							| (graph.getEdge(adjacent[q], adjacent[p]) != null)) {
						triads++;
					}
				}
				if (i > 1) {
					sum += (2 * triads / (i * (i - 1)));
				}
			}
		}

		// Clustering coefficient
		float cc = sum / tot_nodes;
		// System.out.println("Network Average Clustering Coefficient: " + cc);
		return cc;
	}

	public static void main(String[] args) throws IOException {
		Graph graph = new Graph();
		graph.addColumn("id", Integer.class);
		graph.addColumn("label", String.class);
		graph.addColumn("value", String.class);
		
		// reading the graph
		FileInputStream fstream = new FileInputStream("polbooks.gml");
		DataInputStream data = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(data));

		br.readLine();
		br.readLine();
		br.readLine();
		br.readLine();
		
		// reading nodes
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

		br.close();
		
		// creating random nodes
		
		for (int j = 0; j < 10000; j++) {

			Random rand = new Random();
			// We'll leave the random connections.
			for (int i = 0; i < 441; i++) {
				int first = rand.nextInt(105);
				int second = rand.nextInt(105);
				if (first != second)
					graph.addEdge(first, second);
				else
					i--;
			}
			/*
			 * float abc = pcgenerate(graph); 
			 * FileWriter fw = new FileWriter("pcs.txt", true); 
			 * BufferedWriter bw = new BufferedWriter(fw); 
			 * bw.write(abc + "\n"); 
			 * bw.close(); 
			 * fw.close();
			 */

			float cls = clustered(graph);    			// calls required function
			System.out.println(cls + " for graph no.  " + j);
			FileWriter fw2 = new FileWriter("cls.txt", true);
			BufferedWriter bw2 = new BufferedWriter(fw2);
			if (cls != 0.0) {
				bw2.write(cls + "\n");
			}
			bw2.close();
			fw2.close();
			
			// removes current edges to create a new graph
			for (int i = 0; i < 441; i++) {
				graph.removeEdge(i);
			}
		}
	}
}
