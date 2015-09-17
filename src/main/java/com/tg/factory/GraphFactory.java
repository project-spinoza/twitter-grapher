package com.tg.factory;

import java.util.Map;

import org.gephi.graph.api.Graph;

import com.tg.factory.util.Builder;
import com.tg.graph.TwitterGraph;
import com.tg.graph.SigmaGraph;

public class GraphFactory {
	
	public TwitterGraph getGraph(String graphType, Map<String, Object> settings){
		boolean DIRECTED = true;
		Graph gephiGraph = Builder.build(DIRECTED, settings);
		
		TwitterGraph graph;
		
		switch(graphType){
		
		case "sigmaGraph" :
				graph = new SigmaGraph();
				graph.build(gephiGraph, settings);
			break;
			
			default:
				graph = null;
		}
		
		return graph;
	}
}
