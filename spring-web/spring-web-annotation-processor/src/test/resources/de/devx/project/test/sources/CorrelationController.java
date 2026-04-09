package de.devx.project.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/correlations")
public class CorrelationController {

    @GetMapping
    public CorrelationDTO getCorrelation() {
        return null;
    }
}
