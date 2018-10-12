## WWARN Maps Surveyor overview [![Build Status](https://travis-ci.org/WorldwideAntimalarialResistanceNetwork/WWARN-Maps-Surveyor.svg?branch=master)](https://travis-ci.org/WorldwideAntimalarialResistanceNetwork/WWARN-Maps-Surveyor)

WWARN Maps Surveyor is a mapping platform for visualising data to support research synthesis and 
reporting in global health settings and health research. However, the use of the this 
goes beyond geo visualisation of research data. The platform can, for instance, facilitate 
functions such as decision support, statistical mapping, crime surveillance etc.

At WWARN we use maps to communicate information about the incidence of malarial drug resistance  across a region, 
report on anti malarial drug quality in a country, map the aetiology of non malarial febrile  illness across 
continents. As the demand for creating surveyors increased, we decided to refactor the existing GWT code base for
mapping and develop a reusable framework in GWT to create new maps. 

These requirements can be summarised as:

* reduce the time needed to develop maps 
* reduce the maintenance overhead for each implementation of the maps surveyor, reduce code duplication across several implementations  
 
We approached these requirements by developing these features:

* refactoring the code base to create reusable components for the UI, for instance Map, Filter, Markers, Popups etc
* designed a simple DSL to configure the web application using XML configuration file
* improve automated testing across the platform to improve code maintainability, we are following a [test pyramid](http://martinfowler.com/bliki/TestPyramid.html) approach, focusing intially on unit testing, followed by service and UI tests.

Features supported in XML configuration:

* a dataprovider abstraction was added as a basic facade over a data source , with a view support multiple data 
   sources, for instance the current data source is a local JSON file, in the future this could be a google spreadsheet
* support for [faceted browsing](http://en.wikipedia.org/wiki/Faceted_search), with several configurable facet filter UI components, including multiple selection
* a schema datatype to map the data source to an internal type representation
* support views beyond maps, such as tables, links static embedded content etc.

Limitations:

* For Big data, ie billions of records, consider solutions which down samples very large data sets such as [imMens](http://vis.stanford.edu/projects/immens/) which aggregates over very large data sets and partitions the data based on the projection of the data needed for visualisation
    
## Demos and implementations

The offline capable surveyor is available at:
[http://www.wwarn.org/surveyor/NMFI/](http://www.wwarn.org/surveyor/NMFI/) and uses [http://offlinefirst.org](http://offlinefirst.org) principles and a demo of the offline capability is here : [https://vimeo.com/135569913](https://vimeo.com/135569913)

There are several other implementations of the surveyor:
* Kelch13 resistance markers: [http://www.wwarn.org/molecular/surveyor/k13/](http://www.wwarn.org/molecular/surveyor/k13/)
* Mapping resistance markers with the molecular surveyor:  [http://www.wwarn.org/molecular/surveyor](http://www.wwarn.org/molecular/surveyor)
* Pharmaceutical crimes with Interpol: [http://pharmacrimelaws.org](http://pharmacrimelaws.org)
* WWARN Explorer: [http://www.wwarn.org/explorer/app/](http://www.wwarn.org/explorer/app/) , this explores antimalarial drug resistance in 4 areas - 
clinical trials, pharmacology, in vitro drug testing, and molecular markers of resistance. This is although not strictly completely on surveyor codebase, the core mapping components are from the same code base.
* [Live demonstrator](http://wwarn-maps-surveyor-demo.appspot.com) based on [SurveyorSimpleDemoApp](https://github.com/WorldwideAntimalarialResistanceNetwork/WWARN-Maps-Surveyor/tree/master/SurveyorSimpleDemoApp)
* [AQ Surveyor](http://www.wwarn.org/aqsurveyor/) - Custom views of published poor-quality antimalarial medicine reports
* [Molecular Surveyor](http://www.wwarn.org/surveyor/) - Visualise data from publications  examining molecular markers of antimalarial resistance in malaria-endemic countries
* [Offline capable NMFI Fever Series Surveyor](http://www.wwarn.org/surveyor/NMFI/#) - A visual analysis of published health literature for causes of non-malaria febrile disease

## Deployment and versioning:
We try to follow semantic versioning [semver.org](http://semver.org) principles outlined:

Given a version number MAJOR.MINOR.PATCH, increment the:

MAJOR version when you make incompatible API changes,
MINOR version when you add functionality in a backwards-compatible manner, and
PATCH version when you make backwards-compatible bug fixes.
Additional labels for pre-release and build metadata are available as extensions to the MAJOR.MINOR.PATCH format.

Set new version:
mvn versions:set -DnewVersion=1.4.1-SNAPSHOT

Test
mvn clean verfiy

Commit/revert:
mvn versions:(commit|revert)


## How do I use it?
The use of this framework assumes some familiarity with GWT framework, mvn, and an IDE like Eclipse or Intellij.

1. Clone the sample application, and configure xml
` git clone https://github.com/WorldwideAntimalarialResistanceNetwork/WWARN-Maps-Surveyor.git`

2. Build
   Maven commands: Deployment - Jetty:

```
 cd WWARN-Maps-Surveyor/
 mvn -DskipTests
 cd SurveyorSimpleDemoApp/
 mvn clean verify org.codehaus.cargo:cargo-maven2-plugin:run
```

   Deployment - Tomcat6x: Should deploy to http://localhost:8080/[artifactID]-[version]

   ```
    cd SurveyorSimpleDemoApp
    mvn clean verify org.codehaus.cargo:cargo-maven2-plugin:run -DskipTests -Dcargo.maven.containerId=tomcat6x -Dcargo.maven.containerUrl=http://archive.apache.org/dist/tomcat/tomcat-6/v6.0.37/bin/apache-tomcat-6.0.37.zip
   ```
   
   Deployment - Tomcat7x

   ```
     cd SurveyorSimpleDemoApp
     mvn clean verify org.codehaus.cargo:cargo-maven2-plugin:run -DskipTests -Dcargo.maven.containerId=tomcat7x -Dcargo.maven.containerUrl=http://archive.apache.org/dist/tomcat/tomcat-7/v7.0.16/bin/apache-tomcat-7.0.16.zip
   ```

## Where can I learn more
Check the README files in the SurveyorCore and MapCore modules

## Get in touch
We have setup a google web forum at [Worldwide Antimalarial Resistance Network Open Source Software](https://groups.google.com/forum/?hl=en#!forum/worldwide-antimalarial-resistance-network-open-source-software), please use this to get in touch if you have any questions. Alternatively, if you have noticed a bug/have a new feature request use [GitHub issue tracking system to report this](https://github.com/WorldwideAntimalarialResistanceNetwork/WWARN-Maps-Surveyor/issues). 

## Project Sponsored by
[IntelliJ IDEA Free Open Source License](https://www.jetbrains.com/buy/opensource/?product=idea)
