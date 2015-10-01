# Twitter Grapher
Twitter Grapher is an open source project for visualizing large data from multiple sources...<br/>

# Installation
<p>Prerequisites</p>
1. Java 8<br>
2. Maven<br>


# Adding dependencies
<p>twitter-grapher have some dependencies, so before building the application you've to add them first.</p>
#### Note:
<p>Please clone the Project to directory whose path doesn't contain blank spaces.</p>
1. clone the twitter-grapher repository on your machine.<br/>
   `git clone https://github.com/project-spinoza/twitter-grapher.git`<br/>
2. <p>To add dependencies run the following commands from the root directory of twitter-grapher</p>
  <p>`mvn install:install-file -Dfile=src/lib/gephi-toolkit.jar -DgroupId=gephi-toolkit -DartifactId=gephi-toolkit -Dversion=1.0 -Dpackaging=jar`</p>
<p>`mvn install:install-file -Dfile=src/lib/gephi-cw.jar -DgroupId=gephi-cw -DartifactId=gephi-cw -Dversion=0.0.1 -Dpackaging=jar`</p>
<p>`mvn install:install-file -Dfile=src/lib/uk-ac-ox-oii-sigmaexporter.jar -DgroupId=uk-ac-ox-oii-sigmaexporter -DartifactId=uk-ac-ox-oii-sigmaexporter -Dversion=1.0 -Dpackaging=jar`</p>

# How to build the application
1. <p>navigate to the twitter-grapher directory and run the following command</p>
  `mvn clean compile package`<br>

# Running the application

## Running at the command line
1. <p>run the following command from the root directory</p>
  <p>`java -jar target/twitter-grapher-v1.0-fat.jar -conf config.json`</p>
2. <p>open your favorite web browser and navigate to `localhost:8080/graph`</p>

## Running in your IDE (Eclipse)
1. Import twitter-grapher project
<p>`File -> Import -> Existing Maven Projects`</p>
2. Set Configuration file (`config.json`) located in the root directory
<p>Navigate to `Run -> Run Configurations` Select `Arguments`, In the `Program arguments` add the following tab</p>
<p>`-conf config.json`</p>
3. Run `com.tg.TwitterGrapher.java`
