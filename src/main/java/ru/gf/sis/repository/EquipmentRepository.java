package ru.gf.sis.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import ru.gf.sis.dto.Equipment;

import java.util.List;

public interface EquipmentRepository extends MongoRepository<Equipment, String> {
  List<Equipment> findByProductTime(String productTime);

  @Query("{ 'name':{$regex:?0, $options: 'i'}, 'type':'Fairy'}")
  List<Equipment> findByNameAndTypeFairy(String name);

  List<Equipment> findByType(String type);

  List<Equipment> findByAliases(String alias);
}
