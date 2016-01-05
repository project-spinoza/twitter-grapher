package org.projectspinoza.twittergrapher.configuration;

import java.util.Map;

public class Configuration {
	
	//mysql cred properties
	private String mysqlHost;
	private int mysqlPort;
	private String mysqlUserName;
	private String mysqlUserPassword;
	private String mysqlDatabaseName;
	private String mysqlTableName;
	private String mysqlDataColumnName;

	//mongodb cred properties
	private String mongodbHost;
	private int mongodbPort;
	private String mongodbDatabaseName;
	private String mongodbCollectionName;
	private String mongodbFieldName;

	//server conf
	private int serverPort;
	private String serverHost;
	
	//elasticsearch conf
	private String elasticsearchHost;
	private int elasticsearchPort;
	private  String elasticSearchClusterName;
	private String elasticsearchIndex;
	private String elasticsearchIndexType;

	//input file
	private String fileName;
	
	//layout settings
	private double nodecentralitythreshHold;
	private double pagerankthreshHold;
	private double neighborcountThreshHold;
	private String edgecolorBy;
	private String edgeType;
	private String nodeSizeBy;
	private String backColor;
	private String layoutAlgoName;
	private int layoutAlgoIterations;
	private float layoutAlgoDistance;
	
	//. forceAtlas
	private boolean forceAtlasLayoutIsConverged;
	private double forceAtlasLayoutSpeed;
	private double forceAtlasLayoutInertia;
	private double forceAtlasLayoutGravity;
	private double forceAtlasLayoutMaxDisplacement;
	
	//. fruchtermanReingoldLayout
	private float fruchtermanReingoldLayoutArea;
	private double fruchtermanReingoldLayoutSpeed;
	private double fruchtermanReingoldLayoutGravity;
	
	
	//custom
	private String dataSource;
	private String searchValue;

	
	@SuppressWarnings("unchecked")
	public void loadConfigurations(ConfigHolder cHolder) {

		//mysql cred        	
		this.mysqlHost = containsKey(cHolder.getMysqlCred(), "host") ?  cHolder.getMysqlCred().get("host").toString() : "127.0.0.1";
		this.mysqlPort = containsKey(cHolder.getMysqlCred(), "port") ?  (int)cHolder.getMysqlCred().get("port") : 3306;
		this.mysqlUserName = containsKey(cHolder.getMysqlCred(), "user") ?  cHolder.getMysqlCred().get("user").toString() : "admin";
		this.mysqlUserPassword = containsKey(cHolder.getMysqlCred(), "password") ?  cHolder.getMysqlCred().get("password").toString() : "";
		this.mysqlDatabaseName = containsKey(cHolder.getMysqlCred(), "database") ?  cHolder.getMysqlCred().get("database").toString() : null;
		this.mysqlTableName = containsKey(cHolder.getMysqlCred(), "table_name") ?  cHolder.getMysqlCred().get("table_name").toString() : null;
		this.mysqlDataColumnName = containsKey(cHolder.getMysqlCred(), "data_column") ?  cHolder.getMysqlCred().get("data_column").toString() : null;
		
		
		//elasticsearch cred
		this.elasticsearchHost = containsKey(cHolder.getElasticSearchCred(), "host") ?  cHolder.getElasticSearchCred().get("host").toString() : "127.0.0.1";
		this.elasticsearchPort = containsKey(cHolder.getElasticSearchCred(), "port") ?  (int)cHolder.getElasticSearchCred().get("port") : 9300;
		this.elasticSearchClusterName = containsKey(cHolder.getElasticSearchCred(), "cluster.name") ?  cHolder.getElasticSearchCred().get("cluster.name").toString() : "elasticsearch";
		this.elasticsearchIndex = containsKey(cHolder.getElasticSearchCred(), "index") ?  cHolder.getElasticSearchCred().get("index").toString() : null;
		this.elasticsearchIndexType = containsKey(cHolder.getElasticSearchCred(), "type") ?  cHolder.getElasticSearchCred().get("type").toString() : null;
		
		//monogodb cred
		this.mongodbHost = containsKey(cHolder.getMongoDbCred(), "host") ?  cHolder.getMongoDbCred().get("host").toString() : "127.0.0.1";
		this.mongodbPort = containsKey(cHolder.getMongoDbCred(), "port") ?  (int)cHolder.getMongoDbCred().get("port") : 27017;
		this.mongodbDatabaseName = containsKey(cHolder.getMongoDbCred(), "database") ?  cHolder.getMongoDbCred().get("database").toString() : null;
		this.mongodbCollectionName = containsKey(cHolder.getMongoDbCred(), "collection") ?  cHolder.getMongoDbCred().get("collection").toString() : null;
		this.mongodbFieldName = containsKey(cHolder.getMongoDbCred(), "field") ?  cHolder.getMongoDbCred().get("field").toString() : null;
		
		//server cred
		this.serverHost = containsKey(cHolder.getAppSettings(), "host") ?  cHolder.getAppSettings().get("host").toString() : "127.0.0.1";
		this.serverPort = containsKey(cHolder.getAppSettings(), "port") ?  (int) cHolder.getAppSettings().get("port") : 8080;

		//graph settings		
		this.nodecentralitythreshHold = containsKey(cHolder.getLayoutSettings(), "nct") ?  Double.parseDouble(cHolder.getLayoutSettings().get("nct").toString()) : 0.0;
		this.pagerankthreshHold = containsKey(cHolder.getLayoutSettings(), "prt") ?  Double.parseDouble(cHolder.getLayoutSettings().get("prt").toString()) : 0.0;
		this.neighborcountThreshHold = containsKey(cHolder.getLayoutSettings(), "neighborcountrange") ?  Double.parseDouble(cHolder.getLayoutSettings().get("neighborcountrange").toString()) : 0.0;
		this.edgecolorBy = containsKey(cHolder.getLayoutSettings(), "ecb") ?  cHolder.getLayoutSettings().get("ecb").toString() : "mix";
		this.edgeType = containsKey(cHolder.getLayoutSettings(), "et") ?  cHolder.getLayoutSettings().get("et").toString() : "curve";
		this.nodeSizeBy = containsKey(cHolder.getLayoutSettings(), "nsb") ?  cHolder.getLayoutSettings().get("nsb").toString() : "pr";
		this.backColor = containsKey(cHolder.getLayoutSettings(), "bk_color") ?  cHolder.getLayoutSettings().get("bk_color").toString() : "#000";
		this.layoutAlgoName = containsKey((Map<String, Object>) cHolder.getLayoutSettings().get("la"), "name") ?  ((Map<String, Object>) cHolder.getLayoutSettings().get("la")).get("name").toString() : "YifanHuLayout";
		this.layoutAlgoIterations = containsKey((Map<String, Object>) cHolder.getLayoutSettings().get("la"), "it") ?  (int) ((Map<String, Object>) cHolder.getLayoutSettings().get("la")).get("it") : 100;
		this.layoutAlgoDistance = containsKey((Map<String, Object>) cHolder.getLayoutSettings().get("la"), "distance") ?  (float) ((int) ((Map<String, Object>) cHolder.getLayoutSettings().get("la")).get("distance")) : 260f;

		this.fileName = cHolder.getInputFileCred();
		this.dataSource = null;
		this.searchValue = null;
		
	}
	
	private boolean containsKey (Map<String, Object> map, String key) {
		if (map.containsKey(key)) {
			return true;
		}else {
			return false;
		}
	}

	public String getMysqlUserPassword() {
		return mysqlUserPassword;
	}

	public void setMysqlUserPassword(String mysqlUserPassword) {
		this.mysqlUserPassword = mysqlUserPassword;
	}

	public String getMysqlDatabaseName() {
		return mysqlDatabaseName;
	}

	public void setMysqlDatabaseName(String mysqlDatabaseName) {
		this.mysqlDatabaseName = mysqlDatabaseName;
	}

	public String getMysqlTableName() {
		return mysqlTableName;
	}

	public void setMysqlTableName(String mysqlTableName) {
		this.mysqlTableName = mysqlTableName;
	}

	public String getMysqlDataColumnName() {
		return mysqlDataColumnName;
	}

	public void setMysqlDataColumnName(String mysqlDataColumnName) {
		this.mysqlDataColumnName = mysqlDataColumnName;
	}

	public void setMysqlUserName(String mysqlUserName) {
		this.mysqlUserName = mysqlUserName;
	}
	
	
	public String getDataSource() {
		return dataSource;
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	public String getSearchValue() {
		return searchValue;
	}

	public void setSearchValue(String searchValue) {
		this.searchValue = searchValue;
	}

	private static Configuration instance = null;
	
	private Configuration(){
	}
	
	public String getMysqlHost() {
		return mysqlHost;
	}
	public void setMysqlHost(String mysqlHost) {
		this.mysqlHost = mysqlHost;
	}
	public String getElasticsearchHost() {
		return elasticsearchHost;
	}
	public void setElasticsearchHost(String elasticsearchHost) {
		this.elasticsearchHost = elasticsearchHost;
	}
	public String getMongodbHost() {
		return mongodbHost;
	}
	public void setMongodbHost(String mongodbHost) {
		this.mongodbHost = mongodbHost;
	}
	public int getServerPort() {
		return serverPort;
	}
	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}
	public String getServerHost() {
		return serverHost;
	}
	public void setServerHost(String serverhost) {
		this.serverHost = serverhost;
	}
	public double getNodecentralitythreshHold() {
		return nodecentralitythreshHold;
	}
	public void setNodecentralitythreshHold(double nodecentralitythreshHold) {
		this.nodecentralitythreshHold = nodecentralitythreshHold;
	}
	public double getPagerankthreshHold() {
		return pagerankthreshHold;
	}
	public void setPagerankthreshHold(double pagerankthreshHold) {
		this.pagerankthreshHold = pagerankthreshHold;
	}
	public double getNeighborcountThreshHold() {
		return neighborcountThreshHold;
	}
	public void setNeighborcountThreshHold(double neighborcountThreshHold) {
		this.neighborcountThreshHold = neighborcountThreshHold;
	}
	public String getEdgecolorBy() {
		return edgecolorBy;
	}
	public void setEdgecolorBy(String edgecolorBy) {
		this.edgecolorBy = edgecolorBy;
	}
	public String getEdgeType() {
		return edgeType;
	}
	public void setEdgeType(String edgeType) {
		this.edgeType = edgeType;
	}
	public String getNodeSizeBy() {
		return nodeSizeBy;
	}
	public void setNodeSizeBy(String nodeSizeBy) {
		this.nodeSizeBy = nodeSizeBy;
	}
	public String getBackColor() {
		return backColor;
	}
	public void setBackColor(String backColor) {
		this.backColor = backColor;
	}
	public String getLayoutAlgoName() {
		return layoutAlgoName;
	}
	public void setLayoutAlgoName(String layoutAlgoName) {
		this.layoutAlgoName = layoutAlgoName;
	}
	public int getLayoutAlgoIterations() {
		return layoutAlgoIterations;
	}
	public void setLayoutAlgoIterations(int layoutAlgoIterations) {
		this.layoutAlgoIterations = layoutAlgoIterations;
	}
	public float getLayoutAlgoDistance() {
		return layoutAlgoDistance;
	}
	public void setLayoutAlgoDistance(float layoutAlgoDistance) {
		this.layoutAlgoDistance = layoutAlgoDistance;
	}
	public int getElasticsearchPort() {
		return elasticsearchPort;
	}
	public void setElasticsearchPort(int elasticsearchPort) {
		this.elasticsearchPort = elasticsearchPort;
	}
	public String getElasticsearchClusterName() {
		return elasticSearchClusterName;
	}
	public void setElasticsearchClusterName(String clusterName) {
		this.elasticSearchClusterName = clusterName;
	}
	public String getElasticsearchIndex() {
		return elasticsearchIndex;
	}
	public void setElasticsearchIndex(String elasticsearchIndex) {
		this.elasticsearchIndex = elasticsearchIndex;
	}
	public String getElasticsearchIndexType() {
		return elasticsearchIndexType;
	}
	public void setElasticsearchIndexType(String elasticsearchIndexType) {
		this.elasticsearchIndexType = elasticsearchIndexType;
	}
	public int getMongodbPort() {
		return mongodbPort;
	}
	public void setMongodbPort(int mongodbPort) {
		this.mongodbPort = mongodbPort;
	}

	public String getMongodbDatabaseName() {
		return mongodbDatabaseName;
	}

	public void setMongodbDatabaseName(String mongodbDatabaseName) {
		this.mongodbDatabaseName = mongodbDatabaseName;
	}

	public String getMongodbCollectionName() {
		return mongodbCollectionName;
	}

	public void setMongodbCollectionName(String mongodbCollectionName) {
		this.mongodbCollectionName = mongodbCollectionName;
	}

	public String getMongodbFieldName() {
		return mongodbFieldName;
	}

	public void setMongodbFieldName(String mongodbFieldName) {
		this.mongodbFieldName = mongodbFieldName;
	}

	public int getMysqlPort() {
		return mysqlPort;
	}
	public void setMysqlPort(int mysqlPort) {
		this.mysqlPort = mysqlPort;
	}
	public String getMysqlUserName() {
		return mysqlUserName;
	}

	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public static void setInstance(Configuration instance) {
		Configuration.instance = instance;
	}
	
	public boolean forceAtlasLayoutIsConverged() {
		return forceAtlasLayoutIsConverged;
	}

	public void setForceAtlasLayoutIsConverged(boolean forceAtlasLayoutIsConverged) {
		this.forceAtlasLayoutIsConverged = forceAtlasLayoutIsConverged;
	}

	public double getForceAtlasLayoutSpeed() {
		return forceAtlasLayoutSpeed;
	}

	public void setForceAtlasLayoutSpeed(double forceAtlasLayoutSpeed) {
		this.forceAtlasLayoutSpeed = forceAtlasLayoutSpeed;
	}

	public double getForceAtlasLayoutInertia() {
		return forceAtlasLayoutInertia;
	}

	public void setForceAtlasLayoutInertia(double forceAtlasLayoutInertia) {
		this.forceAtlasLayoutInertia = forceAtlasLayoutInertia;
	}

	public double getForceAtlasLayoutGravity() {
		return forceAtlasLayoutGravity;
	}

	public void setForceAtlasLayoutGravity(double forceAtlasLayoutGravity) {
		this.forceAtlasLayoutGravity = forceAtlasLayoutGravity;
	}

	public double getForceAtlasLayoutMaxDisplacement() {
		return forceAtlasLayoutMaxDisplacement;
	}

	public void setForceAtlasLayoutMaxDisplacement(double forceAtlasLayoutMaxDisplacement) {
		this.forceAtlasLayoutMaxDisplacement = forceAtlasLayoutMaxDisplacement;
	}

	public float getFruchtermanReingoldLayoutArea() {
		return fruchtermanReingoldLayoutArea;
	}

	public void setFruchtermanReingoldLayoutArea(float fruchtermanReingoldLayoutArea) {
		this.fruchtermanReingoldLayoutArea = fruchtermanReingoldLayoutArea;
	}

	public double getFruchtermanReingoldLayoutSpeed() {
		return fruchtermanReingoldLayoutSpeed;
	}

	public void setFruchtermanReingoldLayoutSpeed(double fruchtermanReingoldLayoutSpeed) {
		this.fruchtermanReingoldLayoutSpeed = fruchtermanReingoldLayoutSpeed;
	}

	public double getFruchtermanReingoldLayoutGravity() {
		return fruchtermanReingoldLayoutGravity;
	}

	public void setFruchtermanReingoldLayoutGravity(double fruchtermanReingoldLayoutGravity) {
		this.fruchtermanReingoldLayoutGravity = fruchtermanReingoldLayoutGravity;
	}
	
	
	
	@Override
	public String toString() {
		return "Configuration [mysqlHost=" + mysqlHost + ", mysqlPort=" + mysqlPort + ", mysqlUserName=" + mysqlUserName
				+ ", mysqlUserPassword=" + mysqlUserPassword + ", mysqlDatabaseName=" + mysqlDatabaseName
				+ ", mysqlTableName=" + mysqlTableName + ", mysqlDataColumnName=" + mysqlDataColumnName
				+ ", mongodbHost=" + mongodbHost + ", mongodbPort=" + mongodbPort + ", mongodbDatabaseName="
				+ mongodbDatabaseName + ", mongodbCollectionName=" + mongodbCollectionName + ", mongodbFieldName="
				+ mongodbFieldName + ", serverPort=" + serverPort + ", serverHost=" + serverHost
				+ ", elasticsearchHost=" + elasticsearchHost + ", elasticsearchPort=" + elasticsearchPort
				+ ", elasticSearchClusterName=" + elasticSearchClusterName + ", elasticsearchIndex="
				+ elasticsearchIndex + ", elasticsearchIndexType=" + elasticsearchIndexType + ", fileName=" + fileName
				+ ", nodecentralitythreshHold=" + nodecentralitythreshHold + ", pagerankthreshHold="
				+ pagerankthreshHold + ", neighborcountThreshHold=" + neighborcountThreshHold + ", edgecolorBy="
				+ edgecolorBy + ", edgeType=" + edgeType + ", nodeSizeBy=" + nodeSizeBy + ", backColor=" + backColor
				+ ", layoutAlgoName=" + layoutAlgoName + ", layoutAlgoIterations=" + layoutAlgoIterations
				+ ", layoutAlgoDistance=" + layoutAlgoDistance + ", forceAtlasLayoutIsConverged="
				+ forceAtlasLayoutIsConverged + ", forceAtlasLayoutSpeed=" + forceAtlasLayoutSpeed
				+ ", forceAtlasLayoutInertia=" + forceAtlasLayoutInertia + ", forceAtlasLayoutGravity="
				+ forceAtlasLayoutGravity + ", forceAtlasLayoutMaxDisplacement=" + forceAtlasLayoutMaxDisplacement
				+ ", fruchtermanReingoldLayoutArea=" + fruchtermanReingoldLayoutArea
				+ ", fruchtermanReingoldLayoutSpeed=" + fruchtermanReingoldLayoutSpeed
				+ ", fruchtermanReingoldLayoutGravity=" + fruchtermanReingoldLayoutGravity + ", dataSource="
				+ dataSource + ", searchValue=" + searchValue + "]";
	}

	public static synchronized Configuration getInstance(){
		if(instance == null){
			instance = new Configuration();
		}
		return instance;
	}	
}
