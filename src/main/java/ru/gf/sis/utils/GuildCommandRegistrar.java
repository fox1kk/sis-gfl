package ru.gf.sis.utils;

import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
public class GuildCommandRegistrar {

  private final RestClient restClient;
  private final long guildId;
  private final List<ApplicationCommandRequest> commandRequests;
  private final Mono<Long> applicationId;

  private GuildCommandRegistrar(
      RestClient restClient, long guildId, List<ApplicationCommandRequest> commandRequests) {
    this.restClient = restClient;
    this.guildId = guildId;
    this.commandRequests = commandRequests;
    this.applicationId = restClient.getApplicationId().cache();
  }

  public static GuildCommandRegistrar create(
      RestClient restClient, long guildId, List<ApplicationCommandRequest> commandRequests) {
    return new GuildCommandRegistrar(restClient, guildId, commandRequests);
  }

  public Flux<ApplicationCommandData> registerCommands() {
    return bulkOverwriteCommands(commandRequests);
  }

  private Flux<ApplicationCommandData> bulkOverwriteCommands(
      List<ApplicationCommandRequest> requests) {
    return applicationId.flatMapMany(
        id ->
            restClient
                .getApplicationService()
                .bulkOverwriteGuildApplicationCommand(id, guildId, requests)
                .doOnNext(it -> log.info("Registered command {} at guild {}", it.name(), guildId)));
  }
}
