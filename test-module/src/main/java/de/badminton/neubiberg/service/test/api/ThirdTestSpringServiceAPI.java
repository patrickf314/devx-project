package de.badminton.neubiberg.service.test.api;

import de.badminton.neubiberg.service.test.api.dto.GenericDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("{id}")
public interface ThirdTestSpringServiceAPI {

    @GetMapping
    GenericDTO<String, Number> get(@PathVariable int id);

}
