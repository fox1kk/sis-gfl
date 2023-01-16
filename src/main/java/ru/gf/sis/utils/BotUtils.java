package ru.gf.sis.utils;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BotUtils {
  public static final String NOTIFY = "860549071755149332";
  public static final String DEVELOPER_NOTIFY = "805570919736147968";

  private final GatewayDiscordClient client;

  public TextChannel getTextChannel(String channelId) {
    return (TextChannel) client.getChannelById(Snowflake.of(channelId)).block();
  }
}
