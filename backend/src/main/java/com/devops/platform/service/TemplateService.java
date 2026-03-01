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
     * Загружает шаблон из classpath:templates/ и заменяет все {{KEY}} на значения из variables.
     */
    public String loadTemplate(String templatePath, Map<String, String> variables) {
        String content = loadTemplateRaw(templatePath);
        if (content == null) {
            return null;
        }
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            content = content.replace(placeholder,
                    entry.getValue() != null ? entry.getValue() : "");
        }
        return content;
    }

    /**
     * Загружает шаблон без замены переменных.
     */
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

    /**
     * Проверяет существование шаблона.
     */
    public boolean templateExists(String templatePath) {
        return new ClassPathResource(TEMPLATES_PATH + templatePath).exists();
    }
}