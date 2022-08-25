package de.badminton.neubiberg.service.test.api;

import de.badminton.neubiberg.service.test.api.dto.ScoreDTO;
import de.badminton.neubiberg.service.test.api.dto.TestDTO;
import de.badminton.neubiberg.service.test.api.dto.TestRecordDTO;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.Map;

@RequestMapping("test")
public interface TestSpringServiceAPI {

    @GetMapping
    @RequestMapping(value = "dto", method = {RequestMethod.GET, RequestMethod.DELETE})
    ResponseEntity<TestDTO> getTestDTO(HttpRequest request, @RequestParam(value = "abc") String testQueryParam);

    @DeleteMapping("{id}")
    void deleteTestDTO(@RequestHeader(name = "TEST_HEADER", required = false) String header, @PathVariable int id);

    @PostMapping
    @PutMapping
    List<TestRecordDTO> bulkCreate(@RequestBody List<TestDTO> dtos);

    @GetMapping("{id}/stream")
    StreamingResponseBody stream(@PathVariable int id);

    @GetMapping("{id}/sse")
    SseEmitter serverSendEvent(@PathVariable int id);

    @PostMapping("upload")
    void upload(@RequestParam MultipartFile file);

    @GetMapping("score")
    ScoreDTO getScore(@RequestBody Map<Integer, String> fieldValue);
}
