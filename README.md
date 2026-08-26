# SFG Beer Works - Brewery Microservices

This project has a services of microservices for deployment via Docker Compose and Kubernetes and is one element of the KBE.
See Gateway Project for Detailed description:
https://github.com/dboeckli/kbe-brewery-gateway/blob/master/README.md

This project has been upgraded to spring boot 3.4.1.
Original git repository: https://github.com/springframeworkguru/kbe-sb-microservices.git

Artemis Gui when starting locally: http://localhost:8161/console

## Deployment

### Deployment with Kubernetes

To run maven filtering for destination target/k8s.

```bash
mvn clean install -DskipTests 
```

Deployment goes into the default namespace.

To deploy all resources:

```bash
kubectl apply -f target/k8s/
```

To remove all resources:

```bash
kubectl delete -f target/k8s/
```

Check

```bash
kubectl get deployments -o wide
kubectl get pods -o wide
```

You can use the actuator rest call to verify via port 30080

### Deployment with Helm

Be aware that we are using a different namespace here (not default).

To run maven filtering for destination target/helm

```bash
mvn clean install -DskipTests 
```

Go to the directory where the tgz file has been created after 'mvn install'

```powershell
cd target/helm/repo
```

unpack

```powershell
$file = Get-ChildItem -Filter kbe-brewery-beer-micro-service-chart-*.tgz | Select-Object -First 1
tar -xvf $file.Name
```

install

```powershell
$APPLICATION_NAME = Get-ChildItem -Directory | Where-Object { $_.LastWriteTime -ge $file.LastWriteTime } | Select-Object -ExpandProperty Name
helm upgrade --install $APPLICATION_NAME ./$APPLICATION_NAME --namespace kbe-brewery-beer-micro-service --create-namespace --wait --timeout 5m --debug --render-subchart-notes
```

show logs

```powershell
kubectl get pods -l app.kubernetes.io/name=$APPLICATION_NAME -n kbe-brewery-beer-micro-service
```

replace $POD with pods from the command above

```powershell
kubectl logs $POD -n kbe-brewery-beer-micro-service --all-containers
```

test

```powershell
helm test $APPLICATION_NAME --namespace kbe-brewery-beer-micro-service --logs
```

uninstall

```powershell
helm uninstall $APPLICATION_NAME --namespace kbe-brewery-beer-micro-service
```

delete all

```powershell
kubectl delete all --all -n kbe-brewery-beer-micro-service
```

create busybox sidecar

```powershell
kubectl run busybox-test --rm -it --image=busybox:1.36 --namespace=kbe-brewery-beer-micro-service --command -- sh
```

You can use the actuator rest call to verify via port 30080

## Sandbox (local dev environment)

The sandbox consists of the app (Spring Boot, port 8080) plus MySQL, Artemis (JMS) and the
inventory apps, provided by `compose.yaml`. The services start automatically via
`spring.docker.compose.enabled=true` when the app boots, so usually one step is enough.

### Start the sandbox (opencode-sandbox-kit)

The sandbox is provisioned by the opencode-sandbox-kit and runs as a Docker container. It mounts this
repo, starts opencode, and connects the IntelliJ MCP server.

Allow the kit source (GitHub without cloning):

```powershell
sbx settings set kit.allowedSources --% "[\"docker.io/\",\"github.com/dboeckli/\"]"
```

Start a new sandbox:

```powershell
sbx run opencode --name kbe-brewery-beer-micro-service --kit "git+https://github.com/dboeckli/opencode-sandbox-kit.git#dir=opencode-agent" "C:\development\projects\kbe-brewery-beer-micro-service"
```

Start the sandbox with Kubernetes support:

```powershell
sbx run opencode --name kbe-brewery-beer-micro-service --kit "git+https://github.com/dboeckli/opencode-sandbox-kit.git#dir=opencode-agent" "C:\development\projects\kbe-brewery-beer-micro-service" "$env:USERPROFILE\.kube:ro"
```

Apply the kit to an existing sandbox (restarts the sandbox, VM state is kept):

```powershell
sbx kit add kbe-brewery-beer-micro-service "git+https://github.com/dboeckli/opencode-sandbox-kit.git#dir=opencode-agent"
```

### Start the app

Run the `BeerServiceApplication` run configuration in IntelliJ
(`.run/BeerServiceApplication.run.xml`, main class
`ch.dboeckli.springframeworkguru.kbe.beer.services.BeerServiceApplication`). Alternatively start via
`./mvnw spring-boot:run`.

The compose file brings up:

- `mysql` (port 3306) — database `beerservice`
- `jms` (ports 61616/8161) — Artemis broker + console
- `inventory-service` (port 8082) and `inventory-failover` (port 8083) — inventory apps

### Verify

- Actuator health: http://localhost:8080/actuator/health
- Artemis console: http://localhost:8161/console

## Contributing

Contributions to improve this template are welcome. Please follow the standard GitHub flow:
1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a new Pull Request
