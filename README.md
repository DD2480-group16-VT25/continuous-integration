# continuous-integration
Assignment #2 for DD2480 - Group 16

## Pre-requisites

To run the server, you need to have maven installed. If you don't have maven installed, you can install it by following the instructions [here](https://maven.apache.org/install.html).

You also need Java $\geq$ 18.

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