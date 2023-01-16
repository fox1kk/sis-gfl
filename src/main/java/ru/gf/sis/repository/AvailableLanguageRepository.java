package ru.gf.sis.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.gf.sis.dto.AvailableLanguage;

public interface AvailableLanguageRepository extends MongoRepository<AvailableLanguage, String> {
  AvailableLanguage findByLanguage(String language);
  AvailableLanguage findByServerSetShortcut(String serverSetShortcut);
}
