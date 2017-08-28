package edu.kit.ipd.sdq.kamp.ruledsl.runtime.graph;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;

import edu.kit.ipd.sdq.kamp.ruledsl.support.IRule;


public class KampRuleGraph implements Iterable<KampRuleVertex> {
	private final Set<KampRuleVertex> vertices = new HashSet<>();
	
	public void addVertex(KampRuleVertex vertex) {
		this.vertices.add(vertex);
	}
	
	/**
	 * This is run once all vertices and edges are inserted.
	 * We mark the graph in this step in order to determine which vertices will be exported.
	 */
	public void runExclusionAlgorithms() {
		
		// exclude all parents of a vertex, if disable all parents is set on a vertex
		for(KampRuleVertex vertex : this) {
			if(vertex.isDisableAllParents()) {
				// follow the path recursively in order to disable all vertices on the path
				KampRuleVertex cVertex = vertex.getParent();
				while(cVertex != null) {
					cVertex.setActive(false);
					cVertex = cVertex.getParent();
				}
			}
		}
	}
	
	/**
	 * Validate the graph.
	 * Check if there are no cycles.
	 * @throws GraphException thrown if a validation error occurred
	 */
	public void validate() throws GraphException {
		// for each vertex, follow the path until no parent is available
		// keep track of the vertices on the path and check if they are recurring
		for(KampRuleVertex vertex : this) {
			// this is possible because we have only one parent per vertex
			List<KampRuleVertex> verticesExamined = new ArrayList<>();
			verticesExamined.add(vertex);
			KampRuleVertex cVertex = vertex.getParent();
			
			while(cVertex != null) {
				if(verticesExamined.contains(cVertex)) {
					verticesExamined.add(cVertex);
					throw new GraphException("Cycle detected: " + getCycleAsString(verticesExamined));
				}
				
				verticesExamined.add(cVertex);
				cVertex = cVertex.getParent();
			}
		}
	}
	
	/**
	 * Caution: This method destroys the graph!!
	 * In order to get a topological order, vertices are subsequently removed from the graph.
	 * 
	 * @return a list of rules which is topologically sorted
	 */
	public List<Class<? extends IRule>> topologicalSort() {
		List<Class<? extends IRule>> rules = new ArrayList<>();
		Set<KampRuleVertex> verticesWithoutParent = new HashSet<>();
//		Set<KampRuleVertex> allVertices = new HashSet<>();	// copy the set
//		for(KampRuleVertex v : this) {
//			allVertices.add(new KampRuleVertex(v));
//		}
		
		for(KampRuleVertex cVertex : this) {
			if(cVertex.getParent() == null) {
				verticesWithoutParent.add(cVertex);
			}
		}
		
		while(!verticesWithoutParent.isEmpty()) {
			Iterator<KampRuleVertex> it = new HashSet<>(verticesWithoutParent).iterator(); 
			verticesWithoutParent.clear();
			while(it.hasNext()) {
				KampRuleVertex cVertex = it.next();
				rules.add(cVertex.getContent());
				for(KampRuleVertex nextVertex : cVertex.getChildren()) {
					nextVertex.setParent(null);
					verticesWithoutParent.add(nextVertex);
				}
				cVertex.removeAllChildren();
			}
		}
		
		// clear the graph
		this.vertices.clear();
		
		return rules;
	}
	
	public String getCycleAsString(List<KampRuleVertex> cycle) {
		String out = "";
		int i = 0;
		
		for(KampRuleVertex vertex : cycle) {
			out += ((i > 0) ? " -> " : "") + vertex.toString();
			i++;
		}
		
		return out;
	}
	
	public KampRuleVertex getVertex(Class<?> clazz) {
		for(KampRuleVertex cVertex : this) {
			if(cVertex.getContent().equals(clazz)) {
				return cVertex;
			}
		}
		
		return null;
	}
	
	private static final boolean PARENT_VERTEX_ONLY = false;
	
	public String toDotNotation() {
		// note: "digraph G {" is needed for online graph creation - normally it is "graph {"
		String out = "digraph G {\n\tcenter=true; orientation=portrait; label=\"Rule-Graph\"; rankdir=LR; clusterrank=global; fontname=Arial; fontsize=22; concentrate=true;\n\n";

		for(KampRuleVertex cVertex : this) {
			for(KampRuleEdge edge : cVertex.getEdges()) {
				if(PARENT_VERTEX_ONLY && edge.getType() != KampRuleEdge.Type.PARENT) {
					continue;
				}
					
				out += "\t\"" + edge.getOrigin().toString() + "\" -> \"" + edge.getDestination().toString() + "\""
						+ "[label=\"" + edge.getType().toString() + "\" "
						+ "id=" + edge.hashCode() + " "
						+ "fontname=Arial "
						+ "fontsize=10 "
						+ "dir=forward]";
	//					+ "color=" + ((edge.isMarked()) ? "red" : "black" ) + "]\n";
			}
		}
		
		for(KampRuleVertex v : this) {
			//if(!verticesProcessed.contains(v)) {	// put in all of them to add some custom styles
				out += "\t\"" + v.toString() + "\""
					+ "[color=" + (v.isActive() ? "blue" : "black") + " "
					+ "style=" + ((v.isActive()) ? "filled" : "bold") + " "
					+ "fontcolor=" + (v.isActive() ? "white" : "black") + " "
					+ "fontname=Arial "
					+ "fontsize=16 "
					+ "shape=box]\n";
			//}
		}
		
		out += "}";
		return out;
	}
	
	private static int imgCount = 0;
	private static boolean onlineCreation = false;	// if not online then it tries to create graph via dot.exe
	private static final String pathToDotExecutable = "C:\\Program Files (x86)\\Graphviz2.38\\bin\\dot.exe";
	
	@SuppressWarnings("unused")
	public void show() {
		String dot = toDotNotation();
		String path = System.getProperty("java.io.tmpdir");
		String outFile = "output" + (imgCount++) + ".png";
		
		File file = new File(path + outFile);
		if(file.exists()) {
			file.delete();
		}
		
		// TODO edit Graphviz2.38 path for your custom installation!!
		if(!onlineCreation  && !new File(pathToDotExecutable).exists()) {
			//throw new RuntimeException("Please specify the correct path to GraphViz .dot executable in AbstractGraph.show(). If you do not know about GraphViz, visit: http://www.graphviz.org/Download..php.");
			// fallback is online graph creation
			onlineCreation = true;
		}
		
		if(!onlineCreation) {		
			try {
				PrintWriter out = new PrintWriter(path + "input.dot");
				out.print(dot);
				out.close();
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			try {
				String[] params = new String [] { pathToDotExecutable, "-Tpng", path + "input.dot" };
				
			    ProcessBuilder builder = new ProcessBuilder(params);
			    builder.redirectOutput(new File(path + outFile));
			    Process p = builder.start();
			    InputStreamReader isr = new  InputStreamReader(p.getInputStream());
			    BufferedReader br = new BufferedReader(isr);
	
			    String lineRead;
			    while ((lineRead = br.readLine()) != null) {
			    	System.out.println(lineRead);
			    }
	
			    p.waitFor();
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
		else {
			String urlParameters;
			try {
				urlParameters = "dot=" + URLEncoder.encode(dot, "UTF-8");
			} catch (UnsupportedEncodingException e1) {
				return;
			}
			HttpURLConnection connection = null;  
			  try {
			    //Create connection
			    URL url = new URL("http://dot-graphics1.appspot.com/dotgraphicstest");
			    connection = (HttpURLConnection)url.openConnection();
			    connection.setRequestMethod("POST");
			    connection.setRequestProperty("Content-Type", 
			        "application/x-www-form-urlencoded");

			    connection.setRequestProperty("Content-Length", 
			        Integer.toString(urlParameters.getBytes().length));
			    connection.setRequestProperty("Content-Language", "en-US");  

			    connection.setUseCaches(false);
			    connection.setDoOutput(true);

			    //Send request
			    DataOutputStream wr = new DataOutputStream (
			        connection.getOutputStream());
			    wr.writeBytes(urlParameters);
			    wr.close();

			    //Get Response  
			    InputStream is = connection.getInputStream();
			    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			    StringBuilder response = new StringBuilder();
			    String line;
			    while((line = rd.readLine()) != null) {
			      response.append(line);
			      response.append('\r');
			    }
			    rd.close();

			    // Create a JPEG transcoder
		        PNGTranscoder t = new PNGTranscoder();

		        // Set the transcoding hints.
		        t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY,
		                   new Float(1.0));

		        // Create the transcoder input
		        TranscoderInput input = new TranscoderInput(new ByteArrayInputStream(response.toString().getBytes(StandardCharsets.UTF_8)));

		        // Create the transcoder output.
		        OutputStream ostream = new FileOutputStream(path + outFile);
		        TranscoderOutput output = new TranscoderOutput(ostream);

		        // Save the image.
		        t.transcode(input, output);

		        // Flush and close the stream.
		        ostream.flush();
		        ostream.close();
		        
			  } catch (Exception e) {
				  System.out.println("Error, creating graph on server. Do you have an internet connection?");
			    e.printStackTrace();
			    return;
			  } finally {
			    if(connection != null) {
			      connection.disconnect(); 
			    }
			  }			
		}
		
		new Thread(() -> {
			ImageViewer.init(path + outFile);
		}).start();
	}
	
	@Override
	public String toString() {
		return toDotNotation();
	}

	@Override
	public Iterator<KampRuleVertex> iterator() {
		return this.vertices.iterator();
	}
}
