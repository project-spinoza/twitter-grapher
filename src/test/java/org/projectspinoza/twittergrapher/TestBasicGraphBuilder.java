package org.projectspinoza.twittergrapher;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.ranking.api.RankingController;
import org.gephi.statistics.plugin.GraphDistance;
import org.junit.Before;
import org.junit.Test;
import org.openide.util.Lookup;
import org.projectspinoza.twittergrapher.configuration.Configuration;
import org.projectspinoza.twittergrapher.factory.util.BasicGraphBuilder;
import org.projectspinoza.twittergrapher.factory.util.DataSourceType;
import org.projectspinoza.twittergrapher.factory.util.GraphFilter;
import org.projectspinoza.twittergrapher.factory.util.GraphLayout;
import org.projectspinoza.twittergrapher.factory.util.GraphPreview;
import org.projectspinoza.twittergrapher.importers.DataSourceImporter;

public class TestBasicGraphBuilder {
	BasicGraphBuilder bgb;
	private static Logger log = LogManager.getLogger(BasicGraphBuilder.class);
	private ProjectController projectController;
	private Workspace workspace; //. depends on pc
	private GraphModel graphModel;
	private Graph graph; //. depends on graphModel
	
	private PreviewModel previewModel;
	private ImportController importController; //. depends on workspace
	private Container graphContainer; //. depends on importController
	private RankingController rankingController;
	private AttributeModel attributeModel;
	private GraphFilter graphFilter; //. AttributeModel, Graph, AttributeColumn, AttributeModel, RankingController
	private GraphLayout graphLayout; //. depends on GraphModel
	private GraphPreview graphPreview; //. depends on GraphPreviewModel
	Configuration config ;
	
	@Before
	public void setup() throws FileNotFoundException{
		bgb = new BasicGraphBuilder();
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
		
	    config = Configuration.getInstance();
	    config.setDataSource("inputfile");
	    config.setFileName("tweets.txt");
	    config.setSearchValue("data");
	    config.setPagerankthreshHold(30L);
	    config.setNodecentralitythreshHold(30L);
	    config.setNeighborcountThreshHold(30L);
	    config.setNeighborcountThreshHold(50L);
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
		graph =  graphModel.getDirectedGraph() ;
	} 
	
	@Test
	public void TestTotalNodes(){
		
		int expected = 201;
		int result = graph.getNodeCount();
		assertEquals(expected, result);

	}
	@Test
	public void TestTotalEdges(){
		
		int expected = 585;
		int result = graph.getEdgeCount();
		assertEquals(expected, result);
		
	}
	@Test
	public void ColumnAddedTest(){
		int expected = 3;	
		 Graph testGraph; //. depends on graphModel
		testGraph = bgb.addColumnAttributeModel(graph,attributeModel,"NeighborCount");
		int result = testGraph.getNode(1).getAttributes().countValues();
		assertEquals(expected, result);
		
		
	}
	@Test
	public void TestPageranksFilter(){
	    Graph testGraph; //. depends on graphModel
		int expected = 140;
		String columnname = "pageranks";
		AttributeColumn column = attributeModel.getNodeTable().getColumn(columnname);
		double prThreshHold = (double) config.getPagerankthreshHold() / 100;
		testGraph = graphFilter.removePercentageNodes(graph, column, prThreshHold ,columnname);
		int result = testGraph.getNodeCount();
		assertEquals(expected, result);
	}
	@Test
	public void TestBetweennessCentrality(){
		 Graph testGraph;
		int expected = 140;
		String columnname = "Betweenness Centrality";
		AttributeColumn column = attributeModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
		testGraph = graphFilter.removePercentageNodes(graph, column, config.getNodecentralitythreshHold()/100, columnname);
		int result = graph.getNodeCount();
		assertEquals(expected, result);
	}
	
	@Test
	public void TestNeighbourCount(){
		int expected = 100;
		Graph testGraph;
		String columnname = "NeighborCount";
		AttributeColumn column = attributeModel.getNodeTable().getColumn(columnname);
		testGraph = graphFilter.removePercentageNodes(graph, column, config.getNeighborcountThreshHold()/100, columnname);
	    int result = testGraph.getNodeCount();
	    assertEquals(expected, result);
	}
	
}
