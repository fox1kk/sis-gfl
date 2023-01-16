package ru.gf.sis.dto;

import discord4j.core.spec.EmbedCreateSpec;
import ru.gf.sis.dto.lang.Language;

public interface SmallEmbed {
    default EmbedCreateSpec fillSmallEmbed(Language language, String footer) {
        return null;
    }
}
