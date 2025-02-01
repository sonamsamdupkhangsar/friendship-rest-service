# friendship-rest-service
This is the microservice deployment for the [friendship-api](https://github.com/sonamsamdupkhangsar/friendship-api)

## Run locally using profile
Use local profile `application-local.yml` to run locally.


```
./gradlew bootRun --args="--spring.profiles.active=local"
```

## Build Docker image

Build docker image using included Dockerfile.

`docker build -t ghcr.io/<username>/friendship-rest-service:latest .`

## Push Docker image to repository

`docker push ghcr.io/<username>/friendship-rest-service:latest`

## Deploy Docker image locally

`docker run -e POSTGRES_USERNAME=dummy \
-e POSTGRES_PASSWORD=dummy -e POSTGRES_DBNAME=account \
-e POSTGRES_SERVICE=localhost:5432 \
-e apiKey=123 -e DB_SSLMODE=DISABLE
--publish 8080:8080 ghcr.io/<username>/friendshps-rest-service:latest`


## Installation on Kubernetes
Use my Helm chart here @ [sonam-helm-chart](https://github.com/sonamsamdupkhangsar/sonam-helm-chart):

```
helm install user-rest-service sonam/mychart -f values-backend.yaml --version 0.1.15 --namespace=yournamespace
```

## Instruction for port-forwarding database pod
```
export PGMASTER=$(kubectl get pods -o jsonpath={.items..metadata.name} -l application=spilo,cluster-name=friendship-minimal-cluster,spilo-role=master -n yournamesapce); 
echo $PGMASTER;
kubectl port-forward $PGMASTER 6432:5432 -n backend;
```

### Login to database instruction
```
export PGPASSWORD=$(kubectl get secret <SECRET_NAME> -o 'jsonpath={.data.password}' -n backend | base64 -d);
echo $PGPASSWORD;
export PGSSLMODE=require;
psql -U <USER> -d projectdb -h localhost -p 6432

```
### Send post request to create user account
```
 curl -X POST -json '{"firstName": "dummy", "lastName": "lastnamedummy", "email": "yakApiKey", "authenticationId": "dummy123", "password": "12", "apiKey": "APIKEY"}' https://user-rest-service.sonam.cloud/signup
```