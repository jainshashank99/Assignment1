import java.io.*;

import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import prefuse.data.Graph;
import prefuse.data.Node;
// file for creating random graphs for polblogs

public class randomblogs {

	// function for calculating coefficient
	public static float polar(Graph graph) {
		float tot_edges = graph.getEdgeCount();
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

		// Polarization constant
		float pc = ((h[0] + h[1]) / tot_edges);
		return (pc);
	}

	// function for calculating global clustering coefficient
	public static float global(Graph graph) {
		float tot_nodes = graph.getNodeCount();
		float triads = 0;
		Node[] adjacent;
		for (int m = 0; m < tot_nodes; m++) {
			Iterator<?> it = graph.outNeighbors(graph.getNode(m));
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
					if (graph.getEdge(check, (graph.getNode(m))) != null) {
						triads++;
					}
				}
			}
		}

		triads = (triads / 3);
		float cc = triads / (tot_nodes * (tot_nodes - 1) * (tot_nodes - 2) / 6);
		return cc;
	}

	// Generates the network average clustering coefficient for a graph
	public static float nacc(Graph graph) {
		float tot_nodes = graph.getNodeCount();
		float sum = 0;
		Node[] adjacent;
		for (int m = 0; m < tot_nodes; m++) {
			Iterator<?> it = graph.neighbors(graph.getNode(m));
			adjacent = new Node[500];
			int i = 0;
			while (it.hasNext()) {
				adjacent[i] = (Node) it.next();
				i++;
			}
			float triads = 0;
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
		float cc = sum / tot_nodes;
		return (cc);
	}

	public static void main(String[] args) throws IOException {
		Graph graph = new Graph(true);
		graph.addColumn("id", Integer.class);
		graph.addColumn("label", String.class);
		graph.addColumn("value", Integer.class);
		graph.addColumn("source", String.class);

		// reading data
		FileInputStream fstream = new FileInputStream("polblogs.gml");
		DataInputStream data = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(data));
		br.readLine();
		br.readLine();
		br.readLine();
		
		// reading nodes
		String s = br.readLine();
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
			sc.close();
			s = br.readLine();
		}
		br.close();
		
		for (int j = 0; j < 600; j++) {

			// generating random edges
			Random rand = new Random();
			for (int i = 0; i < 19090; i++) {
				int first = rand.nextInt(1490);
				int second = rand.nextInt(1490);
				if (first != second)
					graph.addEdge(first, second);
				else
					i--;
			}

			 float abc = nacc(graph);			// calling required function
			 System.out.println(abc + " for graph no.  " + j);
			 FileWriter fw = new FileWriter("pcs.txt", true);
			 BufferedWriter bw = new BufferedWriter(fw);
			 bw.write(abc + "\n");			// writing in file
			 bw.close();
			 fw.close();

	//		float cls = global(graph);
	//		System.out.println(cls + " for graph no.  " + j);
	//		FileWriter fw2 = new FileWriter("pcs.txt", true);
	//		BufferedWriter bw2 = new BufferedWriter(fw2);
	//		bw2.write(cls + "\n");
	//		bw2.close();
	//		fw2.close();
			 
			// removing the randomly connected edges to generate new graph
			for (int i = 0; i < 19090; i++) {
				graph.removeEdge(i);
			}
		}

	}

}
