package org.projectspinoza.twittergrapher.factory.util;

import io.vertx.core.json.JsonObject;

import java.awt.Color;
import java.io.File;
import java.util.Map;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
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

		AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		PreviewModel model = Lookup.getDefault().lookup(PreviewController.class).getModel();
		ImportController importController = Lookup.getDefault().lookup(ImportController.class);
//		FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
		RankingController rankingController = Lookup.getDefault().lookup(RankingController.class);
		PageRank pr = new PageRank();
		double epsilon =0.001 ;
		double probability = 0.85;
		Graph getgraph=null;
		String coloumnValue = (String) settings.get("nsb");
		String columnname = "NeighborCount";
//		double range = 0.0;
		int pagerankthreshhold = Integer.parseInt(settings.get("prt").toString());
		Double neighborcountrange = Double.parseDouble(settings.get("neighborcountrange").toString());
		int nodecentrality = (Integer) settings.get("nct");
		double Nodecentralitythreshhold = (double) nodecentrality / 100;
		double prthreshhold = (double) pagerankthreshhold / 100;
	
		// Getting layout Options
		JsonObject layoutAlgoJson = new JsonObject(settings.get("la").toString());
		Map<String, Object> layoutSettings = layoutAlgoJson.getMap();
		JsonObject sourcesCredJson = (JsonObject) ((Map<String, Object>) settings.get("settings")).get("sources_cred");
		String inputFile = sourcesCredJson.getString("file");
		String dataSource = ((Map<String, Object>) settings.get("settings")).get("source_selected").toString();
		
		// Import file
		Container container = null;
		try{
			if (DataSourceType.contains(dataSource)) {
				container = (new DataSourceImporter()).importDataSource(settings);
				if (container == null){
					System.out.println("Error generating graph from "+inputFile);
				}
			} else if (dataSource.equals("graphfile")){
				File file = new File(inputFile);
				container = importController.importFile(file);
				if (container == null){
					System.out.println("No graph found in "+inputFile+". Make sure you are using correct graph file format.");
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
		Graph graph = getgraph(graphModel,IS_DIRECTED,getgraph); 
		calculatePageRank(graphModel, attributeModel, pr, IS_DIRECTED, epsilon, probability);
		addingcolumntoattributemodel(graph , attributeModel, columnname);
		ChineseWhispersClusterer cwc = new ChineseWhispersClusterer();
		cwc.execute(graphModel);

		// Run Layout for 100 passes - The layout always takes the
		int iterations = layoutSettings.containsKey("it") ? Integer.parseInt(layoutSettings.get("it").toString()) : 50;
		setLayouts(graphModel, layoutSettings, iterations);
		
		GraphDistance distance = new GraphDistance();
		getcentrality(graphModel, attributeModel, distance , true);

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
	
	public static void addingcolumntoattributemodel(Graph graph, AttributeModel attributeModel, String columnname){
		attributeModel.getNodeTable().addColumn(columnname,AttributeType.DOUBLE);
		Node[] node1 = graph.getNodes().toArray();
		double neighborcount = 0.0;
		for (int i = 0; i < node1.length; i++) {
			Node[] neighbors = graph.getNeighbors(node1[i]).toArray();
			neighborcount = neighbors.length;
			node1[i].getAttributes().setValue(columnname, neighborcount);
		}
	}
	
	public static void getcentrality(GraphModel graphModel,  AttributeModel attributeModel, GraphDistance distance , Boolean bool){
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
	
	public static void setLayouts(GraphModel graphModel, Map<String, Object> layoutSettings, int iterations){
		
		if (layoutSettings.get("name").toString().equals("YifanHuLayout")) {
			
			System.out.println("\nLayout: YifanHuLayout");
			Layouts.YifanHuLayout(graphModel, layoutSettings, iterations);
		} else if (layoutSettings.get("name").toString().equals("ForceAtlasLayout")) {
			
			System.out.println("\nLayout: ForceAtlasLayout");
			Layouts.ForceAtlasLayout(graphModel, layoutSettings, iterations);
		} else if (layoutSettings.get("name").toString().equals("FruchtermanReingold")) {
			
			System.out.println("\nLayout: FruchtermanReingold");
			Layouts.FruchtermanReingoldLayout(graphModel, layoutSettings, iterations);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static void rankingColorByDegree(RankingController rankingController){
		Ranking degreeRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, Ranking.DEGREE_RANKING);
		AbstractColorTransformer colorTransformer = (AbstractColorTransformer) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT,Transformer.RENDERABLE_COLOR);
		colorTransformer.setColors(new Color[] { new Color(0xFEF0D9),new Color(0xB30000) });
		rankingController.transform(degreeRanking, colorTransformer);

	}
}
