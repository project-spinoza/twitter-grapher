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
import java.util.List;

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
import org.projectspinoza.twittergrapher.configuration.Configuration;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

public class DataImporter {

	private Configuration settingsConf;
	
	private TransportClient elasticSearchClient;
	private Settings clientSettings;
	List<String> responseListStrContainer = null;

	public DataImporter() {
		settingsConf = Configuration.getInstance();
	}

	public List<String> importDataList() throws IOException {

		List<String> response_tweets_list = null;

		switch (settingsConf.getDataSource()) {
		
		case "elasticsearch":
			
			if (elasticsearch_connect()) {
				response_tweets_list = elasticsearch_search(
						settingsConf.getElasticsearchIndex(),
						settingsConf.getElasticsearchIndexType(),
						settingsConf.getSearchValue(), 5000);
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
					+ settingsConf.getMysqlHost() + ":"
					+ settingsConf.getMysqlPort() + "/"
					+ settingsConf.getMysqlDatabaseName(), "root", "");

			if (connection != null) {

				String[] query_terms = settingsConf.getSearchValue().trim().split(" ");
				String query_like = " LIKE '%" + query_terms[0] + "%' ";

				for (int i = 1; i < query_terms.length; i++) {
					query_like.concat("or " + settingsConf.getMysqlDataColumnName()
							+ " LIKE '%" + query_terms[i] + "%' ");
				}

				String query_mysql = "SELECT "
						+ settingsConf.getMysqlDataColumnName()
						+ " from "
						+ settingsConf.getMysqlTableName()
						+ " where "
						+ settingsConf.getMysqlDataColumnName()
						+ query_like;

				statement = connection.createStatement();
				ResultSet results_set = statement.executeQuery(query_mysql);

				while (results_set.next()) {
					String tweet = results_set.getString(settingsConf.getMysqlDataColumnName());
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

		MongoClient mongoClient = new MongoClient(settingsConf.getMongodbHost(), settingsConf.getMongodbPort());
		MongoDatabase db = mongoClient.getDatabase(settingsConf.getMongodbDatabaseName());

		FindIterable<Document> iterable = db.getCollection(
				settingsConf.getMongodbCollectionName()).find(
				new Document("$text", new Document("$search", settingsConf.getSearchValue())));

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
		String[] search_keywords = settingsConf.getSearchValue().split("\\s");
 		try {
			reader = new BufferedReader(new FileReader(new File(settingsConf.getFileName())));
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

		if (settingsConf.getElasticsearchClusterName() != null
				&& !settingsConf.getElasticsearchClusterName().trim().isEmpty()) {

			this.clientSettings = ImmutableSettings
					.settingsBuilder()
					.put("cluster.name",settingsConf.getElasticsearchClusterName()).build();
		} else {
			this.clientSettings = ImmutableSettings.settingsBuilder()
					.put("cluster.name", "elasticsearch").build();
		}

		setElasticSearchClient(new TransportClient(this.clientSettings));
		getElasticSearchClient().addTransportAddress(
				new InetSocketTransportAddress(settingsConf.getElasticsearchHost(), settingsConf.getElasticsearchPort()));
		
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
