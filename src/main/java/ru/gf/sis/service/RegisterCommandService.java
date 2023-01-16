package ru.gf.sis.service;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.gf.sis.utils.GuildCommandRegistrar;

import java.util.List;

import static ru.gf.sis.service.ExpProcessService.*;
import static ru.gf.sis.service.InfoProcessService.*;
import static ru.gf.sis.service.TimerProcessService.REGISTER_COMMANDS__TIMER;

@Service
@Slf4j
@AllArgsConstructor
public class RegisterCommandService {
  private final GatewayDiscordClient client;

  public static final String EXP = "exp";
  public static final String FEXP = "fexp";
  public static final String DOLL = "doll";
  public static final String D = "d";
  public static final String INFO = "info";
  public static final String ID = "id";
  public static final String E = "e";
  public static final String EQUIP = "equip";
  public static final String HOC = "hoc";
  public static final String SF = "sf";
  public static final String SET_LANG = "setlang";
  public static final String WEIBO = "weibo";

  public void registerForAllGuilds() {
    client
        .getGuilds()
        .toStream()
        .forEach(
            item -> {
              try {
                registerAllCommands(item.getId().asLong());
              } catch (Exception e) {
                log.error(
                    "Error registering commands for GuildId {}. Error: {}",
                    item.getId().asLong(),
                    e.getMessage());
              }
            });
  }

  public void registerForOneGuild(long guildId) {
    try {
      registerAllCommands(guildId);
    } catch (Exception e) {
      log.error("Error registering commands for GuildId {}. Error: {}", guildId, e.getMessage());
    }
  }

  private void registerAllCommands(long guildId) {
    ApplicationCommandRequest expCommand =
        ApplicationCommandRequest.builder()
            .name(EXP)
            .description("Shows amount of combat reports needed to level-up a T-Doll.")
            .addOption(
                ApplicationCommandOptionData.builder()
                    .name(REGISTER_COMMANDS__CURRENT_LV)
                    .description("Current level of T-Doll")
                    .type(ApplicationCommandOption.Type.INTEGER.getValue())
                    .required(true)
                    .build())
            .addOption(
                ApplicationCommandOptionData.builder()
                    .name(REGISTER_COMMANDS__CURRENT_EXP)
                    .description("Current amount of exp")
                    .type(ApplicationCommandOption.Type.INTEGER.getValue())
                    .required(true)
                    .build())
            .addOption(
                ApplicationCommandOptionData.builder()
                    .name(REGISTER_COMMANDS__TARGET_LV)
                    .description("Target level")
                    .type(ApplicationCommandOption.Type.INTEGER.getValue())
                    .required(true)
                    .build())
            .addOption(
                ApplicationCommandOptionData.builder()
                    .name(REGISTER_COMMANDS__OATH)
                    .description("Oath")
                    .type(ApplicationCommandOption.Type.BOOLEAN.getValue())
                    .required(false)
                    .build())
            .build();

    ApplicationCommandRequest fexpCommand =
        ApplicationCommandRequest.builder()
            .name(FEXP)
            .description("Shows amount of combat reports needed to level-up a Fairy.")
            .addOption(
                ApplicationCommandOptionData.builder()
                    .name(REGISTER_COMMANDS__CURRENT_LV)
                    .description("Current level of Fairy")
                    .type(ApplicationCommandOption.Type.INTEGER.getValue())
                    .required(true)
                    .build())
            .addOption(
                ApplicationCommandOptionData.builder()
                    .name(REGISTER_COMMANDS__CURRENT_EXP)
                    .description("Current amount of exp")
                    .type(ApplicationCommandOption.Type.INTEGER.getValue())
                    .required(true)
                    .build())
            .addOption(
                ApplicationCommandOptionData.builder()
                    .name(REGISTER_COMMANDS__TARGET_LV)
                    .description("Target level")
                    .type(ApplicationCommandOption.Type.INTEGER.getValue())
                    .required(true)
                    .build())
            .build();

    ApplicationCommandRequest d =
        ApplicationCommandRequest.builder()
            .name(D)
            .description("Shows T-Dolls corresponding to the specified production time.")
            .addOption(
                ApplicationCommandOptionData.builder()
                    .name(REGISTER_COMMANDS__TIMER)
                    .description("Examples: 03:20, 0320, 320")
                    .type(ApplicationCommandOption.Type.STRING.getValue())
                    .required(true)
                    .build())
            .build();

    ApplicationCommandRequest doll =
        ApplicationCommandRequest.builder()
            .name(DOLL)
            .description("Shows T-Dolls corresponding to the specified production time.")
            .addOption(
                ApplicationCommandOptionData.builder()
                    .name(REGISTER_COMMANDS__TIMER)
                    .description("Examples: 03:20, 0320, 320")
                    .type(ApplicationCommandOption.Type.STRING.getValue())
                    .required(true)
                    .build())
            .build();

    ApplicationCommandRequest info =
        ApplicationCommandRequest.builder()
            .name(INFO)
            .description("Shows the information of a specific T-Doll/Fairy/HOC")
            .addOption(
                ApplicationCommandOptionData.builder()
                    .name(REGISTER_COMMANDS__REQUEST)
                    .description("Examples: /info g11, /info bgm, /info shield")
                    .type(ApplicationCommandOption.Type.STRING.getValue())
                    .required(true)
                    .build())
            .build();

    ApplicationCommandRequest byId =
        ApplicationCommandRequest.builder()
            .name(ID)
            .description("Shows the information of a specific T-Doll by the ID number")
            .addOption(
                ApplicationCommandOptionData.builder()
                    .name(REGISTER_COMMANDS__ID)
                    .description("Examples: /id 1")
                    .type(ApplicationCommandOption.Type.INTEGER.getValue())
                    .required(true)
                    .build())
            .addOption(
                ApplicationCommandOptionData.builder()
                    .name(REGISTER_COMMANDS__EXTRA)
                    .description("Extra rarity")
                    .type(ApplicationCommandOption.Type.BOOLEAN.getValue())
                    .required(false)
                    .build())
            .build();

    ApplicationCommandRequest e =
        ApplicationCommandRequest.builder()
            .name(E)
            .description("Shows equipment corresponding to the specified production time.")
            .addOption(
                ApplicationCommandOptionData.builder()
                    .name(REGISTER_COMMANDS__TIMER)
                    .description("Examples: 00:55, 0055, 55")
                    .type(ApplicationCommandOption.Type.STRING.getValue())
                    .required(true)
                    .build())
            .build();

    ApplicationCommandRequest equip =
        ApplicationCommandRequest.builder()
            .name(EQUIP)
            .description("Shows equipment corresponding to the specified production time.")
            .addOption(
                ApplicationCommandOptionData.builder()
                    .name(REGISTER_COMMANDS__TIMER)
                    .description("Examples: 00:55, 0055, 55")
                    .type(ApplicationCommandOption.Type.STRING.getValue())
                    .required(true)
                    .build())
            .build();

    ApplicationCommandRequest hoc =
        ApplicationCommandRequest.builder()
            .name(HOC)
            .description("Shows the information of a specific HOC unit")
            .addOption(
                ApplicationCommandOptionData.builder()
                    .name(REGISTER_COMMANDS__REQUEST)
                    .description("Examples: /hoc bgm")
                    .type(ApplicationCommandOption.Type.STRING.getValue())
                    .required(false)
                    .build())
            .build();

    ApplicationCommandRequest sf =
        ApplicationCommandRequest.builder()
            .name(SF)
            .description("Shows the information of a specific SF unit")
            .addOption(
                ApplicationCommandOptionData.builder()
                    .name(REGISTER_COMMANDS__REQUEST)
                    .description("Examples: /sf scarecrow")
                    .type(ApplicationCommandOption.Type.STRING.getValue())
                    .required(true)
                    .build())
            .build();

    ApplicationCommandRequest setLang =
        ApplicationCommandRequest.builder()
            .name(SET_LANG)
            .description(
                "Set the server language. This command needs server administrator privileges.")
            .build();

    ApplicationCommandRequest weibo =
        ApplicationCommandRequest.builder()
            .name(WEIBO)
            .description(
                "Toggle weibo news-feed feature on the channel. This command needs server administrator privileges.")
            .build();

    GuildCommandRegistrar.create(
            client.getRestClient(),
            guildId,
            List.of(
                expCommand, fexpCommand, d, doll, info, byId, e, equip, hoc, sf, setLang, weibo))
        .registerCommands()
        .doOnError(
            error ->
                log.warn(
                    "Unable to create guild command: '"
                        + guildId
                        + "'. Error: "
                        + error.getMessage()))
        .onErrorResume(error -> Mono.empty())
        .subscribe();
  }
}
