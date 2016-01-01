package org.projectspinoza.twittergrapher;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.templ.ThymeleafTemplateEngine;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.projectspinoza.twittergrapher.factory.GraphFactory;
import org.projectspinoza.twittergrapher.graph.TwitterGraph;

public class GraphServer {
	
	private enum GraphProcessID {
		GENERATE_GRAPH,
		POST_PROCESS_GRAPH
	}
	private JsonObject graphConfigJson;
	private Vertx vertx;
	private HttpServer server;
	private Router router;
	
	JsonObject layoutSettingsJson;
	Map<String, Object> layoutSettings;
	JsonObject layoutAlgo;
	
	GraphServer()  {
	}

	GraphServer(JsonObject conf_json_obj) {
		setGraphConfig(conf_json_obj);
	}

	GraphServer(String conf_json_str) {
		setGraphConfig(new JsonObject(conf_json_str));
	}

	public boolean deployServer() {

		String host = graphConfigJson.getJsonObject("app_settings")
				.getString("host");
		int Port = graphConfigJson.getJsonObject("app_settings").getInteger(
				"port");

		layoutSettingsJson = this.graphConfigJson.getJsonObject("layout_settings");
		layoutSettings = layoutSettingsJson.getMap();
		layoutAlgo = layoutSettingsJson.getJsonObject("la");
		
		VertxOptions options = new VertxOptions();
		options.setMaxEventLoopExecuteTime(Long.MAX_VALUE);
		vertx = Vertx.vertx(options);
		router = Router.router(vertx);

		
		// static resources CSS/JS files
		router.getWithRegex(".*/css/.*|.*/js/.*|.*/images/.*").handler(
				StaticHandler.create("webroot").setCachingEnabled(false));
		
		//routes
		router.getWithRegex("/").method(HttpMethod.GET).handler(request -> {
			request.response().end("Welcome to Twitter-Grapher");
		});

		router.getWithRegex("/graph.*").method(HttpMethod.GET).handler(routingContext -> {
			graphResponseHandler(routingContext);
		});

		router.getWithRegex("/ajax.*").method(HttpMethod.GET).handler(routingContext -> {
			ajaxResponseHandler(routingContext,GraphProcessID.GENERATE_GRAPH);
		});
		
		router.getWithRegex("/processGraph.*").method(HttpMethod.GET).handler(routingContext -> {
			ajaxResponseHandler(routingContext,GraphProcessID.POST_PROCESS_GRAPH);
		});

		// deploy app server on requested port
		server = vertx.createHttpServer().requestHandler(router::accept);

		// checking free port if given port in use;
		int port_open = Port;
		while (serverListening(host, port_open)) {
			port_open++;
		}

		if (port_open != Port) {
			System.out.println("Port " + Port + " already in use on " + host);
			server.listen(port_open);
			Port = port_open;
		} else {
			server.listen(Port);
		}
		Main.app_port = Port;
		return serverListening(host, Port);
	}

	private void graphResponseHandler(RoutingContext routingContext) {
		
		final ThymeleafTemplateEngine engine = ThymeleafTemplateEngine.create();
		routingContext.put("color", this.graphConfigJson.getJsonObject("layout_settings").getString("bk_color"));
		routingContext.put("graph_settings", this.graphConfigJson.getJsonObject("graph_settings").getMap());
		engine.render(routingContext,"webroot/index.html",results_async -> {
			if (results_async.succeeded()) {
				routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE,"text/html").end(results_async.result());
			} else {
				routingContext.fail(results_async.cause());
			}
		});
	}

	private void ajaxResponseHandler(RoutingContext routingContext, GraphProcessID postProcessing) {
		
		MultiMap parameters = routingContext.request().params();
		Map<String, Object> sources_settings = new HashMap<String, Object>();
		JsonObject graph_settings = this.graphConfigJson.getJsonObject("graph_settings");
		String data_source = routingContext.request().getParam("datasource");

		if (data_source == null) {
			routingContext.response().end("No Data source specified.");
		}

		if (parameters.get("NodeSizeBy").contains("PageRank")) {
			layoutSettings.put("nsb", "pr");
		} else if (parameters.get("NodeSizeBy").contains("NodeCentrality")) {
			layoutSettings.put("nsb", "nc");
		}
		
		if (parameters.get("layouttype").contains("FruchtermanReingold")) {

			layoutAlgo.put("name", "FruchtermanReingold");
			layoutSettings.put("la", layoutAlgo.toString());
			
		} else if (parameters.get("layouttype").contains("ForceAtlasLayout")) {

			layoutAlgo.put("name", "ForceAtlasLayout");
			layoutSettings.put("la", layoutAlgo.toString());
		} else if (parameters.get("layouttype").contains("YifanHuLayout")) {
			
			layoutAlgo.put("name", "YifanHuLayout");
			layoutSettings.put("la", layoutAlgo.toString());
		}

		if (!parameters.get("nc").contains("null")) {
			layoutSettings.put("nct", Integer.parseInt(parameters.get("nc")));
		} else {
			layoutSettings.put("nct", 0);
		}
		
		if(!parameters.get("NeighborCountRange").contains("null")){
			layoutSettings.put("neighborcountrange",Double.parseDouble(parameters.get("NeighborCountRange")));
		}else{
			layoutSettings.put("neighborcountrange",0);
		}
		if (!parameters.get("prt").contains("null")) {
			layoutSettings.put("prt", Integer.parseInt(parameters.get("prt")));
		} else {
			layoutSettings.put("prt", 0);
		}
		
		sources_settings.put("source_selected", data_source);
		
		if (postProcessing == GraphProcessID.POST_PROCESS_GRAPH && Main.searchValues != null){
			sources_settings.put("query_str", Main.searchValues.toString());
		}else {
			sources_settings.put("query_str", parameters.get("searchField").toString());
			Main.searchValues = parameters.get("searchField").toString();
		}
		sources_settings.put("sources_cred", this.graphConfigJson.getJsonObject("data_sources"));
		layoutSettings.put("settings", sources_settings);
		
//		String s_test = "{\"nct\":0,\"prt\":0,\"neighborcountrange\":0.0,\"la\":\"{\\\"name\\\":\\\"YifanHuLayout\\\",\\\"it\\\":100,\\\"distance\\\":260}\",\"ncb\":\"cluster\",\"nsb\":\"pr\",\"ecb\":\"mix\",\"et\":\"curve\",\"bk_color\":\"#000\",\"settings\":{\"source_selected\":\"inputfile\",\"sources_cred\":{\"elasticsearch\":{\"host\":\"127.0.0.1\",\"port\":9300,\"cluster.name\":\"elasticsearch\",\"index\":\"myindex\",\"type\":\"mytype\"},\"mongodb\":{\"host\":\"127.0.0.1\",\"port\":27017,\"database\":\"twittergrapher\",\"collection\":\"tweets\",\"field\":\"tweet\"},\"mysql\":{\"host\":\"127.0.0.1\",\"port\":3306,\"user\":\"root\",\"password\":\"\",\"database\":\"twittergrapher\",\"table_name\":\"tweets\",\"data_column\":\"tweet\"},\"file\":\"tweets.txt\"},\"query_str\":\"data\"}}";
//		Map<String,Object> test_map = (new JsonObject(s_test)).getMap();
//		layoutSettings = test_map;
		
		routingContext.put("color", layoutSettings.get("bk_color")
				.toString());
		routingContext.put("graph_settings", graph_settings.getMap());

		//generating graph...
		String type = "sigmaGraph";
		GraphFactory graphFactory = new GraphFactory();
		Map<String, Object> result = null;
		try {
			TwitterGraph graph = graphFactory.getGraph(type,layoutSettings);
			result = new HashMap<String, Object>();
			result.put("nodes", graph);
			routingContext.response().end(new JsonObject(result).toString());
		}catch (NullPointerException ex){
			routingContext.response().end(new JsonObject(result).toString());
		}
	}

	private boolean serverListening(String host, int port) {
		Socket socket = null;
		try {
			socket = new Socket(host, port);
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			if (socket != null)
				try {socket.close();} catch (Exception e) {}
		}
	}

	public JsonObject getGraphConfig() {
		return graphConfigJson;
	}

	public void setGraphConfig(JsonObject graphConfigJson) {
		this.graphConfigJson = graphConfigJson;
	}

}
