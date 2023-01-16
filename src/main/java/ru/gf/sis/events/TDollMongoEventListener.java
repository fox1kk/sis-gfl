package ru.gf.sis.events;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import ru.gf.sis.dto.SpecialEquipment;
import ru.gf.sis.dto.TDoll;
import ru.gf.sis.dto.analysis.Analytics;
import ru.gf.sis.repository.AnalyticsRepository;
import ru.gf.sis.repository.SpecialEquipmentRepository;

import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class TDollMongoEventListener extends AbstractMongoEventListener<TDoll> {
  private final SpecialEquipmentRepository specialEquipmentRepository;
  private final AnalyticsRepository analyticsRepository;

  @Override
  public void onAfterConvert(AfterConvertEvent<TDoll> event) {
    TDoll tDoll = event.getSource();

    List<SpecialEquipment> ex = specialEquipmentRepository.findByIds(tDoll.getLinkedEntitiesIds());

    if (!ObjectUtils.isEmpty(ex)) {
      tDoll.setSpecialEquipment(ex);
    }

    List<Analytics> an = analyticsRepository.findByDollId(tDoll.getId());
    tDoll.setAnalysis(an);
  }
}
