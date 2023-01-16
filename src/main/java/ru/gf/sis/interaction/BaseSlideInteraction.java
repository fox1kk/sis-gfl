package ru.gf.sis.interaction;

import discord4j.core.object.component.Button;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.spec.EmbedCreateSpec;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.util.ObjectUtils;

import java.util.Collections;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseSlideInteraction extends Interaction {
  protected Integer currentPage;

  public static final String CUSTOM_ID_PREFIXES__RIGHT_ARROW = "right-arrow-";
  public static final String CUSTOM_ID_PREFIXES__LEFT_ARROW = "left-arrow-";

  protected List<Button> getSlideButtons(List<?> list) {
    if (ObjectUtils.isEmpty(list)) {
      return Collections.emptyList();
    }
    if (list.size() < 2) {
      return Collections.emptyList();
    }
    if (currentPage == 1) {
      return List.of(Button.success(CUSTOM_ID_PREFIXES__RIGHT_ARROW + interactionId, "→"));
    }
    if (currentPage == list.size()) {
      return List.of(Button.success(CUSTOM_ID_PREFIXES__LEFT_ARROW + interactionId, "←"));
    }
    return List.of(
        Button.success(CUSTOM_ID_PREFIXES__LEFT_ARROW + interactionId, "←"),
        Button.success(CUSTOM_ID_PREFIXES__RIGHT_ARROW + interactionId, "→"));
  }

  public abstract EmbedCreateSpec getContent();

  public abstract List<LayoutComponent> getButtons();

  public abstract void slideRight();

  public abstract void slideLeft();
}
