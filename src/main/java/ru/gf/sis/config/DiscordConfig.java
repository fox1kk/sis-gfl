package ru.gf.sis.config;

import discord4j.common.retry.ReconnectOptions;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Configuration
public class DiscordConfig {

  private final GatewayDiscordClient client;

  public DiscordConfig(@Value("${discord.token}") String token) {
    client =
        DiscordClient.create(token)
            .gateway()
            .setReconnectOptions(
                ReconnectOptions.builder()
                    .setBackoffScheduler(
                        Schedulers.newParallel("sis-backoff", Schedulers.DEFAULT_POOL_SIZE, true))
                    .setFirstBackoff(Duration.ofSeconds(10))
                    .setMaxBackoffInterval(Duration.ofSeconds(30))
                    .setMaxRetries(Long.MAX_VALUE)
                    .build())
            .login()
            .block();
  }

  @Bean(name = "gatewayDiscordClient")
  public GatewayDiscordClient getGatewayDiscordClient() {
    return client;
  }
}
