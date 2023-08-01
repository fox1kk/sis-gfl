package ru.gf.sis.events;

import static ru.gf.sis.interaction.BaseSlideInteraction.CUSTOM_ID_PREFIXES__LEFT_ARROW;
import static ru.gf.sis.interaction.BaseSlideInteraction.CUSTOM_ID_PREFIXES__RIGHT_ARROW;
import static ru.gf.sis.interaction.CommonInteraction.CUSTOM_ID_PREFIXES__ANALYSIS;
import static ru.gf.sis.interaction.CommonInteraction.CUSTOM_ID_PREFIXES__MOD;
import static ru.gf.sis.interaction.CommonInteraction.CUSTOM_ID_PREFIXES__MOD_ANALYSIS;
import static ru.gf.sis.interaction.CommonInteraction.CUSTOM_ID_PREFIXES__SPEC;
import static ru.gf.sis.interaction.CommonInteraction.CUSTOM_ID_PREFIXES__STOCK;

import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Mono;
import ru.gf.sis.interaction.BaseSlideInteraction;
import ru.gf.sis.interaction.CommonInteraction;
import ru.gf.sis.interaction.InteractionState;
import ru.gf.sis.service.InteractionStoreService;

@Service
@Slf4j
@AllArgsConstructor
public class ButtonInteractionEventListener implements EventListener<ButtonInteractionEvent> {

  private InteractionStoreService interactions;

  @Override
  public Class<ButtonInteractionEvent> getEventType() {
    return ButtonInteractionEvent.class;
  }

  @Override
  public Mono<Void> execute(ButtonInteractionEvent event) {
    if (event.getCustomId().startsWith(CUSTOM_ID_PREFIXES__RIGHT_ARROW)) {
      BaseSlideInteraction interaction =
          (BaseSlideInteraction)
              interactions.get(event.getCustomId().replace(CUSTOM_ID_PREFIXES__RIGHT_ARROW, ""));
      if (ObjectUtils.isEmpty(interaction)) {
        return Mono.empty();
      }
      interaction.slideRight();
      deferEdit(event, interaction);
    }

    if (event.getCustomId().startsWith(CUSTOM_ID_PREFIXES__LEFT_ARROW)) {
      BaseSlideInteraction interaction =
          (BaseSlideInteraction)
              interactions.get(event.getCustomId().replace(CUSTOM_ID_PREFIXES__LEFT_ARROW, ""));
      if (ObjectUtils.isEmpty(interaction)) {
        return Mono.empty();
      }
      interaction.slideLeft();
      deferEdit(event, interaction);
    }

    if (event.getCustomId().startsWith(CUSTOM_ID_PREFIXES__STOCK)) {
      CommonInteraction interaction =
          (CommonInteraction)
              interactions.get(event.getCustomId().replace(CUSTOM_ID_PREFIXES__STOCK, ""));
      if (ObjectUtils.isEmpty(interaction)) {
        return Mono.empty();
      }
      interaction.updateState(InteractionState.STOCK);
      deferEdit(event, interaction);
    }

    if (event.getCustomId().startsWith(CUSTOM_ID_PREFIXES__MOD_ANALYSIS)) {
      CommonInteraction interaction =
          (CommonInteraction)
              interactions.get(event.getCustomId().replace(CUSTOM_ID_PREFIXES__MOD_ANALYSIS, ""));
      if (ObjectUtils.isEmpty(interaction)) {
        return Mono.empty();
      }
      interaction.updateState(InteractionState.MOD_ANALYSIS);
      deferEdit(event, interaction);
    }

    if (event.getCustomId().startsWith(CUSTOM_ID_PREFIXES__MOD)) {
      CommonInteraction interaction =
          (CommonInteraction)
              interactions.get(event.getCustomId().replace(CUSTOM_ID_PREFIXES__MOD, ""));
      if (ObjectUtils.isEmpty(interaction)) {
        return Mono.empty();
      }
      interaction.updateState(InteractionState.MOD);
      deferEdit(event, interaction);
    }

    if (event.getCustomId().startsWith(CUSTOM_ID_PREFIXES__SPEC)) {
      CommonInteraction interaction =
          (CommonInteraction)
              interactions.get(event.getCustomId().replace(CUSTOM_ID_PREFIXES__SPEC, ""));
      if (ObjectUtils.isEmpty(interaction)) {
        return Mono.empty();
      }
      interaction.updateState(InteractionState.EQUIP);
      deferEdit(event, interaction);
    }

    if (event.getCustomId().startsWith(CUSTOM_ID_PREFIXES__ANALYSIS)) {
      CommonInteraction interaction =
          (CommonInteraction)
              interactions.get(event.getCustomId().replace(CUSTOM_ID_PREFIXES__ANALYSIS, ""));
      if (ObjectUtils.isEmpty(interaction)) {
        return Mono.empty();
      }
      interaction.updateState(InteractionState.STOCK_ANALYSIS);
      deferEdit(event, interaction);
    }

    return Mono.empty();
  }

  private void deferEdit(ButtonInteractionEvent event, BaseSlideInteraction interaction) {
    event
        .deferEdit()
        .then(
            event
                .editReply()
                .withEmbeds(interaction.getContent())
                .withComponentsOrNull(interaction.getButtons()))
        .subscribe();
  }
}
