package com.devops.platform.service;

import com.devops.platform.entity.TechStack;
import com.devops.platform.entity.enums.BackendTech;
import com.devops.platform.entity.enums.FrontendTech;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ProjectTemplateService {

    private static final Logger log = LoggerFactory.getLogger(ProjectTemplateService.class);

    private final TemplateService templateService;

    public ProjectTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    // =========================================================================
    //  MAIN ENTRY POINT
    // =========================================================================

    /**
     * Генерирует полный набор файлов для нового проекта.
     *
     * @return Map: repoFilePath → fileContent
     */
    public Map<String, String> generateProjectFiles(String projectName, TechStack stack) {
        Map<String, String> files = new LinkedHashMap<>();
        Map<String, String> vars = createBaseVariables(projectName, stack);

        log.info("Generating project files: name={}, backend={}, frontend={}, docker={}",
                projectName, stack.getBackend(), stack.getFrontend(), stack.getUseDocker());

        // ── Root files ──────────────────────────────────────────────
        addTemplate(files, "README.md", "README.md", vars);
        addTemplate(files, ".gitignore", "gitignore.txt", vars);

        // ── .gitlab-ci.yml ──────────────────────────────────────────
        String ciPath = getCiTemplatePath(stack);
        addTemplate(files, ".gitlab-ci.yml", ciPath, vars);

        // ── docker-compose.yml ──────────────────────────────────────
        if (Boolean.TRUE.equals(stack.getUseDocker())) {
            String dcPath = getDockerComposeTemplatePath(stack);
            addTemplate(files, "docker-compose.yml", dcPath, vars);
        }

        // ── Database ────────────────────────────────────────────────
        generateDatabaseFiles(files, vars);

        // ── Backend ─────────────────────────────────────────────────
        generateBackendFiles(files, stack, vars);

        // ── Frontend ────────────────────────────────────────────────
        generateFrontendFiles(files, stack, vars);

        log.info("Generated {} files for project '{}'", files.size(), projectName);
        return files;
    }

    // =========================================================================
    //  VARIABLES
    // =========================================================================

    private Map<String, String> createBaseVariables(String projectName, TechStack stack) {
        Map<String, String> v = new LinkedHashMap<>();

        // ── Name variants ───────────────────────────────────────────
        // "My-Project"  →  safeName = "myproject"
        //                    dashName = "my-project"
        //                    underName = "my_project"
        String safeName = projectName.toLowerCase()
                .replaceAll("[^a-z0-9]", "");
        String dashName = projectName.toLowerCase()
                .replaceAll("[^a-z0-9-]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        String underName = projectName.toLowerCase()
                .replaceAll("[^a-z0-9_]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");

        v.put("PROJECT_NAME", projectName);
        v.put("project_name", safeName);
        v.put("project-name", dashName);
        v.put("project_name_py", underName);
        v.put("project_name_db", underName);

        // ── Java package ────────────────────────────────────────────
        v.put("PACKAGE_NAME", safeName);
        v.put("PACKAGE_PATH", "com/example/" + safeName);

        // ── Tech ────────────────────────────────────────────────────
        v.put("BACKEND_TECH", stack.getBackend().name().toLowerCase());
        v.put("FRONTEND_TECH", stack.getFrontend().name().toLowerCase());
        v.put("BACKEND_LABEL", backendLabel(stack.getBackend()));
        v.put("FRONTEND_LABEL", frontendLabel(stack.getFrontend()));
        v.put("USE_DOCKER", Boolean.TRUE.equals(stack.getUseDocker()) ? "true" : "false");

        // ── Ports ───────────────────────────────────────────────────
        v.put("BACKEND_PORT",
                stack.getBackend() == BackendTech.PYTHON ? "8000" : "8080");

        // ── Database ────────────────────────────────────────────────
        v.put("DB_NAME", underName + "_db");

        return v;
    }

    // =========================================================================
    //  CI / DOCKER-COMPOSE TEMPLATE PATHS
    // =========================================================================

    /**
     * gitlab-ci/{backend}-{frontend}-{docker|no-docker}.yml
     * Пример: gitlab-ci/java-react-docker.yml
     */
    private String getCiTemplatePath(TechStack stack) {
        String back = stack.getBackend().name().toLowerCase();
        String front = stack.getFrontend().name().toLowerCase();
        String suffix = Boolean.TRUE.equals(stack.getUseDocker()) ? "docker" : "no-docker";
        return "gitlab-ci/" + back + "-" + front + "-" + suffix + ".yml";
    }

    /**
     * docker-compose/{backend}-{frontend}.yml
     * Пример: docker-compose/java-react.yml
     */
    private String getDockerComposeTemplatePath(TechStack stack) {
        String back = stack.getBackend().name().toLowerCase();
        String front = stack.getFrontend().name().toLowerCase();
        return "docker-compose/" + back + "-" + front + ".yml";
    }

    // =========================================================================
    //  BACKEND FILES
    // =========================================================================

    private void generateBackendFiles(Map<String, String> files,
                                      TechStack stack,
                                      Map<String, String> vars) {
        String tp = "backend/" + stack.getBackend().name().toLowerCase() + "/";

        switch (stack.getBackend()) {

            // ── JAVA ────────────────────────────────────────────────
            case JAVA -> {
                String pkgPath = vars.get("PACKAGE_PATH");

                addTemplate(files,
                        "backend/pom.xml",
                        tp + "pom.xml", vars);

                addTemplate(files,
                        "backend/src/main/java/" + pkgPath + "/Application.java",
                        tp + "Application.java", vars);

                addTemplate(files,
                        "backend/src/main/java/" + pkgPath + "/controller/HelloController.java",
                        tp + "HelloController.java", vars);

                addTemplate(files,
                        "backend/src/main/resources/application.yml",
                        tp + "application.yml", vars);

                if (Boolean.TRUE.equals(stack.getUseDocker())) {
                    addTemplate(files,
                            "backend/Dockerfile",
                            tp + "Dockerfile", vars);
                }
            }

            // ── PYTHON (Django) ─────────────────────────────────────
            case PYTHON -> {
                String pyPkg = vars.get("project_name_py");

                // Django project package
                addTemplate(files,
                        "backend/" + pyPkg + "/__init__.py",
                        tp + "api_init.py", vars);
                addTemplate(files,
                        "backend/" + pyPkg + "/settings.py",
                        tp + "settings.py", vars);
                addTemplate(files,
                        "backend/" + pyPkg + "/urls.py",
                        tp + "urls.py", vars);
                addTemplate(files,
                        "backend/" + pyPkg + "/wsgi.py",
                        tp + "wsgi.py", vars);

                // API app
                addTemplate(files,
                        "backend/api/__init__.py",
                        tp + "api_init.py", vars);
                addTemplate(files,
                        "backend/api/urls.py",
                        tp + "api_urls.py", vars);
                addTemplate(files,
                        "backend/api/views.py",
                        tp + "views.py", vars);

                // Root files
                addTemplate(files,
                        "backend/manage.py",
                        tp + "manage.py", vars);
                addTemplate(files,
                        "backend/requirements.txt",
                        tp + "requirements.txt", vars);

                if (Boolean.TRUE.equals(stack.getUseDocker())) {
                    addTemplate(files,
                            "backend/Dockerfile",
                            tp + "Dockerfile", vars);
                }
            }

            // ── C# (.NET 8) ─────────────────────────────────────────
            case CSHARP -> {
                String csprojName = vars.get("project-name");

                addTemplate(files,
                        "backend/" + csprojName + ".csproj",
                        tp + "project.csproj", vars);
                addTemplate(files,
                        "backend/Program.cs",
                        tp + "Program.cs", vars);
                addTemplate(files,
                        "backend/Controllers/HelloController.cs",
                        tp + "HelloController.cs", vars);
                addTemplate(files,
                        "backend/Data/AppDbContext.cs",
                        tp + "AppDbContext.cs", vars);
                addTemplate(files,
                        "backend/appsettings.json",
                        tp + "appsettings.json", vars);

                if (Boolean.TRUE.equals(stack.getUseDocker())) {
                    addTemplate(files,
                            "backend/Dockerfile",
                            tp + "Dockerfile", vars);
                }
            }
        }
    }

    // =========================================================================
    //  FRONTEND FILES
    // =========================================================================

    private void generateFrontendFiles(Map<String, String> files,
                                       TechStack stack,
                                       Map<String, String> vars) {
        String tp = "frontend/" + stack.getFrontend().name().toLowerCase() + "/";

        switch (stack.getFrontend()) {

            // ── REACT ───────────────────────────────────────────────
            case REACT -> {
                addTemplate(files, "frontend/package.json",
                        tp + "package.json", vars);
                addTemplate(files, "frontend/vite.config.js",
                        tp + "vite.config.js", vars);
                addTemplate(files, "frontend/index.html",
                        tp + "index.html", vars);
                addTemplate(files, "frontend/src/main.jsx",
                        tp + "main.jsx", vars);
                addTemplate(files, "frontend/src/App.jsx",
                        tp + "App.jsx", vars);
                addTemplate(files, "frontend/src/index.css",
                        tp + "index.css", vars);
            }

            // ── VUE ─────────────────────────────────────────────────
            case VUE -> {
                addTemplate(files, "frontend/package.json",
                        tp + "package.json", vars);
                addTemplate(files, "frontend/vite.config.js",
                        tp + "vite.config.js", vars);
                addTemplate(files, "frontend/index.html",
                        tp + "index.html", vars);
                addTemplate(files, "frontend/src/main.js",
                        tp + "main.js", vars);
                addTemplate(files, "frontend/src/App.vue",
                        tp + "App.vue", vars);
            }

            // ── ANGULAR ─────────────────────────────────────────────
            // ── ANGULAR ─────────────────────────────────────────────
            case ANGULAR -> {
                addTemplate(files, "frontend/package.json",
                        tp + "package.json", vars);
                addTemplate(files, "frontend/angular.json",
                        tp + "angular.json", vars);
                addTemplate(files, "frontend/tsconfig.json",
                        tp + "tsconfig.json", vars);
                addTemplate(files, "frontend/tsconfig.app.json",
                        tp + "tsconfig.app.json", vars);
                addTemplate(files, "frontend/proxy.conf.json",
                        tp + "proxy.conf.json", vars);
                addTemplate(files, "frontend/src/index.html",
                        tp + "src/index.html", vars);
                addTemplate(files, "frontend/src/main.ts",
                        tp + "src/main.ts", vars);
                addTemplate(files, "frontend/src/styles.css",
                        tp + "src/styles.css", vars);
                addTemplate(files, "frontend/src/app/app.component.ts",
                        tp + "src/app/app.component.ts", vars);
}
        }

        // ── Общие файлы frontend (Docker) ───────────────────────────
        if (Boolean.TRUE.equals(stack.getUseDocker())) {
            addTemplate(files, "frontend/Dockerfile",
                    tp + "Dockerfile", vars);
            addTemplate(files, "frontend/nginx.conf",
                    tp + "nginx.conf", vars);
        }
    }

    // =========================================================================
    //  DATABASE FILES
    // =========================================================================

    private void generateDatabaseFiles(Map<String, String> files,
                                       Map<String, String> vars) {
        addTemplate(files, "database/init.sql",
                "database/init.sql", vars);
    }

    // =========================================================================
    //  HELPERS
    // =========================================================================

    /**
     * Загружает шаблон, заменяет плейсхолдеры, кладёт в map файлов.
     * Если шаблон не найден — просто warn-лог, файл пропускается.
     */
    private void addTemplate(Map<String, String> files,
                             String repoPath,
                             String templatePath,
                             Map<String, String> vars) {
        String content = templateService.loadTemplate(templatePath, vars);
        if (content != null) {
            files.put(repoPath, content);
        } else {
            log.warn("Template not found: {} → skipping repo file: {}",
                    templatePath, repoPath);
        }
    }

    private String backendLabel(BackendTech tech) {
        return switch (tech) {
            case JAVA -> "Java (Spring Boot)";
            case CSHARP -> "C# (.NET 8)";
            case PYTHON -> "Python (Django)";
        };
    }

    private String frontendLabel(FrontendTech tech) {
        return switch (tech) {
            case REACT -> "React";
            case VUE -> "Vue.js";
            case ANGULAR -> "Angular";
        };
    }
}