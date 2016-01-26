package org.projectspinoza.twittergrapher.factory.util;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
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
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.ranking.api.RankingController;
import org.gephi.statistics.plugin.GraphDistance;
import org.gephi.statistics.plugin.PageRank;
import org.openide.util.Lookup;
import org.projectspinoza.twittergrapher.configuration.Configuration;
import org.projectspinoza.twittergrapher.importers.DataSourceImporter;

public class BasicGraphBuilder {
	
	private static Logger log = LogManager.getLogger(BasicGraphBuilder.class);
	
	private ProjectController projectController;
	private Workspace workspace; //. depends on pc
	private GraphModel graphModel;
	private Graph graph; //. depends on graphModel
	private PreviewModel previewModel;
	private ImportController importController; //. depends on workspace
	private Container graphContainer; //. depends on importController
	private RankingController rankingController;
	AttributeModel attributeModel;
	
	private GraphFilter graphFilter; //. AttributeModel, Graph, AttributeColumn, AttributeModel, RankingController
	private GraphLayout graphLayout; //. depends on GraphModel
	private GraphPreview graphPreview; //. depends on GraphPreviewModel
	
	public BasicGraphBuilder(){
		initialize();
	}
	
	public void initialize(){
		projectController = Lookup.getDefault().lookup(ProjectController.class);
		projectController.newProject();
		workspace = projectController.getCurrentWorkspace();
		
		attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
		graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		previewModel = Lookup.getDefault().lookup(PreviewController.class).getModel();
		importController = Lookup.getDefault().lookup(ImportController.class);
		rankingController = Lookup.getDefault().lookup(RankingController.class);
		
		
		graphFilter = new GraphFilter();
		graphLayout = new GraphLayout();
		graphPreview = new GraphPreview(); 
	}
	
	public Graph buildBasicGraph(boolean IS_DIRECTED) throws FileNotFoundException{
		Configuration config = Configuration.getInstance();
		//System.out.println(config);
		if (DataSourceType.contains(config.getDataSource())) {
			graphContainer = (new DataSourceImporter()).importDataSource();
			if (graphContainer == null){
				log.error("Error generating graph from "+config.getFileName());
			}
		} else if (config.getDataSource().equals("graphfile")){
			File file = new File(config.getFileName());
			graphContainer = importController.importFile(file);
			if (graphContainer == null){
				log.error("No graph found in "+config.getFileName()+". Make sure you are using correct graph file format.");
			}
		}else {
			log.error("Unsupported file format.");
		}
		
		importController.process(graphContainer, new DefaultProcessor(), workspace);
		graph = IS_DIRECTED ? graphModel.getDirectedGraph() : graphModel.getUndirectedGraph();
		calculatePageRank(IS_DIRECTED);
		graph = addColumnAttributeModel(graph, attributeModel, "NeighborCount");
		//graphLayout.applayLayouts(graphModel);
		
		GraphDistance graphDistance = new GraphDistance();
		getcentrality(graphModel, attributeModel, graphDistance , true);
		applayGraphFilters(config);
		graphLayout.applayLayouts(graphModel);
		graphPreview.setGraphPreview(previewModel);
		
		log.info("Nodes["+graph.getNodeCount()+"]");
		log.info("Edges["+graph.getEdgeCount()+"]");
		
		return graph;
	}
	
	public Graph addColumnAttributeModel(Graph graph, AttributeModel attributeModel, String columnName){
		attributeModel.getNodeTable().addColumn(columnName, AttributeType.DOUBLE);
		Node[] node1 = graph.getNodes().toArray();
		double neighborcount = 0.0;
		for (int i = 0; i < node1.length; i++) {
			Node[] neighbors = graph.getNeighbors(node1[i]).toArray();
			neighborcount = neighbors.length;
			node1[i].getAttributes().setValue(columnName, neighborcount);
		}
		return graph;
	}
	
	public void calculatePageRank(Boolean IS_DIRECTED){
		PageRank pr = new PageRank();
		pr.setDirected(IS_DIRECTED);
		pr.setEpsilon(0.001);
		pr.setProbability(0.85);
		pr.execute(graphModel, attributeModel);
	}
	
	public void getcentrality(GraphModel graphModel,  AttributeModel attributeModel, GraphDistance distance , Boolean bool){
		distance.setDirected(bool);
		distance.execute(graphModel, attributeModel);
	}
	
	public void applayGraphFilters(Configuration config){
		if (config.getPagerankthreshHold() != 0) {
			String columnname = "pageranks";
			AttributeColumn column = attributeModel.getNodeTable().getColumn(columnname);
			double prThreshHold = (double) config.getPagerankthreshHold() / 100;
			graph = graphFilter.removePercentageNodes(graph, column, prThreshHold ,columnname);
		}
		if (config.getNodecentralitythreshHold() != 0) {
			String columnname = "Betweenness Centrality";
			AttributeColumn column = attributeModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
			graph = graphFilter.removePercentageNodes(graph, column, config.getNodecentralitythreshHold()/100, columnname);
		}
		if (config.getNeighborcountThreshHold() != 0) {
			String columnname = "NeighborCount";
			AttributeColumn column = attributeModel.getNodeTable().getColumn(columnname);
			graph = graphFilter.removePercentageNodes(graph, column, config.getNeighborcountThreshHold()/100, columnname);
		}
		graphPreview.rankingColorByDegree(rankingController);
		if (config.getNodeSizeBy().equals("nc")) {
			graphFilter.rankSize("Betweenness Centrality", attributeModel,rankingController);
		} else if (config.getNodeSizeBy().equals("pr")) {
			graphFilter.rankSize("pageranks", attributeModel, rankingController);
		}
	}
	
}
