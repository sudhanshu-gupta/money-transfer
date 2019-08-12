package io.sudhanshugupta.moneytransfer.errors;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ServiceException extends RuntimeException {

  private final ErrorEnum errorEnum;
  private Map<String, String> context = new HashMap<>();

  public ServiceException(ErrorEnum errorEnum, Map<String, String> context, Throwable ex) {
    super(ex);
    this.errorEnum = errorEnum;
    this.context = Optional.ofNullable(context).orElse(Collections.emptyMap());
  }
}
