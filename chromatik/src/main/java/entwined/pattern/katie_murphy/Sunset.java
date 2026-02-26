package entwined.pattern.katie_murphy;

import heronarts.lx.color.LXColor;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.pattern.LXPattern;
import entwined.utils.SimplexNoise;
import heronarts.lx.model.LXModel;

import entwined.core.CubeManager;
import entwined.utils.EntwinedUtils;


public class Sunset extends LXPattern {
  static final int cheekFlashDurationMs = 3000;

  final SinLFO brightnessWave = new SinLFO(0, 100, 2000);
  final SinLFO blueWave = new SinLFO(210, 256, 2000);
  final SinLFO greenWave = new SinLFO(140, 155, 2000);
  final SinLFO yellowWave = new SinLFO(40, 50, 2000);
  final SinLFO redWave = new SinLFO(0, 20, 2000);
  final SawLFO sawWave = new SawLFO(0, 360, 1000);

  private float minz = Float.MAX_VALUE;
  private float maxz = -Float.MAX_VALUE;

  float height = 0.0f;
  float sawHeight = 0.0f;

  final CompoundParameter fillParam = new CompoundParameter("BRT", 25f, 0.001f,
        100f);
  final CompoundParameter cockatooFillParam = new CompoundParameter("cWinBrt", 25, 10,
        100);
  final CompoundParameter ospreyFillParam = new CompoundParameter("oWinBrt", 25f, 0.001f,
        100f);
  final CompoundParameter magpieFillParam = new CompoundParameter("mWinBrt", 25f, 0.001f,
        100f);

  private boolean cockatooCheekFlashActive = false;
  private double cockatooCheekFlashEnd = 0;
  final BooleanParameter cockatooCheekParam = new BooleanParameter("cCheek", false).setMode(BooleanParameter.Mode.MOMENTARY);

  // 0 = 0ff, 1-3 = low/med/high
  public final DiscreteParameter cockatooWindowSpin = new DiscreteParameter("cWinSpin", 0, 4);
  

  public Sunset(LX lx) {
    super(lx);
    
    addModulator(brightnessWave).start();

    addParameter("brightness", fillParam);
    addParameter("cockatooWindowBrightness", cockatooFillParam);
    addParameter("ospreyWindowBrightness", ospreyFillParam);
    addParameter("magpieWindowBrightness", magpieFillParam);

    addParameter("cockatooCheekTrigger", cockatooCheekParam);
    cockatooCheekParam.addListener(p -> {
      if (cockatooCheekParam.getValueb() && !cockatooCheekFlashActive) {
        startCheekFlash();
      }
    });

    addParameter(cockatooWindowSpin);

    addModulator(sawWave).start();
    addModulator(blueWave).start();
    addModulator(greenWave).start();
    addModulator(yellowWave).start();
    addModulator(redWave).start();
  }

  private void renderDefault(double deltaMs) {
    float velocity = 10f / 200f;
    float blobWidth = 90f;
    float blobHeight = 60f;
        
    height += deltaMs * velocity / blobHeight;

    float pointnum = 0;
    for (LXModel component : model.children) {

      float fill = fillParam.getValuef() / 50f;

      float componentHeight = component.yMax - component.yMin;
      float componentWidth = component.zMax - component.zMin;
          
      for (LXPoint point : component.points) {
        float hue = 0;
        if (component.tags.contains("WindowPane")) {

          if (component.tags.contains("CockatooSegment")) {
            fill = cockatooFillParam.getValuef() / 50f;
          } else if (component.tags.contains("OspreySegment")) {
            fill = ospreyFillParam.getValuef() / 50f;
          } else if (component.tags.contains("MagpieSegment")) {
            fill = magpieFillParam.getValuef() / 50f;
          }

          float y = CubeManager.getCube(lx, point.index).localY;
          if (y >= component.yMin + (componentHeight * .70f)) {
            hue = blueWave.getValuef();
          } else if (y >= component.yMin + (componentHeight * .55f)) {
            hue = greenWave.getValuef();
          } else if (y >= component.yMin + (componentHeight * .25f)) {
            hue = yellowWave.getValuef();
          } else {
            hue = redWave.getValuef();
          }
        } else if (component.tags.contains("Cheek")) {
            // skip cockatoo cheek
            continue;
        } else {
          hue = blueWave.getValuef();
        }

        pointnum++;

        // FreeFall math to give it a bit of glimmer
        float baseline = Math.max(0f, 1f - fill);
        float boost = Math.max(0f, fill - 1f);

        float noise1 = 0.5f
            + (float) SimplexNoise.noise(point.x / blobWidth,
                point.z / blobWidth, height + point.y / blobHeight) / 2f;

        float cutoffNoise1 = Math.min(1.0f, Math.max(0.0f,
            (noise1 + boost - baseline) / (1.0f - boost - baseline)));
        float brightness = Float.max(40, cutoffNoise1 * 100f);

        colors[point.index] = LX.hsb(hue, 85, brightness);
      }
    }
  }

  private void applyTagOverrides(double deltaMs) {
    sawHeight = sawWave.getValuef();

    for (LXModel component : model.children) {
      for (LXPoint point : component.points) {

        if (component.tags.contains("Cheek")) {
          applyCockatooCheekOverrides(deltaMs, point);
        } else if (component.tags.contains("CockatooSegment") && component.tags.contains("WindowPane")) {
          applyCockatooWindowOverrides(deltaMs, point);
        }

      }
    }
  }

  @Override
  public void run(double deltaMs) {
    double now = EntwinedUtils.millis();
    
    // check for expired timers
    if (cockatooCheekFlashActive && (now > cockatooCheekFlashEnd)) {
      cockatooCheekFlashActive = false;
    }

    // set the base pattern
    renderDefault(deltaMs);    

    // do trigger overrides 
    applyTagOverrides(deltaMs); 
  }

  /////////////// Cockatoo Cheek Functions
  private void applyCockatooCheekOverrides(double deltaMs, LXPoint point) {
    if (cockatooCheekFlashActive) {
      double remainingMs = cockatooCheekFlashEnd - EntwinedUtils.millis();
      float t = (float)(remainingMs / cheekFlashDurationMs);
      colors[point.index] = LXColor.lerp(colors[point.index], LXColor.RED, t);
    }
  }

  private void startCheekFlash() {
    cockatooCheekFlashActive = true;
    cockatooCheekFlashEnd = EntwinedUtils.millis() + cheekFlashDurationMs;
  }

  private void applyCockatooWindowOverrides(double deltaMs, LXPoint point) {

    if (cockatooWindowSpin.getValue() > 0) {
    colors[point.index] = HavenDefaultInputsUtils.SpinningStainedRun(lx, point, sawHeight * cockatooWindowSpin.getValuef());
    }
  }

}
