package ru.gf.sis.service;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.interaction.*;
import discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono;
import discord4j.rest.util.Permission;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Mono;
import ru.gf.sis.dto.StoredInfo;
import ru.gf.sis.dto.lang.Language;
import ru.gf.sis.interaction.*;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static ru.gf.sis.interaction.BaseSlideInteraction.CUSTOM_ID_PREFIXES__LEFT_ARROW;
import static ru.gf.sis.interaction.BaseSlideInteraction.CUSTOM_ID_PREFIXES__RIGHT_ARROW;
import static ru.gf.sis.interaction.CommonInteraction.CUSTOM_ID_PREFIXES__MOD_ANALYSIS;
import static ru.gf.sis.interaction.CommonInteraction.CUSTOM_ID_PREFIXES__STOCK;
import static ru.gf.sis.interaction.CommonInteraction.CUSTOM_ID_PREFIXES__MOD;
import static ru.gf.sis.interaction.CommonInteraction.CUSTOM_ID_PREFIXES__SPEC;
import static ru.gf.sis.interaction.CommonInteraction.CUSTOM_ID_PREFIXES__ANALYSIS;
import static ru.gf.sis.interaction.SelectMenuInteraction.CUSTOM_ID_PREFIXES__SELECT_MENU;
import static ru.gf.sis.service.RegisterCommandService.*;
import static ru.gf.sis.service.UtilsProcessService.CUSTOM_ID_PREFIXES__LANG_SELECT_MENU;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiscordService {

  private final GatewayDiscordClient client;

  private final RegisterCommandService registerCommandService;
  private final ExpProcessService expProcessService;
  private final TimerProcessService timerProcessService;
  private final InfoProcessService infoProcessService;
  private final UtilsProcessService utilsProcessService;
  private final TranslationService translationService;

  public static final String MESSAGES__INSUFFICIENT_PERMISSION = "insufficient_permission";

  private final Map<String, Interaction> interactions = new HashMap<>();

  public void clearInteractions() {
    interactions
        .entrySet()
        .removeIf(item -> item.getValue().getCreated().isBefore(LocalDateTime.now().minusDays(1L)));
  }

  @PostConstruct
  private void postConstruct() {
    client
        .on(
            new ReactiveEventAdapter() {
              @Override
              @NonNull
              public Publisher<?> onGuildCreate(@NonNull GuildCreateEvent event) {
                registerCommandService.registerForOneGuild(event.getGuild().getId().asLong());
                return Mono.empty();
              }

              @Override
              @NonNull
              public Publisher<?> onChatInputInteraction(@NonNull ChatInputInteractionEvent event) {
                Language language = TranslationService.getLanguageFromInteractionEvent(event);

                if (event.getCommandName().equals(EXP)) {
                  String result =
                      expProcessService.process(
                          event.getInteraction().getCommandInteraction().get(), language);
                  return event.reply(result);
                }

                if (event.getCommandName().equals(FEXP)) {
                  String result =
                      expProcessService.processFairy(
                          event.getInteraction().getCommandInteraction().get(), language);
                  return event.reply(result);
                }

                if (event.getCommandName().equals(D) || event.getCommandName().equals(DOLL)) {
                  BaseSlideInteraction interaction =
                      timerProcessService.processTDoll(event.getInteraction(), language);
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
                      timerProcessService.processEquip(event.getInteraction(), language);
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
                      infoProcessService.processInfoSelectMenu(event.getInteraction(), language);
                  if (!ObjectUtils.isEmpty(interaction.getError())) {
                    return event.reply().withContent(interaction.getError());
                  }
                  if (interaction.isOnlyOneEntity()) {
                    return generateCommonInteraction(
                        interaction.getOnlyOneEntity(), event, language);
                  }
                  interactions.put(event.getInteraction().getId().asString(), interaction);
                  return event.reply().withComponents(interaction.getSelectMenu());
                }

                if (event.getCommandName().equals(ID)) {
                  CommonInteraction interaction =
                      infoProcessService.processById(event.getInteraction(), language);
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
                  SelectMenuInteraction interaction =
                      infoProcessService.processHoc(event.getInteraction(), language);
                  if (!ObjectUtils.isEmpty(interaction.getError())) {
                    return event.reply().withContent(interaction.getError());
                  }
                  if (interaction.isOnlyOneEntity()) {
                    return generateCommonInteraction(
                        interaction.getOnlyOneEntity(), event, language);
                  }
                  interactions.put(event.getInteraction().getId().asString(), interaction);
                  return event.reply().withComponents(interaction.getSelectMenu());
                }

                if (event.getCommandName().equals(SF)) {
                  SelectMenuInteraction interaction =
                      infoProcessService.processSf(event.getInteraction(), language);
                  if (!ObjectUtils.isEmpty(interaction.getError())) {
                    return event.reply().withContent(interaction.getError());
                  }
                  if (interaction.isOnlyOneEntity()) {
                    return generateCommonInteraction(
                        interaction.getOnlyOneEntity(), event, language);
                  }
                  interactions.put(event.getInteraction().getId().asString(), interaction);
                  return event.reply().withComponents(interaction.getSelectMenu());
                }

                if (event.getCommandName().equals(SET_LANG)) {
                  if (!isAdmin(event)) {
                    return event
                        .reply()
                        .withContent(
                            TranslationService.getVariation(
                                MESSAGES__INSUFFICIENT_PERMISSION, language));
                  }
                  return event
                      .reply()
                      .withComponents(utilsProcessService.processLanguageSelectMenu());
                }

                if (event.getCommandName().equals(WEIBO)) {
                  if (!isAdmin(event)) {
                    return event
                        .reply()
                        .withContent(
                            TranslationService.getVariation(
                                MESSAGES__INSUFFICIENT_PERMISSION, language));
                  }
                  return event
                      .reply()
                      .withContent(
                          utilsProcessService.processWeiboChannelSet(
                              event.getInteraction().getChannelId().asString(),
                              event.getInteraction().getGuildId().get().asLong(),
                              language));
                }

                return Mono.empty();
              }

              @Override
              @NonNull
              public Publisher<?> onSelectMenuInteraction(
                  @NonNull SelectMenuInteractionEvent event) {
                Language language = TranslationService.getLanguageFromInteractionEvent(event);

                if (event.getCustomId().startsWith(CUSTOM_ID_PREFIXES__SELECT_MENU)) {
                  SelectMenuInteraction interaction =
                      (SelectMenuInteraction)
                          interactions.get(
                              event.getCustomId().replace(CUSTOM_ID_PREFIXES__SELECT_MENU, ""));
                  Optional<? extends StoredInfo> storedInfo =
                      interaction.getEntityById(event.getValues().get(0));
                  if (storedInfo.isPresent()) {
                    if (event.getMessage().isPresent()) {
                      event.getMessage().get().delete().subscribe();
                    }
                    return generateCommonInteraction(storedInfo.get(), event, language);
                  }
                }

                if (event.getCustomId().startsWith(CUSTOM_ID_PREFIXES__LANG_SELECT_MENU)) {
                  utilsProcessService.processLanguageSet(
                      event.getValues().get(0), event.getInteraction().getGuildId().get().asLong());
                  if (event.getMessage().isPresent()) {
                    event.getMessage().get().delete().subscribe();
                  }
                  return event.reply().withContent("Language updated");
                }

                return Mono.empty();
              }

              @Override
              @NonNull
              public Publisher<?> onButtonInteraction(@NonNull ButtonInteractionEvent event) {
                if (event.getCustomId().startsWith(CUSTOM_ID_PREFIXES__RIGHT_ARROW)) {
                  BaseSlideInteraction interaction =
                      (BaseSlideInteraction)
                          interactions.get(
                              event.getCustomId().replace(CUSTOM_ID_PREFIXES__RIGHT_ARROW, ""));
                  if (ObjectUtils.isEmpty(interaction)) {
                    return Mono.empty();
                  }
                  interaction.slideRight();
                  return event.deferEdit().then(getEditForBaseInteraction(event, interaction));
                }

                if (event.getCustomId().startsWith(CUSTOM_ID_PREFIXES__LEFT_ARROW)) {
                  BaseSlideInteraction interaction =
                      (BaseSlideInteraction)
                          interactions.get(
                              event.getCustomId().replace(CUSTOM_ID_PREFIXES__LEFT_ARROW, ""));
                  if (ObjectUtils.isEmpty(interaction)) {
                    return Mono.empty();
                  }
                  interaction.slideLeft();
                  return event.deferEdit().then(getEditForBaseInteraction(event, interaction));
                }

                if (event.getCustomId().startsWith(CUSTOM_ID_PREFIXES__STOCK)) {
                  CommonInteraction interaction =
                      (CommonInteraction)
                          interactions.get(
                              event.getCustomId().replace(CUSTOM_ID_PREFIXES__STOCK, ""));
                  if (ObjectUtils.isEmpty(interaction)) {
                    return Mono.empty();
                  }
                  interaction.updateState(InteractionState.STOCK);
                  return event.deferEdit().then(getEditForBaseInteraction(event, interaction));
                }

                if (event.getCustomId().startsWith(CUSTOM_ID_PREFIXES__MOD_ANALYSIS)) {
                  CommonInteraction interaction =
                      (CommonInteraction)
                          interactions.get(
                              event.getCustomId().replace(CUSTOM_ID_PREFIXES__MOD_ANALYSIS, ""));
                  if (ObjectUtils.isEmpty(interaction)) {
                    return Mono.empty();
                  }
                  interaction.updateState(InteractionState.MOD_ANALYSIS);
                  return event.deferEdit().then(getEditForBaseInteraction(event, interaction));
                }

                if (event.getCustomId().startsWith(CUSTOM_ID_PREFIXES__MOD)) {
                  CommonInteraction interaction =
                      (CommonInteraction)
                          interactions.get(
                              event.getCustomId().replace(CUSTOM_ID_PREFIXES__MOD, ""));
                  if (ObjectUtils.isEmpty(interaction)) {
                    return Mono.empty();
                  }
                  interaction.updateState(InteractionState.MOD);
                  return event.deferEdit().then(getEditForBaseInteraction(event, interaction));
                }

                if (event.getCustomId().startsWith(CUSTOM_ID_PREFIXES__SPEC)) {
                  CommonInteraction interaction =
                      (CommonInteraction)
                          interactions.get(
                              event.getCustomId().replace(CUSTOM_ID_PREFIXES__SPEC, ""));
                  if (ObjectUtils.isEmpty(interaction)) {
                    return Mono.empty();
                  }
                  interaction.updateState(InteractionState.EQUIP);
                  return event.deferEdit().then(getEditForBaseInteraction(event, interaction));
                }

                if (event.getCustomId().startsWith(CUSTOM_ID_PREFIXES__ANALYSIS)) {
                  CommonInteraction interaction =
                      (CommonInteraction)
                          interactions.get(
                              event.getCustomId().replace(CUSTOM_ID_PREFIXES__ANALYSIS, ""));
                  if (ObjectUtils.isEmpty(interaction)) {
                    return Mono.empty();
                  }
                  interaction.updateState(InteractionState.STOCK_ANALYSIS);
                  return event.deferEdit().then(getEditForBaseInteraction(event, interaction));
                }

                return Mono.empty();
              }
            })
        .subscribe();
  }

  private InteractionApplicationCommandCallbackReplyMono generateCommonInteraction(
      StoredInfo entity, DeferrableInteractionEvent event, Language language) {
    BaseSlideInteraction interactionSequence =
        infoProcessService.processInfo(entity, event.getInteraction().getId().asString(), language);
    interactions.put(event.getInteraction().getId().asString(), interactionSequence);
    var reply = event.reply().withEmbeds(interactionSequence.getContent());
    return ObjectUtils.isEmpty(interactionSequence.getButtons())
        ? reply
        : reply.withComponents(interactionSequence.getButtons());
  }

  private Mono<?> getEditForBaseInteraction(
      ButtonInteractionEvent event, BaseSlideInteraction interaction) {
    return ObjectUtils.isEmpty(interaction.getButtons())
        ? event.editReply().withEmbeds(interaction.getContent())
        : event
            .editReply()
            .withEmbeds(interaction.getContent())
            .withComponentsOrNull(interaction.getButtons());
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
