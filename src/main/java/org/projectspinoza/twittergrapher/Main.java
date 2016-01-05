package org.projectspinoza.twittergrapher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;

public class Main {
	private static Logger log = LogManager.getLogger(Main.class);
	
	static int app_port = 0;
	static String searchValues;

	public static void main(String[] args) {
		log.debug("Initializing Twitter Grapher!!!");
		// read command line arguments
		ConfigParams confParams = readCommandLineArguments(args);
		JsonObject graphConfJson = getConfigJson(confParams.configFile);
		GraphServer graphServer = (graphConfJson == null)? null: new GraphServer(graphConfJson);
		boolean appDeployed = (graphServer == null)? false:graphServer.deployServer();
		
		if (appDeployed) {
			System.out.println("App server deployed successfully at port " + app_port);
		}else {
			System.out.println("Error deploying App server.");
		}
	}

	private static ConfigParams readCommandLineArguments(String[] args) {
		ConfigParams confParams = new ConfigParams();
		JCommander rootParams = null;
		try {
			rootParams = new JCommander(confParams, args);
		} catch (ParameterException e) {
			System.out.println("error found unknown arguments "
					+ rootParams.getUnknownOptions());
		}
		return confParams;
	}

	private static JsonObject getConfigJson(String file) {
		String confJsonStr;
		JsonObject confJson = null;
		
		try {
			confJsonStr = new String(Files.readAllBytes(Paths.get(file)));
			confJson = new JsonObject(confJsonStr);
		} catch (IOException e) {
			System.out.println("File doesn't exists or error reading file.");
		} catch (JSONException | DecodeException e) {
			System.out.println("Error parsing configuration file JSON.");
		}
		return confJson;
	}

	private static class ConfigParams {
		@Parameter(names = "-conf", description = "Graph configuration file.")
		private String configFile;
	}
}
