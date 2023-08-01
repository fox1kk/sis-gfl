package ru.gf.sis.service;

import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.Interaction;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import ru.gf.sis.dto.StoredInfo;
import ru.gf.sis.dto.lang.Language;
import ru.gf.sis.interaction.SimpleSlideInteraction;
import ru.gf.sis.repository.EquipmentRepository;
import ru.gf.sis.repository.TDollRepository;
import ru.gf.sis.utils.Utils;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class TimerProcessService {
  private final TDollRepository tDollRepository;
  private final EquipmentRepository equipmentRepository;

  public static final String REGISTER_COMMANDS__TIMER = "timer";
  public static final String MESSAGES__NOT_FOUND = "not_found";

  public SimpleSlideInteraction processTDoll(Interaction interaction, Language language) {
    return getSimpleSlideInteraction(
        tDollRepository.findByProductTime(Utils.parseProductionTime(getTimer(interaction))),
        interaction.getId().asString(),
        language);
  }

  public SimpleSlideInteraction processEquip(Interaction interaction, Language language) {
    return getSimpleSlideInteraction(
        equipmentRepository.findByProductTime(Utils.parseProductionTime(getTimer(interaction))),
        interaction.getId().asString(),
        language);
  }

  private String getTimer(Interaction interaction) {
    return interaction
        .getCommandInteraction()
        .get()
        .getOption(REGISTER_COMMANDS__TIMER)
        .flatMap(ApplicationCommandInteractionOption::getValue)
        .map(ApplicationCommandInteractionOptionValue::asString)
        .orElse("");
  }

  private SimpleSlideInteraction getSimpleSlideInteraction(
      List<? extends StoredInfo> entities, String interactionId, Language language) {
    if (ObjectUtils.isEmpty(entities)) {
      return SimpleSlideInteraction.builder()
          .error(TranslationService.getVariation(MESSAGES__NOT_FOUND, language))
          .build();
    }

    return SimpleSlideInteraction.builder()
        .interactionId(interactionId)
        .language(language)
        .entities(entities)
        .currentPage(1)
        .build();
  }
}
