package com.tg.graph;

import java.util.HashSet;
import java.util.Map;

import org.gephi.graph.api.DirectedGraph;

import uk.ac.ox.oii.sigmaexporter.model.GraphElement;

public interface Graph {
	
	public void build(DirectedGraph graph, Map<String, Object> settings);
	public HashSet<GraphElement> getNodes();
	public HashSet<GraphElement> getEdges();
}
