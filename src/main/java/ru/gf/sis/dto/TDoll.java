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
public class TDoll extends StoredInfo {
  private List<String> aliases = new ArrayList<>();
  private String gunName = "";
  private String type = "";
  private Integer rarity = 0;
  private String productTime = "";
  private String dollId = "";
  private String buff = "";
  private String buffTo = "";
  private LanguageVariation buffDescription = new LanguageVariation();
  private LanguageVariation skillName = new LanguageVariation();
  private LanguageVariation skillDescription = new LanguageVariation();
  private String initialCd = "";
  private String cd = "";
  private String illustrator = "";
  private String voiceActor = "";
  private String imageUrl = "";

  private Boolean isMod = false;
  private Integer modRarity = 0;
  private String modBuff = "";
  private String modBuffTo = "";
  private LanguageVariation modBuffDescription = new LanguageVariation();
  private LanguageVariation modSkillName = new LanguageVariation();
  private LanguageVariation modSkillDescription = new LanguageVariation();
  private String modSkillInitialCd = "";
  private String modSkillCd = "";
  private LanguageVariation modSecondSkillName = new LanguageVariation();
  private LanguageVariation modSecondSkillDescription = new LanguageVariation();
  private String modSecondSkillInitialCd = "";
  private String modSecondSkillCd = "";
  private String modImageUrl = "";

  private Boolean additionalSkillField = false;
  private LanguageVariation additionalSkillFieldName = new LanguageVariation();
  private LanguageVariation additionalSkillFieldDescription = new LanguageVariation();

  public void updateBuffDescription(String language, String translate) {
    this.buffDescription.updateVariation(language, translate);
  }

  public void updateSkillName(String language, String translate) {
    this.skillName.updateVariation(language, translate);
  }

  public void updateSkillDescription(String language, String translate) {
    this.skillDescription.updateVariation(language, translate);
  }

  public void updateModBuffDescription(String language, String translate) {
    this.modBuffDescription.updateVariation(language, translate);
  }

  public void updateModSkillName(String language, String translate) {
    this.modSkillName.updateVariation(language, translate);
  }

  public void updateModSkillDescription(String language, String translate) {
    this.modSkillDescription.updateVariation(language, translate);
  }

  public void updateModSecondSkillName(String language, String translate) {
    this.modSecondSkillName.updateVariation(language, translate);
  }

  public void updateModSecondSkillDescription(String language, String translate) {
    this.modSecondSkillDescription.updateVariation(language, translate);
  }

  @Override
  public EmbedCreateSpec fillSmallEmbed(Language language, String footer) {
    return Utils.fillSmallTDollEmbedSpec(this, language, footer);
  }

  @Override
  public EmbedCreateSpec fillFullEmbed(Language language, boolean mod) {
    return Utils.fillTDollEmbedSpec(this, mod, language);
  }

  @Override
  public boolean isMod() {
    return isMod;
  }

  @Override
  public String getStarRarity(Boolean mod) {
    StringBuilder stars = new StringBuilder();
    Integer rarity = (mod && isMod) ? this.modRarity : this.rarity;
    if (rarity == 0) { // extra
      stars = new StringBuilder("★");
    } else {
      stars.append("★".repeat(Math.max(0, rarity)));
    }
    return stars.toString();
  }

  @Override
  public String getName() {
    return gunName;
  }
}
