package mx.centinela.bootstrap.api;

import mx.centinela.bootstrap.infrastructure.sse.SseAlertStreamAdapter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/stream")
class AlertStreamController {

  private final SseAlertStreamAdapter stream;

  AlertStreamController(SseAlertStreamAdapter stream) {
    this.stream = stream;
  }

  @GetMapping(path = "/alerts", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  SseEmitter alerts() {
    return stream.subscribe();
  }
}
