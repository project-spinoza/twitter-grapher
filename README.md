# twitter-grapher
Twitter Grapher is an open source project for visualizing large data from multiple sources...<br/>

# Adding dependencies
<p>twitter-grapher have some dependencies so before building the application you've to add them first.</p>
1. clone the twitter-grapher repository on your machine.<br/>
   `git clone https://github.com/project-spinoza/twitter-grapher.git`<br/>
2. to add dependencies run the following commands from the root directory of twitter-grapher
  `mvn install:install-file -Dfile=src/lib/gephi-toolkit.jar -DgroupId=gephi-toolkit -DartifactId=gephi-toolkit -Dversion=1.0 -Dpackaging=jar`<br/>
`mvn install:install-file -Dfile=src/lib/gephi-cw.jar -DgroupId=gephi-cw -DartifactId=gephi-cw -Dversion=0.0.1 -Dpackaging=jar`<br/>
`mvn install:install-file -Dfile=src/lib/uk-ac-ox-oii-sigmaexporter.jar -DgroupId=uk-ac-ox-oii-sigmaexporter -DartifactId=uk-ac-ox-oii-sigmaexporter -Dversion=1.0 -Dpackaging=jar`

# How to build the application
1. navigate to twitter-grapher directory and run the following commands<br/>
  `mvn clean compile`<br/>
  `mvn clean package`<br/>

# Running the application
1. navigate to the target directory and run the following command<br/>
  `java -jar twitter-grapher-v1.0-fat.jar -conf ../config.json`<br/>
2. open your favorite web browser and navigate to `localhost:8080/graph`
