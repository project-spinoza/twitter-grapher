package org.projectspinoza.twittergrapher.factory;

import java.io.FileNotFoundException;
import java.util.Map;

import org.gephi.graph.api.Graph;
import org.projectspinoza.twittergrapher.factory.util.BasicGraphBuilder;
import org.projectspinoza.twittergrapher.graph.SigmaGraph;
import org.projectspinoza.twittergrapher.graph.TwitterGraph;

public class GraphFactory {

	public TwitterGraph getGraph(String graphType, Map<String, Object> settings) throws FileNotFoundException{
		boolean DIRECTED = true;
		//Graph gephiGraph = Builder.build(DIRECTED, settings);
		Graph basicGraph = (new BasicGraphBuilder()).buildBasicGraph(DIRECTED);
		TwitterGraph graph;

		switch (graphType) {

		case "sigmaGraph":
			graph = new SigmaGraph();
			graph.build(basicGraph, settings);
			break;

		default:
			graph = null;
		}

		return graph;
	}
}
