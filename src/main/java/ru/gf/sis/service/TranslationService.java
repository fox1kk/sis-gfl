package ru.gf.sis.service;

import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gf.sis.dto.lang.BasicTranslations;
import ru.gf.sis.dto.lang.Language;
import ru.gf.sis.dto.lang.LanguageVariation;
import ru.gf.sis.dto.lang.ServerLanguage;
import ru.gf.sis.repository.BasicTranslationsRepository;
import ru.gf.sis.repository.ServerLanguageRepository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TranslationService {
  private final BasicTranslationsRepository basicTranslationsRepository;
  private final ServerLanguageRepository serverLanguageRepository;

  private static List<ServerLanguage> serverLanguages;
  private static Map<String, LanguageVariation> translations;

  @PostConstruct
  private void postConstruct() {
    serverLanguages = serverLanguageRepository.findAll();
    translations =
        basicTranslationsRepository.findAll().stream()
            .collect(
                Collectors.toMap(BasicTranslations::getName, BasicTranslations::getTranslation));
  }

  public static void updateServerLanguages(List<ServerLanguage> languages) {
    serverLanguages = languages;
  }

  public static String getVariation(String name, Language language) {
    return translations.get(name).getVariation(language);
  }

  public static Language getLanguageFromInteractionEvent(InteractionCreateEvent event) {
    Language language = Language.ENGLISH;
    if (event.getInteraction().getGuildId().isPresent()) {
      Optional<ServerLanguage> lang =
          serverLanguages.stream()
              .filter(
                  item ->
                      item.getServerId().equals(event.getInteraction().getGuildId().get().asLong()))
              .findFirst();
      language =
          lang.map(serverLanguage -> Language.valueOf(serverLanguage.getLanguage()))
              .orElse(language);
    }
    return language;
  }
}
