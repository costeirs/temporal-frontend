package com.kinandcarta.cjug.temporalfrontend.models;

import lombok.Data;

@Data
public class Claim {
  private final String id;
  private final String name;
  private final String description;
  private final String status;
}
