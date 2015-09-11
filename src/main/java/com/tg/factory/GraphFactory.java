package com.tg.factory;

import java.util.HashMap;
import java.util.Map;

import org.gephi.graph.api.DirectedGraph;

import com.tg.factory.util.Builder;
import com.tg.graph.Graph;
import com.tg.graph.SigmaGraph;

public class GraphFactory {
	
	public Graph getGraph(String graphType, Map<String, Object> settings){
		
		DirectedGraph rawGraph = Builder.build();
		
		Graph sigmaGraph;
		
		switch(graphType){
		
		case "sigmaGraph" :
				sigmaGraph = new SigmaGraph();
				sigmaGraph.build(rawGraph, settings);
			break;
			default:
				sigmaGraph = null;
		}
		
		return sigmaGraph;
	}
}
