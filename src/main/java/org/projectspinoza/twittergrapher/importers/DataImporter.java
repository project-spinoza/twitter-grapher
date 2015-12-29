package org.projectspinoza.twittergrapher.importers;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
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
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

public class DataImporter {

	private TransportClient elasticSearchClient;
	private Settings clientSettings;
	private JsonObject sourcesCredJson;
	private Map<String, Object> elasticSearchCred = new HashMap<String, Object>();
	private Map<String, Object> mongodbCred = new HashMap<String, Object>();
	private Map<String, Object> mysqlCred = new HashMap<String, Object>();
	private String inputFile;
	private String queryString;
	private String sourceSelected;
	List<String> responseListStrContainer = null;

	public DataImporter(Map<String, Object> settings) {

		this.sourcesCredJson = (JsonObject) settings.get("sources_cred");
		this.elasticSearchCred = sourcesCredJson.getJsonObject(
				"elasticsearch").getMap();
		this.mongodbCred = sourcesCredJson.getJsonObject("mongodb").getMap();
		this.mysqlCred = sourcesCredJson.getJsonObject("mysql").getMap();
		this.inputFile = sourcesCredJson.getString("file");
		this.queryString = (String) settings.get("query_str");
		this.sourceSelected = (String) settings.get("source_selected");

	}

	public List<String> importDataList() throws IOException {

		List<String> response_tweets_list = null;

		switch (sourceSelected) {
		case "elasticsearch":

			if (elasticsearch_connect()) {
				response_tweets_list = elasticsearch_search(
						this.elasticSearchCred.get("index").toString(),
						this.elasticSearchCred.get("type").toString(),
						this.queryString, 5000);
			} else {
				System.out.println("Error connecting to elasticsearch...!");
				return null;
			}
			break;
		case "mongodb":
			response_tweets_list = mongodb_search();
			break;
		case "mysql":
			response_tweets_list = mysqlDataReader();
			break;
		case "inputfile":
			response_tweets_list = fileDataReader();
			break;
		default:
			break;
		}

		return response_tweets_list;
	}

	private List<String> mysqlDataReader() {

		List<String> responseList = new ArrayList<String>();
		Connection connection = null;
		Statement statement = null;

		try {

			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://"
					+ this.mysqlCred.get("host").toString() + ":"
					+ this.mysqlCred.get("port").toString() + "/"
					+ this.mysqlCred.get("database").toString(), "root", "");

			if (connection != null) {

				String[] query_terms = this.queryString.trim().split(" ");
				String query_like = " LIKE '%" + query_terms[0] + "%' ";

				for (int i = 1; i < query_terms.length; i++) {
					query_like.concat("or " + this.mysqlCred.get("data_column").toString()
							+ " LIKE '%" + query_terms[i] + "%' ");
				}

				String query_mysql = "SELECT "
						+ this.mysqlCred.get("data_column").toString()
						+ " from "
						+ this.mysqlCred.get("table_name").toString()
						+ " where "
						+ this.mysqlCred.get("data_column").toString()
						+ query_like;

				statement = connection.createStatement();
				ResultSet results_set = statement.executeQuery(query_mysql);

				while (results_set.next()) {
					String tweet = results_set.getString(this.mysqlCred.get(
							"data_column").toString());
					try{
						responseList.add(new JSONObject(tweet).get("text")
								.toString());
					} catch (JSONException | DecodeException e) {
					}

				}

			} else {
				System.out.println("Database connection failed...");
			}

		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			return null;
		}

		return responseList;
	}

	private List<String> mongodb_search() {

		MongoClient mongoClient = new MongoClient(this.mongodbCred.get("host")
				.toString(), Integer.parseInt(this.mongodbCred.get("port")
				.toString()));
		MongoDatabase db = mongoClient.getDatabase(this.mongodbCred.get(
				"database").toString());

		FindIterable<Document> iterable = db.getCollection(
				this.mongodbCred.get("collection").toString()).find(
				new Document("$text", new Document("$search", this.queryString)));

		responseListStrContainer = new ArrayList<String>();
		iterable.forEach(new TgBlock());
		mongoClient.close();
		return responseListStrContainer;
	}

	private class TgBlock implements Block<Document> {

		JsonObject tweet;

		@Override
		public void apply(Document document) {
			try{
				tweet = new JsonObject(document.getString("tweet"));
				responseListStrContainer.add(tweet.getString("text"));
			} catch (JSONException | DecodeException e) {
			}
		}
	}

	private List<String> fileDataReader() throws IOException {

		List<String> responseList = new ArrayList<String>();
		BufferedReader reader = null;
		String[] search_keywords = this.queryString.split("\\s");
 		try {
			reader = new BufferedReader(new FileReader(new File(inputFile)));
			String line;
			while ((line = reader.readLine()) != null) {
				String tweet_text = null;
				try {
					tweet_text = new JsonObject(line).getString("text");
				} catch (JSONException | DecodeException e) {
				}
				 
				boolean contains_keyword = false;
				for (String keyword : search_keywords) {
					if(tweet_text.contains(keyword)){
						contains_keyword = true;
					}
				}
				if (contains_keyword){
					responseList.add(tweet_text);
				}
			}
		} catch (IOException e) {
			System.out.println("File not exists or error reading file.!");
		} finally {
			if (reader != null) {
				reader.close();
			}
		}

		return responseList;
	}

	private boolean elasticsearch_connect() {

		if (this.elasticSearchCred.containsKey("cluster.name")
				&& !this.elasticSearchCred.get("cluster.name").toString()
						.trim().isEmpty()) {

			this.clientSettings = ImmutableSettings
					.settingsBuilder()
					.put("cluster.name",
							this.elasticSearchCred.get("cluster.name")
									.toString()).build();
		} else {
			this.clientSettings = ImmutableSettings.settingsBuilder()
					.put("cluster.name", "elasticsearch").build();
		}

		setElasticSearchClient(new TransportClient(this.clientSettings));
		getElasticSearchClient().addTransportAddress(
				new InetSocketTransportAddress(this.elasticSearchCred.get(
						"host").toString(), Integer
						.parseInt(this.elasticSearchCred.get("port")
								.toString())));

		return verifyConnection();
	}

	private boolean verifyConnection() {
		ImmutableList<DiscoveryNode> nodes = this.elasticSearchClient
				.connectedNodes();
		if (nodes.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	private List<String> elasticsearch_search(String indexname,
			String typename, String query_terms, int size) {

		List<String> responseList = new ArrayList<String>();

		SearchResponse response = getElasticSearchClient()
				.prepareSearch(indexname).setTypes(typename)
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(QueryBuilders.queryString(query_terms)).setFrom(0)
				.setSize(size).setExplain(true).execute().actionGet();

		for (SearchHit hit : response.getHits()) {
			responseList.add(hit.getSource().get("text").toString());
		}
		return responseList;
	}

	public TransportClient getElasticSearchClient() {
		return elasticSearchClient;
	}

	public void setElasticSearchClient(TransportClient elasticSearchClient) {
		this.elasticSearchClient = elasticSearchClient;
	}
}
