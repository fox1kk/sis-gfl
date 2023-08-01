package ru.gf.sis.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import ru.gf.sis.dto.analysis.Analytics;

import java.util.ArrayList;
import java.util.List;

@Data
public abstract class StoredInfo implements SmallEmbed, FullEmbed {
  @Id private String id;

  private List<String> linkedEntitiesIds = new ArrayList<>();
  
  private Boolean wikiSync;

  @Transient private List<SpecialEquipment> specialEquipment = new ArrayList<>();

  @Transient private List<Analytics> analysis = new ArrayList<>();

  public abstract boolean isMod();

  public abstract String getStarRarity(Boolean mod);

  public abstract String getName();

  public void addLinkedEnt(String ent) {
    if (!linkedEntitiesIds.contains(ent)) {
      linkedEntitiesIds.add(ent);
    }
  }
}
