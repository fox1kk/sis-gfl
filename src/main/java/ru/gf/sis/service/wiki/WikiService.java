package ru.gf.sis.service.wiki;

import discord4j.core.object.entity.channel.TextChannel;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import ru.gf.sis.dto.Equipment;
import ru.gf.sis.dto.Hoc;
import ru.gf.sis.dto.SFBoss;
import ru.gf.sis.dto.SpecialEquipment;
import ru.gf.sis.dto.TDoll;
import ru.gf.sis.dto.wiki.GetLinks;
import ru.gf.sis.dto.wiki.GetList;
import ru.gf.sis.dto.wiki.GetParse;
import ru.gf.sis.repository.EquipmentRepository;
import ru.gf.sis.repository.HocRepository;
import ru.gf.sis.repository.SFBossRepository;
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
  private final EquipmentRepository equipmentRepository;
  private final HocRepository hocRepository;
  private final SFBossRepository sfRepository;
  private final BotUtils botUtils;

  public void wikiParse() {
    TextChannel channel = botUtils.getTextChannel(DEVELOPER_NOTIFY);

    // T-Dolls
    GetList allDollsList = new WikiApi().requestAllDollsList();
    if (allDollsList.getQuery().getCategorymembers().size() > 490) {
      channel
          .createMessage("(!) При запросе списка всех tdoll из Wiki скоро достигнем лимита")
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
                        MessageFormat.format(
                            "(!) При парсинге Wiki (tdoll) {0}, произошла ошибка: {1} {2}",
                            item.getTitle(), e.getClass().getSimpleName(), e.getMessage()))
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
                        MessageFormat.format(
                            "(!) При парсинге Wiki (эквип) {0}, произошла ошибка: {1} {2}",
                            item.getTitle(), e.getClass().getSimpleName(), e.getMessage()))
                    .subscribe();
                e.printStackTrace();
              }
            });

    // Fairies
    wikiParseAllFairies();

    // Hoc
    wikiParseHoc();
    
    // SF
    wikiParseSf();
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
    Set<String> aliases = new HashSet<>(doll.getAliases());
    if (dollName.toLowerCase().contains("-")) {
      aliases.add(dollName.toLowerCase().replace("-", " "));
      aliases.add(dollName.toLowerCase().replace("-", ""));
    }
    if (dollName.toLowerCase().contains(" ")) {
      aliases.add(dollName.toLowerCase().replace(" ", "-"));
      aliases.add(dollName.toLowerCase().replace(" ", ""));
    }
    
    doll.setAliases(new ArrayList<>(aliases));

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
      throw new RuntimeException();
    }
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

  public void wikiParseAllFairies() {
    wikiParseFairies(new WikiApi().requestAllBattleFairies());
    wikiParseFairies(new WikiApi().requestAllStrategyFairies());
  }

  private void wikiParseFairies(GetList fairies) {
    TextChannel channel = botUtils.getTextChannel(DEVELOPER_NOTIFY);

    fairies
        .getQuery()
        .getCategorymembers()
        .forEach(
            item -> {
              try {
                if ("Origin Fairy".equals(item.getTitle())) {
                  return;
                }
                List<Equipment> fairy = equipmentRepository.findByNameAndTypeFairy(item.getTitle());
                if (fairy.size() > 2) {
                  channel
                      .createMessage(
                          MessageFormat.format(
                              "(!) При парсинге Wiki (fairies) {0}, произошла ошибка: в базе с таким именем обнаружено две",
                              item.getTitle()))
                      .subscribe();
                }
                wikiParseFairy(fairy, item.getTitle());
              } catch (Exception e) {
                channel
                    .createMessage(
                        MessageFormat.format(
                            "(!) При парсинге Wiki (fairies) {0}, произошла ошибка: {1} {2}",
                            item.getTitle(), e.getClass().getSimpleName(), e.getMessage()))
                    .subscribe();
                e.printStackTrace();
              }
            });
  }

  private void wikiParseFairy(List<Equipment> fairy, String name) {
    if (!fairy.isEmpty() && !Boolean.TRUE.equals(fairy.get(0).getWikiSync())) {
      return;
    }

    if (!fairy.isEmpty()) {
      gainFairyFromWiki(fairy.get(0), name);
      equipmentRepository.save(fairy.get(0));
    } else {
      Equipment newFairy = new Equipment();
      newFairy.setWikiSync(true);
      gainFairyFromWiki(newFairy, name);
      equipmentRepository.save(newFairy);
    }
  }

  private void gainFairyFromWiki(Equipment fairy, String name) {
    WikiApi wikiApi = new WikiApi();
    GetParse b = wikiApi.requestBasic(name);

    String text = b.getParse().getWikitext().getText();

    fairy.setName(name);
    fairy.setIsFairy(true);
    fairy.setType("Fairy");
    fairy.setRarity(0);
    fairy.updateSkillName(DEFAULT_LANGUAGE, WikiUtils.parseFairySkillName(text));
    fairy.updateSkillDescription(DEFAULT_LANGUAGE, WikiUtils.parseFairySkillDescription(text));
    fairy.updateStatDescription(DEFAULT_LANGUAGE, WikiUtils.parseFairyStats(text));
    fairy.setImageUrl(generateWikiImgLink(name.replace(" ", "_") + "_3.png"));
    String craftTime = WikiUtils.parseCraft(text);

    if (ObjectUtils.isEmpty(craftTime)) {
      fairy.setProductTime("N/A");
    } else {
      String[] splits = craftTime.split(":");
      fairy.setProductTime(Utils.parseProductionTime(splits[0] + ":" + splits[1]));
    }
  }

  public void wikiParseHoc() {
    TextChannel channel = botUtils.getTextChannel(DEVELOPER_NOTIFY);

    new WikiApi()
        .requestAllHoc()
        .getQuery()
        .getCategorymembers()
        .forEach(
            item -> {
              try {
                List<Hoc> hoc = hocRepository.findByName(item.getTitle());
                if (hoc.size() > 2) {
                  channel
                      .createMessage(
                          MessageFormat.format(
                              "(!) При парсинге Wiki (hoc) {0}, произошла ошибка: в базе с таким именем обнаружено две",
                              item.getTitle()))
                      .subscribe();
                }
                wikiParseHoc(hoc, item.getTitle());
              } catch (Exception e) {
                channel
                    .createMessage(
                        MessageFormat.format(
                            "(!) При парсинге Wiki (hoc) {0}, произошла ошибка: {1} {2}",
                            item.getTitle(), e.getClass().getSimpleName(), e.getMessage()))
                    .subscribe();
                e.printStackTrace();
              }
            });
  }

  private void wikiParseHoc(List<Hoc> hoc, String name) {
    if (!hoc.isEmpty() && !Boolean.TRUE.equals(hoc.get(0).getWikiSync())) {
      return;
    }

    if (!hoc.isEmpty()) {
      gainHocFromWiki(hoc.get(0), name);
      hocRepository.save(hoc.get(0));
    } else {
      Hoc newHoc = new Hoc();
      newHoc.setWikiSync(true);
      gainHocFromWiki(newHoc, name);
      hocRepository.save(newHoc);
    }
  }

  private void gainHocFromWiki(Hoc hoc, String name) {
    WikiApi wikiApi = new WikiApi();
    GetParse b = wikiApi.requestBasic(name);

    String text = b.getParse().getWikitext().getText();

    hoc.setName(name);
    Set<String> aliases = new HashSet<>(hoc.getAliases());
    if (name.toLowerCase().contains("-")) {
      aliases.add(name.toLowerCase().replace("-", " "));
      aliases.add(name.toLowerCase().replace("-", ""));
    }
    if (name.toLowerCase().contains(" ")) {
      aliases.add(name.toLowerCase().replace(" ", "-"));
      aliases.add(name.toLowerCase().replace(" ", ""));
    }
    hoc.setAliases(new ArrayList<>(aliases));
    hoc.setType(WikiUtils.parseClass(text));
    hoc.setShellingDamage(WikiUtils.parseHocDmg(text));
    hoc.setDefensePiercing(WikiUtils.parseHocPiercing(text));
    hoc.setShellingAccuracy(WikiUtils.parseHocAccuracy(text));
    hoc.setReloadSpeed(WikiUtils.parseHocReload(text));
    hoc.setImageUrl(generateWikiImgLink(name.replace(" ", "_") + "_Artwork.jpg"));

    GetParse s = wikiApi.requestSkill(name);
    String skillText = s.getParse().getWikitext().getText();
    hoc.updateFirstSkillName(DEFAULT_LANGUAGE, WikiUtils.parseSkillName(skillText));
    hoc.updateFirstSkillDescription(DEFAULT_LANGUAGE, WikiUtils.parseSkillDescription(skillText));

    GetParse s2 = wikiApi.requestSkill2(name);
    skillText = s2.getParse().getWikitext().getText();
    hoc.updateSecondSkillName(DEFAULT_LANGUAGE, WikiUtils.parseSkillName(skillText));
    hoc.updateSecondSkillDescription(DEFAULT_LANGUAGE, WikiUtils.parseSkillDescription(skillText));

    GetParse s3 = wikiApi.requestSkill3(name);
    skillText = s3.getParse().getWikitext().getText();
    hoc.updateThirdSkillName(DEFAULT_LANGUAGE, WikiUtils.parseSkillName(skillText));
    hoc.updateThirdSkillDescription(DEFAULT_LANGUAGE, WikiUtils.parseSkillDescription(skillText));
  }

  public void wikiParseSf() {
    TextChannel channel = botUtils.getTextChannel(DEVELOPER_NOTIFY);

    new WikiApi()
        .requestAllSF()
        .getQuery()
        .getCategorymembers()
        .forEach(
            item -> {
              try {
                SFBoss sf = sfRepository.findByName(item.getTitle().replace("/Assimilated", ""));
                wikiParseSf(sf, item.getTitle());
              } catch (Exception e) {
                channel
                    .createMessage(
                        MessageFormat.format(
                            "(!) При парсинге Wiki (sf) {0}, произошла ошибка: {1} {2}",
                            item.getTitle(), e.getClass().getSimpleName(), e.getMessage()))
                    .subscribe();
                e.printStackTrace();
              }
            });
  }

  private void wikiParseSf(SFBoss sf, String name) {
    if (Objects.nonNull(sf) && !Boolean.TRUE.equals(sf.getWikiSync())) {
      return;
    }

    if (Objects.nonNull(sf)) {
      gainSfFromWiki(sf, name);
      if (!ObjectUtils.isEmpty(
          sf.getSkill1().getSkillDescription().getVariation(DEFAULT_LANGUAGE))) {
        sfRepository.save(sf);
      }
    } else {
      SFBoss newSf = new SFBoss();
      newSf.setWikiSync(true);
      gainSfFromWiki(newSf, name);
      if (!ObjectUtils.isEmpty(
          newSf.getSkill1().getSkillDescription().getVariation(DEFAULT_LANGUAGE))) {
        sfRepository.save(newSf);
      }
    }
  }

  private void gainSfFromWiki(SFBoss sf, String name) {
    WikiApi wikiApi = new WikiApi();
    GetParse b = wikiApi.requestBasic(name);

    String text = b.getParse().getWikitext().getText();

    sf.setName(name.replace("/Assimilated", ""));
    sf.setRarity(3);
    sf.setCost(10);
    sf.setBuff(
        "\uD83D\uDD35\uD83D\uDD35\uD83D\uDD35\n\uD83D\uDD35⚪\uD83D\uDD35\n\uD83D\uDD35\uD83D\uDD35\uD83D\uDD35");
    String type = WikiUtils.parseSfType(text);
    if (!"SF".equals(type)) {
      sf.getType().updateVariation(DEFAULT_LANGUAGE, type);
    }

    sf.setImageUrl(generateWikiImgLink(WikiUtils.parseSfImgName(text) + " Upgrade.png"));

    GetParse s = wikiApi.requestSkill(name);
    if (ObjectUtils.isEmpty(s) || ObjectUtils.isEmpty(s.getParse())) {
      return;
    }

    sf.getBuffDescription().updateVariation(DEFAULT_LANGUAGE, WikiUtils.parseSfAura(text));
    sf.setSfStats(WikiUtils.parseSfStats(text));

    if (!ObjectUtils.isEmpty(s) && !ObjectUtils.isEmpty(s.getParse())) {
      String skillText = s.getParse().getWikitext().getText();
      sf.getSkill1()
          .getSkillName()
          .updateVariation(DEFAULT_LANGUAGE, WikiUtils.parseSkillName(skillText));
      sf.getSkill1()
          .getSkillDescription()
          .updateVariation(DEFAULT_LANGUAGE, WikiUtils.parseSkillDescription(skillText));
      sf.getSkill1().setInitialCd(WikiUtils.parseSkillInitialCd(skillText));
      sf.getSkill1().setCd(WikiUtils.parseSkillCd(skillText));
    }

    s = wikiApi.requestSkill2(name);
    if (!ObjectUtils.isEmpty(s) && !ObjectUtils.isEmpty(s.getParse())) {
      String skillText = s.getParse().getWikitext().getText();
      sf.getSkill2()
          .getSkillName()
          .updateVariation(DEFAULT_LANGUAGE, WikiUtils.parseSkillName(skillText));
      sf.getSkill2()
          .getSkillDescription()
          .updateVariation(DEFAULT_LANGUAGE, WikiUtils.parseSkillDescription(skillText));
      sf.getSkill2().setInitialCd(WikiUtils.parseSkillInitialCd(skillText));
      sf.getSkill2().setCd(WikiUtils.parseSkillCd(skillText));
    }

    s = wikiApi.requestSkill3(name);
    if (!ObjectUtils.isEmpty(s) && !ObjectUtils.isEmpty(s.getParse())) {
      String skillText = s.getParse().getWikitext().getText();
      sf.getSkill3()
          .getSkillName()
          .updateVariation(DEFAULT_LANGUAGE, WikiUtils.parseSkillName(skillText));
      sf.getSkill3()
          .getSkillDescription()
          .updateVariation(DEFAULT_LANGUAGE, WikiUtils.parseSkillDescription(skillText));
      sf.getSkill3().setInitialCd(WikiUtils.parseSkillInitialCd(skillText));
      sf.getSkill3().setCd(WikiUtils.parseSkillCd(skillText));
    }

    s = wikiApi.requestSkill4(name);
    if (!ObjectUtils.isEmpty(s) && !ObjectUtils.isEmpty(s.getParse())) {
      String skillText = s.getParse().getWikitext().getText();
      sf.getSkill4()
          .getSkillName()
          .updateVariation(DEFAULT_LANGUAGE, WikiUtils.parseSkillName(skillText));
      sf.getSkill4()
          .getSkillDescription()
          .updateVariation(DEFAULT_LANGUAGE, WikiUtils.parseSkillDescription(skillText));
      sf.getSkill4().setInitialCd(WikiUtils.parseSkillInitialCd(skillText));
      sf.getSkill4().setCd(WikiUtils.parseSkillCd(skillText));
    }
  }
}
