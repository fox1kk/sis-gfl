package ru.gf.sis.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import ru.gf.sis.dto.TDoll;

import java.util.List;

public interface TDollRepository extends MongoRepository<TDoll, String> {
  List<TDoll> findByAliases(String alias);

  TDoll findByDollId(String id);

  List<TDoll> findByProductTime(String productTime);

  @Query("{ 'gunName':{$regex:?0, $options: 'i'} }")
  List<TDoll> findByGunNameRegex(String gunName);

  TDoll findByGunName(String gunName);

  List<TDoll> findAll();
}
