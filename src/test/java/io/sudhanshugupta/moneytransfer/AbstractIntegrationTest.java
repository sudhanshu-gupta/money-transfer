package io.sudhanshugupta.moneytransfer;

import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import redis.embedded.RedisServer;

public abstract class AbstractIntegrationTest {

  protected static RedisServer redisServer;

  @BeforeAll
  public static void setUp() throws IOException {
    redisServer = new RedisServer(6379);
    redisServer.start();
  }

  @AfterAll
  public static void tearDown() {
    redisServer.stop();
  }
}
