### Usage

```java
${testStandard}
```

#### Customize by Override

```java
${customizeMongodByOverride}
```

#### Customize by Builder

```java
${customizeMongodByBuilder}
```

#### Customize by Replacement

```java
${customizeMongodByReplacement}
```

### Usage - Optimization

All artifacts are cached. Even so the extracted results.

### Unit Tests

TODO

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

```java
${testCustomOutputToFile.FileStreamProcessor}
```

#### ... to null device
```java
${testDefaultOutputToNone}
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
${testMongosAndMongod}
```

### Import JSON file with mongoimport command
```java
${importJsonIntoMongoDB}
```
                      
### User/Roles setup

```java
${setupUserAndRoles}
```

```java
${setupUserAndRoles.EnableAuthentication}
```

----

YourKit is kindly supporting open source projects with its full-featured Java Profiler.
YourKit, LLC is the creator of innovative and intelligent tools for profiling
Java and .NET applications. Take a look at YourKit's leading software products:
<a href="http://www.yourkit.com/java/profiler/index.jsp">YourKit Java Profiler</a> and
<a href="http://www.yourkit.com/.net/profiler/index.jsp">YourKit .NET Profiler</a>.
