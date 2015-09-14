package com.tg;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.templ.ThymeleafTemplateEngine;

import java.util.Map;

import com.tg.factory.GraphFactory;
import com.tg.graph.Graph;

public class GraphServer extends AbstractVerticle {
	@Override
	public void start(Future<Void> startFuture) throws Exception {
		// TODO Auto-generated method stub
		super.start(startFuture);
		
		 final ThymeleafTemplateEngine engine = ThymeleafTemplateEngine.create();
		    // To simplify the development of the web components we use a Router to route all HTTP requests
		    // to organize our code in a reusable way.
		    
		    final Router mainrouter = Router.router(vertx);
		    final Router infoRouter = Router.router(vertx);
		    JsonObject config;
		    config = context.config();
		    Map<String,Object> settings = config.getMap();
		    
	    	 JsonObject lso = (JsonObject) settings.get("layout_settings");
	    	 Map<String, Object> ls = lso.getMap();
	    	 String bk_color = ls.get("bk_color").toString();
	    	 JsonObject ips = (JsonObject) ls.get("ips");
	    	 Map<String, Object> ip = ips.getMap();
	    	 JsonObject aso = (JsonObject)settings.get("app_settings");
	    	 Map<String, Object> as = aso.getMap();
	    	 JsonObject gso = (JsonObject) settings.get("graph_settings");
	    	 Map<String, Object> gs = gso.getMap();
	    	  String getdata = as.get("getdata").toString();
	    	  System.out.println("value of getdata is: "+getdata);
	    	 /*if(getdata.equals("file")){
	    		  path = as.get("input_file").toString();
	    	 }*/
	    	 int Port = Integer.parseInt(as.get("port").toString());
	    	 
		     mainrouter.route("/templates/*").handler(StaticHandler.create("com/tg/templates").setCachingEnabled(false));
		     
		     infoRouter.route("/").handler(ct ->{
		    	 engine.render(ct, "com/tg/templates/info.html", res -> {
				        if (res.succeeded()) {
				          ct.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/html")
				          .end(res.result());
				        } else { 
				          ct.fail(res.cause()); 
				        }
				      });
		     });
		     
		     mainrouter.get("/graph").method(HttpMethod.GET).handler(ctx -> {
		    	 
		     ls.put("sv", getSearchValue(ctx.request().absoluteURI()));
		     ctx.put("color",bk_color );	 
		     ctx.put("welcome", "Hi there!");
		     ctx.put("graph_settings", gs);
		     
		     //. generating graph...
		     String type = "sigmaGraph";
	    	 GraphFactory graphFactory = new GraphFactory();
	    	 Graph graph = graphFactory.getGraph(type, ls);
	    	 ctx.put("nodes", graph);
		     
		     // and now delegate to the engine to render it.
		     engine.render(ctx, "com/tg/templates/index.html", res -> {
		    	 
		        if (res.succeeded()) {
		        	 
		         ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/html")
		         .end(res.result());
		         
		        } else {
		          ctx.fail(res.cause()); 
		        }
		      });
		    });
		     mainrouter.mountSubRouter("/", infoRouter);
	    vertx.createHttpServer().requestHandler(mainrouter::accept).listen(Port);
	}

	private String getSearchValue(String url)
	{
		String [] sv = url.split("=");
		if(sv.length == 2)
		return sv[1];
		else
			return "hyundai";
	}
}
