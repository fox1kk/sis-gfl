package ru.gf.sis.dto;

import discord4j.core.spec.EmbedCreateSpec;
import ru.gf.sis.dto.lang.Language;

public interface FullEmbed {
    default EmbedCreateSpec fillFullEmbed(Language language, boolean mod) {
        return null;
    }
}
