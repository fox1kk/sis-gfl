package ru.gf.sis.dto.google;

import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public class AnalyticsFairy extends AnalyticsBase {
  private String recommendations;
}
