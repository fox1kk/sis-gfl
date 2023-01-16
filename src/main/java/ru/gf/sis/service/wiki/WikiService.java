package ru.gf.sis.service.wiki;

import discord4j.core.object.entity.channel.TextChannel;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import ru.gf.sis.dto.SpecialEquipment;
import ru.gf.sis.dto.TDoll;
import ru.gf.sis.dto.wiki.GetLinks;
import ru.gf.sis.dto.wiki.GetList;
import ru.gf.sis.dto.wiki.GetParse;
import ru.gf.sis.repository.SpecialEquipmentRepository;
import ru.gf.sis.repository.TDollRepository;
import ru.gf.sis.utils.BotUtils;
import ru.gf.sis.utils.Utils;
import ru.gf.sis.utils.WikiUtils;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ru.gf.sis.dto.lang.LanguageVariation.DEFAULT_LANGUAGE;
import static ru.gf.sis.service.wiki.WikiApi.BASE_URL;
import static ru.gf.sis.utils.BotUtils.DEVELOPER_NOTIFY;
import static ru.gf.sis.utils.BotUtils.NOTIFY;

@Service
@AllArgsConstructor
public class WikiService {
  private final TDollRepository tDollRepository;
  private final SpecialEquipmentRepository specialEquipmentRepository;
  private final BotUtils botUtils;

  public void wikiParse() {
    TextChannel channel = botUtils.getTextChannel(DEVELOPER_NOTIFY);

    // T-Dolls
    GetList allDollsList = new WikiApi().requestAllDollsList();
    if (allDollsList.getQuery().getCategorymembers().size() > 490) {
      channel
          .createMessage("(!) При запросе списка всех долок из Wiki скоро достигнем лимита")
          .subscribe();
    }

    allDollsList
        .getQuery()
        .getCategorymembers()
        .forEach(
            item -> {
              try {
                TDoll doll = tDollRepository.findByGunName(item.getTitle());
                if (!item.getTitle().equals("K7") && !item.getTitle().equals("KGP-9")) {
                  wikiParseDoll(doll, item.getTitle());
                }
              } catch (Exception e) {
                channel
                    .createMessage(
                        "(!) При парсинге Wiki (тянки) произошла ошибка: " + e.getMessage())
                    .subscribe();
                e.printStackTrace();
              }
            });

    // Equip
    GetList allSpecList = new WikiApi().requestAllSepcList();
    if (allSpecList.getQuery().getCategorymembers().size() > 490) {
      channel
          .createMessage("(!) При запросе списка всего эквипа из Wiki скоро достигнем лимита.")
          .subscribe();
    }
    allSpecList
        .getQuery()
        .getCategorymembers()
        .forEach(
            item -> {
              try {
                Optional<SpecialEquipment> spec =
                    specialEquipmentRepository.findByName(item.getTitle());
                if (spec.isEmpty()) {
                  GetParse specParse = new WikiApi().requestSpec(item.getTitle());
                  String specText = specParse.getParse().getWikitext().getText();

                  String compatibleDoll = WikiUtils.parseCompatibleTo(specText);
                  TDoll tDoll = tDollRepository.findByGunName(compatibleDoll);
                  if (tDoll != null && !compatibleDoll.equals("Jill")) {
                    gainSpecsFromWiki(tDoll);
                  }
                  if (tDoll == null) {
                    channel
                        .createMessage("(!) Обнаружен эквип без tDoll: " + item.getTitle())
                        .subscribe();
                  }
                }
              } catch (Exception e) {
                channel
                    .createMessage(
                        "(!) При парсинге Wiki (эквип) произошла ошибка: " + e.getMessage())
                    .subscribe();
                e.printStackTrace();
              }
            });
  }

  private void wikiParseDoll(TDoll doll, String dollName) {
    WikiApi wikiApi = new WikiApi();
    TextChannel channel = botUtils.getTextChannel(NOTIFY);

    if (doll != null && Boolean.TRUE.equals(doll.getWikiSync())) {
      gainFullFromWiki(doll, dollName);
      tDollRepository.save(doll);
      gainSpecsFromWiki(doll);
    }

    if (doll != null && !doll.isMod()) {
      String dollText = wikiApi.requestBasic(doll.getGunName()).getParse().getWikitext().getText();

      if (WikiUtils.isMod(dollText)) {
        gainModFromWiki(doll, dollText);
        tDollRepository.save(doll);
        channel
            .createMessage(
                "Добавлен MOD для '"
                    + doll.getGunName()
                    + "'. Необходимо проверить корректность. (ID: '"
                    + doll.getId()
                    + "')")
            .subscribe();
        gainSpecsFromWiki(doll);
      }
    }

    if (doll == null) {
      doll = new TDoll();
      doll.setWikiSync(true);
      gainFullFromWiki(doll, dollName);
      tDollRepository.save(doll);
      channel
          .createMessage(
              "Добавлена новая TDoll: '"
                  + doll.getGunName()
                  + "'. Необходимо проверить корректность. (ID: '"
                  + doll.getId()
                  + "')")
          .subscribe();
      gainSpecsFromWiki(doll);
    }
  }

  private void gainModFromWiki(TDoll tDoll, String dollText) {
    tDoll.setIsMod(true);
    Integer rarity = WikiUtils.parseRarity(dollText);
    switch (rarity) {
      case 5:
        tDoll.setModRarity(6);
        break;
      case 4:
        tDoll.setModRarity(5);
        break;
      case 3:
      case 2:
        tDoll.setModRarity(4);
        break;
      default:
        tDoll.setRarity(rarity);
        break;
    }
    tDoll.setModBuff(tDoll.getBuff());
    HashMap<Integer, String> tiles = WikiUtils.parseTiles(dollText);
    HashMap<Integer, String> modTiles = WikiUtils.parseModTiles(dollText);
    IntStream.range(1, 10)
        .forEach(
            i -> {
              if (!ObjectUtils.isEmpty(modTiles.get(i))) {
                tiles.put(i, modTiles.get(i));
              }
            });
    tDoll.setModBuff(getBullFromTiles(tiles));
    if (!WikiUtils.parseModAura(dollText).isEmpty()) {
      tDoll.setModBuffTo(WikiUtils.parseModAura(dollText));
    } else {
      tDoll.setModBuffTo(WikiUtils.parseAura(dollText));
    }
    tDoll.updateModBuffDescription(
        DEFAULT_LANGUAGE,
        WikiUtils.parseModAura2(dollText) + "\n" + WikiUtils.parseModAura3(dollText));
    String imgName = WikiUtils.parseModImgName(dollText);
    tDoll.setModImageUrl(generateWikiImgLink(imgName));

    WikiApi wikiApi = new WikiApi();
    GetParse s1 = wikiApi.requestMod1Skill(tDoll.getGunName());
    GetParse s2 = wikiApi.requestSkill2(tDoll.getGunName());

    String skillTextMod1 = s1.getParse().getWikitext().getText();
    tDoll.updateModSkillName(DEFAULT_LANGUAGE, WikiUtils.parseSkillName(skillTextMod1));
    tDoll.updateModSkillDescription(
        DEFAULT_LANGUAGE, WikiUtils.parseSkillDescription(skillTextMod1));
    tDoll.setModSkillInitialCd(WikiUtils.parseSkillInitialCd(skillTextMod1));
    tDoll.setModSkillCd(WikiUtils.parseSkillCd(skillTextMod1));

    String skillText2 = s2.getParse().getWikitext().getText();
    tDoll.updateModSecondSkillName(DEFAULT_LANGUAGE, WikiUtils.parseSkillName(skillText2));
    tDoll.updateModSecondSkillDescription(
        DEFAULT_LANGUAGE, WikiUtils.parseSkillDescription(skillText2));
    tDoll.setModSecondSkillInitialCd(WikiUtils.parseSkillInitialCd(skillText2));
    tDoll.setModSecondSkillCd(WikiUtils.parseSkillCd(skillText2));
  }

  private void gainFullFromWiki(TDoll doll, String dollName) {
    WikiApi wikiApi = new WikiApi();
    GetParse b = wikiApi.requestBasic(dollName);
    GetParse s = wikiApi.requestSkill(dollName);

    String dollText = b.getParse().getWikitext().getText();

    doll.setGunName(dollName);
    List<String> aliases = new ArrayList<>();
    if (dollName.toLowerCase().contains("-")) {
      aliases.add(dollName.toLowerCase().replace("-", " "));
      aliases.add(dollName.toLowerCase().replace("-", ""));
    }
    if (dollName.toLowerCase().contains(" ")) {
      aliases.add(dollName.toLowerCase().replace(" ", "-"));
      aliases.add(dollName.toLowerCase().replace(" ", ""));
    }
    doll.setAliases(aliases);

    doll.setType(WikiUtils.parseClass(dollText));
    doll.setRarity(WikiUtils.parseRarity(dollText));
    doll.setDollId(WikiUtils.parseIndex(dollText));

    String craftTime = WikiUtils.parseCraft(dollText);

    if (!craftTime.isEmpty()) {
      String[] splits = craftTime.split(":");
      doll.setProductTime(Utils.parseProductionTime(splits[0] + ":" + splits[1]));
    } else {
      doll.setProductTime("N/A");
    }

    HashMap<Integer, String> tiles = WikiUtils.parseTiles(dollText);
    doll.setBuff(getBullFromTiles(tiles));
    doll.setBuffTo(WikiUtils.parseAura(dollText));
    String buffDescription =
        ObjectUtils.isEmpty(WikiUtils.parseAura4(dollText))
            ? WikiUtils.parseAura2(dollText) + "\n" + WikiUtils.parseAura3(dollText)
            : WikiUtils.parseAura2(dollText)
                + "\n"
                + WikiUtils.parseAura3(dollText)
                + "\n"
                + WikiUtils.parseAura4(dollText);
    doll.updateBuffDescription(DEFAULT_LANGUAGE, buffDescription);

    doll.setIllustrator(WikiUtils.parseIllustrator(dollText));
    String voiceActor = WikiUtils.parseVoice(dollText);
    doll.setVoiceActor(voiceActor.isEmpty() ? "None" : voiceActor);
    String imgName = WikiUtils.parseImgName(dollText);
    doll.setImageUrl(generateWikiImgLink(imgName));

    String skillText = s.getParse().getWikitext().getText();
    doll.updateSkillName(DEFAULT_LANGUAGE, WikiUtils.parseSkillName(skillText));
    doll.updateSkillDescription(DEFAULT_LANGUAGE, WikiUtils.parseSkillDescription(skillText));
    doll.setInitialCd(WikiUtils.parseSkillInitialCd(skillText));
    doll.setCd(WikiUtils.parseSkillCd(skillText));

    if (WikiUtils.isMod(dollText)) {
      gainModFromWiki(doll, dollText);
    }
  }

  private void gainSpecsFromWiki(TDoll doll) {
    WikiApi wikiApi = new WikiApi();
    TextChannel channel = botUtils.getTextChannel(NOTIFY);
    GetLinks l = wikiApi.requestLinks(doll.getGunName());
    GetList sp = wikiApi.requestAllSepcList();

    List<String> allSpecs = new ArrayList<>();
    sp.getQuery().getCategorymembers().forEach(item -> allSpecs.add(item.getTitle()));

    List<GetLinks.Links> dollSpec =
        l.getParse().getLinks().stream()
            .filter(item -> allSpecs.contains(item.getLink()))
            .collect(Collectors.toList());

    dollSpec.forEach(
        item -> {
          GetParse specParse = new WikiApi().requestSpec(item.getLink());
          String specText = specParse.getParse().getWikitext().getText();

          Optional<SpecialEquipment> spec = specialEquipmentRepository.findByName(item.getLink());
          if (spec.isPresent()) {
            List<String> linkEntIds = doll.getLinkedEntitiesIds();
            if (!linkEntIds.contains(spec.get().getId())) {
              linkEntIds.add(spec.get().getId());
            }
          } else {
            SpecialEquipment specialEquipment = new SpecialEquipment();
            specialEquipment.setName(item.getLink());
            specialEquipment.setImageUrl(generateWikiImgLink(item.getLink() + ".png"));
            specialEquipment.updateStats(DEFAULT_LANGUAGE, WikiUtils.parseSpecStats(specText));
            specialEquipment.updateObtainable(
                DEFAULT_LANGUAGE, WikiUtils.parseSpecObtain(specText));
            specialEquipment = specialEquipmentRepository.save(specialEquipment);

            channel
                .createMessage(
                    "Для '"
                        + doll.getGunName()
                        + "' добавлен спец эквип. Необходимо проверить корректность. (ID: '"
                        + specialEquipment.getId()
                        + "')")
                .subscribe();

            doll.addLinkedEnt(specialEquipment.getId());
            tDollRepository.save(doll);
          }
        });
  }

  private String generateWikiImgLink(String fileName) {
    fileName = fileName.replace(" ", "_");

    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(fileName.getBytes());
      byte[] digest = md.digest();

      String checksum = DatatypeConverter.printHexBinary(digest).toLowerCase();

      return BASE_URL
          + "/images/"
          + checksum.substring(0, 1)
          + "/"
          + checksum.substring(0, 2)
          + "/"
          + fileName;

    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    throw new RuntimeException();
  }

  private String getTileUniSymbol(String tile) {
    return tile.equals("0") ? "⚪" : tile.equals("1") ? "\uD83D\uDD35" : "⚫";
  }

  private String getBullFromTiles(HashMap<Integer, String> tiles) {
    return getTileUniSymbol(tiles.get(7))
        + getTileUniSymbol(tiles.get(8))
        + getTileUniSymbol(tiles.get(9))
        + "\n"
        + getTileUniSymbol(tiles.get(4))
        + getTileUniSymbol(tiles.get(5))
        + getTileUniSymbol(tiles.get(6))
        + "\n"
        + getTileUniSymbol(tiles.get(1))
        + getTileUniSymbol(tiles.get(2))
        + getTileUniSymbol(tiles.get(3));
  }
}
