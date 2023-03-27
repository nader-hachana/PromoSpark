# Overview
 The goal of this application is to application is to:
 - Calculate metrics:
    * Baseline: The median of sales when there is no promotion
    * Incremental sales lift (lift unit):  weekly sales - baseline
    * Promotion lift percentage: weekly sales / baseline 
    * Total sales: the sum of weekly sales for every (product, promotion)
 - Store the data inside a Cassandra database.
 - Fetch the data from the table using an Akka-http API and return the total sales and the promotion per product per promotion on the URL: (http://localhost:8080/api/data/prod_purch/promotio_category/promotion_discount)


# Technologies

* ### Data processing: 

    * Scala:2.12
    * Spark:3.2.1
    * Database: Cassandra:4.0
* ### Backend:

    * Web services: Scala/Akka HTTP
* ### Microservice architecture: 

    * Docker to package the application
* ### Kubernetes to deploy

# How to run the Application

## 1. Building the jar files:

- ### ETL with Spark:
```
cd ~/processing 
sbt clean assembly
```

- ### Akka-http server-side:
```
cd ~/akka_http 
sbt clean assembly
```

## 2. Dockerizing the different components of the application:

- ### Cassandra database:
```
cd ~/processing
docker-compose -f cassandra_db.yml build
```

- ### ETL component:
```
cd ~/processing
docker-compose -f processing.yml build
```

- ### Akka-http component:
```
cd ~/akka_http
docker-compose -f akka_http.yml build
```

## 3. Loading the the docker images to the KinD-k8s local cluster:
```
kind load docker-image cassandra_db:4.0 processing:1.0 akka:1.0
```

## 4. Deploying the app on Kubernetes:

- ### Deploying the Cassandra database:
```
cd ~/kind 
kubectl apply -f cassandra_db.yml
```

- ### Deploying the ETL component:
```
kubectl apply -f processing-deployment.yml
```

- ### Deploying the Akka-http component:
```
kubectl apply -f akka-deployment.yml
```

- ### Applying an external connection with the akka service from outside the cluster
```
kubectl port-forward svc/akka 8080:8080
```
