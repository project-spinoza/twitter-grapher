package org.projectspinoza.twittergrapher.importers;

import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;


public class ImporterElasticSearch {

	private TransportClient elasticSearchClient;
	private Settings client_settings;
	JsonObject elasticsearch_cred_json;

	public ImporterElasticSearch(TransportClient esClient) {
		this.elasticSearchClient = esClient;
	}

	public ImporterElasticSearch(JsonObject elasticsearch) {

		this.elasticsearch_cred_json = elasticsearch;
	}

	public ImporterElasticSearch() {
	}

	public boolean connect() {

		return connect(this.elasticsearch_cred_json);
	}

	public boolean connect(JsonObject elasticsearch_credentials) {

		if (elasticsearch_credentials == null) {

			// get default configuration
			this.client_settings = ImmutableSettings.settingsBuilder()
					.put("cluster.name", "elasticsearch").build();

			setElasticSearchClient(new TransportClient(this.client_settings));

			getElasticSearchClient().addTransportAddress(
					new InetSocketTransportAddress("127.0.0.1", 9300));

		} else {

			if (elasticsearch_credentials.containsKey("cluster.name")
					&& !elasticsearch_credentials.getString("cluster.name")
							.trim().isEmpty()) {

				this.client_settings = ImmutableSettings
						.settingsBuilder()
						.put("cluster.name",
								elasticsearch_credentials
										.getString("cluster.name")).build();
			} else {
				this.client_settings = ImmutableSettings.settingsBuilder()
						.put("cluster.name", "elasticsearch").build();
			}

			setElasticSearchClient(new TransportClient(this.client_settings));
			getElasticSearchClient().addTransportAddress(
					new InetSocketTransportAddress(elasticsearch_credentials
							.getString("host"), elasticsearch_credentials
							.getInteger("port")));
		}

		return verifyConnection();
	}

	public boolean verifyConnection(TransportClient client) {
		ImmutableList<DiscoveryNode> nodes = client.connectedNodes();
		if (nodes.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	public boolean verifyConnection() {
		ImmutableList<DiscoveryNode> nodes = this.elasticSearchClient
				.connectedNodes();
		if (nodes.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	public List<String> Search(String indexname, String typename,
					   String query_terms, int size) {

		List<String> response_list = new ArrayList<String> ();
		
		SearchResponse response = getElasticSearchClient().prepareSearch(indexname)
				.setTypes(typename)
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(QueryBuilders.queryString(query_terms)).setFrom(0)
				.setSize(size).setExplain(true).execute().actionGet();
		
		for (SearchHit hit : response.getHits()) {
			response_list.add(hit.getSource().get("text").toString());
		}
		return response_list;
	}

	public TransportClient getElasticSearchClient() {
		return elasticSearchClient;
	}

	public void setElasticSearchClient(TransportClient elasticSearchClient) {
		this.elasticSearchClient = elasticSearchClient;
	}

}
