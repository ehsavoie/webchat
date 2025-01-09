Simple WebChat Example
========================

This simple application is aiming to provide a demonstrator of the WildFly AI Feature Pack.

## Setting up Ollama

You will need to have either **docker** or **podman** installed.

To start Ollama and select the proper model (aka `llama3.1:8b) executethe following commands:

```shell
podman run -d --rm --name ollama --replace --pull=always -p 11434:11434 -v ollama:/root/.ollama --stop-signal=SIGKILL docker.io/ollama/ollama

podman exec -it ollama ollama run llama3.1:8b
```
To quit the Ollama prompt, type **/bye**.

Once you have finished you can stop Ollama using:
```shell
podman stop ollama
```
##  Provisioning using the [WildFly Maven Plugin](https://github.com/wildfly/wildfly-maven-plugin/)

You need to include the WildFly AI feature-pack and layers in the Maven Plugin configuration. This looks like:

```xml
...
<feature-packs>
  <feature-pack>
    <location>org.wildfly:wildfly-galleon-pack:35.0.0.Final</location>
  </feature-pack>
  <feature-pack>
    <location>org.wildfly:wildfly-ai-galleon-pack:1.0.0-SNAPSHOT</location>
  </feature-pack>
</feature-packs>
<layers>
    <!-- layers may be used to customize the server to provision-->
    <layer>cloud-server</layer>
    <layer>ollama-chat-model</layer>
    <layer>default-embedding-content-retriever</layer>
    <!-- providing the following layers -->
    <!--
      <layer>in-memory-embedding-model-all-minilm-l6-v2</layer>
      <layer>in-memory-embedding-store</layer>
    -->
    <!-- Exisiting layers thart can be used -->
    <!--
      <layer>ollama-embedding-model</layer>
      <layer>openai-chat-model</layer>
      <layer>mistral-ai-chat-model</layer>
      <layer>weaviate-embedding-store</layer>
      <layer>web-search-engines</layer>
    -->
</layers>
...
```
##  Provisioning using the [WildFly Maven Plugin and Glow](https://github.com/wildfly/wildfly-maven-plugin/)

This makes the provisioning seamless and the XML simpler:

```xml
...
<discoverProvisioningInfo>
  <spaces>
    <space>incubating</space>
  </spaces>
</discoverProvisioningInfo>
...
```


##  Building and running the example application

You build using Apache Maven with the following command:

```shell
mvn clean install
```
You can now start the server:
```shell
 ./target/server/bin/standalone.sh 
```
You can interact with the application using:
* some simple REST endpoints defined on [the service page](http://localhost:8080/service.html)
* A simple webchat interface for [RAG](http://localhost:8080/) using WildFly documentation. 
You can ask question that the RAG will try to answer like: **"How do you configure a connection factory to a remote Artemis server ?"**.

##  Building and running the example application with OpenTelemetry

You build using Apache Maven with the following command:

```shell
mvn clean install -Popentelemetry
```
You can now start the otle collector:
```shell
 ./opentelemetry.sh 
```
You can now start the server:
```shell
 ./target/server/bin/standalone.sh 
```
You can interact with the application using:
* some simple REST endpoints defined on [the service page](http://localhost:8080/service.html)
* A simple webchat interface for [RAG](http://localhost:8080/service.html) using WildFly documentation. 
You can ask question that the RAG will try to answer like: **"How do you configure a connection factory to a remote Artemis server ?"**.
