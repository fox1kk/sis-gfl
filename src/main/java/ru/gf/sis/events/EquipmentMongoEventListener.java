package ru.gf.sis.events;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import org.springframework.stereotype.Component;
import ru.gf.sis.dto.Equipment;
import ru.gf.sis.dto.analysis.Analytics;
import ru.gf.sis.repository.AnalyticsRepository;

import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class EquipmentMongoEventListener extends AbstractMongoEventListener<Equipment> {
  private final AnalyticsRepository analyticsRepository;

  @Override
  public void onAfterConvert(AfterConvertEvent<Equipment> event) {
    Equipment eq = event.getSource();

    List<Analytics> an = analyticsRepository.findByDollId(eq.getId());
    eq.setAnalysis(an);
  }
}
