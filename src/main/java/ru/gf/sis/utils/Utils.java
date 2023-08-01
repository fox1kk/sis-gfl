package ru.gf.sis.utils;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Color;
import org.springframework.util.ObjectUtils;
import ru.gf.sis.dto.*;
import ru.gf.sis.dto.analysis.Analytics;
import ru.gf.sis.dto.lang.Language;
import ru.gf.sis.service.TranslationService;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.gf.sis.service.analysis.AnalysisService.GOOGLE_DOC_URL;

public class Utils {

  public static final String EMBED__GUN_NAME = "embed_gun_name";
  public static final String EMBED__TYPE = "embed_type";
  public static final String EMBED__TDOLL_TYPE = "embed_tdoll_type";
  public static final String EMBED__RARITY = "embed_rarity";
  public static final String EMBED__PRODUCTION_TIME = "embed_production_time";
  public static final String EMBED__DOLL_ID = "embed_doll_id";
  public static final String EMBED__BUFF = "embed_buff";
  public static final String EMBED__CD = "embed_cd_format_row";
  public static final String EMBED__SKILL = "embed_skill";
  public static final String EMBED__ILLUSTRATOR = "embed_illustrator";
  public static final String EMBED__VOICE_ACTOR = "embed_voice_actor";
  public static final String EMBED__BUFF_TO = "embed_buff_to";
  public static final String EMBED__NAME = "embed_name";
  public static final String EMBED__STATS = "embed_stats";
  public static final String EMBED__HOC_DAMAGE = "embed_hoc_damage";
  public static final String EMBED__HOC_PENETRATION = "embed_hoc_penetration";
  public static final String EMBED__HOC_ACCURACY = "embed_hoc_accuracy";
  public static final String EMBED__HOC_RELOAD = "embed_hoc_reload";
  public static final String EMBED__OBTAINABLE = "embed_obtainable";
  public static final String EMBED__INITIAL_RARITY = "embed_initial_rarity";
  public static final String EMBED__SF_COST = "embed_sf_cost";
  public static final String EMBED__SF_DAMAGE = "embed_sf_damage";
  public static final String EMBED__SF_ACCURACY = "embed_sf_accuracy";
  public static final String EMBED__SF_EVASION = "embed_sf_evasion";
  public static final String EMBED__SF_CRITICAL_CH = "embed_sf_critical_сh";
  public static final String EMBED__SF_ARMOR_PEN = "embed_sf_armor_pen";
  public static final String EMBED__SF_ROF = "embed_sf_rof";
  public static final String EMBED__SF_ARMOR = "embed_sf_armor";
  public static final String EMBED__SF_HP = "embed_sf_hp";
  public static final String EMBED__SF_CRITICAL_DMG = "embed_sf_critical_dmg";
  public static final String EMBED__SF_MOVE_SPEED = "embed_sf_move_speed";

  public static EmbedCreateSpec fillTDollEmbedSpec(TDoll tDoll, Boolean mod, Language language) {
    EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder();
    builder.color(Color.SEA_GREEN);
    if (!mod) {
      if (!ObjectUtils.isEmpty(tDoll.getImageUrl())) {
        builder.image(tDoll.getImageUrl());
      }
    } else {
      if (!ObjectUtils.isEmpty(tDoll.getModImageUrl())) {
        builder.image(tDoll.getModImageUrl());
      }
    }
    builder.addField(
        TranslationService.getVariation(EMBED__GUN_NAME, language), tDoll.getGunName(), true);
    builder.addField(
        TranslationService.getVariation(EMBED__TDOLL_TYPE, language), tDoll.getType(), true);
    builder.addField(
        TranslationService.getVariation(EMBED__RARITY, language), tDoll.getStarRarity(mod), true);
    builder.addField(
        TranslationService.getVariation(EMBED__PRODUCTION_TIME, language),
        tDoll.getProductTime(),
        true);
    builder.addField(
        TranslationService.getVariation(EMBED__DOLL_ID, language), tDoll.getDollId(), false);
    String buffTo =
        !mod
            ? tDoll.getBuffTo()
            : (tDoll.getModBuffTo().isEmpty() || tDoll.getModBuffTo() == null)
                ? tDoll.getBuffTo()
                : tDoll.getModBuffTo();
    builder.addField(
        TranslationService.getVariation(EMBED__BUFF, language),
        (!mod
                ? tDoll.getBuff()
                : (tDoll.getModBuff() != null && !tDoll.getModBuff().isEmpty()
                    ? tDoll.getModBuff()
                    : tDoll.getBuff()))
            + "\n\n"
            + (!mod
                ? tDoll.getBuffDescription().getVariation(language)
                : tDoll.getModBuffDescription().getVariation(language))
            + " "
            + TranslationService.getVariation(EMBED__BUFF_TO, language)
            + " "
            + buffTo,
        false);

    StringBuilder sb = new StringBuilder();
    sb.append("**");
    sb.append(
        mod
            ? tDoll.getModSkillName().getVariation(language)
            : tDoll.getSkillName().getVariation(language));
    sb.append("**");
    if (!mod && !tDoll.getInitialCd().isEmpty() && !tDoll.getCd().isEmpty()) {
      sb.append(getSkillCdRow(tDoll.getInitialCd(), tDoll.getCd(), language));
    }
    if (mod && !tDoll.getModSkillInitialCd().isEmpty() && !tDoll.getModSkillCd().isEmpty()) {
      sb.append(getSkillCdRow(tDoll.getModSkillInitialCd(), tDoll.getModSkillCd(), language));
    }
    sb.append("\n");
    sb.append(
        mod
            ? tDoll.getModSkillDescription().getVariation(language)
            : tDoll.getSkillDescription().getVariation(language));
    builder.addField(TranslationService.getVariation(EMBED__SKILL, language), sb.toString(), false);

    if (mod) {
      sb = new StringBuilder();
      sb.append("**");
      sb.append(tDoll.getModSecondSkillName().getVariation(language));
      sb.append("**");
      if (!tDoll.getModSecondSkillCd().isEmpty() && !tDoll.getModSecondSkillInitialCd().isEmpty()) {
        sb.append(
                getSkillCdRow(
                        tDoll.getModSecondSkillInitialCd(), tDoll.getModSecondSkillCd(), language));
      }
      sb.append("\n");
      sb.append(tDoll.getModSecondSkillDescription().getVariation(language));
      builder.addField(TranslationService.getVariation(EMBED__SKILL, language), sb.toString(), false);
    }

    if (tDoll.getAdditionalSkillField()) {
      builder.addField(
          tDoll.getAdditionalSkillFieldName().getVariation(language),
          tDoll.getAdditionalSkillFieldDescription().getVariation(language),
          false);
    }
    builder.addField(
        TranslationService.getVariation(EMBED__ILLUSTRATOR, language),
        tDoll.getIllustrator(),
        true);
    builder.addField(
        TranslationService.getVariation(EMBED__VOICE_ACTOR, language), tDoll.getVoiceActor(), true);

    return builder.build();
  }

  public static EmbedCreateSpec fillSmallTDollEmbedSpec(
      TDoll tDoll, Language language, String footer) {
    return EmbedCreateSpec.builder()
        .color(Color.SEA_GREEN)
        .addField(
            TranslationService.getVariation(EMBED__GUN_NAME, language), tDoll.getGunName(), true)
        .addField(
            TranslationService.getVariation(EMBED__TDOLL_TYPE, language), tDoll.getType(), true)
        .addField(
            TranslationService.getVariation(EMBED__RARITY, language),
            tDoll.getStarRarity(false),
            true)
        .addField(
            TranslationService.getVariation(EMBED__PRODUCTION_TIME, language),
            tDoll.getProductTime(),
            true)
        .image(
            ObjectUtils.isEmpty(tDoll.getImageUrl())
                ? Possible.absent()
                : Possible.of(tDoll.getImageUrl()))
        .footer(footer, null)
        .build();
  }

  public static EmbedCreateSpec fillHocEmbedSpec(Hoc hoc, Language language, String footer) {
    EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder();
    builder.color(Color.SEA_GREEN);
    if (!ObjectUtils.isEmpty(hoc.getImageUrl())) {
      builder.image(hoc.getImageUrl());
    }

    builder.addField(TranslationService.getVariation(EMBED__NAME, language), hoc.getName(), true);
    builder.addField(TranslationService.getVariation(EMBED__TYPE, language), hoc.getType(), true);

    StringBuilder sb = new StringBuilder();
    sb.append(TranslationService.getVariation(EMBED__HOC_DAMAGE, language));
    sb.append(": ");
    sb.append(hoc.getShellingDamage());
    sb.append("\n");
    sb.append(TranslationService.getVariation(EMBED__HOC_PENETRATION, language));
    sb.append(": ");
    sb.append(hoc.getDefensePiercing());
    sb.append("\n");
    sb.append(TranslationService.getVariation(EMBED__HOC_ACCURACY, language));
    sb.append(": ");
    sb.append(hoc.getShellingAccuracy());
    sb.append("\n");
    sb.append(TranslationService.getVariation(EMBED__HOC_RELOAD, language));
    sb.append(": ");
    sb.append(hoc.getReloadSpeed());
    sb.append("\n");

    builder.addField(TranslationService.getVariation(EMBED__STATS, language), sb.toString(), true);

    sb = new StringBuilder();
    sb.append("**");
    sb.append(hoc.getFirstSkillName().getVariation(language));
    sb.append("**");
    sb.append("\n");
    sb.append(hoc.getFirstSkillDescription().getVariation(language));
    sb.append("\n\n");
    sb.append("**");
    sb.append(hoc.getSecondSkillName().getVariation(language));
    sb.append("**");
    sb.append("\n");
    sb.append(hoc.getSecondSkillDescription().getVariation(language));
    sb.append("\n\n");
    sb.append("**");
    sb.append(hoc.getThirdSkillName().getVariation(language));
    sb.append("**");
    sb.append("\n");
    sb.append(hoc.getThirdSkillDescription().getVariation(language));

    builder.addField(TranslationService.getVariation(EMBED__SKILL, language), sb.toString(), false);

    if (!ObjectUtils.isEmpty(footer)) {
      builder.footer(footer, null);
    }
    return builder.build();
  }

  public static EmbedCreateSpec fillSFBossEmbedSpec(SFBoss sfBoss, Language language) {
    EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder();
    builder.color(Color.SEA_GREEN);
    if (!ObjectUtils.isEmpty(sfBoss.getImageUrl())) {
      builder.image(sfBoss.getImageUrl());
    }

    builder.addField(
        TranslationService.getVariation(EMBED__NAME, language), sfBoss.getName(), true);
    builder.addField(
        TranslationService.getVariation(EMBED__TYPE, language),
        sfBoss.getType().getVariation(language),
        true);
    builder.addField(
        TranslationService.getVariation(EMBED__INITIAL_RARITY, language),
        sfBoss.getStarRarity(false),
        true);
    builder.addField(
        TranslationService.getVariation(EMBED__SF_COST, language),
        sfBoss.getCost().toString(),
        true);

    StringBuilder sb = new StringBuilder();
    if (sfBoss.getSfStats() != null
        && sfBoss.getSfStats().getDamage() != null
        && !sfBoss.getSfStats().getDamage().isEmpty()) {
      sb.append(TranslationService.getVariation(EMBED__SF_DAMAGE, language));
      sb.append(": ");
      sb.append(sfBoss.getSfStats().getDamage());
      sb.append("\n");
      sb.append(TranslationService.getVariation(EMBED__SF_ACCURACY, language));
      sb.append(": ");
      sb.append(sfBoss.getSfStats().getAccuracy());
      sb.append("\n");
      sb.append(TranslationService.getVariation(EMBED__SF_EVASION, language));
      sb.append(": ");
      sb.append(sfBoss.getSfStats().getEvasion());
      sb.append("\n");
      sb.append(TranslationService.getVariation(EMBED__SF_CRITICAL_CH, language));
      sb.append(": ");
      sb.append(sfBoss.getSfStats().getCriticalCh());
      sb.append("\n");
      sb.append(TranslationService.getVariation(EMBED__SF_ARMOR_PEN, language));
      sb.append(": ");
      sb.append(sfBoss.getSfStats().getArmorPen());
      sb.append("\n");
      sb.append(TranslationService.getVariation(EMBED__SF_ROF, language));
      sb.append(": ");
      sb.append(sfBoss.getSfStats().getRateOfFire());
      sb.append("\n");
      if (!sfBoss.getSfStats().getArmor().isEmpty()) {
        sb.append(TranslationService.getVariation(EMBED__SF_ARMOR, language));
        sb.append(": ");
        sb.append(sfBoss.getSfStats().getArmor());
        sb.append("\n");
      }
      sb.append(TranslationService.getVariation(EMBED__SF_HP, language));
      sb.append(": ");
      sb.append(sfBoss.getSfStats().getHp());
      sb.append("\n");
      sb.append(TranslationService.getVariation(EMBED__SF_CRITICAL_DMG, language));
      sb.append(": ");
      sb.append(sfBoss.getSfStats().getCriticalDmg());
      sb.append("\n");
      sb.append(TranslationService.getVariation(EMBED__SF_MOVE_SPEED, language));
      sb.append(": ");
      sb.append(sfBoss.getSfStats().getMoveSpeed());
      sb.append("\n");

      builder.addField(
          TranslationService.getVariation(EMBED__STATS, language), sb.toString(), false);
    }

    if (sfBoss.getBuff() != null && !sfBoss.getBuff().isEmpty()) {
      builder.addField(
          TranslationService.getVariation(EMBED__BUFF, language),
          sfBoss.getBuff() + "\n\n" + sfBoss.getBuffDescription().getVariation(language),
          false);
    }

    if (!sfBoss.getSkill1().getSkillName().isEmpty()) {
      sb = new StringBuilder();
      sb.append("**");
      sb.append(sfBoss.getSkill1().getSkillName().getVariation(language));
      sb.append("**");
      if (!sfBoss.getSkill1().getInitialCd().isEmpty() && !sfBoss.getSkill1().getCd().isEmpty()) {
        sb.append("\n");
        sb.append(
            getSFSkillCdRow(
                sfBoss.getSkill1().getInitialCd(), sfBoss.getSkill1().getCd(), language));
      }
      sb.append("\n");
      sb.append(sfBoss.getSkill1().getSkillDescription().getVariation(language));

      builder.addField(
          TranslationService.getVariation(EMBED__SKILL, language) + " 1", sb.toString(), false);
    }

    if (!sfBoss.getSkill2().getSkillName().isEmpty()) {
      sb = new StringBuilder();
      sb.append("**");
      sb.append(sfBoss.getSkill2().getSkillName().getVariation(language));
      sb.append("**");
      if (!sfBoss.getSkill2().getInitialCd().isEmpty() && !sfBoss.getSkill2().getCd().isEmpty()) {
        sb.append("\n");
        sb.append(
            getSFSkillCdRow(
                sfBoss.getSkill2().getInitialCd(), sfBoss.getSkill2().getCd(), language));
      }
      sb.append("\n");
      sb.append(sfBoss.getSkill2().getSkillDescription().getVariation(language));

      builder.addField(
          TranslationService.getVariation(EMBED__SKILL, language) + " 2", sb.toString(), false);
    }

    if (!sfBoss.getSkill3().getSkillName().isEmpty()) {
      sb = new StringBuilder();
      sb.append("**");
      sb.append(sfBoss.getSkill3().getSkillName().getVariation(language));
      sb.append("**");
      if (!sfBoss.getSkill3().getInitialCd().isEmpty() && !sfBoss.getSkill3().getCd().isEmpty()) {
        sb.append("\n");
        sb.append(
            getSFSkillCdRow(
                sfBoss.getSkill3().getInitialCd(), sfBoss.getSkill3().getCd(), language));
      }
      sb.append("\n");
      sb.append(sfBoss.getSkill3().getSkillDescription().getVariation(language));

      builder.addField(
          TranslationService.getVariation(EMBED__SKILL, language) + " 3", sb.toString(), false);
    }

    if (!sfBoss.getSkill4().getSkillName().isEmpty()) {
      sb = new StringBuilder();
      sb.append("**");
      sb.append(sfBoss.getSkill4().getSkillName().getVariation(language));
      sb.append("**");
      if (!sfBoss.getSkill4().getInitialCd().isEmpty() && !sfBoss.getSkill4().getCd().isEmpty()) {
        sb.append("\n");
        sb.append(
            getSFSkillCdRow(
                sfBoss.getSkill4().getInitialCd(), sfBoss.getSkill4().getCd(), language));
      }
      sb.append("\n");
      sb.append(sfBoss.getSkill4().getSkillDescription().getVariation(language));

      builder.addField(
          TranslationService.getVariation(EMBED__SKILL, language) + " 4", sb.toString(), false);
    }

    if (sfBoss.getIllustrator() != null && !sfBoss.getIllustrator().isEmpty()) {
      builder.addField(
          TranslationService.getVariation(EMBED__ILLUSTRATOR, language),
          sfBoss.getIllustrator(),
          true);
    }
    if (sfBoss.getVoiceActor() != null && !sfBoss.getVoiceActor().isEmpty()) {
      builder.addField(
          TranslationService.getVariation(EMBED__VOICE_ACTOR, language),
          sfBoss.getVoiceActor(),
          true);
    }

    return builder.build();
  }

  public static EmbedCreateSpec fillEquipEmbedSpec(
      Equipment equipment, Language language, String footer) {
    EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder();
    builder.color(Color.SEA_GREEN);
    if (!ObjectUtils.isEmpty(equipment.getImageUrl())) {
      if (!equipment.isFairy()) {
        builder.thumbnail(equipment.getImageUrl());
      } else {
        builder.image(equipment.getImageUrl());
      }
    }
    builder.addField(
        TranslationService.getVariation(EMBED__NAME, language), equipment.getName(), true);
    builder.addField(
        TranslationService.getVariation(EMBED__TYPE, language), equipment.getType(), true);

    if (!equipment.isFairy()) {
      builder.addField(
          TranslationService.getVariation(EMBED__RARITY, language),
          equipment.getStarRarity(false),
          true);
    }

    builder.addField(
        TranslationService.getVariation(EMBED__PRODUCTION_TIME, language),
        equipment.getProductTime(),
        !equipment.isFairy());

    builder.addField(
        TranslationService.getVariation(EMBED__STATS, language),
        equipment.getStats().getVariation(language),
        true);

    if (equipment.isFairy()) {
      builder.addField(
          TranslationService.getVariation(EMBED__SKILL, language),
          "**"
              + equipment.getSkillName().getVariation(language)
              + "**\n"
              + equipment.getSkillDescription().getVariation(language),
          false);
    }
    if (!ObjectUtils.isEmpty(footer)) {
      builder.footer(footer, null);
    }

    return builder.build();
  }

  public static EmbedCreateSpec fillSpecialEquipmentEmbedSpec(
      SpecialEquipment equipment, Language language, String footer) {
    EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder();
    builder.color(Color.SEA_GREEN);
    if (!equipment.getImageUrl().isEmpty()) {
      builder.thumbnail(equipment.getImageUrl());
    }

    builder.addField(
        TranslationService.getVariation(EMBED__NAME, language), equipment.getName(), true);

    builder.addField(TranslationService.getVariation(EMBED__RARITY, language), "★★★★★", true);

    builder.addField(
        TranslationService.getVariation(EMBED__STATS, language),
        equipment.getStats().getVariation(language),
        false);

    builder.addField(
        TranslationService.getVariation(EMBED__OBTAINABLE, language),
        equipment.getObtainable().getVariation(language),
        false);

    builder.footer(footer, null);

    return builder.build();
  }

  public static EmbedCreateSpec fillAnalysis(Analytics analysis, Integer analysisCounter) {
    EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder();

    builder.author(analysis.getName(), GOOGLE_DOC_URL + analysis.getUrlParams(), null);
    builder.description(GOOGLE_DOC_URL + analysis.getUrlParams());

    builder.color(Color.SEA_GREEN);

    if (!analysis.getImageUrl().isEmpty()) {
      builder.thumbnail(analysis.getImageUrl());
    }

    if (!ObjectUtils.isEmpty(analysis.getStatus())) {
      builder.addField("Status", analysis.getStatus(), false);
    }

    if (!ObjectUtils.isEmpty(analysis.getPros())) {
      builder.addField("Pros", analysis.getPros(), false);
    }

    if (!ObjectUtils.isEmpty(analysis.getCons())) {
      builder.addField("Cons", analysis.getCons(), false);
    }

    if (!ObjectUtils.isEmpty(analysis.getRecommendation())) {
      builder.addField("Recommendations", analysis.getRecommendation(), false);
    }

    if (!ObjectUtils.isEmpty(analysis.getSplitAnalysis())) {
      builder.addField("Analysis", analysis.getSplitAnalysis().get(analysisCounter), false);
    }

    builder.footer((analysisCounter + 1) + "/" + analysis.getSplitAnalysis().size(), null);

    return builder.build();
  }

  private static String getSkillCdRow(String initialCd, String cd, Language language) {
    StringBuilder row = new StringBuilder();
    String cd_row =
        String.format(TranslationService.getVariation(EMBED__CD, language), initialCd, cd);
    row.append("\t*(").append(cd_row).append(")*");
    return row.toString();
  }

  private static String getSFSkillCdRow(String initialCd, String cd, Language language) {
    StringBuilder row = new StringBuilder();
    String cd_row =
        String.format(TranslationService.getVariation(EMBED__CD, language), initialCd, cd);
    row.append("\t").append(cd_row);
    return row.toString();
  }

  public static String parseProductionTime(String input) {
    List<String> split = Arrays.asList(input.split(":"));

    if (split.size() < 1 || split.size() > 2) {
      return input; // Not a time
    } else if (split.size() == 2) {
      String hours =
          split.get(0).length() == 1 ? "0" + split.get(0) : split.get(0); // Add leading zeros
      String minutes =
          split.get(1).length() == 1 ? "0" + split.get(1) : split.get(1); // Add leading zeros
      return hours + ":" + minutes;
    } else {
      Pattern p = Pattern.compile("[^0-9]+");
      Matcher m = p.matcher(split.get(0));
      if (m.find()) {
        return input; // Not only a numbers
      } else {
        String toReturn = split.get(0).replaceFirst("^0+(?!$)", ""); // Remove leading zeros
        switch (toReturn.length()) {
          case 1:
            return "00:0" + toReturn;
          case 2:
            return "00:" + toReturn;
          case 3:
            return "0" + toReturn.charAt(0) + ":" + toReturn.charAt(1) + toReturn.charAt(2);
          case 4:
            return toReturn.charAt(0)
                + toReturn.charAt(1)
                + ":"
                + toReturn.charAt(2)
                + toReturn.charAt(3);
          default:
            return input; // Incorrect input
        }
      }
    }
  }
}
