## WWARN Maps Surveyor overview
WWARN Maps Surveyors are a mapping platform for visualising data to support statistical 
reporting, public health surveillance, and health research. However, the use of the this 
goes beyond geo visualisation of research data. The platform can, for instance, facilitate 
functions such as decision support, statistical mapping, crime surveillance etc.

At WWARN we use maps to communicate information about the incidence of malarial drug resistance  across a region, 
report on anti malarial drug quality in a country, map the aetiology of non malarial febrile  illness across 
continents. As the demand for creating surveyors increased, we decided to refactor the existing GWT code base for
mapping and develop a reusable framework in GWT to create new maps. 

These requirements can be summarised as:

* reduce the time needed to develop maps 
* reduce the duplication and maintenance overhead for each implementation of the maps surveyor
 
We approached these requirements by developing these features:

* refactoring the code base to create reusable components for the UI, for instance Map, Filter, Markers, Popups etc
* designed a simple DSL to configure the web application using XML configuration file

Features supported in XML configuration:

* a dataprovider abstraction was added as a basic facade over a data source , with a view support multiple data 
   sources, for instance the current data source is a local JSON file, in the future this could be a google spreadsheet
* support for faceted browsing, with several configurable facet filter UI components, including multiple selection
* a schema datatype to map the data source to an internal representation
* view types beyond maps are supported, like tables, links static html  
    
## How do I use it?
The use of this framework assumes some familiarity with GWT framework, mvn, and an IDE like Eclipse or Intellij.

1. Add Maven dependencies

```xml
    <dependency>
        <groupId>org.wwarn.surveyor</groupId>
        <artifactId>SurveyorCore</artifactId>
        <version>1.7.2-SNAPSHOT</version>
    </dependency>
```

2. Add the inherits statement to your module.gwt.xml.	
```xml
    <inherits name="org.wwarn.surveyor.SurveyorCore"/>
```
	
3. Clone the sample application, and configure xml

4. Build
   Maven commands: Deployment - Jetty:

   ```
    mvn clean verify org.codehaus.cargo:cargo-maven2-plugin:run
   ```

   Deployment - Tomcat6x: Should deploy to http://localhost:8080/nmfisurveyor-1.0-SNAPSHOT

   ```
    mvn clean verify org.codehaus.cargo:cargo-maven2-plugin:run -DskipTests -Dcargo.maven.containerId=tomcat6x -Dcargo.maven.containerUrl=http://archive.apache.org/dist/tomcat/tomcat-6/v6.0.37/bin/apache-tomcat-6.0.37.zip
   ```
   
   Deployment - Tomcat7x

   ```
     mvn clean verify org.codehaus.cargo:cargo-maven2-plugin:run -DskipTests -Dcargo.maven.containerId=tomcat7x -Dcargo.maven.containerUrl=http://archive.apache.org/dist/tomcat/tomcat-7/v7.0.16/bin/apache-tomcat-7.0.16.zip
   ```

## Where can I learn more
Check the README files in the SurveyorCore and MapCore modules