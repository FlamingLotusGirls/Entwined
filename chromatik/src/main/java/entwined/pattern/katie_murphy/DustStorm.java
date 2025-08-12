package entwined.pattern.katie_murphy;

import entwined.utils.EntwinedUtils;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXModel;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;

// Sparkle pattern with some tweaks to speed up chaos, make it dusty hue, surge

public class DustStorm extends LXPattern {

  private SinLFO[] bright;
  final CompoundParameter brightnessParam = new CompoundParameter("Brightness", 0.8, 0.5, 1);
  final int numBrights = 18;
  final int density = 20;
  int[] sparkleTimeOuts;
  int[] pointToModulatorMapping;
  final double surgeIntervalMs = 20 * 1000;
  double timeUntilNextSurgeMs = surgeIntervalMs;
  final double surgeLengthMs = 5 * 1000;
  double surgeTimeLeftMs = surgeLengthMs;
  boolean doSurge = false;

  public DustStorm(LX lx) {
    super(lx);
    addParameter("brightness", brightnessParam);

    sparkleTimeOuts = new int[model.points.length];
    pointToModulatorMapping = new int[model.points.length];

    for (int i = 0; i < pointToModulatorMapping.length; i++ ) {
      pointToModulatorMapping[i] = (int)EntwinedUtils.random(numBrights);
    }

    bright = new SinLFO[numBrights];
    int numLight = density / 100 * bright.length; // number of brights array that are most bright
    int numDarkReverse = (bright.length - numLight) / 2; // number of brights array that go from light to dark

    for (int i = 0; i < bright.length; i++ ) {
      if (i <= numLight) {
        if (EntwinedUtils.random(1) < 0.5f) {
          bright[i] = new SinLFO((int)EntwinedUtils.random(80, 100), 0, (int)EntwinedUtils.random(2300, 7700));
        }
        else {
          bright[i] = new SinLFO(0, (int)EntwinedUtils.random(70, 90), (int)EntwinedUtils.random(5300, 9200));
        }
      }
      else if ( i < numDarkReverse ) {
        bright[i] = new SinLFO((int)EntwinedUtils.random(50, 70), 0, (int)EntwinedUtils.random(3300, 11300));
      }
      else {
        bright[i] = new SinLFO(0, (int)EntwinedUtils.random(30, 80), (int)EntwinedUtils.random(3100, 9300));
      }
      addModulator(bright[i]).start();
    }
  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    timeUntilNextSurgeMs -= deltaMs;
    if (timeUntilNextSurgeMs <= 0)
    {
      doSurge = true;
      surgeTimeLeftMs -= deltaMs;
      if (surgeTimeLeftMs <= 0)
      {
        doSurge = false;
        timeUntilNextSurgeMs = surgeIntervalMs;
        surgeTimeLeftMs = surgeLengthMs;
      }
    }

    for (LXModel component : model.children) {
      for (LXPoint point : component.points) {

        if (sparkleTimeOuts[point.index] < EntwinedUtils.millis()) {
          // randomly change modulators
          if (EntwinedUtils.random(10) <= 3) {
            pointToModulatorMapping[point.index] = (int)EntwinedUtils.random(numBrights);
          }
          sparkleTimeOuts[point.index] = EntwinedUtils.millis() + (int)EntwinedUtils.random(10, 80);
        }

        float brightness = bright[pointToModulatorMapping[point.index]].getValuef() * brightnessParam.getValuef();
        if (doSurge)
        {
          brightness = EntwinedUtils.min(100, brightness + 20);
        }

        // special fixture considerations
        if (component.tags.contains("Cheek")) {
          // skip cockatoo cheeks
          break;
        }
        if (component.tags.contains("NestSurface")) {
          // prevent flicker off on the spotlights for smoother effect
          brightness = EntwinedUtils.max(30, brightness);
        }

        colors[point.index] = LX.hsb(44, 70, brightness);
      }
    }
  }

}

