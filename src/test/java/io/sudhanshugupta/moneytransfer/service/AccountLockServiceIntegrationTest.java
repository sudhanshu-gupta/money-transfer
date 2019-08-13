package io.sudhanshugupta.moneytransfer.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import io.lettuce.core.SetArgs;
import io.lettuce.core.api.sync.RedisCommands;
import io.quarkus.test.junit.QuarkusTest;
import io.sudhanshugupta.moneytransfer.AbstractIntegrationTest;
import io.sudhanshugupta.moneytransfer.errors.ErrorEnum;
import io.sudhanshugupta.moneytransfer.errors.LockAcquisitionException;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AccountLockServiceIntegrationTest extends AbstractIntegrationTest {

  private static final String KEY_PLACEHOLDER = "acc:%d";
  private static final long LOCK_TIMEOUT_IN_MILLIS = 1000;
  @Inject
  RedisCommands<String, String> redisCommands;
  @Inject
  AccountLockService accountLockService;

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