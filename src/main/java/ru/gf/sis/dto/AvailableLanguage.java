package ru.gf.sis.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class AvailableLanguage {
  @Id private String id;
  private String language;
  private String googleTranslateShortcut;
  private String serverSetShortcut;
  private boolean active;
  private String displayName;
}
