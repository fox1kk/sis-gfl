package ru.gf.sis.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import ru.gf.sis.dto.Hoc;

import java.util.List;

public interface HocRepository extends MongoRepository<Hoc, String> {
  List<Hoc> findByAliases(String alias);

  @Query("{ 'name':{$regex:?0, $options: 'i'} }")
  List<Hoc> findByName(String name);

  List<Hoc> findAll();
}
