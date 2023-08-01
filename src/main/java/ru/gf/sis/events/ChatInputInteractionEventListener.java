package ru.gf.sis.events;

import static ru.gf.sis.events.GuildCreateEventListener.D;
import static ru.gf.sis.events.GuildCreateEventListener.DOLL;
import static ru.gf.sis.events.GuildCreateEventListener.E;
import static ru.gf.sis.events.GuildCreateEventListener.EQUIP;
import static ru.gf.sis.events.GuildCreateEventListener.EXP;
import static ru.gf.sis.events.GuildCreateEventListener.FEXP;
import static ru.gf.sis.events.GuildCreateEventListener.HOC;
import static ru.gf.sis.events.GuildCreateEventListener.ID;
import static ru.gf.sis.events.GuildCreateEventListener.INFO;
import static ru.gf.sis.events.GuildCreateEventListener.SET_LANG;
import static ru.gf.sis.events.GuildCreateEventListener.SF;
import static ru.gf.sis.events.GuildCreateEventListener.WEIBO;

import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono;
import discord4j.rest.util.Permission;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Mono;
import ru.gf.sis.dto.StoredInfo;
import ru.gf.sis.dto.lang.Language;
import ru.gf.sis.interaction.BaseSlideInteraction;
import ru.gf.sis.interaction.CommonInteraction;
import ru.gf.sis.interaction.SelectMenuInteraction;
import ru.gf.sis.service.ExpProcessService;
import ru.gf.sis.service.InfoProcessService;
import ru.gf.sis.service.InteractionStoreService;
import ru.gf.sis.service.TimerProcessService;
import ru.gf.sis.service.TranslationService;
import ru.gf.sis.service.UtilsProcessService;

@Service
@Slf4j
@AllArgsConstructor
public class ChatInputInteractionEventListener implements EventListener<ChatInputInteractionEvent> {

  private final ExpProcessService expService;
  private final TimerProcessService timerService;
  private final InfoProcessService infoService;
  private final UtilsProcessService utilsService;
  
  private final TranslationService translationService;
  private final InteractionStoreService interactions;

  public static final String MESSAGES__INSUFFICIENT_PERMISSION = "insufficient_permission";

  @Override
  public Class<ChatInputInteractionEvent> getEventType() {
    return ChatInputInteractionEvent.class;
  }

  @Override
  public Mono<Void> execute(ChatInputInteractionEvent event) {

    Language language = TranslationService.getLanguageFromInteractionEvent(event);

    if (event.getCommandName().equals(EXP)) {
      String result =
          expService.process(event.getInteraction().getCommandInteraction().get(), language);
      return event.reply(result);
    }

    if (event.getCommandName().equals(FEXP)) {
      String result =
          expService.processFairy(event.getInteraction().getCommandInteraction().get(), language);
      return event.reply(result);
    }

    if (event.getCommandName().equals(D) || event.getCommandName().equals(DOLL)) {
      BaseSlideInteraction interaction =
          timerService.processTDoll(event.getInteraction(), language);
      if (!ObjectUtils.isEmpty(interaction.getError())) {
        return event.reply().withContent(interaction.getError());
      }
      interactions.put(event.getInteraction().getId().asString(), interaction);
      var reply = event.reply().withEmbeds(interaction.getContent());
      return ObjectUtils.isEmpty(interaction.getButtons())
          ? reply
          : reply.withComponents(interaction.getButtons());
    }

    if (event.getCommandName().equals(E) || event.getCommandName().equals(EQUIP)) {
      BaseSlideInteraction interaction =
          timerService.processEquip(event.getInteraction(), language);
      if (!ObjectUtils.isEmpty(interaction.getError())) {
        return event.reply().withContent(interaction.getError());
      }
      interactions.put(event.getInteraction().getId().asString(), interaction);
      var reply = event.reply().withEmbeds(interaction.getContent());
      return ObjectUtils.isEmpty(interaction.getButtons())
          ? reply
          : reply.withComponents(interaction.getButtons());
    }

    if (event.getCommandName().equals(INFO)) {
      SelectMenuInteraction interaction =
          infoService.processInfoSelectMenu(event.getInteraction(), language);
      if (!ObjectUtils.isEmpty(interaction.getError())) {
        return event.reply().withContent(interaction.getError());
      }
      if (interaction.isOnlyOneEntity()) {
        return generateCommonInteraction(interaction.getOnlyOneEntity(), event, language);
      }
      interactions.put(event.getInteraction().getId().asString(), interaction);
      return event.reply().withComponents(interaction.getSelectMenu());
    }

    if (event.getCommandName().equals(ID)) {
      CommonInteraction interaction = infoService.processById(event.getInteraction(), language);
      if (!ObjectUtils.isEmpty(interaction.getError())) {
        return event.reply().withContent(interaction.getError());
      }
      interactions.put(event.getInteraction().getId().asString(), interaction);
      var reply = event.reply().withEmbeds(interaction.getContent());
      return ObjectUtils.isEmpty(interaction.getButtons())
          ? reply
          : reply.withComponents(interaction.getButtons());
    }

    if (event.getCommandName().equals(HOC)) {
      SelectMenuInteraction interaction = infoService.processHoc(event.getInteraction(), language);
      if (!ObjectUtils.isEmpty(interaction.getError())) {
        return event.reply().withContent(interaction.getError());
      }
      if (interaction.isOnlyOneEntity()) {
        return generateCommonInteraction(interaction.getOnlyOneEntity(), event, language);
      }
      interactions.put(event.getInteraction().getId().asString(), interaction);
      return event.reply().withComponents(interaction.getSelectMenu());
    }

    if (event.getCommandName().equals(SF)) {
      SelectMenuInteraction interaction = infoService.processSf(event.getInteraction(), language);
      if (!ObjectUtils.isEmpty(interaction.getError())) {
        return event.reply().withContent(interaction.getError());
      }
      if (interaction.isOnlyOneEntity()) {
        return generateCommonInteraction(interaction.getOnlyOneEntity(), event, language);
      }
      interactions.put(event.getInteraction().getId().asString(), interaction);
      return event.reply().withComponents(interaction.getSelectMenu());
    }

    if (event.getCommandName().equals(SET_LANG)) {
      if (!isAdmin(event)) {
        return event
            .reply()
            .withContent(
                TranslationService.getVariation(MESSAGES__INSUFFICIENT_PERMISSION, language));
      }
      return event.reply().withComponents(utilsService.processLanguageSelectMenu());
    }

    if (event.getCommandName().equals(WEIBO)) {
      if (!isAdmin(event)) {
        return event
            .reply()
            .withContent(
                    TranslationService.getVariation(MESSAGES__INSUFFICIENT_PERMISSION, language));
      }
      return event
          .reply()
          .withContent(
              utilsService.processWeiboChannelSet(
                  event.getInteraction().getChannelId().asString(),
                  event.getInteraction().getGuildId().get().asLong(),
                  language));
    }

    return Mono.empty();
  }

  private InteractionApplicationCommandCallbackReplyMono generateCommonInteraction(
      StoredInfo entity, DeferrableInteractionEvent event, Language language) {
    BaseSlideInteraction interactionSequence =
        infoService.processInfo(entity, event.getInteraction().getId().asString(), language);
    interactions.put(event.getInteraction().getId().asString(), interactionSequence);
    var reply = event.reply().withEmbeds(interactionSequence.getContent());
    return ObjectUtils.isEmpty(interactionSequence.getButtons())
        ? reply
        : reply.withComponents(interactionSequence.getButtons());
  }

  private Boolean isAdmin(ApplicationCommandInteractionEvent event) {
    return event.getInteraction().getMember().isPresent()
        && event
            .getInteraction()
            .getMember()
            .get()
            .getBasePermissions()
            .block()
            .asEnumSet()
            .contains(Permission.ADMINISTRATOR);
  }
}
