package ru.gf.sis.dto.wiki;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GetParse {
    private Parse parse;

    @Data
    public static class Parse {
        private WikiText wikitext;
    }

    @Data
    public static class WikiText {
        @JsonProperty("*")
        private String text;
    }
}
