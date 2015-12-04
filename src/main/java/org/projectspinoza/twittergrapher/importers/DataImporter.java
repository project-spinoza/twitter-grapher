package org.projectspinoza.twittergrapher.importers;

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
import org.json.JSONObject;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

public class DataImporter {

	private TransportClient elasticSearchClient;
	private Settings client_settings;
	private JsonObject sources_cred_json;
	private Map<String, Object> elasticsearch_cred = new HashMap<String, Object>();
	private Map<String, Object> mongodb_cred = new HashMap<String, Object>();
	private Map<String, Object> mysql_cred = new HashMap<String, Object>();
	private String input_file;
	private String query_str;
	private String source_selected;
	List<String> response_list_str_container = null;


	public DataImporter(Map<String, Object> settings) {

		this.sources_cred_json = (JsonObject) settings.get("sources_cred");
		this.elasticsearch_cred = sources_cred_json.getJsonObject(
				"elasticsearch").getMap();
		this.mongodb_cred = sources_cred_json.getJsonObject("mongodb").getMap();
		this.mysql_cred = sources_cred_json.getJsonObject("mysql").getMap();
		this.input_file = sources_cred_json.getString("file");
		this.query_str = (String) settings.get("query_str");
		this.source_selected = (String) settings.get("source_selected");

	}

	public List<String> importDataList() throws IOException {

		List<String> response_tweets_list = null;
		
		switch (source_selected) {
		case "elasticsearch":
			
			if (elasticsearch_connect()) {
				response_tweets_list = elasticsearch_search(this.elasticsearch_cred.get("index").toString(),
						this.elasticsearch_cred.get("type").toString(),this.query_str,5000);
			}else {
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

		List<String> response_list = new ArrayList<String>();
		Connection connection = null;
		Statement statement = null;

		try {
			
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://"
					+ this.mysql_cred.get("host").toString() + ":"
					+ this.mysql_cred.get("port").toString() + "/"
					+ this.mysql_cred.get("database").toString(),
					"root", "");
			
			if (connection != null) {

				String[] query_terms = this.query_str.trim().split(" ");
				String query_like =" LIKE '%"+query_terms[0]+"%' ";
				
				for (int i=1 ; i<query_terms.length; i++) {
					query_like.concat("or "+this.mysql_cred.get("data_column").toString()+" LIKE '%"+query_terms[i]+"%' ");
				}
				
				String query_mysql = "SELECT "+this.mysql_cred.get("data_column").toString()+" from "+this.mysql_cred.get("table_name").toString()+" where "+this.mysql_cred.get("data_column").toString()+query_like;

				statement = connection.createStatement();
				ResultSet results_set = statement.executeQuery(query_mysql);
				
				while (results_set.next()) {
					String tweet = results_set.getString(this.mysql_cred.get("data_column").toString());
					response_list.add(new JSONObject(tweet).get("text").toString());
				}
				
			}else {
				System.out.println("Database connection failed...");
			}
			
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			return null;
		}

		return response_list;
	}
	
	private List<String> mongodb_search() {

		MongoClient mongoClient = new MongoClient(this.mongodb_cred.get("host")
				.toString(), Integer.parseInt(this.mongodb_cred.get("port")
				.toString()));
		MongoDatabase db = mongoClient.getDatabase(this.mongodb_cred.get(
				"database").toString());

		FindIterable<Document> iterable = db.getCollection(
				this.mongodb_cred.get("collection").toString()).find(
				new Document("$text", new Document("$search", this.query_str)));
		
		response_list_str_container = new ArrayList<String>();
		iterable.forEach(new TgBlock());
		mongoClient.close();
		return response_list_str_container;
	}

	private class TgBlock implements Block<Document> {

		JsonObject tweet;
		@Override
		public void apply(Document document) {
			tweet = new JsonObject(document.getString("tweet"));
			response_list_str_container.add(tweet.getString("text"));
		}
	}
	
	private List<String> fileDataReader() throws IOException {

		List<String> response_list = new ArrayList<String>();
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(new File(input_file)));
			String line;
			while ((line = reader.readLine()) != null) {
				response_list.add(new JsonObject(line).getString("text"));
			}
		} catch (IOException e) {
			System.out.println("File not exists or error reading file.!");
		} finally {
			if (reader != null) {
				reader.close();
			}
		}

		return response_list;
	}

	private boolean elasticsearch_connect() {

			if (this.elasticsearch_cred.containsKey("cluster.name")
					&& !this.elasticsearch_cred.get("cluster.name").toString()
							.trim().isEmpty()) {

				this.client_settings = ImmutableSettings
						.settingsBuilder()
						.put("cluster.name",this.elasticsearch_cred.get("cluster.name").toString()).build();
			} else {
				this.client_settings = ImmutableSettings.settingsBuilder()
						.put("cluster.name", "elasticsearch").build();
			}

			setElasticSearchClient(new TransportClient(this.client_settings));
			getElasticSearchClient().addTransportAddress(
					new InetSocketTransportAddress(this.elasticsearch_cred.get("host").toString(), Integer.parseInt(this.elasticsearch_cred.get("port").toString())));

		return verifyConnection();
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

	public List<String> elasticsearch_search(String indexname, String typename,
			String query_terms, int size) {

		List<String> response_list = new ArrayList<String>();

		SearchResponse response = getElasticSearchClient()
				.prepareSearch(indexname).setTypes(typename)
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
