package org.projectspinoza.twittergrapher.factory;

import java.util.Map;

import org.gephi.graph.api.Graph;
import org.projectspinoza.twittergrapher.factory.util.Builder;
import org.projectspinoza.twittergrapher.graph.SigmaGraph;
import org.projectspinoza.twittergrapher.graph.TwitterGraph;

public class GraphFactory {

	public TwitterGraph getGraph(String graphType, Map<String, Object> settings) {
		boolean DIRECTED = true;
		Graph gephiGraph = Builder.build(DIRECTED, settings);

		TwitterGraph graph;

		switch (graphType) {

		case "sigmaGraph":
			graph = new SigmaGraph();
			graph.build(gephiGraph, settings);
			break;

		default:
			graph = null;
		}

		return graph;
	}
}
