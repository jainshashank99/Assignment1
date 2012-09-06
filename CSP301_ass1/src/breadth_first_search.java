import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;

import prefuse.data.Graph;
import prefuse.data.Node;

// file for applying breadth first search to get diameter of the graph
public class breadth_first_search {

	// function for bfs
	public static int problem(Graph graph, int n) {
		float tot_nodes = graph.getNodeCount();
		int nodes = (int) tot_nodes;
		int[] color = new int[nodes];
		int[] dist = new int[nodes];
		Node[] adjacent;
		int u;
		
		// assigning initial distances
		for (int i = 0; i < nodes; i++) {
			color[i] = 0;
			dist[i] = 30000;
		}
		color[n] = 1;
		dist[n] = 0;
		LinkedList<Integer> l = new LinkedList<>();
		l.add(n);
		while (l.isEmpty() != true) {
			u = l.getFirst();
			//getting the neighbors
			Iterator<?> it = graph.outNeighbors(graph.getNode(u));
			adjacent = new Node[500];
			int j = 0;
			while (it.hasNext()) {
				adjacent[j] = (Node) (it.next());
				j++;
			}
			// updating distances
			for (int k = 0; k < j; k++) {
				int ptr = (int) adjacent[k].get(0);
				if (color[ptr] == 0) {
					color[ptr] = 1;
					dist[ptr] = dist[u] + 1;
					l.add(ptr);
				}
			}
			l.remove();
			color[u] = 2;
		}
		// getting max path length
		int max = 0;
		for (int i = 0; i < nodes; i++) {
			if (dist[i] != 30000) {
				if (dist[i] > max)
					max = dist[i];
			}
		}
		return max;
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
		long t1 = System.currentTimeMillis();
		float tot_nodes = graph.getNodeCount();
		int max = 0;
		for (int i = 0; i < tot_nodes; i++) {
			int dist = problem(graph, i);
			if (dist == 12)
				System.out.println(dist + " for  " + i);
			if (dist > max)
				max = dist;
		}
		long t2 = System.currentTimeMillis();
		System.out.println(max + " " + (t2 - t1));

	}

}
