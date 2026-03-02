# Servidor de Descoberta (discovery-server)

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.6-brightgreen)

## 1. Visão Geral

Este microserviço atua como o **Registro de Serviços** da arquitetura PixPro, utilizando **Spring Cloud Netflix Eureka**.

Sua única responsabilidade é permitir que outros microsserviços se registrem e se descubram na rede dinamicamente. Ele funciona como um "catálogo telefônico": quando um serviço (ex: `api-gateway`) precisa se comunicar com outro (ex: `project-service`), ele pergunta ao `discovery-server` o endereço de rede atual do serviço desejado.

Este componente é crucial para a resiliência e escalabilidade do ecossistema, eliminando a necessidade de endereços de rede fixos.

## 2. Como Executar Localmente

### Pré-requisitos
- JDK 17
- Maven 3.8+

### Configuração
Este serviço possui uma configuração mínima. A porta padrão é `8761` e está definida no arquivo `application.properties`. Não há necessidade de outras variáveis de ambiente para a execução local padrão.

**Arquivo `application.properties` principal:**
```properties
# Nome da aplicação no Eureka
spring.application.name=discovery-server
# Porta padrão do Eureka Server
server.port=8761

# Configuração para que o servidor não tente se registrar com ele mesmo
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

### Execução
Navegue até a pasta raiz do discovery-server e execute:

```
mvn spring-boot:run
```
O serviço estará disponível em http://localhost:8761.

### Dashboard do Eureka
Este serviço não expõe uma API REST, mas sim um painel web para monitoramento. Através dele, é possível visualizar todos os microsserviços que estão registrados e seu status atual.

Após iniciar o serviço, acesse o dashboard em:

http://localhost:8761