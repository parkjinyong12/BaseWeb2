package com.ruokit.baseweb.web;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MeController {

    private static final Logger log = LoggerFactory.getLogger(MeController.class);

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        log.info("API 진입: GET /api/me user={}", authentication.getName());
        return Map.of("name", authentication.getName());
    }
}
