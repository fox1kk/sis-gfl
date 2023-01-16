package ru.gf.sis.dto.google;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Page {
  HG("HG!", "1355884057"),
  RF("RF!", "2064854378"),
  SMG("SMG!", "1258458002"),
  AR("AR!", "1461310240"),
  MG("MG!", "784427543"),
  SG("SG!", "1721860152"),
  FAIRY("Fairies!", "1082531729");

  private final String page;
  private final String gid;
}
