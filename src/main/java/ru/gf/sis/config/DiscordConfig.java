package ru.gf.sis.config;

import discord4j.common.retry.ReconnectOptions;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import ru.gf.sis.events.EventListener;

@Configuration
public class DiscordConfig {

  @Value("${discord.token}")
  private String token;
  
  @Bean
  public <T extends Event> GatewayDiscordClient gatewayDiscordClient(
      List<EventListener<T>> eventListeners) {

    GatewayDiscordClient client =
        DiscordClient.create(token)
            .gateway()
            .setReconnectOptions(
                ReconnectOptions.builder()
                    .setBackoffScheduler(
                        Schedulers.newParallel("cards-backoff", Schedulers.DEFAULT_POOL_SIZE, true))
                    .setFirstBackoff(Duration.ofSeconds(10))
                    .setMaxBackoffInterval(Duration.ofSeconds(30))
                    .setMaxRetries(Long.MAX_VALUE)
                    .build())
            .login()
            .block();

    for (EventListener<T> listener : eventListeners) {
      client
          .on(listener.getEventType())
          .flatMap(listener::execute)
          .onErrorResume(listener::handleError)
          .subscribe();
    }

    return client;
  }
}
