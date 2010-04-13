/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.speedtracer.client.model;

import com.google.gwt.junit.client.GWTTestCase;
import com.google.speedtracer.client.util.Url;

import java.util.List;

/**
 * Tests for the JavaScriptProfileNode class which represents a node in a parsed
 * profile.
 */
public class JavaScriptProfileNodeTests extends GWTTestCase {
  @Override
  public String getModuleName() {
    return "com.google.speedtracer.Common";
  }

  public void testJavaScriptProfileNodeHierarchy() {
    Url resourceUrl = new Url("");
    JavaScriptProfileNode topNode = new JavaScriptProfileNode(new JsSymbol(
        resourceUrl, 0, "top"));
    JavaScriptProfileNode child0 = new JavaScriptProfileNode(new JsSymbol(
        resourceUrl, 0, "child0"));
    topNode.addChild(child0);
    JavaScriptProfileNode child1 = new JavaScriptProfileNode(new JsSymbol(
        resourceUrl, 0, "child1"));
    topNode.addChild(child1);
    JavaScriptProfileNode child2 = new JavaScriptProfileNode(new JsSymbol(
        resourceUrl, 0, "child2"));
    child1.addChild(child2);

    List<JavaScriptProfileNode> topChildren = topNode.getChildren();
    assertEquals(2, topChildren.size());
    assertEquals(child0, topChildren.get(0));
    assertEquals("child0", topChildren.get(0).getSymbol().getSymbolName());
    assertEquals(child1, topChildren.get(1));
    assertEquals("child1", topChildren.get(1).getSymbol().getSymbolName());

    List<JavaScriptProfileNode> child1Children = topChildren.get(1).getChildren();
    assertEquals(1, child1Children.size());
    assertEquals(child2, child1Children.get(0));
    assertEquals("child2", child1Children.get(0).getSymbol().getSymbolName());
  }

  public void testJavaScriptProfileNodeMerge() {
    Url resourceUrl = new Url("");
    JavaScriptProfileNode child0 = new JavaScriptProfileNode(new JsSymbol(
        resourceUrl, 0, "child0"));
    child0.addSelfTime(1);
    child0.addTime(2);
    assertEquals(1.0, child0.getSelfTime(), .001);
    assertEquals(3.0, child0.getTime(), .001);

    JavaScriptProfileNode child1 = new JavaScriptProfileNode(new JsSymbol(
        resourceUrl, 0, "child1"));
    child1.addSelfTime(3);
    child1.addTime(10);
    assertEquals(3.0, child1.getSelfTime(), .001);
    assertEquals(13.0, child1.getTime(), .001);

    child0.merge(child1);
    assertEquals(3.0, child1.getSelfTime(), .001);
    assertEquals(13.0, child1.getTime(), .001);
    assertEquals(4.0, child0.getSelfTime(), .001);
    assertEquals(16.0, child0.getTime(), .001);
  }

  public void testJavaScriptProfileNodeTime() {
    Url resourceUrl = new Url("");
    JavaScriptProfileNode topNode = new JavaScriptProfileNode(new JsSymbol(
        resourceUrl, 0, "top"));
    JavaScriptProfileNode child0 = new JavaScriptProfileNode(new JsSymbol(
        resourceUrl, 0, "child0"));
    topNode.addChild(child0);
    JavaScriptProfileNode child1 = new JavaScriptProfileNode(new JsSymbol(
        resourceUrl, 0, "child1"));
    topNode.addChild(child1);
    JavaScriptProfileNode child2 = new JavaScriptProfileNode(new JsSymbol(
        resourceUrl, 0, "child2"));
    child1.addChild(child2);

    child0.addSelfTime(1.0);
    child0.addSelfTime(1.0);
    child1.addTime(1.0);
    child1.addTime(1.0);
    child1.addTime(1.0);
    child2.addTime(1.0);
    assertEquals(2.0, child0.getSelfTime(), .001);
    assertEquals(2.0, child0.getTime(), .001);
    assertEquals(0, child1.getSelfTime(), .001);
    assertEquals(3.0, child1.getTime(), .001);
    assertEquals(0, child2.getSelfTime(), .001);
    assertEquals(1.0, child2.getTime(), .001);
  }
}
