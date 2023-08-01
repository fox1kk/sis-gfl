package ru.gf.sis.dto;

import discord4j.core.spec.EmbedCreateSpec;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.gf.sis.dto.lang.Language;
import ru.gf.sis.dto.lang.LanguageVariation;
import ru.gf.sis.utils.Utils;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class SFBoss extends StoredInfo {
  private static final String SF_TYPE = "Sangvis Ferri";

  private List<String> aliases = new ArrayList<>();
  private String name = "";
  private Integer rarity = 0;
  private Integer cost = 0;
  private LanguageVariation type =
      new LanguageVariation(LanguageVariation.DEFAULT_LANGUAGE, SF_TYPE);
  private String buff = "";
  private LanguageVariation buffDescription = new LanguageVariation();

  private SFStats sfStats = new SFStats();

  private SkillWrapper skill1 = new SkillWrapper();
  private SkillWrapper skill2 = new SkillWrapper();
  private SkillWrapper skill3 = new SkillWrapper();
  private SkillWrapper skill4 = new SkillWrapper();

  private String illustrator = "";
  private String voiceActor = "";

  private String imageUrl = "";

  @Override
  public EmbedCreateSpec fillFullEmbed(Language language, boolean mod) {
    return Utils.fillSFBossEmbedSpec(this, language);
  }

  @Override
  public boolean isMod() {
    return false;
  }

  @Override
  public String getStarRarity(Boolean mod) {
    StringBuilder stars = new StringBuilder();
    if (rarity == 0) { // extra
      stars = new StringBuilder("★");
    } else {
      stars.append("★".repeat(Math.max(0, rarity)));
    }
    return stars.toString();
  }

  @Override
  public String getName() {
    return name;
  }
}
