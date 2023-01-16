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
public class Hoc extends StoredInfo {
  private List<String> aliases = new ArrayList<>();
  private String name = "";
  private String type = "";
  private LanguageVariation firstSkillName = new LanguageVariation();
  private LanguageVariation firstSkillDescription = new LanguageVariation();
  private LanguageVariation secondSkillName = new LanguageVariation();
  private LanguageVariation secondSkillDescription = new LanguageVariation();
  private LanguageVariation thirdSkillName = new LanguageVariation();
  private LanguageVariation thirdSkillDescription = new LanguageVariation();
  private String shellingDamage = "";
  private String shellingAccuracy = "";
  private String defensePiercing = "";
  private String reloadSpeed = "";
  private String imageUrl = "";

  public void updateFirstSkillName(String language, String translate) {
    this.firstSkillName.updateVariation(language, translate);
  }

  public void updateFirstSkillDescription(String language, String translate) {
    this.firstSkillDescription.updateVariation(language, translate);
  }

  public void updateSecondSkillName(String language, String translate) {
    this.secondSkillName.updateVariation(language, translate);
  }

  public void updateSecondSkillDescription(String language, String translate) {
    this.secondSkillDescription.updateVariation(language, translate);
  }

  public void updateThirdSkillName(String language, String translate) {
    this.thirdSkillName.updateVariation(language, translate);
  }

  public void updateThirdSkillDescription(String language, String translate) {
    this.thirdSkillDescription.updateVariation(language, translate);
  }

  @Override
  public EmbedCreateSpec fillSmallEmbed(Language language, String footer) {
    return Utils.fillHocEmbedSpec(this, language, footer);
  }

  @Override
  public EmbedCreateSpec fillFullEmbed(Language language, boolean mod) {
    return Utils.fillHocEmbedSpec(this, language, null);
  }

  @Override
  public boolean isMod() {
    return false;
  }

  @Override
  public String getStarRarity(Boolean mod) {
    return null;
  }

  @Override
  public String getName() {
    return name;
  }
}
