package com.wds.ads.models;

public class Secure {
  private String ivHex;
  private String keyHex;

  public String getKeyHex() {
    return keyHex;
  }

  public void setKeyHex(String keyHex) {
    this.keyHex = keyHex;
  }

  public String getIvHex() {
    return ivHex;
  }

  public void setIvHex(String ivHex) {
    this.ivHex = ivHex;
  }
}
