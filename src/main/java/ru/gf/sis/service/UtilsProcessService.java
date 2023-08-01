package ru.gf.sis.service;

import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gf.sis.dto.AvailableLanguage;
import ru.gf.sis.dto.WeiboNewsChannel;
import ru.gf.sis.dto.lang.Language;
import ru.gf.sis.dto.lang.ServerLanguage;
import ru.gf.sis.repository.AvailableLanguageRepository;
import ru.gf.sis.repository.ServerLanguageRepository;
import ru.gf.sis.repository.WeiboNewsChannelRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class UtilsProcessService {

  private final AvailableLanguageRepository availableLanguageRepository;
  private final ServerLanguageRepository serverLanguageRepository;
  private final WeiboNewsChannelRepository weiboNewsChannelRepository;

  public static final String MESSAGES__WEIBO_ENABLED = "weibo_enabled";
  public static final String MESSAGES__WEIBO_DISABLED = "weibo_disabled";
  public static final String CUSTOM_ID_PREFIXES__LANG_SELECT_MENU = "lang-select-menu-";

  public ActionRow processLanguageSelectMenu() {
    List<AvailableLanguage> languages = availableLanguageRepository.findAll();

    return ActionRow.of(
        SelectMenu.of(
                CUSTOM_ID_PREFIXES__LANG_SELECT_MENU,
                languages.stream()
                    .filter(AvailableLanguage::isActive)
                    .map(
                        item ->
                            SelectMenu.Option.of(
                                item.getDisplayName(), item.getServerSetShortcut()))
                    .collect(Collectors.toList()))
            .withMaxValues(1));
  }

  public void processLanguageSet(String shortCut, Long serverId) {
    AvailableLanguage availableLanguage =
        availableLanguageRepository.findByServerSetShortcut(shortCut);

    Optional<ServerLanguage> currentLang = serverLanguageRepository.findByServerId(serverId);

    currentLang.ifPresent(
        item -> {
          item.setLanguage(availableLanguage.getLanguage());
          serverLanguageRepository.save(item);
        });

    if (currentLang.isEmpty()) {
      serverLanguageRepository.save(
          ServerLanguage.builder()
              .serverId(serverId)
              .language(availableLanguage.getLanguage())
              .build());
    }

    TranslationService.updateServerLanguages(serverLanguageRepository.findAll());
  }

  public String processWeiboChannelSet(String channelId, Long serverId, Language language) {
    Optional<WeiboNewsChannel> w = weiboNewsChannelRepository.findByChannelId(channelId);
    if (w.isPresent()) {
      weiboNewsChannelRepository.delete(w.get());
      return TranslationService.getVariation(MESSAGES__WEIBO_DISABLED, language);
    } else {
      weiboNewsChannelRepository.save(
          WeiboNewsChannel.builder().channelId(channelId).serverId(serverId).build());
      return TranslationService.getVariation(MESSAGES__WEIBO_ENABLED, language);
    }
  }
}
