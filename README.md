# System of Citizen Appeals to Government

It's an implementation of the test task from the vacancy http://infotech24.ru/tabid/102/Default.aspx.
It uses the jwt authentication. In addition to the specified requirements I've added:
* Account confirmation by email.
* The side of the government representative, where he can view all the appeals.

## Build and deploy the project
```mvn clean install```

You'll get a jar file which can be run by:

```java -jar name.jar```

But, before running, need to configure a psql server listen on port 5432. Once deployed, you can access the app at http://localhost:9010.
