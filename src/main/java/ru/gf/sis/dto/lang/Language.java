package ru.gf.sis.dto.lang;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Language {
  ENGLISH("ENGLISH"),
  RUSSIAN("RUSSIAN");

  private final String lang;
}
