package ru.gf.sis.dto.google;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpreadSheetsList {
  String range;
  String majorDimension;
  List<List<String>> values;
}
