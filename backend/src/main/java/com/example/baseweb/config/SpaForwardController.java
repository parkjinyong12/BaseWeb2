package com.example.baseweb.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaForwardController {

    @RequestMapping(value = {"/{path:^(?!api|swagger-ui|v3|actuator).*$}", "/{path:^(?!api|swagger-ui|v3|actuator).*$}/**/{subpath:[^.]*}"})
    public String forward() {
        return "forward:/index.html";
    }
}
