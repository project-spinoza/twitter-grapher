package org.projectspinoza.twittergrapher;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

public class Main {

	static int app_port = 0;
	static String searchValues;

	public static void main(String[] args) {

		// read command line arguments
		ConfigParams conf_params = readCommandLineArguments(args);
		JsonObject graph_conf_json = getConfigJson(conf_params.config_file);
		GraphServer graph_server = (graph_conf_json == null)? null: new GraphServer(graph_conf_json);
		boolean app_deployed = (graph_server == null)? false:graph_server.deployServer();
		
		if (app_deployed) {
			System.out.println("App server deployed successfully at port " + app_port);
		}else {
			System.out.println("Error deploying App server.");
		}
	}

	private static ConfigParams readCommandLineArguments(String[] args) {
		ConfigParams conf_params = new ConfigParams();
		JCommander root_params = null;
		try {
			root_params = new JCommander(conf_params, args);
		} catch (ParameterException e) {
			System.out.println("error found unknown arguments "
					+ root_params.getUnknownOptions());
		}
		return conf_params;
	}

	private static JsonObject getConfigJson(String file) {
		String conf_json_str;
		JsonObject conf_json = null;
		
		try {
			conf_json_str = new String(Files.readAllBytes(Paths.get(file)));
			conf_json = new JsonObject(conf_json_str);
		} catch (IOException e) {
			System.out.println("File doesn't exists or error reading file.");
		} catch (JSONException | DecodeException e) {
			System.out.println("Error parsing configuration file JSON.");
		}
		return conf_json;
	}

	private static class ConfigParams {
		@Parameter(names = "-conf", description = "Graph configuration file.")
		private String config_file;
	}
}
