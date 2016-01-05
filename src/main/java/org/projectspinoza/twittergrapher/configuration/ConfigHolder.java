package org.projectspinoza.twittergrapher.configuration;

import io.vertx.core.json.JsonObject;

import java.util.Map;

public class ConfigHolder {
	
	private Map<String, Object> appSettings;
	private Map<String, Object> layoutSettings;
	private Map<String, Object> graphSettings;
	private Map<String, Object> elasticSearchCred;
	private Map<String, Object> mongoDbCred;
	private Map<String, Object> mysqlCred;
	private Map<String, Object> sourcesCred;
	private String inputFileCred;
	private JsonObject settingsJson;
	
	public ConfigHolder () {}
	
	public ConfigHolder (JsonObject settings) {
		this();
		setSettings(settings);
	}
	
	
	public void setSettings(JsonObject settings) {
		this.settingsJson = settings;
		expendSettings();
	}
	
	public Map<String, Object> getAppSettings() {
		return appSettings;
	}



	public void setAppSettings(Map<String, Object> appSettings) {
		this.appSettings = appSettings;
	}



	public Map<String, Object> getLayoutSettings() {
		return layoutSettings;
	}



	public void setLayoutSettings(Map<String, Object> layoutSettings) {
		this.layoutSettings = layoutSettings;
	}



	public Map<String, Object> getGraphSettings() {
		return graphSettings;
	}



	public void setGraphSettings(Map<String, Object> graphSettings) {
		this.graphSettings = graphSettings;
	}



	public Map<String, Object> getElasticSearchCred() {
		return elasticSearchCred;
	}



	public void setElasticSearchCred(Map<String, Object> elasticSearchCred) {
		this.elasticSearchCred = elasticSearchCred;
	}



	public Map<String, Object> getMongoDbCred() {
		return mongoDbCred;
	}



	public void setMongoDbCred(Map<String, Object> mongoDbCred) {
		this.mongoDbCred = mongoDbCred;
	}



	public Map<String, Object> getMysqlCred() {
		return mysqlCred;
	}



	public void setMysqlCred(Map<String, Object> mysqlCred) {
		this.mysqlCred = mysqlCred;
	}



	public String getInputFileCred() {
		return inputFileCred;
	}



	public void setInputFileCred(String inputFileCred) {
		this.inputFileCred = inputFileCred;
	}

	
	//single property methods
	
	
	@SuppressWarnings("unchecked")
	private void expendSettings () {
		
		Map <String, Object> settingsMap = this.settingsJson.getMap();
		Map <String, Object> sourceSettingsMap = this.settingsJson.getJsonObject("data_sources").getMap();
		
		setAppSettings((Map<String, Object>) settingsMap.get("app_settings"));
		setLayoutSettings((Map<String, Object>) settingsMap.get("layout_settings"));
		setGraphSettings((Map<String, Object>) settingsMap.get("graph_settings"));
		setElasticSearchCred((Map<String, Object>) sourceSettingsMap.get("elasticsearch"));
		setMongoDbCred((Map<String, Object>) sourceSettingsMap.get("mongodb"));
		setMongoDbCred((Map<String, Object>) sourceSettingsMap.get("mongodb"));
		setMysqlCred((Map<String, Object>) sourceSettingsMap.get("mysql"));
		setInputFileCred((String) sourceSettingsMap.get("file"));
		setSourcesCred(sourceSettingsMap);
		
		Configuration.getInstance().loadConfigurations(this);
		//System.out.println(Configuration.getInstance().toString());
	}


	public Map<String, Object> getSourcesCred() {
		return sourcesCred;
	}


	public void setSourcesCred(Map<String, Object> sourcesCred) {
		this.sourcesCred = sourcesCred;
	}

}
