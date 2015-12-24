package org.projectspinoza.twittergrapher.factory.util;

import io.vertx.core.json.JsonObject;

import java.awt.Color;
import java.io.File;
import java.util.Map;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.filters.api.FilterController;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.io.importer.api.Container;
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
import org.gephi.ranking.api.RankingController;
import org.gephi.ranking.api.Transformer;
import org.gephi.ranking.plugin.transformer.AbstractColorTransformer;
import org.gephi.statistics.plugin.GraphDistance;
import org.gephi.statistics.plugin.PageRank;
import org.openide.util.Lookup;
import org.projectspinoza.twittergrapher.importers.DataSourceImporter;

import de.uni_leipzig.informatik.asv.gephi.chinesewhispers.ChineseWhispersClusterer;

public class Builder {

	@SuppressWarnings("unchecked")
	public static Graph build(boolean IS_DIRECTED, Map<String, Object> settings) {
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
		Workspace workspace = pc.getCurrentWorkspace();

		// Get models and controllers for this new workspace - will be useful
		// later
		AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		PreviewModel model = Lookup.getDefault().lookup(PreviewController.class).getModel();
		ImportController importController = Lookup.getDefault().lookup(ImportController.class);
		FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
		RankingController rankingController = Lookup.getDefault().lookup(RankingController.class);
		PageRank pr = new PageRank();
		double epsilon =0.001 ;
		double probability = 0.85;
		Graph getgraph=null;
		String coloumnValue = (String) settings.get("nsb");
		String columnname = "NeighborCount";
		double range = 0.0;
		int pagerankthreshhold = Integer.parseInt(settings.get("prt").toString());
		Double neighborcountrange = Double.parseDouble(settings.get("neighborcountrange").toString());
		int nodecentrality = (Integer) settings.get("nct");
		double Nodecentralitythreshhold = (double) nodecentrality / 100;
		double prthreshhold = (double) pagerankthreshhold / 100;
	
		// Getting layout Options
		JsonObject jobject = new JsonObject(settings.get("la").toString());
		Map<String, Object> inner = jobject.getMap();
		JsonObject sources_cred_json = (JsonObject) ((Map<String, Object>) settings.get("settings")).get("sources_cred");
		String input_file = sources_cred_json.getString("file");
		String data_source = ((Map<String, Object>) settings.get("settings")).get("source_selected").toString();
		
		// Import file
		Container container = null;
		System.out.println(data_source);
		try{
			if (DataSourceType.contains(data_source)) {
				container = (new DataSourceImporter()).importDataSource(settings);
				if (container == null){
					System.out.println("Error generating graph from "+input_file);
				}
			} else if (data_source.equals("graphfile")){
				File file = new File(input_file);
				container = importController.importFile(file);
				if (container == null){
					System.out.println("No graph found in "+input_file+". Make sure you are using correct graph file format.");
				}
			}else {
				System.out.println ("Unsupported file format.");
				return null;
			}
		} catch (Exception ex) {
			
			ex.printStackTrace();
			return null;
		}

		// Append imported data to GraphAPI
		importController.process(container, new DefaultProcessor(), workspace);

		// See if graph is well imported
		// DirectedGraph graph = graphModel.getDirectedGraph();
		
		Graph graph = getgraph(graphModel,IS_DIRECTED,getgraph); 
		
		//Calculating AND Filtering Based On PageRank
		
		calculatePageRank(graphModel, attributeModel, pr, IS_DIRECTED, epsilon, probability);

		// Adding NeighborCount Column to AttributeModel
		
		addingcolumntoattributemodel(columnname, attributeModel,graph);
		
		/*// Create a attribute range filter query - on the pagerank column
		// Execute the filter query

		columnname = "pageranks";
		range = 0.011;
		GraphView view = Filters.FilterBasedOnRange(attributeModel,
				columnname, range);
		graphModel.setVisibleView(view);
		graph = graphModel.getDirectedGraphVisible();
		
		// Create a attribute range filter query - on the NeighborCount column
		// Execute the filter query
		
		columnname = "NeighborCount";
		range = 2.0;
		GraphView view1 = Filters.FilterBasedOnRange(attributeModel,
				columnname, range);
		graphModel.setVisibleView(view1);
		graph = graphModel.getDirectedGraphVisible();
		
		// Degree Range Filter

		GraphView view2 = Filters.degreerangefilter(graph);
		graphModel.setVisibleView(view);
		*/
		
		// . cluster coloring...
		ChineseWhispersClusterer cwc = new ChineseWhispersClusterer();
		cwc.execute(graphModel);

		// Run Layout for 100 passes - The layout always takes the
		// current visible view
		int iterations = inner.containsKey("it") ? Integer.parseInt(inner.get("it").toString()) : 50;
		setLayouts(inner, graphModel, iterations);
		
		
		// Get Centrality
		GraphDistance distance = new GraphDistance();
		getcentrality(distance, attributeModel, graphModel, true);

		// Sorting Nodes based On some column Value and Removing Percentage
		// Nodes Based On Column Value
		if (pagerankthreshhold != 0) {
			columnname = "pageranks";
			AttributeColumn column = attributeModel.getNodeTable().getColumn(columnname);
			graph = Filters.RemovePercentageNodes(graph, column, prthreshhold,columnname);

		}
		if (nodecentrality != 0) {
			columnname = "Betweenness Centrality";
			AttributeColumn column = attributeModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
			graph = Filters.RemovePercentageNodes(graph, column,Nodecentralitythreshhold, columnname);

		}
		
		if (neighborcountrange != 0) {
			columnname = "NeighborCount";
			AttributeColumn column = attributeModel.getNodeTable().getColumn(columnname);
			graph = Filters.RemovePercentageNodes(graph, column,neighborcountrange/100, columnname);

		}

		// Rank color by Degree
		rankingColorByDegree(rankingController);
		
		// Rank Size By Page Rank Or Node Centrality
		if (coloumnValue.contains("nc")) {

			Filters.RankSize("Betweenness Centrality", attributeModel,rankingController);
		} else if (coloumnValue.contains("pr")) {
			
			Filters.RankSize("pageranks", attributeModel, rankingController);
		}
		
		// Preview
		   getpreview(model);
		   
		   System.out.println("Nodes-------->:"+graph.getNodeCount());
		   System.out.println("Edges-------->:"+graph.getEdgeCount());

		return graph;

	}
	
	public static Graph getgraph(GraphModel graphModel, Boolean IS_DIRECTED, Graph graph){
		graph = IS_DIRECTED ? graphModel.getDirectedGraph() : graphModel
				.getUndirectedGraph();
		return graph;
	}
	
	public static void calculatePageRank(GraphModel graphModel, AttributeModel attributeModel,PageRank pr, Boolean IS_DIRECTED, double epsilon, double probability){
		pr.setDirected(IS_DIRECTED);
		pr.setEpsilon(0.001);
		pr.setProbability(0.85);
		pr.execute(graphModel, attributeModel);
	}
	
	public static void addingcolumntoattributemodel(String columnname, AttributeModel attributeModel, Graph graph){
		attributeModel.getNodeTable().addColumn(columnname,AttributeType.DOUBLE);
		Node[] node1 = graph.getNodes().toArray();
		double neighborcount = 0.0;
		for (int i = 0; i < node1.length; i++) {
			Node[] neighbors = graph.getNeighbors(node1[i]).toArray();
			neighborcount = neighbors.length;
			node1[i].getAttributes().setValue(columnname, neighborcount);
		}
	}
	
	public static void getcentrality(GraphDistance distance , AttributeModel attributeModel, GraphModel graphModel, Boolean bool){
		distance.setDirected(bool);
		distance.execute(graphModel, attributeModel);

	}
	
	public static void getpreview(PreviewModel model){
		model.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS,Boolean.TRUE);
		model.getProperties().putValue(PreviewProperty.EDGE_OPACITY, 100f);
		model.getProperties().putValue(PreviewProperty.EDGE_THICKNESS,new Float(0.5f));
		model.getProperties().putValue(PreviewProperty.EDGE_CURVED,Boolean.TRUE);
		model.getProperties().putValue(PreviewProperty.NODE_LABEL_FONT,model.getProperties().getFontValue(PreviewProperty.NODE_LABEL_FONT).deriveFont(8));

	}
	
	public static void setLayouts(Map<String, Object> inner, GraphModel graphModel, int iterations){
		if (inner.get("name").toString().equals("YifanHuLayout")) {
			System.out.println("\nEntered YifanHuLayout \n");
			YifanHuLayout layout = new YifanHuLayout(null,new StepDisplacement(1f));
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
		} else if (inner.get("name").toString().equals("ForceAtlasLayout")) {
			System.out.println("\nEntered ForceAtlasLayout \n");
			ForceAtlasLayout layout = new ForceAtlasLayout(null);
			layout.setGraphModel(graphModel);
			layout.resetPropertiesValues();

			int speedInt = inner.containsKey("speed") ? Integer.parseInt(inner.get("speed").toString()) : 100;
			int convergedInt = inner.containsKey("converged") ? Integer.parseInt(inner.get("converged").toString()) : 1;
			double inertia = inner.containsKey("inertia") ? Double.parseDouble(inner.get("inertia").toString()) : 0.1;
			int gravityInt = inner.containsKey("gravity") ? Integer.parseInt(inner.get("gravity").toString()) : 50;
			int maxDisplacementInt = inner.containsKey("maxdisplacement") ? Integer.parseInt(inner.get("maxdisplacement").toString()) : 50;
			double speed = (double) speedInt;
			boolean converged = convergedInt == 1 ? true : false;
			double gravity = (double) gravityInt;
			double maxDisplacement = (double) maxDisplacementInt;

			layout.setSpeed(speed);
			layout.setConverged(converged);
			layout.setInertia(inertia);
			layout.setGravity(gravity);
			layout.setMaxDisplacement(maxDisplacement);

			layout.initAlgo();
			for (int i = 0; i < iterations && layout.canAlgo(); i++) {
				layout.goAlgo();
			}

			layout.endAlgo();
		}

		else if (inner.get("name").toString().equals("FruchtermanReingold")) {
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

	}
	
	public static void rankingColorByDegree(RankingController rankingController){
		Ranking degreeRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, Ranking.DEGREE_RANKING);
		AbstractColorTransformer colorTransformer = (AbstractColorTransformer) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT,Transformer.RENDERABLE_COLOR);
		colorTransformer.setColors(new Color[] { new Color(0xFEF0D9),new Color(0xB30000) });
		rankingController.transform(degreeRanking, colorTransformer);

	}
}
