package ru.gf.sis.dto;

import lombok.Data;
import ru.gf.sis.dto.lang.LanguageVariation;

@Data
public class SkillWrapper {
  private LanguageVariation skillName = new LanguageVariation();
  private LanguageVariation skillDescription = new LanguageVariation();
  private String initialCd = "";
  private String cd = "";
}
