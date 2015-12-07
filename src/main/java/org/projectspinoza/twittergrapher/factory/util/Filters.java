package org.projectspinoza.twittergrapher.factory.util;

import java.util.Arrays;
import java.util.Comparator;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.api.Range;
import org.gephi.filters.plugin.attribute.AttributeRangeBuilder;
import org.gephi.filters.plugin.graph.DegreeRangeBuilder.DegreeRangeFilter;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.api.RankingController;
import org.gephi.ranking.api.Transformer;
import org.gephi.ranking.plugin.transformer.AbstractSizeTransformer;
import org.openide.util.Lookup;

public class Filters {

	public static GraphView FilterBasedOnRange(AttributeModel attributeModel,
			String coloumn, double range) {

		GraphModel graphModel = Lookup.getDefault()
				.lookup(GraphController.class).getModel();
		PreviewModel model = Lookup.getDefault()
				.lookup(PreviewController.class).getModel();
		AttributeColumn pagerankCol = attributeModel.getNodeTable().getColumn(
				coloumn);
		FilterController filterController = Lookup.getDefault().lookup(
				FilterController.class);

		AttributeRangeBuilder.AttributeRangeFilter attributeRangeFilter = new AttributeRangeBuilder.AttributeRangeFilter(
				pagerankCol);
		Query pagerankQuery = filterController
				.createQuery(attributeRangeFilter);

		// Set the filters parameters - Keep nodes above 0.010
		attributeRangeFilter.setRange(new Range(range, Double.MAX_VALUE));

		// Execute the filter query
		GraphView view = filterController.filter(pagerankQuery);

		return view;
	}

	public static GraphView degreerangefilter(Graph graph) {
		FilterController filterController = Lookup.getDefault().lookup(
				FilterController.class);

		DegreeRangeFilter degreeFilter = new DegreeRangeFilter();
		degreeFilter.init(graph);
		degreeFilter.setRange(new Range(30, Integer.MAX_VALUE));
		// Remove nodes with degree < 30
		Query query = filterController.createQuery(degreeFilter);
		GraphView view2 = filterController.filter(query);
		return view2;
	}

	public static Graph RemovePercentageNodes(Graph graph,
			AttributeColumn column, double threshhold, String columnValue) {
		int nodestoberemoved = 0;
		Node[] nodesN = sortgraphBasedonColoumnValue(graph, column);

		nodestoberemoved = (int) Math.ceil(nodesN.length * threshhold);
		/*
		 * System.out .println("\n\n\tNodes to be removed ---- " +
		 * nodestoberemoved); System.out .println("\n\n\tcoloumn ---- "
		 * +columnValue);
		 */
		for (int i = 0; i < nodestoberemoved; i++) {
			/*
			 * System.out.println("\n\n\tNodes Removed -----\t" + nodesN[i] +
			 * "----\t" + nodesN[i].getNodeData().getAttributes()
			 * .getValue(columnValue)+"\n\n");
			 */
			graph.removeNode(nodesN[i]);
		}

		return graph;
	}

	public static void RankSize(String column, AttributeModel attributeModel,
			RankingController rankingController) {
		AttributeColumn Column = attributeModel.getNodeTable()
				.getColumn(column);

		Ranking PageRankRanking = rankingController.getModel().getRanking(
				Ranking.NODE_ELEMENT, Column.getId());

		AbstractSizeTransformer sizeTransformer = (AbstractSizeTransformer) rankingController
				.getModel().getTransformer(Ranking.NODE_ELEMENT,
						Transformer.RENDERABLE_SIZE);
		sizeTransformer.setMinSize(1);
		sizeTransformer.setMaxSize(10);
		rankingController.transform(PageRankRanking, sizeTransformer);
	}

	public static Node[] sortgraphBasedonColoumnValue(Graph graph,
			AttributeColumn column) {
		Node[] nodesN = graph.getNodes().toArray();
		if (column != null) {
			Arrays.sort(nodesN, new Comparator<Node>() {

				@Override
				public int compare(Node o1, Node o2) {
					Number n1 = (Number) o1.getAttributes().getValue(
							column.getIndex());
					Number n2 = (Number) o2.getAttributes().getValue(
							column.getIndex());
					if (n1.doubleValue() < n2.doubleValue()) {
						return -1;
					} else if (n1.doubleValue() > n2.doubleValue()) {
						return 1;
					} else {
						return 0;
					}
				}
			});
		}
		return nodesN;
	}

}
