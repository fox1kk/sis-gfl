package ru.gf.sis.service;

import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.gf.sis.dto.exp.ExpBase;
import ru.gf.sis.dto.lang.Language;
import ru.gf.sis.repository.ExpFairyRepository;
import ru.gf.sis.repository.ExpRepository;
import ru.gf.sis.repository.ExpTDollRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@AllArgsConstructor
public class ExpProcessService {
  private final ExpTDollRepository expTDollRepository;
  private final ExpFairyRepository expFairyRepository;

  public static final Integer DEFAULT_REPORT_EXP = 3000;
  public static final Integer INCREASED_REPORT_EXP_LVL_START = 100;

  public static final String MESSAGES__EXP_INCORRECT_DATA = "exp_incorrect_data";
  public static final String MESSAGES__EXP_ROW = "exp_format_row";
  public static final String MESSAGES__EXP_WRONG_CURRENT_ROW = "exp_wrong_current_format_row";

  public static final String REGISTER_COMMANDS__CURRENT_LV = "current_lv";
  public static final String REGISTER_COMMANDS__CURRENT_EXP = "current_exp";
  public static final String REGISTER_COMMANDS__TARGET_LV = "target_lv";
  public static final String REGISTER_COMMANDS__OATH = "oath";

  public String process(ApplicationCommandInteraction acid, Language language) {
    Long currentLvl =
        acid.getOption(REGISTER_COMMANDS__CURRENT_LV)
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .orElse(-1L);

    Long currentExp =
        acid.getOption(REGISTER_COMMANDS__CURRENT_EXP)
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .orElse(-1L);

    Long targetLvl =
        acid.getOption(REGISTER_COMMANDS__TARGET_LV)
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .orElse(-1L);

    boolean oath =
        acid.getOption(REGISTER_COMMANDS__OATH)
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asBoolean)
            .orElse(false);

    if (!isExpArgsValid(currentLvl, currentExp, targetLvl, false)) {
      return TranslationService.getVariation(MESSAGES__EXP_INCORRECT_DATA, language);
    }

    return getResult(currentLvl, currentExp, targetLvl, oath, language, expTDollRepository);
  }

  public String processFairy(ApplicationCommandInteraction acid, Language language) {
    Long currentLvl =
        acid.getOption(REGISTER_COMMANDS__CURRENT_LV)
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .orElse(-1L);

    Long currentExp =
        acid.getOption(REGISTER_COMMANDS__CURRENT_EXP)
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .orElse(-1L);

    Long targetLvl =
        acid.getOption(REGISTER_COMMANDS__TARGET_LV)
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .orElse(-1L);

    if (!isExpArgsValid(currentLvl, currentExp, targetLvl, true)) {
      return TranslationService.getVariation(MESSAGES__EXP_INCORRECT_DATA, language);
    }

    return getResult(currentLvl, currentExp, targetLvl, false, language, expFairyRepository);
  }

  private String getResult(
      Long currentLvl,
      Long currentExp,
      Long targetLvl,
      Boolean oath,
      Language language,
      ExpRepository<? extends ExpBase> repository) {

    ExpBase exp = repository.findByLvl(currentLvl.intValue() + 1);

    if (exp.getExpToLvlUp() < currentExp) {
      return String.format(
          TranslationService.getVariation(MESSAGES__EXP_WRONG_CURRENT_ROW, language),
          currentLvl,
          exp.getExpToLvlUp());
    }

    List<Integer> lvls =
        IntStream.rangeClosed(currentLvl.intValue() + 1, targetLvl.intValue())
            .boxed()
            .collect(Collectors.toList());

    List<? extends ExpBase> lvlsExps = repository.findByLvlIn(lvls);
    ExpBase firstLvl = lvlsExps.get(0);
    firstLvl.setExpToLvlUp(
        BigDecimal.valueOf(firstLvl.getExpToLvlUp())
            .subtract(BigDecimal.valueOf(currentExp))
            .intValue());

    if (oath) {
      lvlsExps =
          lvlsExps.stream()
              .peek(
                  item -> {
                    if (item.getLvl() > INCREASED_REPORT_EXP_LVL_START) {
                      item.setExpToLvlUp(
                          BigDecimal.valueOf(item.getExpToLvlUp())
                              .divide(BigDecimal.valueOf(2), 0, RoundingMode.HALF_UP)
                              .intValue());
                    }
                  })
              .collect(Collectors.toList());
    }

    int sum = lvlsExps.stream().map(ExpBase::getExpToLvlUp).mapToInt(Integer::intValue).sum();

    BigDecimal reportsReq =
        BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(DEFAULT_REPORT_EXP), 0, RoundingMode.UP);

    return String.format(
        TranslationService.getVariation(MESSAGES__EXP_ROW, language),
        currentLvl,
        currentExp,
        targetLvl,
        reportsReq,
        sum);
  }

  private static boolean isExpArgsValid(
      Long currentLvl, Long currentExp, Long targetLvl, Boolean isFairy) {
    int limit = isFairy ? 100 : 120;

    if (currentLvl < 1 || currentLvl > (limit - 1)) {
      return false;
    }

    if (currentExp < 0) {
      return false;
    }

    if (targetLvl < 2 || targetLvl > limit) {
      return false;
    }

    return true;
  }
}
