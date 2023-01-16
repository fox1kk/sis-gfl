package ru.gf.sis.dto.analysis;

import discord4j.core.spec.EmbedCreateSpec;
import lombok.*;
import ru.gf.sis.dto.StoredInfo;
import ru.gf.sis.utils.Utils;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Analytics extends StoredInfo implements AnalysisEmbed {
  private String dollId;
  private Boolean mod;
  private String name;
  private String imageUrl;
  private String pros;
  private String cons;
  private String status;
  private String urlParams;
  private List<String> splitAnalysis;
  private String recommendation;

  @Override
  public EmbedCreateSpec fillAnalysisEmbed(Integer analysisCounter) {
    return Utils.fillAnalysis(this, analysisCounter);
  }

  @Override
  public boolean isMod() {
    return mod;
  }

  @Override
  public String getStarRarity(Boolean mod) {
    return null;
  }

  @Override
  public String getName() {
    return name;
  }
}
