package ru.gf.sis.service;

import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.Interaction;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import ru.gf.sis.dto.*;
import ru.gf.sis.dto.lang.Language;
import ru.gf.sis.interaction.CommonInteraction;
import ru.gf.sis.interaction.InteractionState;
import ru.gf.sis.interaction.SelectMenuInteraction;
import ru.gf.sis.repository.EquipmentRepository;
import ru.gf.sis.repository.HocRepository;
import ru.gf.sis.repository.SFBossRepository;
import ru.gf.sis.repository.TDollRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class InfoProcessService {
  private final TDollRepository tDollRepository;
  private final EquipmentRepository equipmentRepository;
  private final HocRepository hocRepository;
  private final SFBossRepository sfBossRepository;

  public static final String MESSAGES__NOT_FOUND = "not_found";
  public static final String MESSAGES__TOO_SHORT = "too_short";
  public static final String MESSAGES__TOO_MANY_VARIATIONS = "too_many_variations";
  public static final String REGISTER_COMMANDS__REQUEST = "request";
  public static final String REGISTER_COMMANDS__ID = "id";
  public static final String REGISTER_COMMANDS__EXTRA = "extra";

  public SelectMenuInteraction processInfoSelectMenu(Interaction interaction, Language language) {
    ApplicationCommandInteraction acid = interaction.getCommandInteraction().get();

    String inputSearch =
        acid.getOption(REGISTER_COMMANDS__REQUEST)
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .orElse("");

    inputSearch = inputSearch.toLowerCase();

    if (inputSearch.length() < 2) {
      return SelectMenuInteraction.builder()
          .error(TranslationService.getVariation(MESSAGES__TOO_SHORT, language))
          .build();
    }

    List<StoredInfo> entities = new ArrayList<>(tDollRepository.findByAliases(inputSearch));
    entities.addAll(equipmentRepository.findByAliases(inputSearch));
    entities.addAll(hocRepository.findByAliases(inputSearch));
    entities.addAll(sfBossRepository.findByAliases(inputSearch));
    if (ObjectUtils.isEmpty(entities)) {
      entities.addAll(tDollRepository.findByGunNameRegex(inputSearch));
      entities.addAll(
          equipmentRepository.findByNameAndTypeFairy(
              inputSearch.replaceAll("(?i)fairy", "") + ".*Fairy"));
      entities.addAll(hocRepository.findByName(inputSearch));
      entities.addAll(sfBossRepository.findByNameRegEx(inputSearch));
    }

    return checkEntitiesAndReturn(entities, interaction.getId().asString(), language);
  }

  public SelectMenuInteraction processHoc(Interaction interaction, Language language) {
    ApplicationCommandInteraction acid = interaction.getCommandInteraction().get();

    String inputSearch =
        acid.getOption(REGISTER_COMMANDS__REQUEST)
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .orElse("");

    inputSearch = inputSearch.toLowerCase();

    if (ObjectUtils.isEmpty(inputSearch)) {
      List<StoredInfo> entities = new ArrayList<>(hocRepository.findAll());

      return SelectMenuInteraction.builder()
          .interactionId(interaction.getId().asString())
          .language(language)
          .entities(entities)
          .build();
    }

    if (inputSearch.length() < 2) {
      return SelectMenuInteraction.builder()
          .error(TranslationService.getVariation(MESSAGES__TOO_SHORT, language))
          .build();
    }

    List<StoredInfo> entities = new ArrayList<>(hocRepository.findByAliases(inputSearch));
    if (ObjectUtils.isEmpty(entities)) {
      entities.addAll(hocRepository.findByName(inputSearch));
    }

    return checkEntitiesAndReturn(entities, interaction.getId().asString(), language);
  }

  public SelectMenuInteraction processSf(Interaction interaction, Language language) {
    ApplicationCommandInteraction acid = interaction.getCommandInteraction().get();

    String inputSearch =
        acid.getOption(REGISTER_COMMANDS__REQUEST)
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .orElse("");

    inputSearch = inputSearch.toLowerCase();

    if (inputSearch.length() < 2) {
      return SelectMenuInteraction.builder()
          .error(TranslationService.getVariation(MESSAGES__TOO_SHORT, language))
          .build();
    }

    List<StoredInfo> entities = new ArrayList<>(sfBossRepository.findByAliases(inputSearch));
    if (ObjectUtils.isEmpty(entities)) {
      entities.addAll(sfBossRepository.findByNameRegEx(inputSearch));
    }

    return checkEntitiesAndReturn(entities, interaction.getId().asString(), language);
  }

  public CommonInteraction processById(Interaction interaction, Language language) {
    ApplicationCommandInteraction acid = interaction.getCommandInteraction().get();

    Long id =
        acid.getOption(REGISTER_COMMANDS__ID)
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .orElse(-1L);

    Boolean extra =
        acid.getOption(REGISTER_COMMANDS__EXTRA)
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asBoolean)
            .orElse(false);

    TDoll tDoll = tDollRepository.findByDollId(extra ? "Extra " + id : id.toString());

    if (ObjectUtils.isEmpty(tDoll)) {
      return CommonInteraction.builder()
          .error(TranslationService.getVariation(MESSAGES__NOT_FOUND, language))
          .build();
    }

    return processInfo(tDoll, interaction.getId().asString(), language);
  }

  public CommonInteraction processInfo(
      StoredInfo storedInfo, String interactionId, Language language) {
    return CommonInteraction.builder()
        .interactionId(interactionId)
        .language(language)
        .state(InteractionState.STOCK)
        .entity(storedInfo)
        .currentPage(1)
        .build();
  }

  private SelectMenuInteraction checkEntitiesAndReturn(
      List<StoredInfo> entities, String interactionId, Language language) {
    if (ObjectUtils.isEmpty(entities)) {
      return SelectMenuInteraction.builder()
          .error(TranslationService.getVariation(MESSAGES__NOT_FOUND, language))
          .build();
    }

    if (entities.size() > 25) {
      return SelectMenuInteraction.builder()
          .error(TranslationService.getVariation(MESSAGES__TOO_MANY_VARIATIONS, language))
          .build();
    }

    return SelectMenuInteraction.builder()
        .interactionId(interactionId)
        .language(language)
        .entities(entities)
        .build();
  }
}
