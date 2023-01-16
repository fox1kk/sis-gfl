package ru.gf.sis.dto.exp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class ExpBase {
  @Id private String id;
  private Integer lvl;
  private Integer expToLvlUp;
}
