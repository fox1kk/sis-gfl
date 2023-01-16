package ru.gf.sis.interaction;

import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.gf.sis.dto.StoredInfo;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class SelectMenuInteraction extends Interaction {
  private List<? extends StoredInfo> entities;

  public static final String CUSTOM_ID_PREFIXES__SELECT_MENU = "info-select-menu-";

  public ActionRow getSelectMenu() {
    return ActionRow.of(
        SelectMenu.of(
                CUSTOM_ID_PREFIXES__SELECT_MENU + interactionId,
                entities.stream()
                    .map(item -> SelectMenu.Option.of(item.getName(), item.getId()))
                    .collect(Collectors.toList()))
            .withMaxValues(1));
  }

  public Optional<? extends StoredInfo> getEntityById(String id) {
    return entities.stream().filter(item -> item.getId().equals(id)).findFirst();
  }

  public boolean isOnlyOneEntity() {
    return entities.size() == 1;
  }

  public StoredInfo getOnlyOneEntity() {
    return entities.get(0);
  }
}
