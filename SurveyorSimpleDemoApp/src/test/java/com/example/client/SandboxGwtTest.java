package com.example.client;

import com.google.gwt.junit.client.GWTTestCase;

public class SandboxGwtTest extends GWTTestCase {
  
  @Override
  public String getModuleName() {
    return "com.example.Project";
  }

  public void testSandbox() {
    assertTrue(true);
  }

}
