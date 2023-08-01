package ru.gf.sis.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import ru.gf.sis.dto.SFBoss;

import java.util.List;

public interface SFBossRepository extends MongoRepository<SFBoss, String> {
  List<SFBoss> findByAliases(String alias);

  @Query("{ 'name':{$regex:?0, $options: 'i'} }")
  List<SFBoss> findByNameRegEx(String name);
  
  SFBoss findByName(String name);

  List<SFBoss> findAll();
}
