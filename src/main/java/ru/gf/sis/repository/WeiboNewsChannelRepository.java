package ru.gf.sis.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.gf.sis.dto.WeiboNewsChannel;

import java.util.Optional;

public interface WeiboNewsChannelRepository extends MongoRepository<WeiboNewsChannel, String> {
  Optional<WeiboNewsChannel> findByChannelId(String channelId);
}
