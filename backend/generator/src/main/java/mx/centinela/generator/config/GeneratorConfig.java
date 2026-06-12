package mx.centinela.generator.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Scheduling drives the normal traffic loop; async lets attack bursts run off-thread. */
@Configuration
@EnableScheduling
@EnableAsync
public class GeneratorConfig {}
