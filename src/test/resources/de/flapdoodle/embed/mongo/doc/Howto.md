### Usage

```java
${testStandard}
```

### Usage - Optimization

You should make the MongodStarter instance or the RuntimeConfig instance static (per Class or per JVM).
The main purpose of that is the caching of extracted executables and library files. This is done by the ArtifactStore instance
configured with the RuntimeConfig instance. Each instance uses its own cache so multiple RuntimeConfig instances will use multiple
ArtifactStores an multiple caches with much less cache hits:)  

### Usage - custom mongod filename

If you do not restrict `bindId` to `localhost` you get windows firewall dialog popups.
To avoid them you can choose a stable executable name with UserTempNaming.
This way the firewall dialog only popups once. See [Executable Collision](#executable-collision)

```java
${testCustomMongodFilename}
```

### Unit Tests

```java
public abstract class AbstractMongoDBTest extends TestCase {


  /**
   * please store Starter or RuntimeConfig in a static final field
   * if you want to use artifact store caching (or else disable caching) 
   */
  private static final MongodStarter starter = MongodStarter.getDefaultInstance();

  private MongodExecutable _mongodExe;
  private MongodProcess _mongod;

  private MongoClient _mongo;
  private int port;
  
  @Override
  protected void setUp() throws Exception {
    port = Network.getFreeServerPort();
    _mongodExe = starter.prepare(createMongodConfig());
    _mongod = _mongodExe.start();

    super.setUp();

    _mongo = new MongoClient("localhost", port);
  }
  
  public int port() {
    return port;
  }

  protected IMongodConfig createMongodConfig() throws UnknownHostException, IOException {
    return createMongodConfigBuilder().build();
  }

  protected MongodConfigBuilder createMongodConfigBuilder() throws UnknownHostException, IOException {
    return new MongodConfigBuilder()
      .version(Version.Main.PRODUCTION)
      .net(new Net(port, Network.localhostIsIPv6()));
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();

    _mongod.stop();
    _mongodExe.stop();
  }

  public Mongo getMongo() {
    return _mongo;
  }

}
```

#### ... with some more help

```java
${testMongodForTests}
```

### Customize Download URL

```java
${testCustomizeDownloadURL}
```

### Customize Proxy for Download
```java
${testCustomProxy}
```

### Customize Artifact Storage
```java
${testCustomizeArtifactStorage}
```

### Usage - custom mongod process output

#### ... to console with line prefix
```java
${testCustomOutputToConsolePrefix}
```

#### ... to file
```java
...
${testCustomOutputToFile}
...

```

#### ... to java logging
```java
${testCustomOutputToLogging}
```

#### ... to default java logging (the easy way)
```java
${testDefaultOutputToLogging}
```

#### ... to null device
```java
${testDefaultOutputToNone}
```

### Custom Version
```java
${testCustomVersion}
```

### Main Versions
```java
${testMainVersions}
```

### Use Free Server Port

  Warning: maybe not as stable, as expected.

#### ... by hand
```java
${testFreeServerPort}
```

#### ... automagic
```java
${testFreeServerPortAuto}
```

### ... custom timeouts
```java
${testCustomTimeouts}
```

### Command Line Post Processing
```java
${testCommandLinePostProcessing}
```

### Custom Command Line Options

We changed the syncDelay to 0 which turns off sync to disc. To turn on default value used defaultSyncDelay().
```java
${testCommandLineOptions}
```

### Snapshot database files from temp dir

We changed the syncDelay to 0 which turns off sync to disc. To get the files to create an snapshot you must turn on default value (use defaultSyncDelay()).
```java
${testSnapshotDbFiles}
```

### Custom database directory  

If you set a custom database directory, it will not be deleted after shutdown
```java
${testCustomDatabaseDirectory}
```

### Start mongos with mongod instance

this is an very easy example to use mongos and mongod
```java
  int port = 12121;
  int defaultConfigPort = 12345;
  String defaultHost = "localhost";

  MongodProcess mongod = startMongod(defaultConfigPort);

  try {
    MongosProcess mongos = startMongos(port, defaultConfigPort, defaultHost);
    try {
      MongoClient mongoClient = new MongoClient(defaultHost, defaultConfigPort);
      System.out.println("DB Names: " + mongoClient.getDatabaseNames());
    } finally {
      mongos.stop();
    }
  } finally {
    mongod.stop();
  }

  private MongosProcess startMongos(int port, int defaultConfigPort, String defaultHost) throws UnknownHostException,
      IOException {
    IMongosConfig mongosConfig = new MongosConfigBuilder()
      .version(Version.Main.PRODUCTION)
      .net(new Net(port, Network.localhostIsIPv6()))
      .configDB(defaultHost + ":" + defaultConfigPort)
      .build();

    MongosExecutable mongosExecutable = MongosStarter.getDefaultInstance().prepare(mongosConfig);
    MongosProcess mongos = mongosExecutable.start();
    return mongos;
  }

  private MongodProcess startMongod(int defaultConfigPort) throws UnknownHostException, IOException {
    IMongodConfig mongoConfigConfig = new MongodConfigBuilder()
      .version(Version.Main.PRODUCTION)
      .net(new Net(defaultConfigPort, Network.localhostIsIPv6()))
      .configServer(true)
      .build();

    MongodExecutable mongodExecutable = MongodStarter.getDefaultInstance().prepare(mongoConfigConfig);
    MongodProcess mongod = mongodExecutable.start();
    return mongod;
  }
```

### Import JSON file with mongoimport command
```java
${importJsonIntoMongoDB}
```

### Executable Collision

There is a good chance of filename collisions if you use a custom naming schema for the executable (see [Usage - custom mongod filename](#usage---custom-mongod-filename)). If you got an exception, then you should make your RuntimeConfig or MongoStarter class or jvm static (static final in your test class or singleton class for all tests).

----

YourKit is kindly supporting open source projects with its full-featured Java Profiler.
YourKit, LLC is the creator of innovative and intelligent tools for profiling
Java and .NET applications. Take a look at YourKit's leading software products:
<a href="http://www.yourkit.com/java/profiler/index.jsp">YourKit Java Profiler</a> and
<a href="http://www.yourkit.com/.net/profiler/index.jsp">YourKit .NET Profiler</a>.
