package com.demo.keyvault;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class KeyVaultController {

    @Value("${custom.config.keyvault}")
    private String name;

    @GetMapping("/")
    public String getMethodName() {
        return "This is my method name " + name;
    }

    @GetMapping("/hello")
    public String hello() {
        return new String("Hello Everyone !");
    }

}
