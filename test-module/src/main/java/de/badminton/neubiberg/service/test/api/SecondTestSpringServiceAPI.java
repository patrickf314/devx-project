package de.badminton.neubiberg.service.test.api;

import de.badminton.neubiberg.commons.api.dto.SearchResultDTO;
import de.badminton.neubiberg.service.test.api.dto.ExtendedGenericDTO;
import de.badminton.neubiberg.service.test.api.dto.TestRecordDTO;
import de.badminton.neubiberg.service.test.api.type.TestType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RequestMapping("test/{id}/second")
public interface SecondTestSpringServiceAPI {

    @GetMapping
    ExtendedGenericDTO<TestRecordDTO> get(@PathVariable int id, @RequestParam TestType type);

    @PostMapping
    String[] getArray(HttpServletResponse response, @PathVariable int id);

    @GetMapping("{someName}")
    SearchResultDTO<String> test(@PathVariable("id") int notId, @PathVariable("someName") String notTheSameName);

}
