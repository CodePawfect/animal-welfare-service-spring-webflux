<h1 align="center">Animal Welfare Service</h1>

<p align="center">
  <a href="https://skillicons.dev">
    <img src="https://skillicons.dev/icons?i=spring,azure,docker,postgresql" />
  </a>
</p>
<hr>

## 🐕 Introduction
<hr>

Welcome to the **Animal Welfare Service** 🐶 

a project created with a passion for animal welfare and the desire to support the Animal Protection Association. 

This service is dedicated to helping animals in need, and it is being developed for free. 

As a dedicated dog foster parent serving the Animal Protection Association, you are making a significant impact on the lives of animals in your care.

## 🐕 Getting started

Prerequisites for local development:
- Java 21
- Docker

Run docker-compose with `docker-compose up -d`

Ensure you have the Azure CLI installed. If you haven't already, install the Azure CLI. You can find installation instructions on the official Azure documentation.

Create `dog-images` container inside azurite:
```bash
az storage container create --name dog-images --connection-string "DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://127.0.0.1:10000/devstoreaccount1;"
```

Use the local profile from `src/main/resources/application-local.yml`

Run app with Maven: `mvn spring-boot:run -D"spring-boot.run.profiles"=local`

## Formatting

All code is formatted using the Google Java Format. It can be applied using the mvn goal `mvn spotless:apply`

IntelliJ user can also use [google-java-format Plugin](https://plugins.jetbrains.com/plugin/8527-google-java-format)
