package com.devops.platform.service;

import com.devops.platform.entity.TechStack;
import com.devops.platform.entity.enums.BackendTech;
import com.devops.platform.entity.enums.FrontendTech;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ProjectTemplateService {
    
    private static final Logger log = LoggerFactory.getLogger(ProjectTemplateService.class);
    
    private final TemplateService templateService;
    
    public ProjectTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }
    
    /**
     * Генерирует все файлы для нового проекта
     */
    public Map<String, String> generateProjectFiles(String projectName, TechStack stack) {
        Map<String, String> files = new HashMap<>();
        
        // Базовые переменные для всех шаблонов
        Map<String, String> variables = createBaseVariables(projectName, stack);
        
        log.info("Generating project files for: {} (backend={}, frontend={}, docker={})",
                projectName, stack.getBackend(), stack.getFrontend(), stack.getUseDocker());
        
        // README проекта
        addFileFromTemplate(files, "README.md", "README.md", variables);
        
        // .gitignore
        addFileFromTemplate(files, ".gitignore", "gitignore.txt", variables);
        
        // docker-compose.yml
        addFileFromTemplate(files, "docker-compose.yml", "docker-compose.yml", variables);
        
        // .gitlab-ci.yml
        String ciTemplate = getCiTemplatePath(stack);
        addFileFromTemplate(files, ".gitlab-ci.yml", ciTemplate, variables);
        
        // Backend файлы
        generateBackendFiles(files, stack, variables);
        
        // Frontend файлы
        generateFrontendFiles(files, stack, variables);
        
        // Database файлы
        generateDatabaseFiles(files, variables);
        
        log.info("Generated {} files for project {}", files.size(), projectName);
        
        return files;
    }
    
    /**
     * Создаёт базовые переменные для шаблонов
     */
    private Map<String, String> createBaseVariables(String projectName, TechStack stack) {
        Map<String, String> vars = new HashMap<>();
        
        // Основные
        vars.put("PROJECT_NAME", projectName);
        vars.put("PROJECT_NAME_LOWER", projectName.toLowerCase());
        vars.put("PROJECT_NAME_SAFE", projectName.toLowerCase().replaceAll("[^a-z0-9]", ""));
        vars.put("PROJECT_NAME_UNDERSCORE", projectName.toLowerCase().replaceAll("[^a-z0-9]", "_"));
        vars.put("PROJECT_NAME_DASH", projectName.toLowerCase().replaceAll("[^a-z0-9]", "-"));
        
        // Пакет для Java
        vars.put("PACKAGE_NAME", projectName.toLowerCase().replaceAll("[^a-z0-9]", ""));
        vars.put("PACKAGE_PATH", "com/" + projectName.toLowerCase().replaceAll("[^a-z0-9]", ""));
        
        // Стек
        vars.put("BACKEND_TECH", stack.getBackend().name().toLowerCase());
        vars.put("BACKEND_LABEL", getBackendLabel(stack.getBackend()));
        vars.put("FRONTEND_TECH", stack.getFrontend().name().toLowerCase());
        vars.put("FRONTEND_LABEL", getFrontendLabel(stack.getFrontend()));
        vars.put("USE_DOCKER", stack.getUseDocker() ? "true" : "false");
        vars.put("USE_DOCKER_LABEL", stack.getUseDocker() ? "Да" : "Нет");
        
        // Порты
        vars.put("BACKEND_PORT", stack.getBackend() == BackendTech.PYTHON ? "8000" : "8080");
        
        // Database
        vars.put("DB_NAME", projectName.toLowerCase().replaceAll("[^a-z0-9]", "_"));
        
        return vars;
    }
    
    /**
     * Добавляет файл из шаблона
     */
    private void addFileFromTemplate(Map<String, String> files, String targetPath, 
                                      String templatePath, Map<String, String> variables) {
        String content = templateService.loadTemplate(templatePath, variables);
        if (content != null) {
            files.put(targetPath, content);
        } else {
            log.warn("Template not found: {}, skipping file: {}", templatePath, targetPath);
        }
    }
    
    /**
     * Определяет путь к CI шаблону
     */
    private String getCiTemplatePath(TechStack stack) {
        String backend = stack.getBackend().name().toLowerCase();
        String dockerSuffix = Boolean.TRUE.equals(stack.getUseDocker()) ? "docker" : "no-docker";
        return "gitlab-ci/" + backend + "-" + dockerSuffix + ".yml";
    }
    
    /**
     * Генерирует backend файлы
     */
    private void generateBackendFiles(Map<String, String> files, TechStack stack, 
                                       Map<String, String> variables) {
        String backendPath = "backend/" + stack.getBackend().name().toLowerCase() + "/";
        
        switch (stack.getBackend()) {
            case JAVA -> {
                addFileFromTemplate(files, "backend/pom.xml", backendPath + "pom.xml", variables);
                
                String packagePath = variables.get("PACKAGE_PATH");
                addFileFromTemplate(files, 
                        "backend/src/main/java/" + packagePath + "/Application.java",
                        backendPath + "Application.java", variables);
                addFileFromTemplate(files, 
                        "backend/src/main/java/" + packagePath + "/controller/HelloController.java",
                        backendPath + "HelloController.java", variables);
                addFileFromTemplate(files, 
                        "backend/src/main/resources/application.yml",
                        backendPath + "application.yml", variables);
                
                if (Boolean.TRUE.equals(stack.getUseDocker())) {
                    addFileFromTemplate(files, "backend/Dockerfile", backendPath + "Dockerfile", variables);
                }
                
                addFileFromTemplate(files, "backend/README.md", backendPath + "README.md", variables);
            }
            
            case CSHARP -> {
                String safeName = variables.get("PROJECT_NAME_SAFE");
                addFileFromTemplate(files, "backend/" + safeName + ".csproj", 
                        backendPath + "Project.csproj", variables);
                addFileFromTemplate(files, "backend/Program.cs", backendPath + "Program.cs", variables);
                addFileFromTemplate(files, "backend/Controllers/HelloController.cs", 
                        backendPath + "HelloController.cs", variables);
                addFileFromTemplate(files, "backend/appsettings.json", 
                        backendPath + "appsettings.json", variables);
                
                if (Boolean.TRUE.equals(stack.getUseDocker())) {
                    addFileFromTemplate(files, "backend/Dockerfile", backendPath + "Dockerfile", variables);
                }
                
                addFileFromTemplate(files, "backend/README.md", backendPath + "README.md", variables);
            }
            
            case PYTHON -> {
                String safeName = variables.get("PROJECT_NAME_UNDERSCORE");
                addFileFromTemplate(files, "backend/requirements.txt", 
                        backendPath + "requirements.txt", variables);
                addFileFromTemplate(files, "backend/manage.py", backendPath + "manage.py", variables);
                addFileFromTemplate(files, "backend/" + safeName + "/settings.py", 
                        backendPath + "settings.py", variables);
                addFileFromTemplate(files, "backend/" + safeName + "/urls.py", 
                        backendPath + "urls.py", variables);
                addFileFromTemplate(files, "backend/" + safeName + "/__init__.py", 
                        backendPath + "__init__.py", variables);
                addFileFromTemplate(files, "backend/api/__init__.py", 
                        backendPath + "__init__.py", variables);
                addFileFromTemplate(files, "backend/api/urls.py", 
                        backendPath + "api_urls.py", variables);
                addFileFromTemplate(files, "backend/api/views.py", 
                        backendPath + "views.py", variables);
                
                if (Boolean.TRUE.equals(stack.getUseDocker())) {
                    addFileFromTemplate(files, "backend/Dockerfile", backendPath + "Dockerfile", variables);
                }
                
                addFileFromTemplate(files, "backend/README.md", backendPath + "README.md", variables);
            }
        }
    }
    
    /**
     * Генерирует frontend файлы
     */
    private void generateFrontendFiles(Map<String, String> files, TechStack stack,
                                        Map<String, String> variables) {
        String frontendPath = "frontend/" + stack.getFrontend().name().toLowerCase() + "/";
        
        switch (stack.getFrontend()) {
            case REACT -> {
                addFileFromTemplate(files, "frontend/package.json", frontendPath + "package.json", variables);
                addFileFromTemplate(files, "frontend/vite.config.js", frontendPath + "vite.config.js", variables);
                addFileFromTemplate(files, "frontend/index.html", frontendPath + "index.html", variables);
                addFileFromTemplate(files, "frontend/src/main.jsx", frontendPath + "main.jsx", variables);
                addFileFromTemplate(files, "frontend/src/App.jsx", frontendPath + "App.jsx", variables);
                addFileFromTemplate(files, "frontend/src/index.css", frontendPath + "index.css", variables);
            }
            
            case VUE -> {
                addFileFromTemplate(files, "frontend/package.json", frontendPath + "package.json", variables);
                addFileFromTemplate(files, "frontend/vite.config.js", frontendPath + "vite.config.js", variables);
                addFileFromTemplate(files, "frontend/index.html", frontendPath + "index.html", variables);
                addFileFromTemplate(files, "frontend/src/main.js", frontendPath + "main.js", variables);
                addFileFromTemplate(files, "frontend/src/App.vue", frontendPath + "App.vue", variables);
                addFileFromTemplate(files, "frontend/src/style.css", frontendPath + "style.css", variables);
            }
            
            case ANGULAR -> {
                addFileFromTemplate(files, "frontend/package.json", frontendPath + "package.json", variables);
            }
        }
        
        // Общие файлы для frontend
        if (Boolean.TRUE.equals(stack.getUseDocker())) {
            addFileFromTemplate(files, "frontend/Dockerfile", frontendPath + "Dockerfile", variables);
            addFileFromTemplate(files, "frontend/nginx.conf", frontendPath + "nginx.conf", variables);
        }
        
        addFileFromTemplate(files, "frontend/README.md", frontendPath + "README.md", variables);
    }
    
    /**
     * Генерирует database файлы
     */
    private void generateDatabaseFiles(Map<String, String> files, Map<String, String> variables) {
        addFileFromTemplate(files, "database/scripts/01_init.sql", "database/init.sql", variables);
        addFileFromTemplate(files, "database/README.md", "database/README.md", variables);
    }
    
    private String getBackendLabel(BackendTech tech) {
        return switch (tech) {
            case JAVA -> "Java (Spring Boot)";
            case CSHARP -> "C# (.NET 8)";
            case PYTHON -> "Python (Django)";
        };
    }
    
    private String getFrontendLabel(FrontendTech tech) {
        return switch (tech) {
            case REACT -> "React";
            case VUE -> "Vue.js";
            case ANGULAR -> "Angular";
        };
    }
}