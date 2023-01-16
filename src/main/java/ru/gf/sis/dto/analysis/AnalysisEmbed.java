package ru.gf.sis.dto.analysis;

import discord4j.core.spec.EmbedCreateSpec;

public interface AnalysisEmbed {
  EmbedCreateSpec fillAnalysisEmbed(Integer analysisCounter);
}
