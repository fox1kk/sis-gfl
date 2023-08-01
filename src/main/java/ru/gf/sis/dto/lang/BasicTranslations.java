package ru.gf.sis.dto.lang;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BasicTranslations {
  @Id private String id;
  private String name;
  private LanguageVariation translation;

  public void updateTranslation(String language, String translate) {
    this.translation.updateVariation(language, translate);
  }
}
