package entwined.plugin;

import heronarts.lx.LX;
import heronarts.lx.pattern.LXPattern;

import java.util.Map;


public abstract class InputEventPattern extends LXPattern {

  public InputEventPattern(LX lx) {
        super(lx);
  }
  
  public abstract void onInputEvent(Map<String, Object> params);
  
  @Override
  protected void run(double deltaMs) {};
  
}
