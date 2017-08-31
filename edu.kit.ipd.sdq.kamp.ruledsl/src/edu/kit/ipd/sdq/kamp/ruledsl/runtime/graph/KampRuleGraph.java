package edu.kit.ipd.sdq.kamp.ruledsl.runtime.graph;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
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
import java.util.Set;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;

import edu.kit.ipd.sdq.kamp.ruledsl.support.KampRuleStub;


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
		
		// 1. exclude all parents of a vertex, if disable all parents is set on a vertex (iterative depth-first search)
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
	 * In order to get a topological order, the graph is traversed using breadth-first search.
	 * 
	 * @return a list of rules which is topologically sorted
	 */
	public List<KampRuleStub> topologicalSort() {
		List<KampRuleStub> rules = new ArrayList<>();
		Set<KampRuleVertex> verticesWithoutParent = new HashSet<>();
		
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
				
				// only add the rule if it is active
				rules.add(new KampRuleStub(cVertex.getContent(), (cVertex.getParent() == null) ? null : cVertex.getParent().getContent(), cVertex.isActive()));
				
				for(KampRuleVertex nextVertex : cVertex.getChildren()) {
					verticesWithoutParent.add(nextVertex);
				}
			}
		}
		
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
	
	// DEBUG flag which if true restricts the edges which are shown to parent edges only
	private static final boolean PARENT_EDGE_ONLY = true;
	
	public String toDotNotation() {
		// note: "digraph G {" is needed for online graph creation - normally it is "graph {"
		String out = "digraph G {\n\tcenter=true; orientation=portrait; label=\"Dependency Injection Rule-Graph\"; rankdir=LR; clusterrank=global; fontname=Arial; fontsize=22; concentrate=true;\n\n";

		for(KampRuleVertex cVertex : this) {
			for(KampRuleEdge edge : cVertex.getEdges()) {
				if(PARENT_EDGE_ONLY && edge.getType() != KampRuleEdge.Type.PARENT) {
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
			out += "\t\"" + v.toString() + "\""
				+ "[color=" + (v.isActive() ? "yellowgreen" : "black") + " "
				+ "style=" + ((v.isActive()) ? "bold" : "bold") + " "
				+ "fontcolor=" + (v.isActive() ? "black" : "black") + " "
				+ "fontname=Arial "
				+ "fontsize=16 "
				+ "shape=" + ((v.isUserDefined()) ? "signature" : "box") + "]\n";
		}
		
		out += "}";
		return out;
	}
	
	private static int imgCount = 0;

	/**
	 * Show the {@link KampRuleGraph} using an online source of graphviz.
	 * Caution: The online API is unstable!! Please use the method {@link #show(String)} or {@link #show(Consumer, String)}
	 * and pass a local graphviz dot executable location!
	 * @throws InterruptedException thrown if the dot executable is interrupted
	 * @throws IOException thrown if the dot executable could not be accessed or the online source was not accessible
	 */
	public void show() throws IOException, InterruptedException {
		show(null, null);
	}
	
	public void show(String pathToDotExecutable) throws IOException, InterruptedException {
		show(null, pathToDotExecutable);
	}
	
	// TODO fix java.lang.NoClassDefFoundError: org/apache/batik/transcoder/image/PNGTranscoder
	public void show(Consumer<String> viewer, String pathToDotExecutable) throws IOException, InterruptedException {
		String dot = toDotNotation();
		String path = System.getProperty("java.io.tmpdir");
		String outFile = "output" + (imgCount++) + ".png";
		boolean onlineCreation = false;
		
		File file = new File(path + outFile);
		if(file.exists()) {
			file.delete();
		}
		
		if(pathToDotExecutable == null) {
			onlineCreation = true;
		}
		
		if(!onlineCreation  && !new File(pathToDotExecutable).exists()) {
			throw new IOException("Please specify the correct path to GraphViz .dot executable in AbstractGraph.show(). If you do not know about GraphViz, visit: http://www.graphviz.org/Download.php.");
			// do not fallback to online creation automatically
			// onlineCreation = true;
		}
		
		if(!onlineCreation) {		
			PrintWriter out = new PrintWriter(path + "input.dot");
			out.print(dot);
			out.close();
			
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
				  throw new IOException("Error, creating graph on server. Do you have an internet connection?");
			  } finally {
			    if(connection != null) {
			      connection.disconnect(); 
			    }
			  }			
		}
		
		new Thread(() -> {
			// if no viewer is passed, use an exemplary one
			SwingUtilities.invokeLater(new Runnable() {
			    public void run() {
					if(viewer == null) {
						JFrame frame = new JFrame();
			
						try {
							BufferedImage myPicture = ImageIO.read(new File(path + outFile));
							JLabel picLabel = new JLabel(new ImageIcon(myPicture));
			
							frame.setTitle("Dependency Graph for Kamp Rules");
					    	frame.add(picLabel);
					    	frame.setSize(myPicture.getWidth() + 100, myPicture.getHeight() + 100);
					    	frame.setLocationRelativeTo(null);
					    	frame.setVisible(true);
					    	frame.toFront();
					    	frame.repaint();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						viewer.accept(path + outFile);
					}
			    }
			});
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
