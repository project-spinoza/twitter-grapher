package org.projectspinoza.twittergrapher.factory.util;

import java.awt.Color;
import java.awt.Font;

import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.api.RankingController;
import org.gephi.ranking.api.Transformer;
import org.gephi.ranking.plugin.transformer.AbstractColorTransformer;

public class GraphPreview {
	
	public void rankColorByDegree(){
	}
	
	public void setGraphPreview(PreviewModel previewModel){
		previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS,Boolean.TRUE);
		previewModel.getProperties().putValue(PreviewProperty.EDGE_OPACITY, 100f);
		previewModel.getProperties().putValue(PreviewProperty.EDGE_THICKNESS,new Float(0.5f));
		previewModel.getProperties().putValue(PreviewProperty.EDGE_CURVED,Boolean.TRUE);
		previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_FONT,previewModel.getProperties().getFontValue(PreviewProperty.NODE_LABEL_FONT).deriveFont(8));
	}
	public void showNodeLabels(PreviewModel previewModel, Boolean show){
		previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, show);
	}
	public void setEdgeOpacity(PreviewModel previewModel, Float opacity){
		previewModel.getProperties().putValue(PreviewProperty.EDGE_OPACITY, opacity);
	}
	public void setEdgeThickness(PreviewModel previewModel, Float thickness){
		previewModel.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, thickness);
	}
	public void setEdgeCurved(PreviewModel previewModel, Boolean curved){
		previewModel.getProperties().putValue(PreviewProperty.EDGE_CURVED, curved);
	}
	public void setNodeLabelFont(PreviewModel previewModel, Font font){
		previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_FONT, font);
	}
	
	@SuppressWarnings("rawtypes")
	public void rankingColorByDegree(RankingController rankingController){
		Ranking degreeRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, Ranking.DEGREE_RANKING);
		AbstractColorTransformer colorTransformer = (AbstractColorTransformer) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT,Transformer.RENDERABLE_COLOR);
		colorTransformer.setColors(new Color[] { new Color(0xFEF0D9),new Color(0xB30000) });
		rankingController.transform(degreeRanking, colorTransformer);
	}
}
