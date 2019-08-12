package io.sudhanshugupta.moneytransfer.configuration;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.Utf8StringCodec;
import java.time.Duration;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Dependent
public class RedisConfig {

  @ConfigProperty(name = "quarkus.redis.host")
  private String host;
  @ConfigProperty(name = "quarkus.redis.port", defaultValue = "6379")
  private Integer port;

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
}
