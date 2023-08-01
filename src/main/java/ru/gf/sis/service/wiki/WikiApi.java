package ru.gf.sis.service.wiki;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.gf.sis.dto.wiki.GetLinks;
import ru.gf.sis.dto.wiki.GetList;
import ru.gf.sis.dto.wiki.GetParse;

public class WikiApi {
  private final RestTemplate restTemplate;

  public static final String BASE_URL = "https://iopwiki.com";
  private static final String BASE_API_URL = BASE_URL + "/api.php";
  private static final String USER_AGENT =
      "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";

  public WikiApi() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(10000);
    factory.setReadTimeout(10000);
    restTemplate = new RestTemplate(factory);
  }

  public GetList requestAllDollsList() {
    return requestCategory("T-Dolls");
  }

  public GetList requestAllSepcList() {
    return requestCategory("Exclusive Equipments");
  }

  public GetList requestAllBattleFairies() {
    return requestCategory("Battle Fairies");
  }

  public GetList requestAllStrategyFairies() {
    return requestCategory("Strategy Fairies");
  }

  public GetList requestAllHoc() {
    return requestCategory("HOC units");
  }

  public GetList requestAllSF() {
    return requestCategory("Coalition Unit Ringleaders");
  }

  public GetParse requestBasic(String name) {
    return requestParse(name);
  }

  public GetParse requestSkill(String name) {
    return requestParse(name, "/skilldata");
  }

  public GetParse requestMod1Skill(String name) {
    return requestParse(name, "/skilldata/mod1");
  }

  public GetParse requestSkill2(String name) {
    return requestParse(name, "/skill2data");
  }

  public GetParse requestSkill3(String name) {
    return requestParse(name, "/skill3data");
  }

  public GetParse requestSkill4(String name) {
    return requestParse(name, "/skill4data");
  }

  public GetParse requestSpec(String specName) {
    return requestParse(specName);
  }

  public GetLinks requestLinks(String pageName) {
    UriComponentsBuilder builder =
        UriComponentsBuilder.fromHttpUrl(BASE_API_URL)
            .queryParam("action", "parse")
            .queryParam("prop", "links")
            .queryParam("page", pageName)
            .queryParam("format", "json");

    HttpHeaders headers = new HttpHeaders();
    headers.add("user-agent", USER_AGENT);
    HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

    ResponseEntity<GetLinks> responseEntity =
        restTemplate.exchange(
            builder.build().encode().toUri(), HttpMethod.GET, entity, GetLinks.class);

    return responseEntity.getBody();
  }

  private GetParse requestParse(String name, String add) {
    UriComponentsBuilder builder =
        UriComponentsBuilder.fromHttpUrl(BASE_API_URL)
            .queryParam("action", "parse")
            .queryParam("prop", "wikitext")
            .queryParam("page", name + (add == null ? "" : add))
            .queryParam("format", "json");

    HttpHeaders headers = new HttpHeaders();
    headers.add("user-agent", USER_AGENT);
    HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

    ResponseEntity<GetParse> responseEntity =
        restTemplate.exchange(
            builder.build().encode().toUri(), HttpMethod.GET, entity, GetParse.class);

    return responseEntity.getBody();
  }

  private GetParse requestParse(String name) {
    return requestParse(name, null);
  }

  private GetList requestCategory(String category) {
    UriComponentsBuilder builder =
        UriComponentsBuilder.fromHttpUrl(BASE_API_URL)
            .queryParam("action", "query")
            .queryParam("list", "categorymembers")
            .queryParam("cmtitle", "Category:" + category)
            .queryParam("cmtype", "page")
            .queryParam("cmlimit", "500")
            .queryParam("format", "json");

    HttpHeaders headers = new HttpHeaders();
    headers.add("user-agent", USER_AGENT);
    HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

    ResponseEntity<GetList> responseEntity =
        restTemplate.exchange(
            builder.build().encode().toUri(), HttpMethod.GET, entity, GetList.class);

    return responseEntity.getBody();
  }
}
