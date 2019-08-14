package io.sudhanshugupta.moneytransfer.configuration;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.Utf8StringCodec;
import java.io.IOException;
import java.time.Duration;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import redis.embedded.RedisServer;

@Slf4j
@ApplicationScoped
public class RedisConfig {

  @ConfigProperty(name = "quarkus.redis.host")
  String host;
  @ConfigProperty(name = "quarkus.redis.port", defaultValue = "6379")
  Integer port;
  RedisServer redisServer;
  @PostConstruct
  public void init() throws IOException {
    redisServer = new RedisServer(6379);
    redisServer.start();
  }

  @Produces
  public RedisCommands<String, String> redisCommands() {
    RedisURI redisURI = RedisURI.Builder
        .redis(host, port)
        .withTimeout(Duration.ofSeconds(1))
        .withDatabase(1).build();
    RedisClient redisClient = RedisClient.create(redisURI);
    StatefulRedisConnection<String, String> connection = redisClient.connect(new Utf8StringCodec());
    return connection.sync();
  }

  @PreDestroy
  public void destroy() {
    redisServer.stop();
  }
}
