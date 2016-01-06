package org.projectspinoza.twittergrapher.factory.util;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.gephi.graph.api.GraphModel;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.layout.plugin.forceAtlas.ForceAtlasLayout;
import org.gephi.layout.plugin.fruchterman.FruchtermanReingold;
import org.projectspinoza.twittergrapher.configuration.Configuration;

import de.uni_leipzig.informatik.asv.gephi.chinesewhispers.ChineseWhispersClusterer;

public class GraphLayout {
	
	private static Logger log = LogManager.getLogger(GraphLayout.class);
	
	public GraphLayout() {
	}
	
	public void applayLayouts(GraphModel graphModel){
		Configuration config = Configuration.getInstance();
		ChineseWhispersClusterer cwc = new ChineseWhispersClusterer();
		cwc.execute(graphModel);
		
		String layoutAlgorithmName = config.getLayoutAlgoName();
		if (layoutAlgorithmName.equals("YifanHuLayout")) {
			log.info("\nLayout: YifanHuLayout");
			yifanHuLayout(graphModel, config);
			
		} else if (layoutAlgorithmName.equals("ForceAtlasLayout")) {
			log.info("\nLayout: ForceAtlasLayout");
			forceAtlasLayout(graphModel, config);
			
		} else if (layoutAlgorithmName.equals("FruchtermanReingold")) {
			
			log.info("\nLayout: FruchtermanReingold");
			fruchtermanReingoldLayout(graphModel, config);
		}
	}
	public void yifanHuLayout (GraphModel graphModel, Configuration config){
		
		YifanHuLayout layout = new YifanHuLayout(null,new StepDisplacement(1f));
		
		layout.setGraphModel(graphModel);
		layout.resetPropertiesValues();
		layout.setOptimalDistance(config.getLayoutAlgoDistance());
		layout.initAlgo();
		
		for (int i = 0; i < config.getLayoutAlgoIterations() && layout.canAlgo(); i++) {
			layout.goAlgo();
		}
		
		layout.endAlgo();
	}
	
	public void forceAtlasLayout (GraphModel graphModel, Configuration config){
	
		ForceAtlasLayout layout = new ForceAtlasLayout(null);
		layout.setGraphModel(graphModel);
		layout.resetPropertiesValues();

		layout.setSpeed(config.getForceAtlasLayoutSpeed());
		layout.setConverged(config.forceAtlasLayoutIsConverged());
		layout.setInertia(config.getForceAtlasLayoutInertia());
		layout.setGravity(config.getForceAtlasLayoutGravity());
		layout.setMaxDisplacement(config.getForceAtlasLayoutMaxDisplacement());

		layout.initAlgo();
		for (int i = 0; i < config.getLayoutAlgoIterations() && layout.canAlgo(); i++) {
			layout.goAlgo();
		}
		layout.endAlgo();
	}
	
	public void fruchtermanReingoldLayout (GraphModel graphModel, Configuration config){
		
		FruchtermanReingold layout = new FruchtermanReingold(null);
		layout.setGraphModel(graphModel);
		layout.resetPropertiesValues();

		layout.setArea(config.getFruchtermanReingoldLayoutArea());
		layout.setSpeed(config.getFruchtermanReingoldLayoutSpeed());
		layout.setGravity(config.getFruchtermanReingoldLayoutGravity());

		layout.initAlgo();
		
		for (int i = 0; i < config.getLayoutAlgoIterations() && layout.canAlgo(); i++) {
			layout.goAlgo();
		}
		
		layout.endAlgo();
	}
	
	public void chineseWhispersClusterer(GraphModel graphModel){
		ChineseWhispersClusterer cwc = new ChineseWhispersClusterer();
		cwc.execute(graphModel);
	}

}
