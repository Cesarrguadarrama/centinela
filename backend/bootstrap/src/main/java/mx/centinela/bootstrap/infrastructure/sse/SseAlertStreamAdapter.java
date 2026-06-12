package mx.centinela.bootstrap.infrastructure.sse;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import mx.centinela.domain.model.Alert;
import mx.centinela.domain.model.ScoredTransaction;
import mx.centinela.domain.port.out.AlertStreamPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Fans alerts out to every connected dashboard over Server-Sent Events. Best-effort by contract: a
 * dead emitter is dropped, never retried — durability lives in Postgres, this is just the live
 * ticker. A periodic heartbeat keeps intermediaries from closing quiet connections.
 */
@Component
public class SseAlertStreamAdapter implements AlertStreamPort {

  private static final Logger log = LoggerFactory.getLogger(SseAlertStreamAdapter.class);
  private static final long EMITTER_TIMEOUT_MS = 30 * 60 * 1000L;

  private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
  private final ObjectMapper objectMapper;

  public SseAlertStreamAdapter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public SseEmitter subscribe() {
    SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);
    emitter.onCompletion(() -> emitters.remove(emitter));
    emitter.onTimeout(() -> emitters.remove(emitter));
    emitter.onError(e -> emitters.remove(emitter));
    emitters.add(emitter);
    log.debug("SSE subscriber added ({} active)", emitters.size());
    return emitter;
  }

  @Override
  public void publish(Alert alert, ScoredTransaction context) {
    if (emitters.isEmpty()) {
      return;
    }
    AlertStreamEvent event = AlertStreamEvent.from(alert, context);
    String json;
    try {
      json = objectMapper.writeValueAsString(event);
    } catch (IOException e) {
      log.error("could not serialize alert {} for streaming", alert.id(), e);
      return;
    }
    broadcast(SseEmitter.event().name("alert").id(alert.id().toString()).data(json));
  }

  @Scheduled(fixedDelay = 25_000)
  void heartbeat() {
    if (!emitters.isEmpty()) {
      broadcast(SseEmitter.event().comment("heartbeat"));
    }
  }

  private void broadcast(SseEmitter.SseEventBuilder event) {
    for (SseEmitter emitter : emitters) {
      try {
        emitter.send(event);
      } catch (Exception e) {
        emitters.remove(emitter);
      }
    }
  }
}
