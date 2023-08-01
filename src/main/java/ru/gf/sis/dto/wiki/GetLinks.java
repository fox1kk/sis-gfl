package ru.gf.sis.dto.wiki;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class GetLinks {
    private ParseLinks parse;

    @Data
    public static class ParseLinks {
        private List<Links> links;
    }

    @Data
    public static class Links {
        @JsonProperty("*")
        private String link;
    }
}
