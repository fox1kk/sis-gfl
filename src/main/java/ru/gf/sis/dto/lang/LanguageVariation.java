package ru.gf.sis.dto.lang;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class LanguageVariation {
  public static final String DEFAULT_LANGUAGE = Language.ENGLISH.getLang();
  private HashMap<String, String> variation = new HashMap<>();

  public LanguageVariation(String language, String translate) {
    this.updateVariation(language, translate);
  }

  public String getVariation(String language) {
    String translate = variation.get(language);
    return translate == null ? variation.get(DEFAULT_LANGUAGE) : variation.get(language);
  }

  public String getVariation(Language language) {
    String translate = variation.get(language.getLang());
    return translate == null ? variation.get(DEFAULT_LANGUAGE) : variation.get(language.getLang());
  }

  public void updateVariation(String language, String translate) {
    if (variation.get(language) != null) {
      for (Map.Entry<String, String> entry : this.variation.entrySet()) {
        String key = entry.getKey();
        if (key.equals(language)) {
          entry.setValue(translate);
        }
      }
    } else {
      this.variation.put(language, translate);
    }
  }

  public boolean isEmpty() {
    return variation.isEmpty();
  }
}
