package ru.gf.sis.utils;

import java.text.MessageFormat;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ru.gf.sis.dto.SFStats;

public class WikiUtils {

  public static String parseIndex(String text) {
    String pattern = "index *=(.+?)\\n";
    return execPattern(pattern, text);
  }

  public static String parseClass(String text) {
    String pattern = "classification *=(.+?)\\n";
    return execPattern(pattern, text);
  }

  public static Integer parseRarity(String text) {
    String pattern = "rarity *=(.+?)\\n";
    if (!execPattern(pattern, text).equals("EXTRA")) {
      return Integer.valueOf(execPattern(pattern, text));
    } else {
      return 1;
    }
  }

  public static String parseCraft(String text) {
    String pattern = "\\| ?craft *=(.+?)\\n";
    return execPattern(pattern, text);
  }

  public static HashMap<Integer, String> parseTiles(String text) {
    HashMap<Integer, String> tiles = new HashMap<>();

    for (int i = 1; i < 10; i++) {
      String pattern = "\\| ?tile" + i + " *=(.+?)\\n";
      tiles.put(i, execPattern(pattern, text));
    }

    return tiles;
  }

  public static HashMap<Integer, String> parseModTiles(String text) {
    HashMap<Integer, String> modTiles = new HashMap<>();

    for (int i = 1; i < 10; i++) {
      String pattern = "\\| ?tile" + i + " *=(.+?)\\n";
      modTiles.put(i, execPattern(pattern, text));
      pattern = "\\| ?mod1_tile" + i + " *=(.+?)\\n";
      String p = execPattern(pattern, text);
      if (!p.isEmpty()) {
        modTiles.put(i, p);
      }
      pattern = "\\| ?mod2_tile" + i + " *=(.+?)\\n";
      p = execPattern(pattern, text);
      if (!p.isEmpty()) {
        modTiles.put(i, p);
      }
      pattern = "\\| ?mod3_tile" + i + " *=(.+?)\\n";
      p = execPattern(pattern, text);
      if (!p.isEmpty()) {
        modTiles.put(i, p);
      }
    }

    return modTiles;
  }

  public static String parseAura(String text) {
    String pattern = "\\| *aura1 *=(.+?)\\n";
    String aura1 = execPattern(pattern, text);

    List<String> auraDecode = auraDecode(aura1);
    return String.join(", ", auraDecode);
  }

  public static String parseAura2(String text) {
    String pattern = "\\| *aura2 *=(.+?)\\n";
    return removeHgAura(execPattern(pattern, text));
  }

  public static String parseAura3(String text) {
    String pattern = "\\| *aura3 *=(.+?)\\n";
    return removeHgAura(execPattern(pattern, text));
  }

  public static String parseAura4(String text) {
    String pattern = "\\| *aura4 *=(.+?)\\n";
    return removeHgAura(execPattern(pattern, text));
  }

  public static String parseModAura(String text) {
    String pattern = "aura1 *=(.+?)\\n";
    String aura = modCyclePattern(text, pattern);

    List<String> auraDecode = auraDecode(aura);
    return String.join(", ", auraDecode);
  }

  public static String parseModAura2(String text) {
    String pattern = "aura2 *=(.+?)\\n";
    return removeHgAura(modCyclePattern(text, pattern));
  }

  public static String parseModAura3(String text) {
    String pattern = "aura3 *=(.+?)\\n";
    return removeHgAura(modCyclePattern(text, pattern));
  }

  public static String parseIllustrator(String text) {
    String pattern = "\\{\\{artist name\\|(.+?)\\}\\}";
    return execPattern(pattern, text);
  }

  public static String parseVoice(String text) {
    String pattern = "\\{\\{voice actor name\\|(.+?)\\}\\}";
    return execPattern(pattern, text);
  }

  public static String parseImgName(String text) {
    String pattern = "Profile image\\nFile:(.+?)\\|Full artwork";
    return execPattern(pattern, text);
  }

  public static String parseModImgName(String text) {
    String pattern = "Upgrade profile image\\nFile:(.+?)\\|Digimind Upgrade full artwork";
    return execPattern(pattern, text);
  }

  public static String parseSkillName(String text) {
    String pattern = "name.*?colspan.*?\\|(.+?)\\n";
    return execPattern(pattern, text);
  }

  public static String parseSkillDescription(String text) {
    String[] skillDataTable = text.split("!");

    String pattern = "text.*?colspan.*?\\|(.+?)\\n";
    String desc = execPattern(pattern, text);

    pattern = "\\(\\$(.+?)\\)";

    Pattern r =
        Pattern.compile(
            pattern, Pattern.UNICODE_CHARACTER_CLASS | Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    Matcher m = r.matcher(desc);

    String toReturn = desc; // Strings are immutable

    while (m.find()) {
      String match = m.group(0);
      String group1 = m.group(1);

      for (String item : skillDataTable) {
        if (item.contains(desc)) {
          continue;
        }
        if (item.contains(group1)) {
          String[] splits = item.split("\\|\\|");
          String replace = splits[splits.length - 1].replaceAll("[^0-9.]", "");
          toReturn = toReturn.replace(match, replace);
        }
      }
    }

    return toReturn;
  }

  public static String parseSkillInitialCd(String text) {
    String pattern = "initial.*?colspan.*?\\|(.+?)\\n";
    return execPattern(pattern, text);
  }

  public static String parseSkillCd(String text) {
    String[] skillDataTable = text.split("!");

    for (String item : skillDataTable) {
      if (item.contains("cooldown") && item.contains("||")) {
        String[] splits = item.split("\\|\\|");
        return splits[splits.length - 1].replaceAll("[^0-9.]", "");
      }
    }

    return "";
  }

  public static boolean isMod(String text) {
    String pattern = "mod1_max_hp *=(.+?)\\n";
    return !ObjectUtils.isEmpty(execPattern(pattern, text));
  }

  public static String parseCompatibleTo(String text) {
    String pattern = "compatibleto *= *\\{\\{doll name\\|(.*?)\\|";
    return execPattern(pattern, text);
  }

  public static String parseSpecObtain(String text) {
    String[] splits = text.replace("[[", "").replace("]]", "").split("\\n");
    Optional<String> obtain =
        Arrays.stream(splits)
            .filter(item -> item.contains("Obtain") || item.contains("obtain"))
            .findFirst();
    return obtain.orElse("");
  }

  public static String parseSpecStats(String text) {
    int i = 1;
    StringBuilder stats = new StringBuilder();
    while (true) {
      String pattern = "stat" + i + " *=(.*?)\\n";
      String statName = execPattern(pattern, text);

      if (!statName.isEmpty()) {
        stats.append(statName).append(": ");

        pattern = "stat" + i + "min *=(.*?)\\n";
        String min = execPattern(pattern, text);
        Integer minInt = Integer.parseInt(min);

        pattern = "stat" + i + "max *=(.*?)\\n";
        String max = execPattern(pattern, text);
        Integer maxInt = max.isEmpty() ? minInt : Integer.parseInt(max);

        pattern = "stat" + i + "growth *=(.*?)\\n";
        String growth = execPattern(pattern, text);
        Float growthFlo = growth.isEmpty() ? null : Float.parseFloat(growth);

        if (maxInt > 0) {
          stats.append("+");
        }
        if (growthFlo != null) {
          stats.append(Math.round(maxInt * growthFlo));
        } else {
          stats.append(maxInt);
        }
        stats.append("\n");

      } else {
        break;
      }

      i++;
    }

    return stats.toString();
  }

  private static String execPattern(String pattern, String text) {
    Pattern r =
        Pattern.compile(
            pattern, Pattern.UNICODE_CHARACTER_CLASS | Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    Matcher m = r.matcher(text);

    StringBuilder toReturn = new StringBuilder();
    while (m.find()) {
      toReturn.append(m.group(1));
    }

    return toReturn.toString().trim();
  }

  private static List<String> auraDecode(String aura) {
    List<String> auras = new ArrayList<>();
    if (aura.contains("all")) {
      auras.add("ALL");
    }
    if (aura.contains("submachine")) {
      auras.add("SMG");
    }
    if (aura.contains("assault rifles") || aura.contains("assault rifle")) {
      auras.add("AR");
    }
    if (aura.contains("handguns")) {
      auras.add("HG");
    }
    if (aura.contains("shotguns")) {
      auras.add("SG");
    }
    if (aura.contains("machineguns") || aura.contains("machine guns")) {
      auras.add("MG");
    }

    return auras;
  }

  private static String modCyclePattern(String text, String pattern) {
    String toReturn = execPattern("\\| *" + pattern, text);
    for (int i = 1; i < 4; i++) {
      String exec = execPattern("mod" + i + "_" + pattern, text);
      if (!exec.equals("")) {
        toReturn = exec;
      }
    }
    return toReturn;
  }

  private static String removeHgAura(String text) {
    return text.replace("{", "").replace("}", "").replace("|", "").replace("HG aura", "");
  }

  public static String parseFairySkillName(String text) {
    String pattern = "skillname *=(.+?)\\n";
    return execPattern(pattern, text);
  }

  public static String parseFairySkillDescription(String text) {
    String pattern = "skilldesc *=(.+?)\\n";
    String desc = execPattern(pattern, text);

    pattern = "skillcost *=(.+?)\\n";
    String cost = execPattern(pattern, text);

    return ObjectUtils.isEmpty(cost) ? desc : MessageFormat.format("({0}) {1}", cost, desc);
  }

  public static String parseFairyStats(String text) {
    String pattern = "max_dmg *=(.+?)\\n";
    String dmg = execPattern(pattern, text);

    pattern = "max_eva *=(.+?)\\n";
    String eva = execPattern(pattern, text);

    pattern = "max_armor *=(.+?)\\n";
    String armor = execPattern(pattern, text);

    pattern = "max_critdmg *=(.+?)\\n";
    String crit = execPattern(pattern, text);

    pattern = "max_acc *=(.+?)\\n";
    String acc = execPattern(pattern, text);

    return MessageFormat.format(
        "Damage {0}, Accuracy {1}, Evasion {2}, Armor {3}, Critical Damage {4}",
        ObjectUtils.isEmpty(dmg) || dmg.contains("-->") ? "0%" : dmg,
        ObjectUtils.isEmpty(acc) || acc.contains("-->") ? "0%" : acc,
        ObjectUtils.isEmpty(eva) || eva.contains("-->") ? "0%" : eva,
        ObjectUtils.isEmpty(armor) || armor.contains("-->") ? "0%" : armor,
        ObjectUtils.isEmpty(crit) || crit.contains("-->") ? "0%" : crit);
  }

  public static String parseHocDmg(String text) {
    String pattern = "shellDmgMax *=(.+?)\\n";
    return execPattern(pattern, text);
  }

  public static String parseHocPiercing(String text) {
    String pattern = "defensePiercingMax *=(.+?)\\n";
    return execPattern(pattern, text);
  }

  public static String parseHocAccuracy(String text) {
    String pattern = "shellAccuracyMax *=(.+?)\\n";
    return execPattern(pattern, text);
  }

  public static String parseHocReload(String text) {
    String pattern = "reloadSpeedMax *=(.+?)\\n";
    return execPattern(pattern, text);
  }

  public static SFStats parseSfStats(String text) {
    String pattern = "\\| ?max_hp *= ?([0-9]*?)\\n";
    String hp = execPattern(pattern, text);

    pattern = "\\| ?max_dmg *= ?([0-9]*?)\\n";
    String dmg = execPattern(pattern, text);

    pattern = "\\| ?max_acc *= ?([0-9]*?)\\n";
    String acc = execPattern(pattern, text);

    pattern = "\\| ?max_eva *= ?([0-9]*?)\\n";
    String eva = execPattern(pattern, text);

    pattern = "\\| ?max_rof *= ?([0-9]*?)\\n";
    String rof = execPattern(pattern, text);

    pattern = "\\| ?critrate *=(.+?)\\n";
    String critCh = execPattern(pattern, text);

    pattern = "\\| ?armorpierce *= ?([0-9]*?)\\n";
    String armP = execPattern(pattern, text);

    pattern = "\\| ?armor *= ?([0-9]*?)\\n";
    String arm = execPattern(pattern, text);

    pattern = "\\| ?critdmg *=(.+?)\\n";
    String critD = execPattern(pattern, text);

    pattern = "\\| ?mov *= ?([0-9]*?)\\n";
    String mov = execPattern(pattern, text);

    SFStats sfStats = new SFStats();
    sfStats.setDamage(ObjectUtils.isEmpty(dmg) ? "" : dmg);
    sfStats.setAccuracy(ObjectUtils.isEmpty(acc) ? "" : acc);
    sfStats.setEvasion(ObjectUtils.isEmpty(eva) ? "" : eva);
    sfStats.setCriticalCh(ObjectUtils.isEmpty(critCh) ? "" : critCh);
    sfStats.setArmorPen(ObjectUtils.isEmpty(armP) ? "" : armP);
    sfStats.setRateOfFire(ObjectUtils.isEmpty(rof) ? "" : rof);
    sfStats.setArmor(ObjectUtils.isEmpty(arm) ? "" : arm);
    sfStats.setHp(ObjectUtils.isEmpty(hp) ? "" : hp);
    sfStats.setCriticalDmg(ObjectUtils.isEmpty(critD) ? "" : critD);
    sfStats.setMoveSpeed(ObjectUtils.isEmpty(mov) ? "" : mov);

    return sfStats;
  }

  public static String parseSfAura(String text) {
    String pattern = "\\| ?aura1 *=(.+?)\\n";
    String aura1 = execPattern(pattern, text).replace("'''", "");

    pattern = "\\| ?aura2 *=(.+?)\\n";
    String aura2 = execPattern(pattern, text).replace("'''", "");

    pattern = "\\| ?aura3 *=(.+?)\\n";
    String aura3 = execPattern(pattern, text).replace("'''", "");

    pattern = "\\| ?ana1_aura1 *=(.+?)\\n";
    String aura4 = execPattern(pattern, text).replace("'''", "");

    pattern = "\\| ?ana1_aura2 *=(.+?)\\n";
    String aura5 = execPattern(pattern, text).replace("'''", "");

    pattern = "\\| ?ana1_aura3 *=(.+?)\\n";
    String aura6 = execPattern(pattern, text).replace("'''", "");

    aura1 = ObjectUtils.isEmpty(aura1) ? aura1 : "\n" + aura1;
    aura2 = ObjectUtils.isEmpty(aura2) ? aura2 : "\n" + aura2;
    aura3 = ObjectUtils.isEmpty(aura3) ? aura3 : "\n" + aura3;
    aura4 = ObjectUtils.isEmpty(aura4) ? aura4 : "\n" + aura4;
    aura5 = ObjectUtils.isEmpty(aura5) ? aura5 : "\n" + aura5;
    aura6 = ObjectUtils.isEmpty(aura6) ? aura6 : "\n" + aura6;

    return MessageFormat.format(
        "Upgrade Level 1{0}\n\nUpgrade Level 2{1}", aura1 + aura2 + aura3, aura4 + aura5 + aura6);
  }

  public static String parseSfType(String text) {
    String pattern = "\\| ?class *=(.+?)\\n";
    return execPattern(pattern, text);
  }

  public static String parseSfImgName(String text) {
    String pattern = "\\.png.*\\nFile:(.+?) Upgrade.png.*?";
    String img = execPattern(pattern, text);
    if (!ObjectUtils.isEmpty(img)) {
      return img;
    }
    pattern = "\\.png.*\\n(.+?) Upgrade.png.*?";
    return execPattern(pattern, text);
  }
}
