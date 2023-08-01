package ru.gf.sis.dto.google;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public abstract class AnalyticsBase {
  private Integer startRow;
  private Integer endRow;
  private String name;
  private List<String> analysis;
  private String urlParams;
}
