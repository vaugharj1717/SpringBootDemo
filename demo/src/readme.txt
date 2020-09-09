~~~Candidate Interview Scheduler Backend Guide~~~

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
OVERVIEW
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
This backend system will be using Spring Boot to provide a web API.
Our frontend will be using React.Js
Since React.Js is used to create single-page web applications, our backend system will NOT send HTML/CSS/Javascript
in its responses, but instead will just send JSON which the React.JS app can use to modify the view

In the main/java/(...) folder four packages can be seen. These packages each correspond to a different layer of the project.

1) Controllers  [Catches URL]
2) Services     [Business Logic]
3) DAOs         [Data retrieval / persistence]
4) Entities     [Representation of Database entities as Java objects]

The flow control will normally go like this...
-User enters URL into browser (or clicks on a link, etc)
-The controller layer will "catch" the URL and call a service function
-The service function will employ any business logic, and ask DAO to get data from database
-DAO will retrieve/update/delete data from database. Automatically converts DB data into "Entities" to do this
-Call stack makes it's way back to controller, who will return JSON response to User.

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
ANNOTATIONS
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
A lot of Spring Boot's logic is executed through annotations (using @ syntax above class or function definitions).
Below is some information on various annotations and how they relate to Spring's built-in functionality

ALL
----------
@Autowired
    -Since Spring Boot does a lot of work for us, it needs to know how to set up our objects' instance variables
    -Putting @Autowired above a "setter" function will let Spring Boot automatically set up our objects for us
    -For our purposes, this is used to "glue" our layers together
        ex) Controller layer uses Service layer. Controller objects will need any Service objects "autowired" into it
        ex) Service layer uses DAO layer. Service objects will need any DAO objects "autowired" into it

CONTROLLER
----------
@RestController
    -Place this above a Controller class definition so Spring Boot knows it's a controller
@RequestMapping("/url/goes/here")
    -Place this above a Controller function and tells Spring that the function should run if it matches with the URL
    -Can also go above the Controller class definition to match every function in the class with a URL prefix
@RequestMapping(value = "/url/goes/here", method = RequestMethod.POST)
    -Similar to above, but now we are also declaring the method used in the HTTP request (by default the method is GET)

SERVICE
---------
@Service
    -Put this above a service class definition so Spring Boot knows it's a service class

DAOs
---------
@Repository
    -Put this above a DAO class definition so Spring Boot knows it's a DAO class (or repository class in Spring Boot lingo)
@PersistenceUnit
    -This tag is only used once, and can be found in the AbstractDAO class
    -Tells Spring Boot that the EntityManagerFactory object will be used to manage all the behind-the-scenes database stuff
    -Since all of our DAOs will extend the AbstractDAO class, they will have access to this EntityManagerFactory object
    -This object can create an EntityManager for each of our database transactions
    -This EntityManager object has all the functionality we need to do database stuff
        ex) Can begin/commit/rollback transactions
        ex) Can query, save, delete data from database based on whatever search criteria we give it

ENTITIES
---------
In many applications, you have to create a database schema and then create Entity classes that match the schema
In Spring Boot you can do this the opposite way. We create our Entity classes and Spring Boot will automatically
interpret the classes and create a database schema for you that matches the entities.
TAKEAWAY: We can design our database schema entirely in Java, no need to use Database Console

@Entity
    -Goes above an Entity class so Spring Boot knows it's an Entity class
@Id
@GeneratedValue(strategy = GenerationType.AUTO)
    -Goes above the Id field for an entity so Spring Boot knows to treat the ID as a Primary Key
    -The @GeneratedValue tag tells Spring to automatically generate an ID for the entity (normally by incrementing)
@ManyToOne
    -If an entity is the "Many" side of a ManyToOne relationship,
     this tag goes above the field corresponding to the "one" side
@OneToMany
    -If an entity is the "One" side of a OneToMany relationship,
     this tag goes above the list corresponding to the "many" side
@OneToOne
    -If an entity has a one-to-one relationship,
     this tag goes above the field corresponding to the other side of the relationship
@ManyToMany
    -Also exists, creates an implicit Join-Table behind the scenes
    -A lot of Spring users suggest not using this, and to just explicitly define your JoinTables to make life easier
@Transient
    -Goes above a field if the field should NOT be placed in the database
    -Not used often. Might use it for password security reasons?
Cascade Types
    -Attached as a parameter to a @ManyToOne, @OneToMany, or @OneToOne
        -ex) @OneToOne(cascade = CascadeType.ALL)
        -ex) @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
        -ex) @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    -Tells Spring that the database action should also be done to the child element
    -Almost always done on the parent side cascading to child side
        -ex) A building has many rooms. If we deleted a building from the database, we'd want to delete the rooms as well
        -ex) A user also has a userDetails table (one-to-one). If we save a user, we also want to save it's details

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
EntityManager
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
The EntityManager class contains all the functionality we will need to interact with the database
Works with any database implementation, meaning same code will work on our test database vs our MySql database
em.find()               Used to retrieve from DB
em.remove()             (Used to remove from DB)
em.merge()              (Used to add to DB if new entry, or update entry in DB if already exists)
em.getTransaction.begin()       Begins DB transaction
em.getTransaction.commit()      Commits DB transaction
em.getTransaction.rollback()    Rolls back DB transactions
em.createQuery(String query)        Used to create a Query object. Query object uses JPQL (Java persistence query language... more info below)
    -The query is a JPQL query (Java Persistence Query Language)
    -Since Spring will automatically fill in linked entities for us, we don't need to do too many joins
        -ex) instead of doing "SELECT u FROM User u JOIN UserDetails ud" we can just do "SELECT u FROM User u"
             if we need to access the UserDetails object, Spring Boot will get that data for us automatically
             behind the scenes
        -TAKEAWAY: Complex JOINS normally not necessary. Just SELECT one type of entity and when you need to access
                   a related entity just use that objects .getXYZ() function to "grab" that entity

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Interfacing
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
-Notice that our DAOs and Service classes have interfaces that they implement in the class definition
-This is normally considered a good programming practice as it allows us to easily swap out different implementations
 for each interface.
-In our case, we're moreso doing things this way to allow testing to work properly (allow use of Mock Objects)
-General rules to follow for this project...
    1) Every service should implement a corresponding service interface
        ex) MeetingServiceImpl implements the MeetingService interface
    2) Every DAO should extend the AbstractDAO class and implement a corresponding interface, which in turn
     implements the DAO interface
        ex) MeetingDAOImpl implements the MeetingDAO interface, which implements the DAO interface
    3) Every Entity should extend the DataObject interface
    4) Public functions (non-helper functions) should be declared in interface so they can be used/tested

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
JSON
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
The current strategy for parsing JSON requests and creating JSON responses is not final. If we find a better way,
we can switch to that. Currently I am doing the following...

To PARSE Json from an HTTP request...
    1) Declare this variable in the controller class:       JsonParser parser = JsonParserFactory.getJsonParser();
    2) Declare "@RequestBody" String body" as a parameter in the controller function
    3) Put this line of code at beginning of function:      Map<String, Object> bodyMap = parser.parseMap(body);
    4) Now JSON values can be pulled from map with:         bodyMap.get("keyNameGoesHere")

To CREATE Json to return to user
    1) Autowire this variable into controller class with setter:
            ObjectMapper mapper;
            @Autowired
            public void setObjectMapper(ObjectMapper objectMapper) { this.mapper = objectMapper; }
    2) Create return node with following line of code. Each node corresponds to a Json object  {}
            ObjectNode response = mapper.createObjectNode();
    3) Add key/values to JSON with
            response.put("KeyName", value_here);
    4) If necessary, you can add nested objects (nodes within nodes)
    5) Return the ObjectNode
            return response;

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
TESTING
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Notice the following two files
    1) main/resources/application.properties
    2) test/resources/application.properties
These are two config files that allow us to set different configurations for when we are testing vs actual runtime
Eventually we will have these files configured to use a MySQL database during runtime.
During testing, we can use the H2 database (an in-memory database) that we can fill with preconfigured data each
time tests are run so we have predictable data sets to run our tests against.

Coming soon...