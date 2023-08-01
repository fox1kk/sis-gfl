package ru.gf.sis.events;

import static ru.gf.sis.interaction.SelectMenuInteraction.CUSTOM_ID_PREFIXES__SELECT_MENU;
import static ru.gf.sis.service.UtilsProcessService.CUSTOM_ID_PREFIXES__LANG_SELECT_MENU;

import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Mono;
import ru.gf.sis.dto.StoredInfo;
import ru.gf.sis.dto.lang.Language;
import ru.gf.sis.interaction.BaseSlideInteraction;
import ru.gf.sis.interaction.SelectMenuInteraction;
import ru.gf.sis.service.InfoProcessService;
import ru.gf.sis.service.InteractionStoreService;
import ru.gf.sis.service.TranslationService;
import ru.gf.sis.service.UtilsProcessService;

@Service
@Slf4j
@AllArgsConstructor
public class SelectMenuInteractionEventListener implements EventListener<SelectMenuInteractionEvent> {
    
    private final InteractionStoreService interactions;
    private final UtilsProcessService utilsService;
    private final InfoProcessService infoService;
    
    @Override
    public Class<SelectMenuInteractionEvent> getEventType() {
        return SelectMenuInteractionEvent.class;
    }

    @Override
    public Mono<Void> execute(SelectMenuInteractionEvent event) {
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
            utilsService.processLanguageSet(
                    event.getValues().get(0), event.getInteraction().getGuildId().get().asLong());
            if (event.getMessage().isPresent()) {
                event.getMessage().get().delete().subscribe();
            }
            return event.reply().withContent("Language updated");
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
}
