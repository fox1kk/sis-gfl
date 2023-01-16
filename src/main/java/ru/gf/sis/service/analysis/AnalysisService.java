package ru.gf.sis.service.analysis;

import discord4j.core.object.entity.channel.TextChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import ru.gf.sis.dto.Equipment;
import ru.gf.sis.dto.TDoll;
import ru.gf.sis.dto.analysis.Analytics;
import ru.gf.sis.repository.AnalyticsRepository;
import ru.gf.sis.repository.EquipmentRepository;
import ru.gf.sis.repository.TDollRepository;
import ru.gf.sis.utils.BotUtils;

import java.util.List;
import java.util.Optional;

import static ru.gf.sis.utils.BotUtils.DEVELOPER_NOTIFY;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnalysisService {
  private final AnalyticsRepository analyticsRepository;
  private final TDollRepository tDollRepository;
  private final EquipmentRepository equipmentRepository;
  private final BotUtils botUtils;

  public static final String GOOGLE_DOC_URL =
      "https://docs.google.com/spreadsheets/d/10LJdksnM3zipOb72IneJD7WVp3765JYJEGg0LnodzDI/edit";

  @Value("${google.api.token}")
  private String key;

  public void refreshGoogleDocAnalysis() {
    TextChannel channel = botUtils.getTextChannel(DEVELOPER_NOTIFY);
    GoogleDocApi api = new GoogleDocApi(key);
    try {
      api.getGoogleDocAnalysis()
          .forEach(
              item -> {
                if ((ObjectUtils.isEmpty(item.getCons()) && ObjectUtils.isEmpty(item.getPros()))
                    || ObjectUtils.isEmpty(item.getAnalysis())
                    || ObjectUtils.isEmpty(item.getUrlParams())) {
                  channel
                      .createMessage(
                          "(!) При парсинге Google Doc анализа обнаружена невалидная сущность: "
                              + item.getName())
                      .subscribe();
                  return;
                }

                Optional<Analytics> analytics =
                    analyticsRepository.findByNameAndMod(item.getName(), item.getIsMod());

                if (analytics.isPresent()) {
                  analytics.get().setPros(item.getPros());
                  analytics.get().setCons(item.getCons());
                  analytics.get().setSplitAnalysis(item.getAnalysis());
                  analytics.get().setStatus(item.getStatus());
                  analytics.get().setUrlParams(item.getUrlParams());
                  analyticsRepository.save(analytics.get());
                  return;
                }

                TDoll tdoll = tDollRepository.findByGunName(item.getName());
                if (ObjectUtils.isEmpty(tdoll)) {
                  List<TDoll> find = tDollRepository.findByAliases(item.getName().toLowerCase());
                  if (find.isEmpty()) {
                    find = tDollRepository.findByGunNameRegex(item.getName());
                  }
                  if (find.isEmpty()) {
                    channel
                        .createMessage(
                            "(!) При парсинге Google Doc анализа обнаружена t-doll не найденная в базе данных: "
                                + item.getName()
                                + " "
                                + GOOGLE_DOC_URL
                                + item.getUrlParams())
                        .subscribe();
                    return;
                  }
                  if (find.size() > 1) {
                    channel
                        .createMessage(
                            "(!) При парсинге Google Doc анализа обнаружена t-doll имя которой не уникально в базе данных: "
                                + item.getName()
                                + " "
                                + GOOGLE_DOC_URL
                                + item.getUrlParams())
                        .subscribe();
                    return;
                  }
                  tdoll = find.get(0);
                }

                analytics = analyticsRepository.findByDollIdAndMod(tdoll.getId(), item.getIsMod());
                if (analytics.isPresent()) {
                  channel
                      .createMessage(
                          "(!) При парсинге Google Doc анализа обнаружена t-doll "
                              + tdoll.getId()
                              + " с уже существующим анализом: "
                              + item.getName()
                              + " "
                              + GOOGLE_DOC_URL
                              + item.getUrlParams())
                      .subscribe();
                  return;
                }

                Analytics newAn =
                    Analytics.builder()
                        .dollId(tdoll.getId())
                        .mod(item.getIsMod())
                        .name(item.getName())
                        .imageUrl(item.getIsMod() ? tdoll.getModImageUrl() : tdoll.getImageUrl())
                        .pros(item.getPros())
                        .cons(item.getCons())
                        .status(item.getStatus())
                        .urlParams(item.getUrlParams())
                        .splitAnalysis(item.getAnalysis())
                        .build();
                analyticsRepository.save(newAn);
                channel
                    .createMessage(
                        "При парсинге Google Doc анализа обнаружена и сохранена новая t-doll: "
                            + item.getName()
                            + " "
                            + GOOGLE_DOC_URL
                            + item.getUrlParams())
                    .subscribe();
              });
    } catch (Exception e) {
      channel
          .createMessage(
              "При парсинге Google Doc анализа произошла непредвиденная ошибка: " + e.getMessage())
          .subscribe();
    }

    try {
      api.getGoogleDocFairyAnalysis()
          .forEach(
              item -> {
                if (item.getName().contains("Prototype")) {
                  return;
                }

                if ((ObjectUtils.isEmpty(item.getRecommendations()))
                    || ObjectUtils.isEmpty(item.getAnalysis())
                    || ObjectUtils.isEmpty(item.getUrlParams())) {
                  channel
                      .createMessage(
                          "(!) При парсинге Google Doc анализа обнаружена невалидная сущность: "
                              + item.getName())
                      .subscribe();
                  return;
                }

                Optional<Analytics> analytics =
                    analyticsRepository.findByNameAndMod(item.getName(), false);

                if (analytics.isPresent()) {
                  analytics.get().setSplitAnalysis(item.getAnalysis());
                  analytics.get().setRecommendation(item.getRecommendations());
                  analytics.get().setUrlParams(item.getUrlParams());
                  analyticsRepository.save(analytics.get());
                  return;
                }

                List<Equipment> find =
                    equipmentRepository.findByAliases(item.getName().toLowerCase());
                if (find.isEmpty()) {
                  find = equipmentRepository.findByNameAndTypeFairy(item.getName());
                }
                if (find.isEmpty()) {
                  channel
                      .createMessage(
                          "(!) При парсинге Google Doc анализа обнаружена fairy не найденная в базе данных: "
                              + item.getName()
                              + " "
                              + GOOGLE_DOC_URL
                              + item.getUrlParams())
                      .subscribe();
                  return;
                }
                if (find.size() > 1) {
                  channel
                      .createMessage(
                          "(!) При парсинге Google Doc анализа обнаружена fairy имя которой не уникально в базе данных: "
                              + item.getName()
                              + " "
                              + GOOGLE_DOC_URL
                              + item.getUrlParams())
                      .subscribe();
                  return;
                }
                Equipment fairy = find.get(0);

                analytics = analyticsRepository.findByDollIdAndMod(fairy.getId(), false);
                if (analytics.isPresent()) {
                  channel
                      .createMessage(
                          "(!) При парсинге Google Doc анализа обнаружена fairy "
                              + fairy.getId()
                              + " с уже существующим анализом: "
                              + item.getName()
                              + " "
                              + GOOGLE_DOC_URL
                              + item.getUrlParams())
                      .subscribe();
                  return;
                }

                Analytics newAn =
                    Analytics.builder()
                        .dollId(fairy.getId())
                        .mod(false)
                        .name(item.getName())
                        .imageUrl(fairy.getImageUrl())
                        .recommendation(item.getRecommendations())
                        .urlParams(item.getUrlParams())
                        .splitAnalysis(item.getAnalysis())
                        .build();
                analyticsRepository.save(newAn);
                channel
                    .createMessage(
                        "При парсинге Google Doc анализа обнаружена и сохранена новая fairy: "
                            + item.getName()
                            + " "
                            + GOOGLE_DOC_URL
                            + item.getUrlParams())
                    .subscribe();
              });
    } catch (Exception e) {
      channel
          .createMessage(
              "При парсинге Google Doc анализа произошла непредвиденная ошибка: " + e.getMessage())
          .subscribe();
    }
  }
}
