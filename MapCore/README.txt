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
can use SOLR backed implementation.

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
