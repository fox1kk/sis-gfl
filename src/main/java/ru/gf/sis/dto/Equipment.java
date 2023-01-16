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
public class Equipment extends StoredInfo {
  private List<String> aliases = new ArrayList<>();
  private String name;
  private String type = "";
  private Integer rarity = 0;
  private String productTime = "";
  private LanguageVariation stats = new LanguageVariation();
  private String imageUrl = "";

  private Boolean isFairy = false;
  private LanguageVariation skillName = new LanguageVariation();
  private LanguageVariation skillDescription = new LanguageVariation();

  public void updateSkillName(String language, String translate) {
    this.skillName.updateVariation(language, translate);
  }

  public void updateSkillDescription(String language, String translate) {
    this.skillDescription.updateVariation(language, translate);
  }

  public void updateStatDescription(String language, String translate) {
    this.stats.updateVariation(language, translate);
  }

  public Boolean isFairy() {
    return isFairy;
  }

  @Override
  public EmbedCreateSpec fillSmallEmbed(Language language, String footer) {
    return Utils.fillEquipEmbedSpec(this, language, footer);
  }

  @Override
  public EmbedCreateSpec fillFullEmbed(Language language, boolean mod) {
    return Utils.fillEquipEmbedSpec(this, language, null);
  }

  @Override
  public boolean isMod() {
    return false;
  }

  @Override
  public String getStarRarity(Boolean mod) {
    StringBuilder stars = new StringBuilder();
    if (this.rarity == 0) { // fairy
      stars = new StringBuilder();
    } else {
      for (int i = 0; i < this.rarity; i++) {
        stars.append("★");
      }
    }
    return stars.toString();
  }

  @Override
  public String getName() {
    return name;
  }
}
