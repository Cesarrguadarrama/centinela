package mx.centinela.bootstrap.infrastructure.redis;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import mx.centinela.domain.activity.WindowEntry;
import mx.centinela.domain.activity.WindowSnapshot;
import mx.centinela.domain.model.Clabe;
import mx.centinela.domain.model.Money;
import mx.centinela.domain.model.Transaction;
import mx.centinela.domain.port.out.ActivityWindowPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;

/**
 * Sliding windows as Redis sorted sets, one per account and direction. Score is the event-time
 * epoch millis; members encode {@code amountCents|counterparty|txId} so a single ZRANGEBYSCORE
 * answers count, sum and distinct-sender questions without touching Postgres. The txId suffix makes
 * registration idempotent under Kafka redelivery (same event, same member).
 */
@Component
class RedisActivityWindowAdapter implements ActivityWindowPort {

  /** Upper bound for trimming and TTL — must exceed the widest rule window. */
  private static final Duration RETENTION = Duration.ofHours(1);

  private static final String OUT_PREFIX = "win:out:";
  private static final String IN_PREFIX = "win:in:";

  private final StringRedisTemplate redis;

  RedisActivityWindowAdapter(StringRedisTemplate redis) {
    this.redis = redis;
  }

  @Override
  public void register(Transaction tx) {
    long eventMillis = tx.timestamp().toEpochMilli();
    long amountCents = tx.amount().amount().movePointRight(2).longValueExact();

    store(
        OUT_PREFIX + tx.source().value(),
        amountCents + "|" + tx.destination().value() + "|" + tx.id(),
        eventMillis);
    store(
        IN_PREFIX + tx.destination().value(),
        amountCents + "|" + tx.source().value() + "|" + tx.id(),
        eventMillis);
  }

  @Override
  public WindowSnapshot outgoing(Clabe account, Duration window, Instant asOf) {
    return query(OUT_PREFIX + account.value(), window, asOf);
  }

  @Override
  public WindowSnapshot incoming(Clabe account, Duration window, Instant asOf) {
    return query(IN_PREFIX + account.value(), window, asOf);
  }

  private void store(String key, String member, long eventMillis) {
    redis.opsForZSet().add(key, member, eventMillis);
    // Opportunistic trim + TTL keep memory bounded without a separate cleanup job
    redis.opsForZSet().removeRangeByScore(key, 0, eventMillis - RETENTION.toMillis());
    redis.expire(key, RETENTION);
  }

  private WindowSnapshot query(String key, Duration window, Instant asOf) {
    Set<TypedTuple<String>> tuples =
        redis
            .opsForZSet()
            .rangeByScoreWithScores(key, asOf.minus(window).toEpochMilli(), asOf.toEpochMilli());
    if (tuples == null || tuples.isEmpty()) {
      return WindowSnapshot.EMPTY;
    }
    List<WindowEntry> entries = tuples.stream().map(RedisActivityWindowAdapter::toEntry).toList();
    return new WindowSnapshot(entries);
  }

  private static WindowEntry toEntry(TypedTuple<String> tuple) {
    String[] parts = tuple.getValue().split("\\|", 3);
    Money amount = Money.of(new BigDecimal(parts[0]).movePointLeft(2));
    Clabe counterparty = Clabe.of(parts[1]);
    Instant at = Instant.ofEpochMilli(tuple.getScore().longValue());
    return new WindowEntry(amount, counterparty, at);
  }
}
