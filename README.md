# Twitter Grapher
The purpose of this project is to allow a user to visualize large set of data from multiple data sources.<br/><br/>
![WordCrowd screenshot](images/twitter-grapher1.jpg)  <br/><br/><br/>
![WordCrowd screenshot](images/twitter-grapher.jpg)  <br/>
# Installation
<p>Prerequisites</p>
1. Java 8<br>
2. Maven<br>


# Adding dependencies
<p>twitter-grapher have some dependencies, so before building the application you've to add them first.</p>
#### Note:
<p>Clone the Project to directory whose path doesn't contain blank spaces.</p>
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

### Running at the command line
<p>run the following command from the root directory</p>
`java -jar target/twitter-grapher-v1.0-fat.jar -conf config.json`

### Running in your IDE (Eclipse)
1. Import twitter-grapher project
<p>`File -> Import -> Existing Maven Projects`</p>
2. Set Configuration file (`config.json`) located in the root directory
<p>In IDE Navigate to `Run -> Run Configurations` then select `Arguments` tab, In the `Program arguments` add the following:</p>
<p>`-conf config.json`</p>
3. <p>Run `com.tg.TwitterGrapher.java`</p>

### Open your favorite web browser
Navigate to `http://localhost:8080/graph`
