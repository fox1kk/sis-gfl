package ru.gf.sis.dto;

import discord4j.core.spec.EmbedCreateSpec;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.gf.sis.dto.lang.Language;
import ru.gf.sis.dto.lang.LanguageVariation;
import ru.gf.sis.utils.Utils;

@EqualsAndHashCode(callSuper = true)
@Data
public class SpecialEquipment extends StoredInfo {
  private String name = "";
  private String imageUrl = "";
  private LanguageVariation stats = new LanguageVariation();
  private LanguageVariation obtainable = new LanguageVariation();

  public void updateStats(String language, String translate) {
    this.stats.updateVariation(language, translate);
  }

  public void updateObtainable(String language, String translate) {
    this.obtainable.updateVariation(language, translate);
  }

  @Override
  public EmbedCreateSpec fillSmallEmbed(Language language, String footer) {
    return Utils.fillSpecialEquipmentEmbedSpec(this, language, footer);
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
