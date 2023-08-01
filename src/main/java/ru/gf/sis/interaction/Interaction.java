package ru.gf.sis.interaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.gf.sis.dto.lang.Language;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class Interaction {
  protected String interactionId;
  protected Language language;
  protected String error;
  @Builder.Default protected LocalDateTime created = LocalDateTime.now();
}
