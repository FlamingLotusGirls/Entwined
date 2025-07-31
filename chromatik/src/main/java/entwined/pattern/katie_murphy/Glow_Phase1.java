package entwined.pattern.katie_murphy;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.model.LXModel;


public class Glow_Phase1 extends LXPattern {

  final CompoundParameter speed = new CompoundParameter("SPEE", 11000, 100000, 1000).setExponent(0.5);
  final CompoundParameter smoothness = new CompoundParameter("SMOO", 100, 1, 100).setExponent(2);

  final SinLFO brightnessWave = new SinLFO(0, 100, 2000);
  final SinLFO saturationWave = new SinLFO(70, 100, 2000);

  /*final SinLFO blueWave = new SinLFO(220, 256, 2000);
  final SinLFO greenWave = new SinLFO(150, 150, 2000);
  final SinLFO yellowWave = new SinLFO(44, 44, 2000);
  final SinLFO redWave = new SinLFO(0, 0, 2000);*/
  final SinLFO bgWave = new SinLFO(150, 256, 1250);
  final SinLFO ryWave = new SinLFO(0, 35, 750);
  final SinLFO yellowWave = new SinLFO(44, 44, 2000);
  boolean switchColor = false;

  double time_ms = 0;

  public enum FixtureType {
    UNKNOWN,
    STAINED_GLASS,
    WINDOW_PANE,
    POOFER_BIRD,
    NEST_SURFACE
  };

  public Glow_Phase1(LX lx) {
    super(lx);
    
    addParameter("speed", speed);
    addParameter("smoothness", smoothness);
    addModulator(brightnessWave).start();
    addModulator(saturationWave).start();

    addModulator(bgWave).start();
    addModulator(ryWave).start();
  }

  public FixtureType getFixtureType(LXModel component){
    if (component.tags.contains("StainedGlass")) {
      return FixtureType.STAINED_GLASS;
    } else if (component.tags.contains("WindowPane")) {
      return FixtureType.WINDOW_PANE;
    } else if (component.tags.contains("PooferBird")) {
      return FixtureType.POOFER_BIRD;
    } else if (component.tags.contains("NestSurface")) {
      return FixtureType.NEST_SURFACE;
    } else {
      // error msg
      return FixtureType.UNKNOWN;
    }
  }

  @Override
  public void run(double deltaMs) {
    time_ms += deltaMs;
    
    for (LXModel component : model.children) {
      FixtureType fixture = getFixtureType(component);

      for (LXPoint point : component.points) {
        if (((time_ms < 2000) && (fixture == FixtureType.STAINED_GLASS))) {
          float hue = yellowWave.getValuef();
          float brightness = brightnessWave.getValuef();
          float saturation = saturationWave.getValuef();
          colors[point.index] = LX.hsb(hue, 70, brightness);
        } else if (((time_ms >= 2000) && (time_ms < 4000)) && (fixture == FixtureType.NEST_SURFACE)) {
          float hue = switchColor ? bgWave.getValuef() : ryWave.getValuef();
          float brightness = brightnessWave.getValuef();
          float saturation = saturationWave.getValuef();
          colors[point.index] = LX.hsb(hue, saturation, brightness);
        } else if (((time_ms >= 4000) && (time_ms < 6000)) && (fixture == FixtureType.WINDOW_PANE)) {
          float hue = switchColor ? ryWave.getValuef() : bgWave.getValuef();
          float brightness = brightnessWave.getValuef();
          float saturation = saturationWave.getValuef();
          colors[point.index] = LX.hsb(hue, saturation, brightness);
        } else if (((time_ms >= 6000) && (time_ms < 8000)) && (fixture == FixtureType.POOFER_BIRD)) {
          float hue = switchColor ? bgWave.getValuef() : ryWave.getValuef();
          float brightness = brightnessWave.getValuef();
          float saturation = saturationWave.getValuef();
          colors[point.index] = LX.hsb(hue, saturation, brightness);
        } else if (time_ms >= 8000) {
          switchColor = !switchColor;
          time_ms = 0;
          return;
        }
      }
    }
  }
}
