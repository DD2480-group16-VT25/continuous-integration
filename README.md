# continuous-integration
Assignment #2 for DD2480 - Group 16

## Pre-requisites

To run the server, you need to have maven installed. If you don't have maven installed, you can install it by following the instructions [here](https://maven.apache.org/install.html).

You also need Java $\geq$ 18.

### Dependencies

#### Testing
- **JUnit 5 (Jupiter)** - Testing framework
  - `junit-jupiter-api`
  - `junit-jupiter-params`
  - `junit-jupiter-engine` (v5.11.4)
- **Mockito** - Mocking framework for unit tests
  - `mockito-core` (v5.14.2)

#### Web Server
- **Jetty** - Embedded web server
  - `jetty-server` (v9.4.51.v20230217)
- **Java Servlet API** - Servlet support
  - `javax.servlet-api` (v4.0.1)

#### JSON Processing
- **org.json** - JSON processing library
  - `json` (v20240205)
- **Gson** - Google's JSON library
  - `gson` (v2.12.1)

#### Git Integration
- **JGit** - Git operations in Java
  - `org.eclipse.jgit` (v7.1.0)

#### Build & Development
- **Maven Invoker**
  - `maven-invoker` (v3.2.0)
- **SLF4J**
  - `slf4j-simple` (v2.0.16)

#### Configuration
- **Dotenv** - Environment variable management
  - `dotenv-java` (v3.0.0)

## Running the server

```
git clone https://github.com/DD2480-group16-VT25/decide.git
cd continuous-integration/my-app
mvn clean compile
mvn exec:java -Dexec.mainClass="com.group16.app.ContinuousIntegrationServer"
```

## Using ngrok to get a public URL for webhooks
Once the server is running, in a **new terminal window**, add your ngrok authtoken to the config if you have not done so already.
```
ngrok config add-authtoken <your_auth_token>
```

Run ngrok
```
ngrok http http://localhost:8080
```

This will show you the public URL that you can use to send requests to the server. Under "forwarding" you will see a URL that looks like `https://<random_string>.ngrok-free.app`. You can use this URL to send requests to the server, for example by adding it to the webhook in your GitHub repository.

## Testing the CI server

To run the tests, you can use the following commands:
```
cd my-app
mvn test
```

## Statement of contributions

- **Linus**: Implemented the notification feature together with Robin, including unit tests. Worked on getting the CI server to use asynchronous processing, and differentiating between different types of requests.
- **Robin**: Implemented the notification feature together with Linus, including unit tests.
-

## Team state

We agree most with the description/checklist of **collaborating**. We felt that we were able to work together effectively and communicate well, we planned many meetings and worked together when needed. We cannot really align ourselves with the next step's "Wasted work and the potential for wasted work are continuously identified and eliminated."


## Implementations explained

### Notifications

We decided to use GitHub status notifications, we looked up the GitHub API documentation and created a Personal Access Token (PAT) to authenticate our requests. In the code we create a JSON object with the required information and sent a POST request to the GitHub API.

The unit tests work by setting the status of a single commit to each of the possible states (success, failure, error, pending) and then checking that the status is set correctly (using a GET request). Between each test, the status is reset to `pending` to ensure that the tests are independent of each other.
