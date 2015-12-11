package org.projectspinoza.twittergrapher.importers;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.EdgeDraft.EdgeType;
import org.gephi.io.importer.api.ImportUtils;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.projectspinoza.twittergrapher.TwitterGrapher;

public class ImporterTweet implements FileImporter, LongTask {

    private Reader reader;
    private ContainerLoader container;
    private Report report;
    private ProgressTicket progressTicket;
    private boolean cancel = false;

    @Override
    public boolean execute(ContainerLoader container) {
        // TODO Auto-generated method stub done.
        this.container = container;
        this.report = new Report();
        LineNumberReader lineReader = ImportUtils.getTextReader(reader);
        try {
            importData(lineReader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return !cancel;
    }

    private void importData(LineNumberReader reader) throws Exception {
        Progress.start(progressTicket);

        List<String> tweetList = getTweetsList(reader);

        for (String tweet : tweetList) {
            List<String[]> edges = buildEdges(tweet);
            addToContainer(edges);
        }

        Progress.finish(progressTicket);
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

    private ArrayList<String> getTweetsList(LineNumberReader reader)
            throws IOException {
        ArrayList<String> tweets = new ArrayList<String>();
        String sv = TwitterGrapher.search_value;
        while (reader.ready()) {
            String line = reader.readLine();
            if (line == null || line.isEmpty()) {
                continue;
            }

            line = line.toLowerCase();

            if (sv != null) {
                sv = sv.toLowerCase();
                String[] svp = sv.split(" ");
                if (svp.length > 1) {
                    boolean sv_found = false;
                    for (String sp : svp) {
                        if (line.contains(sp)) {
                            sv_found = true;
                        }
                    }
                    line = sv_found ? line : null;
                } else {
                    line = line.contains(sv) ? line : null;
                }
            }

            if (line != null) {
                tweets.add(line);
            }
        }

        return tweets;
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

    @Override
    public ContainerLoader getContainer() {
        // TODO Auto-generated method stub
        return container;
    }

    @Override
    public Report getReport() {
        // TODO Auto-generated method stub
        return report;
    }

    @Override
    public boolean cancel() {
        // TODO Auto-generated method stub
        cancel = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        // TODO Auto-generated method stub
        this.progressTicket = progressTicket;
    }

    @Override
    public void setReader(Reader reader) {
        // TODO Auto-generated method stub
        this.reader = reader;
    }

}
