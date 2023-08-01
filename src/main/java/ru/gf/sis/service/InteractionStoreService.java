package ru.gf.sis.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import ru.gf.sis.interaction.Interaction;

@Service
public class InteractionStoreService {
  private final Map<String, Interaction> activeInteractions = new HashMap<>();

  public void clearInteractions() {
    activeInteractions
        .entrySet()
        .removeIf(item -> item.getValue().getCreated().isBefore(LocalDateTime.now().minusDays(1L)));
  }

  public void put(String id, Interaction interaction) {
    activeInteractions.put(id, interaction);
  }

  public Interaction get(String id) {
    return activeInteractions.get(id);
  }
}
