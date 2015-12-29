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
	private JsonObject graph_config_json;
	private Vertx vertx;
	private HttpServer server;
	private Router router;
	
	JsonObject layout_settings_json;
	Map<String, Object> layout_settings;
	JsonObject layout_algo;
	
	GraphServer()  {
	}

	GraphServer(JsonObject conf_json_obj) {
		setGraphConfig(conf_json_obj);
	}

	GraphServer(String conf_json_str) {
		setGraphConfig(new JsonObject(conf_json_str));
	}

	public boolean deployServer() {

		String host = graph_config_json.getJsonObject("app_settings")
				.getString("host");
		int Port = graph_config_json.getJsonObject("app_settings").getInteger(
				"port");

		layout_settings_json = this.graph_config_json.getJsonObject("layout_settings");
		layout_settings = layout_settings_json.getMap();
		layout_algo = layout_settings_json.getJsonObject("la");
		
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
		routingContext.put("color", this.graph_config_json.getJsonObject("layout_settings").getString("bk_color"));
		routingContext.put("graph_settings", this.graph_config_json.getJsonObject("graph_settings").getMap());
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
		JsonObject graph_settings = this.graph_config_json.getJsonObject("graph_settings");
		String data_source = routingContext.request().getParam("datasource");

		if (data_source == null) {
			routingContext.response().end("No Data source specified.");
		}

		if (parameters.get("NodeSizeBy").contains("PageRank")) {
			layout_settings.put("nsb", "pr");
		} else if (parameters.get("NodeSizeBy").contains("NodeCentrality")) {
			layout_settings.put("nsb", "nc");
		}
		
		if (parameters.get("layouttype").contains("FruchtermanReingold")) {

			layout_algo.put("name", "FruchtermanReingold");
			layout_settings.put("la", layout_algo.toString());
			
		} else if (parameters.get("layouttype").contains("ForceAtlasLayout")) {

			layout_algo.put("name", "ForceAtlasLayout");
			layout_settings.put("la", layout_algo.toString());
		} else if (parameters.get("layouttype").contains("YifanHuLayout")) {
			
			layout_algo.put("name", "YifanHuLayout");
			layout_settings.put("la", layout_algo.toString());
		}

		if (!parameters.get("nc").contains("null")) {
			layout_settings.put("nct", Integer.parseInt(parameters.get("nc")));
		} else {
			layout_settings.put("nct", 0);
		}
		
		if(!parameters.get("NeighborCountRange").contains("null")){
			layout_settings.put("neighborcountrange",Double.parseDouble(parameters.get("NeighborCountRange")));
		}else{
			layout_settings.put("neighborcountrange",0);
		}
		if (!parameters.get("prt").contains("null")) {
			layout_settings.put("prt", Integer.parseInt(parameters.get("prt")));
		} else {
			layout_settings.put("prt", 0);
		}
		
		sources_settings.put("source_selected", data_source);
		
		if (postProcessing == GraphProcessID.POST_PROCESS_GRAPH && Main.searchValues != null){
			sources_settings.put("query_str", Main.searchValues.toString());
		}else {
			sources_settings.put("query_str", parameters.get("searchField").toString());
			Main.searchValues = parameters.get("searchField").toString();
		}
		sources_settings.put("sources_cred", this.graph_config_json.getJsonObject("data_sources"));
		layout_settings.put("settings", sources_settings);
		
		routingContext.put("color", layout_settings.get("bk_color")
				.toString());
		routingContext.put("graph_settings", graph_settings.getMap());

		//generating graph...
		String type = "sigmaGraph";
		GraphFactory graphFactory = new GraphFactory();
		Map<String, Object> result = null;
		try {
			TwitterGraph graph = graphFactory.getGraph(type,layout_settings);
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
		return graph_config_json;
	}

	public void setGraphConfig(JsonObject graph_config_json) {
		this.graph_config_json = graph_config_json;
	}

}
