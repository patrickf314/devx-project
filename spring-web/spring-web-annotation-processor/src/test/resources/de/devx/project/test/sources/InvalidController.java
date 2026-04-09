package de.devx.project.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/invalid")
public class InvalidController {

    @GetMapping
    public InvalidBrandedType get() {
        return null;
    }
}
