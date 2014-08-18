SURVEYOR OVERVIEW

The use of the SURVEYOR goes beyond geo visualisation of research data. The platform
can, for instance, facilitate functions such as decision support, statistical reporting,
public health surveillance, health research, and cost analysis.


Original requirements:
* reduce the time needed to develop a Surveyor
* refactor existing code base to reduce duplication across multiple surveyors
* use XML to configure the deployment of the surveyor

Emergent requirements:
During development several additional requirements were identified

* Update client code to use v3 API, current google client API is out of date and due to
reach end of life mid November.
* Current code base lacks reusable components for maps, markers and filters
* Few test cases in code base to assure quality of existing code

The core changes involve:

* Refactoring out most of google maps api specific code into widgets/composite exposed from MapCore.
The new refactored code is responsible for building a map, creating markers, specifying marker colours,
displaying marker hover tooltips, marker on click popup panels. By hiding all of the google API
specific logic from the client, allowing for easier maintenance for future upgrades, and less code
to maintain across multiple implementations. Similarly filter widget and date range widget have been
extracted out into components. This allows simple composition of the components to create more complex
applications, while maintaining some level of consistency across apps.

* A new DataProvider abstraction was added as a basic facade over a data source , with a view support
multiple data sources, for instance the current data source is a local JSON file, in the future this could
be a external webservice. This facade exposes basic retrieval methods which support simple queries and
basic faceted search. Faceted search, also called faceted navigation or faceted browsing, is a technique
for accessing information organized according to a faceted classification system, allowing users to
explore a collection of information by applying multiple filters. The current JSON data provider
implementation is backed by Google DataTable implementation, hopefully future implementation
can use SOLR/Lucene backed implementation.

A formal description of the  DataProvider domain:

DataProvider(FilterQuery x FacetField[]) -> QueryResult
QueryResult: RecordList x FacetList
RecordList: Effectively a data table structure with a reference to the schema
FacetList: A list of filter fields available and distinct values in each field, constrained on FilterQuery
FilterQuery := None | (Field, FieldValueToFilter)
FacetField := <Medicines, Publication_Types, Sampling_Types>
SelectorList := {(FacetField, Unique(FieldValues))}
DataTable := Column1 x Column2 x Column3...
Schema := Field x FieldType


* The final set of changes is a web application, configured almost entirely through XML configuration,
this application is responsible for most of the routing logic needed to take users interactions, and translate
this to content to be presented back to the user. It uses much of the components created and the DataProvider
abstraction to drive the retrieval of data. Logic specific to a particular implementation is applied using basic
dependency injection though GWTBinder xml config. Gwt Event Bus feature is used to decouple much of the view
from the presentation layer, this seems to work well in isolating the logic for receiving user input
and dispatching it to the DataProvider layer.

* Most of the new features have been tested thoroughly using GWTTest cases integration tests and unit tests,
a fair bit of work has been involved in setting these up.

Outcomes:
Reduced the time needed to develop and deploy a surveyor from 6 weeks to approx a week.
Reduced the size of the code base, removing duplicate code across AQSurvyeor, Surveyor and NMFISurveyor, reducing the maintenance expense
XML based configuration added to map a dataset dynamically, create facets, tables, maps
Updated code base to use the latest version of google maps API and reducing strong coupling


IntelliJ Setup:

When adding surveyor core in intellij, ensure that surveyorCore sources is excluded from the project build.
To do this:
  * open module settings
  * go to libraries
  * look for surveyorCore
  * and then click on sources and click on exclude
  * also I have excluded the public folder in classes

Use firefox for running the application, in firefox ensure disable entire cache is set, as this
prevents strange behaviour while testing between various applications..

To run GWT test cases in IntelliJ, for instance GwtTestDefaultLocalJSONDataProvider set the JUnit configuration
VM options to -ea -Xms1024m -Xmx3584m or similar..