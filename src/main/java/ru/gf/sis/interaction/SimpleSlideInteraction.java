package ru.gf.sis.interaction;

import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.spec.EmbedCreateSpec;
import java.util.Collections;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.util.ObjectUtils;
import ru.gf.sis.dto.StoredInfo;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class SimpleSlideInteraction extends BaseSlideInteraction {
  private List<? extends StoredInfo> entities;

  public List<LayoutComponent> getButtons() {
    List<Button> buttons = getSlideButtons(entities);
    if (ObjectUtils.isEmpty(buttons)) {
      return Collections.emptyList();
    }
    return List.of(ActionRow.of(buttons));
  }

  public void slideRight() {
    if (currentPage >= entities.size()) {
      return;
    }
    currentPage++;
  }

  public void slideLeft() {
    if (currentPage <= 1) {
      return;
    }
    currentPage--;
  }

  public EmbedCreateSpec getContent() {
    return entities
        .get(currentPage - 1)
        .fillSmallEmbed(language, currentPage + "/" + entities.size());
  }
}
