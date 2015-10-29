package com.tg.factory.util;

import io.vertx.core.json.JsonObject;

import java.awt.Color;
import java.io.File;
import java.util.Map;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.api.Range;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.layout.plugin.forceAtlas.ForceAtlasLayout;
import org.gephi.layout.plugin.fruchterman.FruchtermanReingold;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.ranking.api.Ranking;
import org.gephi.statistics.plugin.*;
import org.gephi.ranking.api.RankingController;
import org.gephi.ranking.api.Transformer;
import org.gephi.ranking.plugin.transformer.AbstractColorTransformer;
import org.gephi.ranking.plugin.transformer.AbstractSizeTransformer;
import org.gephi.statistics.plugin.GraphDistance;
import org.gephi.filters.plugin.graph.DegreeRangeBuilder.DegreeRangeFilter;
import org.gephi.filters.spi.*;
import org.openide.util.Lookup;

import uk.ac.ox.oii.sigmaexporter.model.GraphElement;

import com.tg.importers.TwitterImportController;

import de.uni_leipzig.informatik.asv.gephi.chinesewhispers.ChineseWhispersClusterer;

public class Builder {

	public static Graph build(boolean IS_DIRECTED, Map<String, Object> settings) {
		ProjectController pc = Lookup.getDefault().lookup(
				ProjectController.class);
		pc.newProject();
		
		Workspace workspace = pc.getCurrentWorkspace();

		// Get models and controllers for this new workspace - will be useful
		// later
		AttributeModel attributeModel = Lookup.getDefault()
				.lookup(AttributeController.class).getModel();
		GraphModel graphModel = Lookup.getDefault()
				.lookup(GraphController.class).getModel();
		PreviewModel model = Lookup.getDefault()
				.lookup(PreviewController.class).getModel();
		// ImportController importController =
		// Lookup.getDefault().lookup(ImportController.class);
		ImportController importController = TwitterImportController
				.getInstance();
		FilterController filterController = Lookup.getDefault().lookup(
				FilterController.class);
		RankingController rankingController = Lookup.getDefault().lookup(
				RankingController.class);
		String coloumnValue=(String) settings.get("nsb");
		String coloumnvalue;
		double range = 0.0;
		int pagerankthreshhold = (Integer) settings.get("prt");
		int nodecentrality = (Integer) settings.get("nct");
		System.out.println("-------"+nodecentrality+"------");
		// System.out.println("Threshold of pagerrank"+pagerankthreshhold+"\n\n\n");
		double Nodecentralitythreshhold = (double) nodecentrality/100;
		double prthreshhold = (double) pagerankthreshhold / 100;
		String columnValue="";
		
		//Getting layout Options
		JsonObject jobject = new JsonObject(settings.get("la").toString());
		Map inner=jobject.getMap();

		// Import file
		Container container;
		try {
			// File file = new
			// File(getClass().getResource("/org/gephi/toolkit/demos/resources/polblogs.gml").toURI());
			// File file = new File("polblogs.gml");
			File file = new File(settings.get("input_file").toString());
			container = importController.importFile(file);
			container.getLoader().setEdgeDefault(EdgeDefault.DIRECTED); // Force
																		// DIRECTED
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

		// Append imported data to GraphAPI
		importController.process(container, new DefaultProcessor(), workspace);

		// See if graph is well imported
		// DirectedGraph graph = graphModel.getDirectedGraph();
		Graph graph = IS_DIRECTED ? graphModel.getDirectedGraph() : graphModel
				.getUndirectedGraph();
		
		// ////////////////////////////////////////////////////////////////////////////////////////////////////

		// Calculating AND Filtering Based On PageRank
		PageRank pr = new PageRank();
		pr.setDirected(IS_DIRECTED);
		pr.setEpsilon(0.001);
		pr.setProbability(0.85);
		pr.execute(graphModel, attributeModel);

		// /////////////////////////////////////////////////////////////////////////////////////////////////////

		// Adding NeighborCount Column to Nodetable
		attributeModel.getNodeTable().addColumn("NeighborCount",
				AttributeType.DOUBLE);
		Node[] node1 = graph.getNodes().toArray();
		double neighborcount = 0.0;
		for (int i = 0; i < node1.length; i++) {
			Node[] neighbors = graph.getNeighbors(node1[i]).toArray();
			neighborcount = neighbors.length;
			node1[i].getAttributes().setValue("NeighborCount", neighborcount);

		}

		// Create a attribute range filter query - on the pagerank column
		// Execute the filter query

		coloumnvalue = "pageranks";
		range = 0.011;
		GraphView view = Filters.FilterBasedOnRange(attributeModel, coloumnvalue,
				range);
		graphModel.setVisibleView(view);
		graph = graphModel.getDirectedGraphVisible();
		int loop = 0;
		// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		// Create a attribute range filter query - on the NeighborCount column
		// Execute the filter query
		
		coloumnvalue = "NeighborCount";
		range = 2.0;
		GraphView view1 = Filters.FilterBasedOnRange(attributeModel, coloumnvalue,
				range);
		graphModel.setVisibleView(view1);
		graph = graphModel.getDirectedGraphVisible();

		// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//Degree Range Filter
		
		 GraphView view2 =Filters.degreerangefilter(graph); 
		 graphModel.setVisibleView(view); 

		 // See visible graph stats
		
		 UndirectedGraph graphVisible = graphModel.getUndirectedGraphVisible(); 
		 System.out.println("\n\nNodes: " + graphVisible.getNodeCount()); 
		 System.out.println("Edges: " + graphVisible.getEdgeCount());
		

		// . cluster coloring...
		ChineseWhispersClusterer cwc = new ChineseWhispersClusterer();
		cwc.execute(graphModel);

		// Run YifanHuLayout for 100 passes - The layout always takes the
		// current visible view
		int iterations = inner.containsKey("it") ? Integer.parseInt(inner.get("it").toString()) : 50;
		if(inner.get("name").toString().equals("YifanHuLayout")){
		
			System.out.println("\nEntered YifanHuLayout \n");
		YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
		layout.setGraphModel(graphModel);
		layout.resetPropertiesValues();
		int distanceInt = inner.containsKey("distance") ? Integer.parseInt(inner.get("distance").toString()) : 200;
		float distance = (float) distanceInt;
		layout.setOptimalDistance(distance);

		layout.initAlgo();
		for (int i = 0; i < iterations && layout.canAlgo(); i++) {
			layout.goAlgo();
		}
		layout.endAlgo();

		}
		else if(inner.get("name").toString().equals("ForceAtlasLayout")){
			System.out.println("\nEntered ForceAtlasLayout \n"); 
			ForceAtlasLayout layout = new ForceAtlasLayout(null);
			   layout.setGraphModel(graphModel);
			   layout.resetPropertiesValues();
			   
			   int speedInt = inner.containsKey("speed") ? Integer.parseInt(inner.get("speed").toString()) : 100;
			   int convergedInt = inner.containsKey("converged") ? Integer.parseInt(inner.get("converged").toString()) : 1;
			   double inertia = inner.containsKey("inertia") ? Double.parseDouble(inner.get("inertia").toString()) : 0.1 ;
			   int gravityInt = inner.containsKey("gravity") ? Integer.parseInt(inner.get("gravity").toString()) : 50;
			   int maxDisplacementInt = inner.containsKey("maxdisplacement") ? Integer.parseInt(inner.get("maxdisplacement").toString()) : 50;
			   double speed = (double) speedInt;
			   boolean converged = convergedInt == 1 ? true : false;
			   double gravity = (double) gravityInt;
			   double maxDisplacement = (double) maxDisplacementInt;
			   
			   layout.setSpeed(speed);
			   layout.setConverged(converged);
			   //layout.inertia =0.1;
			   //layout.setRepulsionStrength(400.0);
			   layout.setInertia(inertia);
			   layout.setGravity(gravity);
			   layout.setMaxDisplacement(maxDisplacement);
			   
			   layout.initAlgo();
			   for (int i = 0; i < iterations && layout.canAlgo(); i++) {
			    layout.goAlgo();
			   }
			   
			   layout.endAlgo();
		}
		
		else if(inner.get("name").toString().equals("FruchtermanReingold")){
			System.out.println("\nEntered FruchtermanReingold \n");
			FruchtermanReingold layout = new FruchtermanReingold(null);
			layout.setGraphModel(graphModel);
			layout.resetPropertiesValues();
			
			int areaInt = inner.containsKey("area") ? Integer.parseInt(inner.get("area").toString()) : 100;
			int speedInt = inner.containsKey("speed") ? Integer.parseInt(inner.get("speed").toString()) : 50;
			int gravityInt = inner.containsKey("gravity") ? Integer.parseInt(inner.get("gravity").toString()) : 0;
			float area = (float) areaInt;
			double speed = (double) speedInt;
			double gravity = (double) gravityInt;
			
			layout.setArea(area);
			layout.setSpeed(speed);
			layout.setGravity(gravity);
			
			layout.initAlgo();
			for (int i = 0; i < iterations && layout.canAlgo(); i++) {
				layout.goAlgo();
			}
			layout.endAlgo();
			
			
		}
		
		// Get Centrality
		GraphDistance distance = new GraphDistance();
		distance.setDirected(true);
		distance.execute(graphModel, attributeModel);
		
		
		// Sorting Nodes based On some column Value and Removing Percentage Nodes Based On Column Value
				if(pagerankthreshhold!=0){
					columnValue="pageranks";
				AttributeColumn column = attributeModel.getNodeTable().getColumn(
						columnValue);
				graph = Filters.RemovePercentageNodes(graph, column, prthreshhold,columnValue);
				
		}
				if(nodecentrality!=0){
					columnValue="Betweenness Centrality";
					AttributeColumn column = attributeModel.getNodeTable().getColumn(
							GraphDistance.BETWEENNESS);
					graph = Filters.RemovePercentageNodes(graph, column, Nodecentralitythreshhold,columnValue);
					
			}
				

		// Rank color by Degree
		Ranking degreeRanking = rankingController.getModel().getRanking(
				Ranking.NODE_ELEMENT, Ranking.DEGREE_RANKING);
		AbstractColorTransformer colorTransformer = (AbstractColorTransformer) rankingController
				.getModel().getTransformer(Ranking.NODE_ELEMENT,
						Transformer.RENDERABLE_COLOR);
		colorTransformer.setColors(new Color[] { new Color(0xFEF0D9),
				new Color(0xB30000) });
		rankingController.transform(degreeRanking, colorTransformer);

		// Rank Size By Page Rank Or Node Centrality
		if(coloumnValue.contains("nc")){
		
			System.out.println("\n Node Size By Node Centrality \n");
			Filters.RankSize("Betweenness Centrality", attributeModel, rankingController);
		}
		else if(coloumnValue.contains("pr")){
			System.out.println("\n Node Size By Page Rank \n");
			Filters.RankSize("pageranks", attributeModel, rankingController);
		}
		
		
		
		// Preview
		model.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS,
				Boolean.TRUE);
		// model.getProperties().putValue(PreviewProperty.EDGE_COLOR, new
		// EdgeColor(Color.GRAY));
		model.getProperties().putValue(PreviewProperty.EDGE_THICKNESS,
				new Float(0.1f));
		model.getProperties().putValue(
				PreviewProperty.NODE_LABEL_FONT,
				model.getProperties()
						.getFontValue(PreviewProperty.NODE_LABEL_FONT)
						.deriveFont(8));

		// Export
		/*
		 * ExportController ec =
		 * Lookup.getDefault().lookup(ExportController.class); try {
		 * ec.exportFile(new File("headless_simple.pdf")); } catch (IOException
		 * ex) { ex.printStackTrace(); return; }
		 */

		return graph;

	}

}
