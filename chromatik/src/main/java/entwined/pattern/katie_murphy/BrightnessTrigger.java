// Fire pattern with tweaks for Haven installation
package entwined.pattern.katie_murphy;

import java.util.ArrayList;
import java.util.List;

import entwined.core.CubeData;
import entwined.core.CubeManager;
import entwined.core.TSTriggerablePattern;
import entwined.utils.EntwinedUtils;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.model.LXModel;
import heronarts.lx.modulator.SinLFO;



public class BrightnessTrigger extends TSTriggerablePattern {
  
  final SinLFO hueWave = new SinLFO(0, 50, 2000);
  final SinLFO rainbowWave = new SinLFO(0, 360, 1000);
  // todo tie to input event instead
  final CompoundParameter brightness = new CompoundParameter("brightnessTrigger", 30, 30, 100);
  private int sparkleAnimationLengthMs = 4 * 1000;
  private int sparkleAnimationTimeMs = 0;

  public BrightnessTrigger(LX lx) {
    super(lx);
    addModulator(hueWave).start();
    addModulator(rainbowWave).start();
    addParameter("brightnessTrigger", brightness);
  }

  @Override
  public void run(double deltaMs) {
    //if (getChannel().fader.getNormalized() == 0) return;
    if (brightness.getValue() == 100) {
      sparkleAnimationTimeMs += deltaMs;
      if (sparkleAnimationTimeMs > sparkleAnimationLengthMs) {
        brightness.setValue(20);
        sparkleAnimationTimeMs = 0;
      }
    }

    for (LXModel component : model.children) {
      for (LXPoint cube : component.points) {

        if (component.tags.contains("NestSurface")) {
          if (brightness.getValue() == 100) {
            colors[cube.index] = LX.hsb(rainbowWave.getValuef(), 100, brightness.getValuef());
          } else {
            colors[cube.index] = LX.hsb(hueWave.getValuef(), 100, brightness.getValuef());
          }
        }
      }
    } // end for component

  }

  @Override
  public void onTriggered() {
    super.onTriggered();
    //brightness = EntwinedUtils.min(brightness + 10, 100);
  };

  @Override
  public void onReleased() {
    super.onReleased();
  }
}

