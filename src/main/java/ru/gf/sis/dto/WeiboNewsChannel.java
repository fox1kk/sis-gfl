package ru.gf.sis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WeiboNewsChannel {
  @Id private String id;
  private String channelId;
  private Long serverId;
}
