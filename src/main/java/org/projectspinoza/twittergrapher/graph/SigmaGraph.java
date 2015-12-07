package org.projectspinoza.twittergrapher.graph;

import java.awt.Color;
import java.util.HashSet;
import java.util.Map;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeData;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.preview.types.EdgeColor;

import uk.ac.ox.oii.sigmaexporter.model.GraphElement;
import uk.ac.ox.oii.sigmaexporter.model.GraphNode;

public class SigmaGraph implements TwitterGraph {

	private HashSet<GraphElement> sigmaNodes;
	private HashSet<GraphElement> sigmaEdges;

	public SigmaGraph() {
		sigmaNodes = new HashSet<GraphElement>();
		sigmaEdges = new HashSet<GraphElement>();
	}

	@Override
	public void build(Graph graph, Map<String, Object> settings) {
		// TODO Auto-generated method stub
		createNodes(graph, settings);
		createEdges(graph, settings);
	}

	public HashSet<GraphElement> getNodes() {
		// TODO Auto-generated method stub
		return sigmaNodes;
	}

	public HashSet<GraphElement> getEdges() {
		// TODO Auto-generated method stub
		return sigmaEdges;
	}

	public int nodeCount() {
		// TODO Auto-generated method stub
		return sigmaNodes.size();
	}

	public int edgeCount() {
		// TODO Auto-generated method stub
		return sigmaEdges.size();
	}

	public String toString() {
		return "nodes[" + nodeCount() + "], edges[" + edgeCount() + "]";
	}

	private void createNodes(Graph graph, Map<String, Object> settings) {
		String nodeSizeBy = settings.get("nsb").toString();
		Node[] nodeArray = graph.getNodes().toArray();
		for (int i = 0; i < nodeArray.length; i++) {

			Node n = nodeArray[i];
			NodeData nd = n.getNodeData();
			String id = nd.getId();
			String label = nd.getLabel();
			float x = nd.x();
			float y = nd.y();
			float size = 1;
			if (nodeSizeBy.equals("pr")) {
				double s = (Double) nodeArray[i].getAttributes().getValue(
						"pagerank");
				size = (float) s;
			} else if (nodeSizeBy.equals("exp_pr")) {
				double dpr = (Double) nodeArray[i].getAttributes().getValue(
						"pagerank");
				size = (float) Math.exp(dpr);
			} else {
				size = nd.getSize();
			}
			String color = "rgb(" + (int) (nd.r() * 255) + ","
					+ (int) (nd.g() * 255) + "," + (int) (nd.b() * 255) + ")";

			GraphNode sigmaNode = new GraphNode(id);
			sigmaNode.setLabel(label);
			sigmaNode.setX(x);
			sigmaNode.setY(y);
			sigmaNode.setSize(size);
			sigmaNode.setColor(color);

			AttributeRow nAttr = (AttributeRow) nd.getAttributes();
			for (int j = 0; j < nAttr.countValues(); j++) {
				Object valObj = nAttr.getValue(j);
				if (valObj == null) {
					continue;
				}
				String val = valObj.toString();
				AttributeColumn col = nAttr.getColumnAt(j);
				if (col == null) {
					continue;
				}
				String name = col.getTitle();
				if (name.equalsIgnoreCase("Id")
						|| name.equalsIgnoreCase("Label")
						|| name.equalsIgnoreCase("uid")) {
					continue;
				}
				sigmaNode.putAttribute(name, val);

			}

			sigmaNodes.add(sigmaNode);
		}
	}

	private void createEdges(Graph graph, Map<String, Object> settings) {

		EdgeColor colorMixer = new EdgeColor(EdgeColor.Mode.MIXED);
		Edge[] edgeArray = graph.getEdges().toArray();

		for (int i = 0; i < edgeArray.length; i++) {
			Edge e = edgeArray[i];
			String sourceId = e.getSource().getNodeData().getId();
			String targetId = e.getTarget().getNodeData().getId();

			SigmaEdge sigmaEdge = new SigmaEdge(String.valueOf(e.getId()));
			// SigmaEdge jEdge = new SigmaEdge(String.valueOf(e.getId()));
			sigmaEdge.setSource(sourceId);
			sigmaEdge.setTarget(targetId);
			sigmaEdge.setSize(e.getWeight());

			if (settings.get("ecb").toString().equals("mix")) {

				EdgeData ed = e.getEdgeData();
				boolean mixColors = false;
				String color = "";
				if (ed != null) {
					float r = ed.r();
					float g = ed.g();
					float b = ed.b();

					if (r == -1 || g == -1 || b == -1) {
						mixColors = true;// We'll mix colors
					} else {
						color = "rgb(" + (int) (r * 255) + ","
								+ (int) (g * 255) + "," + (int) (b * 255) + ")";
					}
				}

				if (mixColors) {
					// Source
					NodeData nd = e.getSource().getNodeData();
					Color source = new Color(nd.r(), nd.g(), nd.b());
					// Target
					nd = e.getTarget().getNodeData();
					Color target = new Color(nd.r(), nd.g(), nd.b());
					Color result = colorMixer.getColor(null, source, target);

					color = "rgb(" + result.getRed() + "," + result.getGreen()
							+ "," + result.getBlue() + ")";
				}
				sigmaEdge.setColor(color);
			}

			/*
			 * if(lc.get("et").toString().equals("curve")){
			 * jEdge.setType("curve"); }
			 */
			sigmaEdges.add(sigmaEdge);
		}
	}

}
