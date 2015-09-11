package com.tg.graph;

import java.util.HashSet;

import uk.ac.ox.oii.sigmaexporter.model.GraphElement;

public class SigmaGraph implements Graph {
	
	private HashSet<GraphElement> nodes;
	private HashSet<GraphElement> edges;
	
	public SigmaGraph(){
		
	}
	public SigmaGraph(HashSet<GraphElement> n, HashSet<GraphElement> e){
		setNodes(n);
		setEdges(n);
	}
	public HashSet<GraphElement> getNodes() {
		// TODO Auto-generated method stub
		return nodes;
	}

	public HashSet<GraphElement> getEdges() {
		// TODO Auto-generated method stub
		return edges;
	}
	
	public void setNodes(HashSet<GraphElement> n){
		nodes = n;
	}
	
	public void setEdges(HashSet<GraphElement> e){
		edges = e;
	}
	public int nodeCount() {
		// TODO Auto-generated method stub
		return nodes.size();
	}

	public int edgeCount() {
		// TODO Auto-generated method stub
		return edges.size();
	}

}
