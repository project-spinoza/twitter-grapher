package com.tg.factory.util;

import java.awt.Color;
import java.io.File;
import java.util.Map;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.api.Range;
import org.gephi.filters.plugin.graph.DegreeRangeBuilder.DegreeRangeFilter;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.api.RankingController;
import org.gephi.ranking.api.Transformer;
import org.gephi.ranking.plugin.transformer.AbstractColorTransformer;
import org.gephi.ranking.plugin.transformer.AbstractSizeTransformer;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;

import com.tg.importers.TwitterImportController;

import de.uni_leipzig.informatik.asv.gephi.chinesewhispers.ChineseWhispersClusterer;

public class Builder {
	
	public static Graph build(boolean IS_DIRECTED, Map<String, Object> settings){
	 ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
     pc.newProject();
     Workspace workspace = pc.getCurrentWorkspace();

     //Get models and controllers for this new workspace - will be useful later
     AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
     GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
     PreviewModel model = Lookup.getDefault().lookup(PreviewController.class).getModel();
     //ImportController importController = Lookup.getDefault().lookup(ImportController.class);
     ImportController importController = TwitterImportController.getInstance();
     FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
     RankingController rankingController = Lookup.getDefault().lookup(RankingController.class);

     //Import file       
     Container container;
     try {
         //File file = new File(getClass().getResource("/org/gephi/toolkit/demos/resources/polblogs.gml").toURI());
         //File file = new File("polblogs.gml");
         File file = new File("tweetfile.tweet");
         container = importController.importFile(file);
         container.getLoader().setEdgeDefault(EdgeDefault.DIRECTED);   //Force DIRECTED
     } catch (Exception ex) {
         ex.printStackTrace();
         return null;
     }

     //Append imported data to GraphAPI
     importController.process(container, new DefaultProcessor(), workspace);

     //See if graph is well imported
     //DirectedGraph graph = graphModel.getDirectedGraph();
     Graph graph = IS_DIRECTED ? graphModel.getDirectedGraph() : graphModel.getUndirectedGraph(); 
     System.out.println("Nodes: " + graph.getNodeCount());
     System.out.println("Edges: " + graph.getEdgeCount());

     //Filter      
     DegreeRangeFilter degreeFilter = new DegreeRangeFilter();
     degreeFilter.init(graph);
     degreeFilter.setRange(new Range(30, Integer.MAX_VALUE));     //Remove nodes with degree < 30
     Query query = filterController.createQuery(degreeFilter);
     GraphView view = filterController.filter(query);
     graphModel.setVisibleView(view);    //Set the filter result as the visible view

     //See visible graph stats
    /* UndirectedGraph graphVisible = graphModel.getUndirectedGraphVisible();
     System.out.println("Nodes: " + graphVisible.getNodeCount());
     System.out.println("Edges: " + graphVisible.getEdgeCount());*/
     
     //. cluster coloring...
     ChineseWhispersClusterer cwc = new ChineseWhispersClusterer();
     cwc.execute(graphModel);
     
     //Run YifanHuLayout for 100 passes - The layout always takes the current visible view
     YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
     layout.setGraphModel(graphModel);
     layout.resetPropertiesValues();
     layout.setOptimalDistance(200f);

     layout.initAlgo();
     for (int i = 0; i < 100 && layout.canAlgo(); i++) {
         layout.goAlgo();
     }
     layout.endAlgo();

     //Get Centrality
     GraphDistance distance = new GraphDistance();
     distance.setDirected(true);
     distance.execute(graphModel, attributeModel);

     //Rank color by Degree
     Ranking degreeRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, Ranking.DEGREE_RANKING);
     AbstractColorTransformer colorTransformer = (AbstractColorTransformer) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_COLOR);
     colorTransformer.setColors(new Color[]{new Color(0xFEF0D9), new Color(0xB30000)});
     rankingController.transform(degreeRanking,colorTransformer);

     //Rank size by centrality
     AttributeColumn centralityColumn = attributeModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
     Ranking centralityRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, centralityColumn.getId());
     AbstractSizeTransformer sizeTransformer = (AbstractSizeTransformer) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_SIZE);
     sizeTransformer.setMinSize(1);
     sizeTransformer.setMaxSize(10);
     rankingController.transform(centralityRanking,sizeTransformer);

     //Preview
     model.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
     //model.getProperties().putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(Color.GRAY));
     model.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, new Float(0.1f));
     model.getProperties().putValue(PreviewProperty.NODE_LABEL_FONT, model.getProperties().getFontValue(PreviewProperty.NODE_LABEL_FONT).deriveFont(8));
     
     
     
     //Export
     /*ExportController ec = Lookup.getDefault().lookup(ExportController.class);
     try {
         ec.exportFile(new File("headless_simple.pdf"));
     } catch (IOException ex) {
         ex.printStackTrace();
         return;
     }*/
     
     return graph;
     
	}
}
