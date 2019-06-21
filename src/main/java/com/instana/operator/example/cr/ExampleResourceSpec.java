package com.instana.operator.example.cr;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Arrays;
import java.util.Set;

@JsonDeserialize
public class ExampleResourceSpec {

  @JsonProperty("specialRoles")
  private Set<String> specialRoles;

  public Set<String> getSpecialRoles() {
    return specialRoles;
  }

  @Override
  public String toString() {
    return "specialRoles=" + Arrays.toString(specialRoles.toArray(new String[] {}));
  }
}
