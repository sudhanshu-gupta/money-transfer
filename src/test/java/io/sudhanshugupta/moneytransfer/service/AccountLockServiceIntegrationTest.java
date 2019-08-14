package io.sudhanshugupta.moneytransfer.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import io.lettuce.core.SetArgs;
import io.lettuce.core.api.sync.RedisCommands;
import io.quarkus.test.junit.QuarkusTest;
import io.sudhanshugupta.moneytransfer.errors.ErrorEnum;
import io.sudhanshugupta.moneytransfer.errors.LockAcquisitionException;
import java.io.IOException;
import javax.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import redis.embedded.RedisServer;

@QuarkusTest
class AccountLockServiceIntegrationTest {

  private static final String KEY_PLACEHOLDER = "acc:%d";
  private static final long LOCK_TIMEOUT_IN_MILLIS = 1000;
  private static RedisServer redisServer;
  @Inject
  RedisCommands<String, String> redisCommands;
  @Inject
  AccountLockService accountLockService;

  @Before
  public static void setUp() throws IOException {
    redisServer = new RedisServer(6379);
    redisServer.start();
  }

  @After
  public static void tearDown() {
    redisServer.stop();
  }

  @Test
  public void shouldAcquireAccountLock() {
    String key = String.format(KEY_PLACEHOLDER, 1L);
    accountLockService.acquireLock(1L);
    Long keyExist = redisCommands.exists(key);
    assertThat(keyExist).isEqualTo(1L);
  }

  @Test
  public void shouldThrowExceptionWhenKeyExist() {
    String key = String.format(KEY_PLACEHOLDER, 2L);
    redisCommands.set(key, key, SetArgs.Builder.nx().px(LOCK_TIMEOUT_IN_MILLIS));
    assertThatExceptionOfType(LockAcquisitionException.class)
        .isThrownBy(() -> accountLockService.acquireLock(2L))
        .matches(ex -> ex.getErrorEnum() == ErrorEnum.LOCK_ACQUISITION_EXCEPTION);
  }

  @Test
  public void shouldReleaseLockWhenKeyExistWithContent() {
    String key = String.format(KEY_PLACEHOLDER, 3L);
    redisCommands.set(key, key, SetArgs.Builder.nx().px(LOCK_TIMEOUT_IN_MILLIS));
    accountLockService.releaseLock(3L);
    Long keyExist = redisCommands.exists(key);
    assertThat(keyExist).isEqualTo(0L);
  }

  @Test
  public void shouldNotReleaseLockWhenKeyExistWithDifferentContent() {
    String key = String.format(KEY_PLACEHOLDER, 4L);
    redisCommands.set(key, "1", SetArgs.Builder.nx().px(LOCK_TIMEOUT_IN_MILLIS));
    accountLockService.releaseLock(4L);
    Long keyExist = redisCommands.exists(key);
    assertThat(keyExist).isEqualTo(1L);
  }
}