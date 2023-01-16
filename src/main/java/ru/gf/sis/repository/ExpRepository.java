package ru.gf.sis.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.gf.sis.dto.exp.ExpBase;

import java.util.List;

public interface ExpRepository<T extends ExpBase> extends MongoRepository<T, String> {
  T findByLvl(Integer lvl);

  List<T> findByLvlIn(List<Integer> lvls);
}
