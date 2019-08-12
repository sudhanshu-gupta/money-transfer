package io.sudhanshugupta.moneytransfer.resource.handler;

import io.sudhanshugupta.moneytransfer.model.ErrorResponse;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ExceptionUtil {

  ErrorResponse errorResponse(String code, String message,
      Map<String, String> context) {
    return ErrorResponse.builder()
        .code(code)
        .message(message)
        .context(Optional.ofNullable(context).orElse(Collections.emptyMap()))
        .build();
  }
}
