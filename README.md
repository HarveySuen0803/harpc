<div align="center">
  <p>
    <img src="https://harvey-image.oss-cn-hangzhou.aliyuncs.com/telegram.png" alt="logo" width="200" height="auto"/>
  </p>
  <h3>Harvey's Awesome Rpc Server</h3>
</div>

# Quick Start

Structure of the quick start case

```
- harpc-boot-api          Declare rpc service's interface
- harpc-boot-consumer     Consume rpc service
- harpc-boot-provider     Provider rpc service
```

harpc-boot-api

1. Define service interfaces

```java
public interface HelloService {
    String sayHello(String name);
}
```

harpc-boot-consumer

1. Import declared interface

```xml
<dependency>
    <groupId>com.harvey</groupId>
    <artifactId>harpc-boot-api</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

2. Import the startup dependencies of the rpc service

```xml
<dependency>
    <groupId>com.harvey</groupId>
    <artifactId>spring-boot-starter-harpc</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

3. Inject the service instance through @HarpcReference, and send Rpc requests to the Provider's Rpc Server as if calling a local service

```java
@HarpcReference
private HelloService helloService;

@Test
public void consume() {
    System.out.println(helloService.sayHello("harvey"));
}
```

4. Set the properties that the Harpc Server needs to use

```properties
rpc.serverVersion=1.0
rpc.isMock=false

rpc.serializer.serializerType=json

rpc.servicemeta.registry.registryType=etcd
rpc.servicemeta.registry.address=http://localhost:2379
```

harpc-boot-provider

1. Import the dependency of declared interface

```xml
<dependency>
    <groupId>com.harvey</groupId>
    <artifactId>harpc-boot-api</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

2. Import the startup dependency of the rpc service

```xml
<dependency>
    <groupId>com.harvey</groupId>
    <artifactId>spring-boot-starter-harpc</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

3. Implement service interface for the provider

```java
@Service
@HarpcService
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "Hello " + name;
    }
}
```

4. Set the properties that the Harpc Server needs to use

```properties
rpc.serverHost=127.0.0.1
rpc.serverPort=10100
rpc.serverVersion=1.0

rpc.serializer.serializerType=json

rpc.servicemeta.registry.registryType=etcd
rpc.servicemeta.registry.serverHost=127.0.0.1
rpc.servicemeta.registry.serverPort=2379

rpc.loadbalancer.loadBalancerType=random
```

# Registry

The current Rpc Server provides a variety of registry implementations, you can choose any implementation

Etcd Registry

1. Build Etcd through Docker

```shell
docker image pull gcr.io/etcd-development/etcd:v3.5.13

docker volume create etcd-data

docker netowrk create etcd-network

docker container run \
    --name etcd \
    --privileged \
    --network etcd-network \
    -v etcd-data:/etcd-data \
    -p 2379:2379 \
    -p 2380:2380 \
    -d gcr.io/etcd-development/etcd:v3.5.13 \
        /usr/local/bin/etcd \
        --name s1 \
        --data-dir /etcd-data \
        --advertise-client-urls http://0.0.0.0:2379 \
        --listen-client-urls http://0.0.0.0:2379 \
        --listen-peer-urls http://0.0.0.0:2380 \
        --initial-advertise-peer-urls http://0.0.0.0:2380 \
        --initial-cluster s1=http://0.0.0.0:2380 \
        --initial-cluster-token tkn \
        --initial-cluster-state new \
        --log-level info \
        --logger zap \
        --log-outputs stderr

docker exec etcd /usr/local/bin/etcd --version
docker exec etcd /usr/local/bin/etcdctl version
docker exec etcd /usr/local/bin/etcdutl version
docker exec etcd /usr/local/bin/etcdctl endpoint health
docker exec etcd /usr/local/bin/etcdctl put foo bar
docker exec etcd /usr/local/bin/etcdctl get foo
```

2. Configure Zookeeper in the project (file: application.properties)

```properties
rpc.servicemeta.registry.registryType=etcd
rpc.servicemeta.registry.address=http://localhost:2379
```

Zookeeper Registry

1. Build Zookeeper through Docker

```shell
docker image pull zookeeper:3.9.2-jre-17

docker container run \
		--name zookeeper \
		--privileged \
		-e TZ="Asia/Shanghai" \
		-p 2181:2181 \
		-d zookeeper:3.9.2-jre-17
```


2. Configure Zookeeper in the project (file: application.properties)

```properties
rpc.servicemeta.registry.registryType=zookeeper
rpc.servicemeta.registry.address=http://localhost:2181
```

