package ru.gf.sis.dto.lang;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServerLanguage {
  @Id private String id;
  private Long serverId;
  private String language;
}
