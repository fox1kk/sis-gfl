package ru.gf.sis.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.gf.sis.dto.lang.ServerLanguage;

import java.util.Optional;

public interface ServerLanguageRepository extends MongoRepository<ServerLanguage, String> {
  Optional<ServerLanguage> findByServerId(Long serverId);
}
