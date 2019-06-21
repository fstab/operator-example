package com.instana.operator.example.cr;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

@JsonDeserialize
public class ExampleStatus implements KubernetesResource {}
