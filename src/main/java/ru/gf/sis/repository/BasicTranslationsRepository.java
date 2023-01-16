package ru.gf.sis.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.gf.sis.dto.lang.BasicTranslations;

public interface BasicTranslationsRepository extends MongoRepository<BasicTranslations, String> {}
