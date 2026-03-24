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

        // ── Ansible ─────────────────────────────────────────────────
        generateAnsibleFiles(files, stack);

        log.info("Generated {} files for project '{}'", files.size(), projectName);
        return files;
    }

    // =========================================================================
    //  VARIABLES
    // =========================================================================

    private Map<String, String> createBaseVariables(String projectName, TechStack stack) {
        Map<String, String> v = new LinkedHashMap<>();

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

        v.put("PACKAGE_NAME", safeName);
        v.put("PACKAGE_PATH", "com/example/" + safeName);

        v.put("BACKEND_TECH", stack.getBackend().name().toLowerCase());
        v.put("FRONTEND_TECH", stack.getFrontend().name().toLowerCase());
        v.put("BACKEND_LABEL", backendLabel(stack.getBackend()));
        v.put("FRONTEND_LABEL", frontendLabel(stack.getFrontend()));
        v.put("USE_DOCKER", Boolean.TRUE.equals(stack.getUseDocker()) ? "true" : "false");

        v.put("BACKEND_PORT",
                stack.getBackend() == BackendTech.PYTHON ? "8000" : "8080");

        v.put("DB_NAME", underName + "_db");

        return v;
    }

    // =========================================================================
    //  CI / DOCKER-COMPOSE TEMPLATE PATHS
    // =========================================================================

    private String getCiTemplatePath(TechStack stack) {
        String back = stack.getBackend().name().toLowerCase();
        String front = stack.getFrontend().name().toLowerCase();
        String suffix = Boolean.TRUE.equals(stack.getUseDocker()) ? "docker" : "no-docker";
        return "gitlab-ci/" + back + "-" + front + "-" + suffix + ".yml";
    }

    private String getDockerComposeTemplatePath(TechStack stack) {
        String back = stack.getBackend().name().toLowerCase();
        String front = stack.getFrontend().name().toLowerCase();
        return "docker-compose/" + back + "-" + front + ".yml";
    }

    // =========================================================================
    //  ANSIBLE COMBO PATH
    // =========================================================================

    /**
     * ansible/{backend}-{frontend}-{docker|no-docker}
     * Пример: ansible/java-react-docker
     */
    private String getAnsibleComboPath(TechStack stack) {
        String back = stack.getBackend().name().toLowerCase();
        String front = stack.getFrontend().name().toLowerCase();
        String suffix = Boolean.TRUE.equals(stack.getUseDocker()) ? "docker" : "no-docker";
        return back + "-" + front + "-" + suffix;
    }

    // =========================================================================
    //  ANSIBLE FILES
    // =========================================================================

    /**
     * Ansible: общие файлы (ansible.cfg, inventory.ini) + роль под конкретный стек.
     * Все файлы загружаются raw — без подстановки переменных,
     * чтобы Jinja2-синтаксис Ansible {{ }} не конфликтовал.
     */
    private void generateAnsibleFiles(Map<String, String> files, TechStack stack) {
        // ── Общие файлы ─────────────────────────────────────────────
        addRawFile(files, "ansible/ansible.cfg",
                         "ansible/ansible.cfg");
        addRawFile(files, "ansible/inventory.ini",
                         "ansible/inventory.ini");

        // ── Файлы, специфичные для стека ────────────────────────────
        String combo = getAnsibleComboPath(stack);

        addRawFile(files, "ansible/playbook.yml",
                         "ansible/" + combo + "/playbook.yml");
        addRawFile(files, "ansible/roles/deploy/defaults/main.yml",
                         "ansible/" + combo + "/roles/deploy/defaults/main.yml");
        addRawFile(files, "ansible/roles/deploy/tasks/main.yml",
                         "ansible/" + combo + "/roles/deploy/tasks/main.yml");
        addRawFile(files, "ansible/roles/deploy/handlers/main.yml",
                         "ansible/" + combo + "/roles/deploy/handlers/main.yml");
    }

    // =========================================================================
    //  BACKEND FILES
    // =========================================================================

    private void generateBackendFiles(Map<String, String> files,
                                      TechStack stack,
                                      Map<String, String> vars) {
        String tp = "backend/" + stack.getBackend().name().toLowerCase() + "/";

        switch (stack.getBackend()) {

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

            case PYTHON -> {
                String pyPkg = vars.get("project_name_py");

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

                addTemplate(files,
                        "backend/api/__init__.py",
                        tp + "api_init.py", vars);
                addTemplate(files,
                        "backend/api/urls.py",
                        tp + "api_urls.py", vars);
                addTemplate(files,
                        "backend/api/views.py",
                        tp + "views.py", vars);

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

    /**
     * Загружает файл БЕЗ подстановки переменных (raw).
     * Для Ansible — чтобы {{ }} не ломался нашим шаблонизатором.
     */
    private void addRawFile(Map<String, String> files,
                            String repoPath,
                            String templatePath) {
        String content = templateService.loadTemplateRaw(templatePath);
        if (content != null) {
            files.put(repoPath, content);
        } else {
            log.warn("Raw template not found: {} → skipping repo file: {}",
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