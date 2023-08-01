package ru.gf.sis.service.analysis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.gf.sis.dto.google.AnalyticsFairy;
import ru.gf.sis.dto.google.AnalyticsTDoll;
import ru.gf.sis.dto.google.Page;
import ru.gf.sis.dto.google.SpreadSheetsList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ru.gf.sis.dto.google.Page.*;

public class GoogleDocApi {
  private final RestTemplate restTemplate;
  private static final String BASE_URL =
      "https://sheets.googleapis.com/v4/spreadsheets/10LJdksnM3zipOb72IneJD7WVp3765JYJEGg0LnodzDI/values/";
  private static final String NAMES_RANGE = "B1:B10000";
  private static final String PROS_RANGE = "O1:X10000";
  private static final String CONS_RANGE = "Y1:AH10000";
  private static final String ANALYSIS_RANGE = "AI1:AJ10000";
  private static final String ANALYSIS_FAIRY_RANGE = "O1:P10000";
  private static final String RECOMMENDATIONS_FAIRY_RANGE = "R1:R10000";

  @Value("${google.api.token}")
  private final String key;

  public GoogleDocApi(String token) {
    restTemplate = new RestTemplate();
    key = token;
  }

  public List<AnalyticsFairy> getGoogleDocFairyAnalysis() {
    return parseFairy();
  }

  public List<AnalyticsTDoll> getGoogleDocAnalysis() {
    List<AnalyticsTDoll> analyticsTDolls = new ArrayList<>();
    analyticsTDolls.addAll(parsePage(HG));
    analyticsTDolls.addAll(parsePage(RF));
    analyticsTDolls.addAll(parsePage(SMG));
    analyticsTDolls.addAll(parsePage(AR));
    analyticsTDolls.addAll(parsePage(MG));
    analyticsTDolls.addAll(parsePage(SG));
    return analyticsTDolls;
  }

  private List<AnalyticsFairy> parseFairy() {
    List<AnalyticsFairy> fairies =
        parseNamesAndRanges(FAIRY.getPage() + NAMES_RANGE).stream()
            .map(
                item ->
                    AnalyticsFairy.builder()
                        .startRow(item.getStartRow())
                        .endRow(item.getEndRow())
                        .name(item.getName())
                        .urlParams("#gid=" + FAIRY.getGid() + "&range=B" + (item.getStartRow() + 1))
                        .build())
            .collect(Collectors.toList());

    List<List<String>> analysis = parseFairyAnalysis();
    List<List<String>> recommendations = parseFairyRecommendations();

    fairies.forEach(
        item -> {
          String an = getAn(item.getStartRow(), item.getEndRow(), analysis);
          item.setAnalysis(getSplitAn(an));
          item.setRecommendations(
              recommendations
                  .get(item.getStartRow())
                  .get(0)
                  .replace("\r", "")
                  .replace("\n\n", "\n"));
        });

    return fairies;
  }

  private List<AnalyticsTDoll> parsePage(Page page) {
    List<AnalyticsTDoll> tDolls = parseNamesAndRanges(page.getPage() + NAMES_RANGE);
    List<List<String>> pros = parsePros(page);
    List<List<String>> cons = parseCons(page);
    List<List<String>> analysis = parseAnalysis(page);

    tDolls.forEach(
        item -> {
          item.setPros(replaceProsAndCons(item.getStartRow(), pros));
          item.setCons(replaceProsAndCons(item.getStartRow(), cons));

          String an = getAn(item.getStartRow(), item.getEndRow(), analysis);
          item.setAnalysis(getSplitAn(an));

          String status = "";
          if (an.toLowerCase().contains("not recommended")) {
            status = "Not recommended";
          } else if (an.toLowerCase().contains("highly recommended")) {
            status = "Highly Recommended";
          } else if (an.toLowerCase().contains("recommended")) {
            status = "Recommended";
          }

          if (an.toLowerCase().contains("optional")) {
            status = status.isEmpty() ? "Optional" : status + "\nOptional";
          }

          if (an.toLowerCase().contains("niche")) {
            status = status.isEmpty() ? "Niche" : status + "\nNiche";
          }

          if (status.isEmpty() && an.toLowerCase().contains("usable")) {
            status = "Usable";
          }

          if (status.isEmpty()) {
            status = "-";
          }
          item.setStatus(status);

          item.setUrlParams("#gid=" + page.getGid() + "&range=B" + (item.getStartRow() + 1));
        });

    return tDolls;
  }

  private List<List<String>> parseAnalysis(Page page) {
    return getValues(page.getPage() + ANALYSIS_RANGE);
  }

  private List<List<String>> parseFairyAnalysis() {
    return getValues(FAIRY.getPage() + ANALYSIS_FAIRY_RANGE);
  }

  private List<List<String>> parseFairyRecommendations() {
    return getValues(FAIRY.getPage() + RECOMMENDATIONS_FAIRY_RANGE);
  }

  private String replaceProsAndCons(Integer index, List<List<String>> prosOrCons) {
    if (prosOrCons.get(index).size() == 0) {
      return "";
    }
    return prosOrCons.get(index).get(0).replace("\r", "").replace("\n\n", "\n").replace("○", "*");
  }

  private List<List<String>> parsePros(Page page) {
    return getValues(page.getPage() + PROS_RANGE);
  }

  private List<List<String>> parseCons(Page page) {
    return getValues(page.getPage() + CONS_RANGE);
  }

  private List<List<String>> getValues(String pageAndRange) {
    UriComponentsBuilder builder =
        UriComponentsBuilder.fromHttpUrl(BASE_URL + pageAndRange).queryParam("key", key);

    SpreadSheetsList spreadSheetsList =
        restTemplate.getForObject(builder.build().encode().toUri(), SpreadSheetsList.class);

    if (ObjectUtils.isEmpty(spreadSheetsList)) {
      throw new RuntimeException("[getValues] spreadSheetsList is empty");
    }

    return spreadSheetsList.getValues();
  }

  private List<AnalyticsTDoll> parseNamesAndRanges(String pageAndRange) {
    UriComponentsBuilder builder =
        UriComponentsBuilder.fromHttpUrl(BASE_URL + pageAndRange).queryParam("key", key);

    SpreadSheetsList spreadSheetsList =
        restTemplate.getForObject(builder.build().encode().toUri(), SpreadSheetsList.class);

    List<AnalyticsTDoll> analyticsTDollList = new ArrayList<>();

    if (ObjectUtils.isEmpty(spreadSheetsList)) {
      throw new RuntimeException("[parseNamesAndRanges] spreadSheetsList is empty");
    }

    IntStream.range(0, spreadSheetsList.getValues().size())
        .skip(5L) // Первые пять строк до первой T-Doll
        .forEach(
            i -> {
              List<String> val = spreadSheetsList.getValues().get(i);
              // Список из одного элемента со всеми данными
              if (val.stream().findFirst().isPresent()) {
                List<String> valRows =
                    Arrays.stream(val.stream().findFirst().get().split("\\n"))
                        .collect(Collectors.toList());
                boolean isMod =
                    valRows.stream().anyMatch(item -> item.toLowerCase().contains("mod"));
                String concatRows = String.join(" ", valRows);
                AnalyticsTDoll newOne =
                    AnalyticsTDoll.builder()
                        .startRow(i)
                        .name(concatRows.substring(0, concatRows.indexOf(" (")))
                        .isMod(isMod)
                        .build();
                analyticsTDollList.stream()
                    .reduce((first, second) -> second)
                    .ifPresent(lastItem -> lastItem.setEndRow(i - 1));
                analyticsTDollList.add(newOne);
              }
            });

    Pattern lastRangePattern = Pattern.compile("[^0-9]+([0-9]+)$");
    Matcher matcher = lastRangePattern.matcher(spreadSheetsList.getRange());
    if (matcher.find()) {
      analyticsTDollList.stream()
          .reduce((first, second) -> second)
          .ifPresent(
              analyticsTDoll -> analyticsTDoll.setEndRow(Integer.parseInt(matcher.group(1))));
    }

    return analyticsTDollList;
  }

  private String getAn(Integer startRow, Integer endRow, List<List<String>> analysis) {
    String an =
        IntStream.range(startRow + 1, endRow)
            .mapToObj(
                i ->
                    analysis.size() > i && !ObjectUtils.isEmpty(analysis.get(i))
                        ? String.join("\n", analysis.get(i))
                        : "")
            .collect(Collectors.joining("\n"));

    return an.replace("\r", "").replaceAll("[\n]+", " \n\n ");
  }

  private List<String> getSplitAn(String an) {
    String[] splits = an.split(" ");
    List<String> anList = new ArrayList<>();

    StringBuilder collector = new StringBuilder();
    int length = 0;

    for (String sp : splits) {
      if (sp.equals("")) {
        continue;
      }
      collector.append(sp);
      length = length + sp.length();

      if (length >= 600) {
        collector.append("...");
        anList.add(collector.toString());
        length = 0;
        collector = new StringBuilder();
        collector.append("...");
      } else {
        collector.append(" ");
        length = length + 1;
      }
    }
    anList.add(collector.toString());
    return anList;
  }
}
