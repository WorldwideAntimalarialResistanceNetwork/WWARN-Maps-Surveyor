Sample surveyor
=============
See surveyor core for documentation on the framework

Build
=====

Maven commands:
 Deployment - Jetty:
 mvn clean verify org.codehaus.cargo:cargo-maven2-plugin:run
 Deployment - Tomcat6x:
 mvn clean verify org.codehaus.cargo:cargo-maven2-plugin:run -DskipTests -Dcargo.maven.containerId=tomcat6x -Dcargo.maven.containerUrl=http://archive.apache.org/dist/tomcat/tomcat-6/v6.0.37/bin/apache-tomcat-6.0.37.zip
 Should deploy to http://localhost:8080/[appname $version]
 mvn clean verify org.codehaus.cargo:cargo-maven2-plugin:run -DskipTests -Dcargo.maven.containerId=tomcat7x -Dcargo.maven.containerUrl=http://archive.apache.org/dist/tomcat/tomcat-7/v7.0.16/bin/apache-tomcat-7.0.16.zip

App engine (https://developers.google.com/appengine)
=========
Maven overview with appengine:
https://developers.google.com/appengine/docs/java/tools/maven

Deploy local dev server:
mvn clean -P gae appengine:devserver

Deploy to live with app engine:
mvn clean -P gae appengine:update

Compile report:
mvn clean verify gwt:compile-report
--check target/site

Error state recovers:
On 409 Conflict Another transaction by user XXX is already in progress
http://stackoverflow.com/questions/3215140/google-app-engine-appcfg-py-rollback
mvn  -P gae appengine:appcfg rollback

IntelliJ setup
==============

For local development use the contents of dev.web.xml to replace the contents
of web.xml, do not commit this to main repo, ensure that live.web.xml is
used for all live builds.

IntelliJ IDE run gwt test cases:
http://raibledesigns.com/rd/entry/testing_gwt_applications

Setup admin pages in IntelliJ for debug

