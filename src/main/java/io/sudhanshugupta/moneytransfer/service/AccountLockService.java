package io.sudhanshugupta.moneytransfer.service;

import io.lettuce.core.SetArgs;
import io.lettuce.core.api.sync.RedisCommands;
import io.sudhanshugupta.moneytransfer.errors.LockAcquisitionException;
import javax.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class AccountLockService {

  private static final String LOCK_FORMAT = "acc:%d";
  private static final long LOCK_TIMEOUT = 1000;
  private final RedisCommands<String, String> redisCommands;

  public void acquireLock(long accountId) {
    String key = String.format(LOCK_FORMAT, accountId);
    String value = redisCommands.set(key, key, SetArgs.Builder.nx().px(LOCK_TIMEOUT));
    if (value == null) {
      throw new LockAcquisitionException();
    }
  }

  public void releaseLock(long accountId) {
    String key = String.format(LOCK_FORMAT, accountId);
    String value = redisCommands.get(key);
    if (value != null && value.equals(key)) {
      redisCommands.del(key);
    }
  }
}
