package ru.gf.sis.scheduler;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gf.sis.service.DiscordService;
import ru.gf.sis.service.analysis.AnalysisService;
import ru.gf.sis.service.wiki.WikiService;
import ru.gf.sis.utils.BotUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Component
@AllArgsConstructor
@Slf4j
public class ScheduledTasks {
  private static final String EN_DIS_PING_ROLE = "891953634445774848";
  private static final String EN_DIS_PING_CHANNEL = "891954625115209728";
  private static final String RU_DIS_PING_ROLE_EN = "445667552660553738";
  private static final String RU_DIS_PING_ROLE_CH = "445667321835552798";
  private static final String RU_DIS_PING_CHANNEL = "435288509335732224";

  private final GatewayDiscordClient client;
  private final BotUtils botUtils;
  private final AnalysisService analysisService;
  private final WikiService wikiService;
  private final DiscordService discordService;

  @Scheduled(cron = "0 */1 * * * *")
  public void updateStatus() {
    ZoneId zone = ZoneId.of("SystemV/PST8");
    ZonedDateTime date = ZonedDateTime.now(zone);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    String time = date.format(formatter);

    client
        .updatePresence(ClientPresence.online(ClientActivity.playing("Server Time: " + time)))
        .subscribe();
  }

  @Scheduled(cron = "0 0 5,13 * * *")
  public void reportBatteryCh() {
    TextChannel channel = botUtils.getTextChannel(RU_DIS_PING_CHANNEL);
    channel.createMessage("<@&" + RU_DIS_PING_ROLE_CH + "> Соберите батарейки UωU").subscribe();
    channel
        .createMessage(
            EmbedCreateSpec.builder().color(Color.WHITE).image(getRuBatteryImageUrl()).build())
        .subscribe();
  }

  @Scheduled(cron = "0 0 5,21 * * *")
  public void reportBatteryEn() {
    TextChannel channel = botUtils.getTextChannel(RU_DIS_PING_CHANNEL);
    channel.createMessage("<@&" + RU_DIS_PING_ROLE_EN + "> Соберите батарейки UωU").subscribe();
    channel
        .createMessage(
            EmbedCreateSpec.builder().color(Color.WHITE).image(getRuBatteryImageUrl()).build())
        .subscribe();

    channel = botUtils.getTextChannel(EN_DIS_PING_CHANNEL);
    channel
        .createMessage("<@&" + EN_DIS_PING_ROLE + "> Time to collect the batteries UωU")
        .subscribe();
    channel
        .createMessage(
            EmbedCreateSpec.builder().color(Color.WHITE).image(getEngBatteryImageUrl()).build())
        .subscribe();
  }

  @Scheduled(cron = "0 0 0 * * *")
  public void refreshGoogleDocAnalysis() {
    analysisService.refreshGoogleDocAnalysis();
  }

  @Scheduled(cron = "0 0 23 * * *")
  public void parseWiki() {
    wikiService.wikiParse();
  }

  @Scheduled(cron = "0 */5 * * * *")
  public void clearInteractions() {
    discordService.clearInteractions();
  }

  private String getRuBatteryImageUrl() {
    switch (randomBatteryImage()) {
      case PUDGE:
        return "https://i.imgur.com/IRZ3xlb.gif";
      case BUBZ:
        return "https://i.imgur.com/AjCF9Ve.gif";
      case BILLY:
        return "https://i.imgur.com/Cngo7NC.gif";
      default:
        return "https://i.imgur.com/CE3LneE.gif";
    }
  }

  private String getEngBatteryImageUrl() {
    return "https://i.imgur.com/CE3LneE.gif";
  }

  private BatteryImage randomBatteryImage() {
    int randomNum = ThreadLocalRandom.current().nextInt(1, 100 + 1);
    if (randomNum < 6) {
      return BatteryImage.PUDGE;
    }
    if (randomNum > 96 && randomNum <= 98) {
      return BatteryImage.BILLY;
    }
    if (randomNum > 98) {
      return BatteryImage.BUBZ;
    }
    return BatteryImage.DEFAULT;
  }

  private enum BatteryImage {
    DEFAULT,
    PUDGE,
    BUBZ,
    BILLY
  }
}
