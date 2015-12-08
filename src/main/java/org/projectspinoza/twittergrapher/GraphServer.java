package org.projectspinoza.twittergrapher;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.templ.ThymeleafTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import org.projectspinoza.twittergrapher.factory.GraphFactory;
import org.projectspinoza.twittergrapher.graph.TwitterGraph;

public class GraphServer extends AbstractVerticle {
	@Override
	public void start(Future<Void> startFuture) throws Exception {

		super.start(startFuture);

		final ThymeleafTemplateEngine engine = ThymeleafTemplateEngine.create();
		final Router mainrouter = Router.router(vertx);
		final Router infoRouter = Router.router(vertx);
		JsonObject config = context.config();

		// extracting data_sources
		JsonObject data_sources_cred = config.getJsonObject("data_sources");
		Map<String, Object> settings = config.getMap();
		JsonObject lso = (JsonObject) settings.get("layout_settings");
		Map<String, Object> layout_settings = lso.getMap();
		JsonObject app_settings = (JsonObject) settings.get("app_settings");
		JsonObject graph_settings = (JsonObject) settings.get("graph_settings");
		final Map<String, Object> sources_settings = new HashMap<String, Object>();

		layout_settings.put("input_file", app_settings.getString("input_file"));

		int Port = app_settings.getInteger("port");

		mainrouter.route("/templates/*").handler(
				StaticHandler.create(
						"org/projectspinoza/twittergrapher/templates")
						.setCachingEnabled(false));

		infoRouter.route("/").handler(request -> {
			request.response().end("Welcome to Twitter-Grapher");
		});

		mainrouter
				.getWithRegex("/graph.*")
				.method(HttpMethod.GET)
				.handler(
						ctx -> {
							ctx.put("color", layout_settings.get("bk_color")
									.toString());
							ctx.put("welcome", "Hi there!");
							ctx.put("graph_settings", graph_settings.getMap());
							// and now delegate to the engine to render it.
							engine.render(
									ctx,
									"org/projectspinoza/twittergrapher/templates/index.html",
									res -> {
										if (res.succeeded()) {
											System.out.println("successed");
											ctx.response()
													.putHeader(
															HttpHeaders.CONTENT_TYPE,
															"text/html")
													.end(res.result());
										} else {
											ctx.fail(res.cause());
										}
									});
						});

		mainrouter.mountSubRouter("/", infoRouter);

		mainrouter
				.getWithRegex("/ajax.*")
				.method(HttpMethod.GET)
				.handler(ctx -> {

					// get global graph paramters
						MultiMap parameters = ctx.request().params();
						TwitterGrapher.search_value = parameters.get(
								"searchField").toString();
						String data_source = ctx.request().getParam(
								"datasource");

						if (data_source == null) {
							ctx.response().end("No Data source specified.");
						}

						if (parameters.get("NodeSizeBy").contains("PageRank")) {
							layout_settings.put("nsb", "pr");
						} else if (parameters.get("NodeSizeBy").contains(
								"NodeCentrality")) {
							layout_settings.put("nsb", "nc");
						}

						if (parameters.get("layouttype").contains(
								"FruchtermanReingold")) {

							JsonObject layout_algo = new JsonObject(
									layout_settings.get("la").toString());
							layout_algo.put("name", "FruchtermanReingold");
							layout_settings.put("la", layout_algo.toString());
						} else if (parameters.get("layouttype").contains(
								"ForceAtlasLayout")) {

							JsonObject layout_algo = new JsonObject(
									layout_settings.get("la").toString());
							layout_algo.put("name", "ForceAtlasLayout");
							layout_settings.put("la", layout_algo.toString());
						} else if (parameters.get("layouttype").contains(
								"YifanHuLayout")) {

							JsonObject layout_algo = new JsonObject(
									layout_settings.get("la").toString());
							layout_algo.put("name", "YifanHuLayout");
							layout_settings.put("la", layout_algo.toString());
						}

						if (!parameters.get("nc").contains("null")) {
							layout_settings.put("nct",
									Integer.parseInt(parameters.get("nc")));
						} else {
							layout_settings.put("nct", 0);
						}

						if (!parameters.get("prt").contains("null")) {
							layout_settings.put("prt",
									Integer.parseInt(parameters.get("prt")));
						} else {
							layout_settings.put("prt", 0);
						}

						sources_settings.put("source_selected", data_source);
						sources_settings.put("query_str",
								TwitterGrapher.search_value);
						sources_settings.put("sources_cred", data_sources_cred);
						layout_settings.put("settings", sources_settings);

						ctx.put("color", layout_settings.get("bk_color")
								.toString());
						ctx.put("graph_settings", graph_settings.getMap());

						// . generating graph...
						String type = "sigmaGraph";
						GraphFactory graphFactory = new GraphFactory();
						TwitterGraph graph = graphFactory.getGraph(type,
								layout_settings);
						Map<String, Object> result = new HashMap<String, Object>();
						result.put("nodes", graph);
						ctx.response().end(new JsonObject(result).toString());

					});
		VertxOptions options = new VertxOptions();
		options.setMaxEventLoopExecuteTime(Long.MAX_VALUE);
		vertx = Vertx.vertx(options);
		vertx.createHttpServer().requestHandler(mainrouter::accept)
				.listen(Port);
	}

}
