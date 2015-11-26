package org.projectspinoza.twittergrapher.graph;

import java.util.HashSet;
import java.util.Map;

import org.gephi.graph.api.Graph;

import uk.ac.ox.oii.sigmaexporter.model.GraphElement;

public interface TwitterGraph {
	
	public void build(Graph graph, Map<String, Object> settings);
	
	public HashSet<GraphElement> getNodes();
	
	public HashSet<GraphElement> getEdges();
	
}
