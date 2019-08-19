package io.sudhanshugupta.moneytransfer.model;

import java.io.Serializable;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ErrorResponse implements Serializable {
  private String code;
  private String message;
  private Map<String, String> context;
}
