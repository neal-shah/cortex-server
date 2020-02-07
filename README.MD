# Cortex Server for Reactive Corda Development

## Running using docker

A cortex-server image is provided in Docker hub [here.](https://hub.docker.com/repository/docker/arhamill/cortex)
Run using:

```$bash
docker run -p 8080:8080 -v "<cordapp-directory>":/opt/cortex/cordapps arhamill/cortex
```

Where `cordapp-directory` is a folder containing your cordapp jars.

The following environment variables can be configured.

<table>
<thead>
<tr><th>Variable</th><th>Description</th><th>Default</th></tr>
</thead>
<tbody>
<tr><td>SERVER_PORT</td><td>Cortex server http port</td><td>8080</td></tr>
<tr><td>CONFIG_RPC_USERNAME</td><td>Corda RPC username</td><td>user1</td></tr>
<tr><td>CONFIG_RPC_PASSWORD</td><td>Corda RPC password</td><td>test</td></tr>
<tr><td>CONFIG_RPC_HOST</td><td>Corda RPC host</td><td>host.docker.internal</td></tr>
<tr><td>CONFIG_RPC_PORT</td><td>Corda RPC port</td><td>10006</td></tr>
<tr><td>CONFIG_RPC_CORDAPPS</td><td>Cordapp directory that Cortex will scan</td><td>/opt/cortex/cordapps</td></tr>
</tbody>
</table>

## Running using local jar

Build the project using:

```$bash
./gradlew bootJar
```

Then start the server using:

```$bash
java -jar build/lib/cortex-server.jar
```