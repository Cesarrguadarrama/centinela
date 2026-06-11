/**
 * Centinela domain core.
 *
 * <p>This module holds the business model (transactions, alerts, rules, scoring) and the ports that
 * the outside world must implement. It is intentionally framework-free: no Spring, no JPA, no
 * Kafka. The Maven module graph enforces this — only test-scoped JUnit/AssertJ are available here.
 *
 * <p>Populated in phase 1.
 */
package mx.centinela.domain;
