package com.tg.graph;

import java.util.HashSet;

import uk.ac.ox.oii.sigmaexporter.model.GraphElement;

public interface Graph {

	public HashSet<GraphElement> getNodes();
	public HashSet<GraphElement> getEdges();
	public int nodeCount();
	public int edgeCount();
}
