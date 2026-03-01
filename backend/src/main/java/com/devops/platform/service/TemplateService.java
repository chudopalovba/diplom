package com.devops.platform.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class TemplateService {

    private static final Logger log = LoggerFactory.getLogger(TemplateService.class);
    private static final String TEMPLATES_PATH = "templates/";

    /**
     * Загружает шаблон и заменяет плейсхолдеры.
     * Порядок: сначала наши {{KEY}}, потом восстанавливаем Angular-интерполяцию.
     */
    public String loadTemplate(String templatePath, Map<String, String> variables) {
        String content = loadTemplateRaw(templatePath);
        if (content == null) {
            return null;
        }

        // 1. Защищаем Angular-интерполяцию: {{ message }} → __ANG_START__ message __ANG_END__
        content = content.replace("{{ '{{' }}", "__ANG_DBL_OPEN__");
        content = content.replace("{{ '}}' }}", "__ANG_DBL_CLOSE__");

        // 2. Заменяем наши плейсхолдеры {{VARIABLE_NAME}}
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            content = content.replace(placeholder,
                    entry.getValue() != null ? entry.getValue() : "");
        }

        // 3. Восстанавливаем Angular-интерполяцию
        content = content.replace("__ANG_DBL_OPEN__", "{{");
        content = content.replace("__ANG_DBL_CLOSE__", "}}");

        return content;
    }

    public String loadTemplateRaw(String templatePath) {
        try {
            ClassPathResource resource = new ClassPathResource(TEMPLATES_PATH + templatePath);
            if (!resource.exists()) {
                log.warn("Template not found: {}{}", TEMPLATES_PATH, templatePath);
                return null;
            }
            return FileCopyUtils.copyToString(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("Failed to load template {}: {}", templatePath, e.getMessage());
            return null;
        }
    }

    public boolean templateExists(String templatePath) {
        return new ClassPathResource(TEMPLATES_PATH + templatePath).exists();
    }
}