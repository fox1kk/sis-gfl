package ru.gf.sis.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.gf.sis.dto.analysis.Analytics;

import java.util.List;
import java.util.Optional;

public interface AnalyticsRepository extends MongoRepository<Analytics, String> {
  List<Analytics> findByDollId(String dollId);

  Optional<Analytics> findByDollIdAndMod(String dollId, Boolean mod);

  Optional<Analytics> findByNameAndMod(String name, Boolean mod);
}
