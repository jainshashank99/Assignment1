

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

	public static void main(String[] args) throws FileNotFoundException, IOException {
		
		//Create the table for graph
		Graph graph = new Graph();
		graph.addColumn("id", Integer.class);
		graph.addColumn("label", String.class);
		graph.addColumn("value", String.class);
		
		//Reading the input file
		FileInputStream fstream = new FileInputStream("polbooks.gml");
        DataInputStream data = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(data));
        
        br.readLine();
        br.readLine();
        br.readLine();
		br.readLine();
		String s = br.readLine();
		
		//Reading nodes
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
			sc.close();
		}
		
		//To check if graph is directed or undirected
		System.out.println("Is graph directed? "+graph.isDirected());
		
		//Total number of nodes
		int tot_nodes = graph.getNodeCount();
		System.out.println("\nTotal number of BOOKS (nodes): "+tot_nodes);
		
		//Number of different types of books
		int[] g = new int[3];
		for(int m=0;m<tot_nodes;m++) {
			Node n = graph.getNode(m);
			Object ch = n.get(2);
			if(ch.equals("c"))
				g[0]++;
			else if(ch.equals("n"))
				g[1]++;
			else
				g[2]++;
		}
		System.out.println("	Conservative (purple nodes): "+g[0]);
		System.out.println("	Neutral (black nodes): "+g[1]);
		System.out.println("	Liberal (green nodes): "+g[2]);
		
		//Reading edges
		while(s.equals("  edge")){
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
		
		//Total number of edges
		float tot_edges = graph.getEdgeCount();
		System.out.println("\nTotal number of Edges: "+((int) tot_edges));
		br.close();
		
		//All kinds of edges
		float[] h = new float[6];
		for(int m=0;m<tot_edges;m++) {
			Object ch1 = ((graph.getEdge(m)).getSourceNode()).get(2);
			Object ch2 = ((graph.getEdge(m)).getTargetNode()).get(2);
			
			if(ch1.equals("c") && ch2.equals("c"))
				h[0]++;
			if(ch1.equals("n") && ch2.equals("n"))
				h[1]++;
			if(ch1.equals("l") && ch2.equals("l"))
				h[2]++;
			if(ch1.equals("c") && ch2.equals("n"))
				h[3]++;
			if(ch1.equals("n") && ch2.equals("l"))
				h[4]++;
			if(ch1.equals("l") && ch2.equals("c"))
				h[5]++;
			
		}
		System.out.println("	Conservative-Conservative: "+((int) h[0]));
		System.out.println("	Neutral-Neutral: "+((int) h[1]));
		System.out.println("	Liberal-Liberal: "+((int) h[2]));
		System.out.println("	Conservative-Neutral: "+((int) h[3]));
		System.out.println("	Neutral-Liberal: "+((int) h[4]));
		System.out.println("	Liberal-Conservative: "+((int) h[5]));
		
		//Polarization constant
		float pc = ((h[0]+h[2])/tot_edges);
		System.out.println("\nPolarization Coefficient: "+pc);
		float ratio = ((h[0]+h[1]+h[2])/tot_edges);
		System.out.println("Similar-linkage Coefficient: "+ratio);
		
		//Finding number of triads
		float triads=0;
		int[] triad = new int[7];
		Node[] adjacent;
		for(int m=0;m<tot_nodes;m++) {
			Iterator it = graph.neighbors(graph.getNode(m));
			
			adjacent = new Node[50];
			int i=0;
			while(it.hasNext()) {
				adjacent[i] = (Node) it.next();
				i++;
			}
			
			for(int p=0;p<i;p++) {
				for(int q=p+1;q<i;q++) {
					if((graph.getEdge(adjacent[p],adjacent[q]) != null) | (graph.getEdge(adjacent[q],adjacent[p]) != null)) {
						triads++;
						Object ch1 = (graph.getNode(m)).get(2);
						Object ch2 = adjacent[p].get(2);
						Object ch3 = adjacent[q].get(2);
						if(ch1.equals("c") && ch2.equals("c") && ch3.equals("c"))
							triad[0]++;
						if(ch1.equals("n") && ch2.equals("n") && ch3.equals("n"))
							triad[1]++;
						if(ch1.equals("l") && ch2.equals("l") && ch3.equals("l"))
							triad[2]++;
						if((ch1.equals("c") && ch2.equals("c") && ch3.equals("n")) |
						   (ch1.equals("c") && ch2.equals("c") && ch3.equals("l")) | 
						   (ch1.equals("n") && ch2.equals("c") && ch3.equals("c")) |
						   (ch1.equals("l") && ch2.equals("c") && ch3.equals("c")) | 
						   (ch1.equals("c") && ch2.equals("n") && ch3.equals("c")) | 
						   (ch1.equals("c") && ch2.equals("l") && ch3.equals("c")) )
							triad[3]++;
						if((ch1.equals("c") && ch2.equals("n") && ch3.equals("n")) |
						   (ch1.equals("l") && ch2.equals("n") && ch3.equals("n")) | 
						   (ch1.equals("n") && ch2.equals("n") && ch3.equals("c")) |
						   (ch1.equals("n") && ch2.equals("n") && ch3.equals("l")) | 
						   (ch1.equals("n") && ch2.equals("c") && ch3.equals("n")) | 
						   (ch1.equals("n") && ch2.equals("l") && ch3.equals("n")) )
							triad[4]++;
						if((ch1.equals("l") && ch2.equals("l") && ch3.equals("c")) |
						   (ch1.equals("l") && ch2.equals("l") && ch3.equals("n")) | 
						   (ch1.equals("c") && ch2.equals("l") && ch3.equals("l")) |
						   (ch1.equals("n") && ch2.equals("l") && ch3.equals("l")) | 
						   (ch1.equals("l") && ch2.equals("c") && ch3.equals("l")) | 
						   (ch1.equals("l") && ch2.equals("n") && ch3.equals("l")) )
							triad[5]++;
						if((ch1.equals("c") && ch2.equals("n") && ch3.equals("l")) |
						   (ch1.equals("c") && ch2.equals("l") && ch3.equals("n")) | 
						   (ch1.equals("n") && ch2.equals("c") && ch3.equals("l")) |
						   (ch1.equals("l") && ch2.equals("c") && ch3.equals("n")) | 
						   (ch1.equals("n") && ch2.equals("l") && ch3.equals("c")) | 
						   (ch1.equals("l") && ch2.equals("n") && ch3.equals("c")) )
							triad[6]++;
					}
				}
			}
		}
		triads = triads/3;
		for(int v=0;v<7;v++)
			triad[v] = triad[v]/3;	
		System.out.println("\nTotal number of triads: "+((int) triads));
		System.out.println("	C-C-C triads: "+triad[0]);
		System.out.println("	N-N-N triads: "+triad[1]);
		System.out.println("	L-L-L triads: "+triad[2]);
		System.out.println("	C-C-x triads: "+triad[3]);
		System.out.println("	N-N-x triads: "+triad[4]);
		System.out.println("	L-L-x triads: "+triad[5]);
		System.out.println("	C-N-L triads: "+triad[6]);
		
		//Clustering coefficient
		float cc = triads/(tot_nodes*tot_nodes/2);
		System.out.println("\nClustering Coefficient: "+cc);
		
        
	}
}

