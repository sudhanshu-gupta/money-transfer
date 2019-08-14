package io.sudhanshugupta.moneytransfer.service;

import io.lettuce.core.SetArgs;
import io.lettuce.core.api.sync.RedisCommands;
import io.sudhanshugupta.moneytransfer.errors.LockAcquisitionException;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class AccountLockService {

  private static final String LOCK_FORMAT = "acc:%d";
  private static final long LOCK_TIMEOUT_IN_MILLIS = 1000;
  @Inject
  RedisCommands<String, String> redisCommands;

  public void acquireLock(long accountId) {
    String key = String.format(LOCK_FORMAT, accountId);
    String value = redisCommands.set(key, key, SetArgs.Builder.nx().px(LOCK_TIMEOUT_IN_MILLIS));
    if (value == null) {
      throw new LockAcquisitionException();
    }
    log.info("Lock Acquired, account={}", accountId);
  }

  public void releaseLock(long accountId) {
    String key = String.format(LOCK_FORMAT, accountId);
    String value = redisCommands.get(key);
    if (value != null && value.equals(key)) {
      redisCommands.del(key);
      log.info("Lock Released, account={}", accountId);
    }
  }
}
