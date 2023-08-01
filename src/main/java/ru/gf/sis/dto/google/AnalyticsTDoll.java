package ru.gf.sis.dto.google;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public class AnalyticsTDoll extends AnalyticsBase {
  private Boolean isMod;
  private String pros;
  private String cons;
  private String status;
}
