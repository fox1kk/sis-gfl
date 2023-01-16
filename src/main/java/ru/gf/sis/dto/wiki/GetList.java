package ru.gf.sis.dto.wiki;

import lombok.Data;

import java.util.List;

@Data
public class GetList {
    private Query query;

    @Data
    public static class Query {
        private List<CategoryMember> categorymembers;
    }

    @Data
    public static class CategoryMember {
        Integer pageid;
        Integer ns;
        String title;
    }
}
