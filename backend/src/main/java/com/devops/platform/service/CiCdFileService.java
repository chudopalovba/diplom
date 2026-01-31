package com.devops.platform.service;

import com.devops.platform.entity.TechStack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class CiCdFileService {
    
    /**
     * Получение содержимого .gitlab-ci.yml на основе стека
     */
    public String getGitLabCiContent(TechStack stack) {
        String templateName = buildTemplateName(stack);
        log.info("Loading CI template: {}", templateName);
        
        try {
            ClassPathResource resource = new ClassPathResource("ci-templates/" + templateName);
            return FileCopyUtils.copyToString(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)
            );
        } catch (IOException e) {
            log.warn("Template not found: {}, using default", templateName);
            return getDefaultCiContent(stack);
        }
    }
    
    private String buildTemplateName(TechStack stack) {
        String backend = stack.getBackend().name().toLowerCase();
        String dockerSuffix = Boolean.TRUE.equals(stack.getUseDocker()) ? "docker" : "no-docker";
        return backend + "-" + dockerSuffix + ".gitlab-ci.yml";
    }
    
    /**
     * Дефолтный CI файл если шаблон не найден
     */
    private String getDefaultCiContent(TechStack stack) {
        boolean useDocker = Boolean.TRUE.equals(stack.getUseDocker());
        
        return switch (stack.getBackend()) {
            case JAVA -> getJavaCiContent(useDocker);
            case CSHARP -> getCSharpCiContent(useDocker);
            case PYTHON -> getPythonCiContent(useDocker);
        };
    }
    
    private String getJavaCiContent(boolean useDocker) {
        if (useDocker) {
            return """
                stages:
                  - build
                  - test
                  - sonar
                  - docker
                  - deploy
                
                variables:
                  MAVEN_OPTS: "-Dmaven.repo.local=$CI_PROJECT_DIR/.m2/repository"
                
                cache:
                  paths:
                    - .m2/repository
                
                build:
                  stage: build
                  image: maven:3.9-eclipse-temurin-17
                  script:
                    - mvn clean package -DskipTests
                  artifacts:
                    paths:
                      - target/*.jar
                
                test:
                  stage: test
                  image: maven:3.9-eclipse-temurin-17
                  script:
                    - mvn test
                
                sonar:
                  stage: sonar
                  image: maven:3.9-eclipse-temurin-17
                  script:
                    - mvn sonar:sonar -Dsonar.host.url=$SONAR_HOST_URL -Dsonar.token=$SONAR_TOKEN
                  allow_failure: true
                
                docker-build:
                  stage: docker
                  image: docker:24
                  services:
                    - docker:24-dind
                  script:
                    - docker build -t $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA .
                    - docker push $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA
                
                deploy:
                  stage: deploy
                  script:
                    - echo "Deploying to server..."
                  environment:
                    name: production
                """;
        } else {
            return """
                stages:
                  - build
                  - test
                  - sonar
                  - deploy
                
                variables:
                  MAVEN_OPTS: "-Dmaven.repo.local=$CI_PROJECT_DIR/.m2/repository"
                
                cache:
                  paths:
                    - .m2/repository
                
                build:
                  stage: build
                  image: maven:3.9-eclipse-temurin-17
                  script:
                    - mvn clean package -DskipTests
                  artifacts:
                    paths:
                      - target/*.jar
                
                test:
                  stage: test
                  image: maven:3.9-eclipse-temurin-17
                  script:
                    - mvn test
                
                sonar:
                  stage: sonar
                  image: maven:3.9-eclipse-temurin-17
                  script:
                    - mvn sonar:sonar -Dsonar.host.url=$SONAR_HOST_URL -Dsonar.token=$SONAR_TOKEN
                  allow_failure: true
                
                deploy:
                  stage: deploy
                  script:
                    - scp target/*.jar $DEPLOY_USER@$DEPLOY_HOST:/opt/app/
                    - ssh $DEPLOY_USER@$DEPLOY_HOST "systemctl restart app"
                  environment:
                    name: production
                """;
        }
    }
    
    private String getCSharpCiContent(boolean useDocker) {
        return """
            stages:
              - build
              - test
              - deploy
            
            build:
              stage: build
              image: mcr.microsoft.com/dotnet/sdk:8.0
              script:
                - dotnet restore
                - dotnet build --no-restore
              artifacts:
                paths:
                  - bin/
            
            test:
              stage: test
              image: mcr.microsoft.com/dotnet/sdk:8.0
              script:
                - dotnet test --no-build --verbosity normal
            
            deploy:
              stage: deploy
              script:
                - echo "Deploying..."
              environment:
                name: production
            """;
    }
    
    private String getPythonCiContent(boolean useDocker) {
        return """
            stages:
              - build
              - test
              - deploy
            
            variables:
              PIP_CACHE_DIR: "$CI_PROJECT_DIR/.pip-cache"
            
            cache:
              paths:
                - .pip-cache/
            
            build:
              stage: build
              image: python:3.11
              script:
                - pip install -r requirements.txt
            
            test:
              stage: test
              image: python:3.11
              script:
                - pip install -r requirements.txt
                - python manage.py test
            
            deploy:
              stage: deploy
              script:
                - echo "Deploying..."
              environment:
                name: production
            """;
    }
    
    /**
     * Генерация Dockerfile
     */
    public String getDockerfileContent(TechStack stack) {
        return switch (stack.getBackend()) {
            case JAVA -> """
                FROM eclipse-temurin:17-jre-alpine
                WORKDIR /app
                COPY target/*.jar app.jar
                EXPOSE 8080
                ENTRYPOINT ["java", "-jar", "app.jar"]
                """;
            case CSHARP -> """
                FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS base
                WORKDIR /app
                EXPOSE 80
                
                FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
                WORKDIR /src
                COPY . .
                RUN dotnet restore
                RUN dotnet publish -c Release -o /app/publish
                
                FROM base AS final
                WORKDIR /app
                COPY --from=build /app/publish .
                ENTRYPOINT ["dotnet", "App.dll"]
                """;
            case PYTHON -> """
                FROM python:3.11-slim
                WORKDIR /app
                COPY requirements.txt .
                RUN pip install --no-cache-dir -r requirements.txt
                COPY . .
                EXPOSE 8000
                CMD ["gunicorn", "--bind", "0.0.0.0:8000", "app.wsgi:application"]
                """;
        };
    }
}