package com.example.freevideo.engine;

/**
 * Created on 2022/11/3
 *
 * @author binlee
 */
public final class EngineFactory {

  private EngineFactory() {
    //no instance
  }

  public static Engine create(String text) {
    if (text == null) return null;

    if (text.contains(DyEngine.DOMAIN)) return new DyEngine(text);

    if (text.contains(KwEngine.DOMAIN)) return new KwEngine(text);

    return null;
  }
}
