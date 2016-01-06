package org.projectspinoza.twittergrapher.importers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.EdgeDraft.EdgeType;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.impl.ImportContainerImpl;

public class DataSourceImporter {
	private ImportContainerImpl container;

	public ImportContainerImpl importDataSource() {

		List<String> tweets = null;
		try {
			tweets = new DataImporter().importDataList();
		} catch (IOException e) {
			e.printStackTrace();
		}

		container = new ImportContainerImpl();
		for (String tweet : tweets) {
			List<String[]> edges = buildEdges(tweet);
			addToContainer(edges);
		}
		return container;
	}

	private void addToContainer(List<String[]> edges) {

		for (String[] edge : edges) {
			
			EdgeDraft edgeDraft = null;
			String edgeId = edge[0] + "-" + edge[1];

			if (container.edgeExists(edgeId)) {
				edgeDraft = container.getEdge(edgeId);

				float weight = edgeDraft.getWeight();
				weight += 1;
				edgeDraft.setWeight(weight);
				continue;
			}

			edgeDraft = container.factory().newEdgeDraft();
			edgeDraft.setId(edgeId);
			edgeDraft.setWeight(1f);
			edgeDraft.setType(EdgeType.DIRECTED);
			// edgeDraft.setType(EdgeType.UNDIRECTED);
			edgeDraft.setLabel("CO_HASHTAG");

			NodeDraft source = getOrCreateNodeDraft(edge[0], edge[0]);
			NodeDraft target = getOrCreateNodeDraft(edge[1], edge[1]);

			edgeDraft.setSource(source);
			edgeDraft.setTarget(target);

			container.addEdge(edgeDraft);
		}
	}

	/**
	 * creates or returns nodeDraft with the id @id and label @label
	 * 
	 * @param id
	 * @param label
	 * @return NodeDraft
	 */
	private NodeDraft getOrCreateNodeDraft(String id, String label) {

		NodeDraft nodeDraft = null;

		if (container.nodeExists(id)) {
			nodeDraft = container.getNode(id);
		} else {
			nodeDraft = container.factory().newNodeDraft();
			nodeDraft.setId(id);
			nodeDraft.setLabel(label);

			container.addNode(nodeDraft);
		}

		return nodeDraft;
	}

	private List<String[]> buildEdges(String tweet) {

		List<String[]> edges = new ArrayList<String[]>();

		String parts[] = tweet.replaceAll("/[^A-Za-z0-9 #]/", " ")
				.replace("\n", " ").replace("\r", " ")
				.replaceAll("\\P{Print}", " ").split(" ");

		Set<String> tags = new HashSet<String>();

		for (String part : parts) {
			part = part.trim();
			if (part.length() < 2)
				continue;
			if (part.equals("#rt"))
				continue;

			if (part.startsWith("#")) {
				// . splits hashtags of type: #tag1#tag2...
				if ((part.length() - part.replace("#", "").length()) > 1) {
					String[] subParts = part.split("#");
					for (String sb : subParts) {
						sb = sb.replaceAll("[^a-zA-Z0-9_-]", "").trim();
						sb = sb.replace("\\s", "");
						if (sb.length() > 1) {
							tags.add(sb);
						}
					}
					continue;
				}
				part = part.replaceAll("[^a-zA-Z0-9_-]", "").trim();
				part = part.replace("\\s", "");
				if (part.length() > 1) {
					tags.add(part);
				}
			}
		}

		List<String> taglist = new ArrayList<String>();
		taglist.addAll(tags);
		Collections.sort(taglist);
		if (taglist.size() < 2)
			return edges;

		for (int i = 0; i < taglist.size() - 1; i++) {
			for (int j = i + 1; j < taglist.size(); j++) {
				String edge[] = new String[2];
				edge[0] = taglist.get(i);
				edge[1] = taglist.get(j);
				edges.add(edge);
			}
		}

		return edges;
	}
}
