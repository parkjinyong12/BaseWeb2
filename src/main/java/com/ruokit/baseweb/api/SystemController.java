package com.ruokit.baseweb.api;

import com.ruokit.baseweb.common.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SystemController {

    private static final Logger log = LoggerFactory.getLogger(SystemController.class);

    private final String appName;
    private final ObjectProvider<BuildProperties> buildProperties;

    public SystemController(
            @Value("${spring.application.name:baseweb-backend}") String appName,
            ObjectProvider<BuildProperties> buildProperties
    ) {
        this.appName = appName;
        this.buildProperties = buildProperties;
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        log.info("API 진입: GET /api/health");
        return ApiResponse.ok(Map.of("status", "OK"));
    }

    @GetMapping("/version")
    public ApiResponse<Map<String, String>> version() {
        log.info("API 진입: GET /api/version");
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("name", appName);
        BuildProperties info = buildProperties.getIfAvailable();
        payload.put("version", info != null ? info.getVersion() : "dev");
        return ApiResponse.ok(payload);
    }
}
