package ru.gf.sis.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import ru.gf.sis.dto.SpecialEquipment;

import java.util.List;
import java.util.Optional;

public interface SpecialEquipmentRepository extends MongoRepository<SpecialEquipment, String> {
  @Query("{_id: { $in: ?0 } })")
  List<SpecialEquipment> findByIds(List<String> ids);

  Optional<SpecialEquipment> findByName(String name);
}
