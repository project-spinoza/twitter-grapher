package org.projectspinoza.twittergrapher.factory.util;

import java.util.Map;

import org.gephi.graph.api.GraphModel;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.layout.plugin.forceAtlas.ForceAtlasLayout;
import org.gephi.layout.plugin.fruchterman.FruchtermanReingold;

public class Layouts {
	
	public Layouts() {
	}
	
	public static void YifanHuLayout (GraphModel graphModel, Map<String, Object> layoutSettings, int iterations){
		
		YifanHuLayout layout = new YifanHuLayout(null,new StepDisplacement(1f));
		layout.setGraphModel(graphModel);
		layout.resetPropertiesValues();
		int distanceInt = layoutSettings.containsKey("distance") ? Integer.parseInt(layoutSettings.get("distance").toString()) : 200;
		float distance = (float) distanceInt;
		layout.setOptimalDistance(distance);

		layout.initAlgo();
		for (int i = 0; i < iterations && layout.canAlgo(); i++) {
			layout.goAlgo();
		}
		layout.endAlgo();
	}
	
	public static void ForceAtlasLayout (GraphModel graphModel, Map<String, Object> layoutSettings, int iterations){
	
		ForceAtlasLayout layout = new ForceAtlasLayout(null);
		layout.setGraphModel(graphModel);
		layout.resetPropertiesValues();

		int speedInt = layoutSettings.containsKey("speed") ? Integer.parseInt(layoutSettings.get("speed").toString()) : 100;
		int convergedInt = layoutSettings.containsKey("converged") ? Integer.parseInt(layoutSettings.get("converged").toString()) : 1;
		double inertia = layoutSettings.containsKey("inertia") ? Double.parseDouble(layoutSettings.get("inertia").toString()) : 0.1;
		int gravityInt = layoutSettings.containsKey("gravity") ? Integer.parseInt(layoutSettings.get("gravity").toString()) : 50;
		int maxDisplacementInt = layoutSettings.containsKey("maxdisplacement") ? Integer.parseInt(layoutSettings.get("maxdisplacement").toString()) : 50;
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
	
	public static void FruchtermanReingoldLayout (GraphModel graphModel, Map<String, Object> layoutSettings, int iterations){
		
		FruchtermanReingold layout = new FruchtermanReingold(null);
		layout.setGraphModel(graphModel);
		layout.resetPropertiesValues();

		int areaInt = layoutSettings.containsKey("area") ? Integer.parseInt(layoutSettings.get("area").toString()) : 100;
		int speedInt = layoutSettings.containsKey("speed") ? Integer.parseInt(layoutSettings.get("speed").toString()) : 50;
		int gravityInt = layoutSettings.containsKey("gravity") ? Integer.parseInt(layoutSettings.get("gravity").toString()) : 0;
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
