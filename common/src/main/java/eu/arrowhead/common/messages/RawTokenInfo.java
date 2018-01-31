/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.messages;

public class RawTokenInfo {

  private String s;
  private String c;
  private Long e;

  public RawTokenInfo() {
  }

  public String getS() {
    return s;
  }

  public void setS(String s) {
    this.s = s;
  }

  public String getC() {
    return c;
  }

  public void setC(String c) {
    this.c = c;
  }

  public Long getE() {
    return e;
  }

  public void setE(Long e) {
    this.e = e;
  }


  @Override
  public String toString() {
    return "ClassPojo [s = " + s + ", c = " + c + ", e = " + e + "]";
  }
}