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

  final SinLFO brightnessWave = new SinLFO(0, 100, 2000);
  final SinLFO blueWave = new SinLFO(210, 256, 2000);
  final SinLFO greenWave = new SinLFO(140, 155, 2000);
  final SinLFO yellowWave = new SinLFO(40, 50, 2000);
  final SinLFO redWave = new SinLFO(7, 20, 2000);
  final SawLFO sawWave = new SawLFO(0, 360, 1000);

  // Spotlight: SinLFO drives saturation 0 (white) -> 100 (yellow), period 2s
  final SinLFO spotlightFade = new SinLFO(0, 100, 2000);
  private static final float SPOTLIGHT_HUE = 55f;

  // Body: cyan wave
  final SinLFO cyanWave   = new SinLFO(170, 190, 2300);
  // Cheek: orange wave
  final SinLFO orangeWave = new SinLFO(30,  45,  2000);

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
  

  public Sunset(LX lx) {
    super(lx);
    
    addModulator(brightnessWave).start();

    addParameter("brightness", fillParam);
    addParameter("cockatooWindowBrightness", cockatooFillParam);
    addParameter("ospreyWindowBrightness", ospreyFillParam);
    addParameter("magpieWindowBrightness", magpieFillParam);

    addModulator(sawWave).start();
    addModulator(blueWave).start();
    addModulator(greenWave).start();
    addModulator(yellowWave).start();
    addModulator(redWave).start();
    addModulator(spotlightFade).start();
    addModulator(cyanWave).start();
    addModulator(orangeWave).start();

    addEffect(new CockatooCheekPulseEffect(lx));
    addEffect(new SpinningStainedEffect(lx));
    addEffect(new CockatooJellyChandelierEffect(lx));
    addEffect(new MagpieWindowPulseEffect(lx));
    addEffect(new OspreyWindowBurstEffect(lx));
  }

  @Override
  public void run(double deltaMs) {    
    float velocity = 10f / 200f;
    float blobWidth = 90f;
    float blobHeight = 60f;
        
    height += deltaMs * velocity / blobHeight;

    float spotSat = spotlightFade.getValuef(); // 0 = white, 100 = yellow

    float pointnum = 0;
    for (LXModel component : model.children) {

      // --- Spotlight: white <-> yellow fade, skip all other logic ---
      if (component.tags.contains("Spotlight")) {
        float intns = fillParam.getValuef();
        for (LXPoint point : component.points) {
          colors[point.index] = LX.hsb(SPOTLIGHT_HUE, spotSat, intns);
        }
        continue;
      }

      // --- Body: cyan SinLFO ---
      if (component.tags.contains("Body")) {
        float intns = fillParam.getValuef();
        for (LXPoint point : component.points) {
          colors[point.index] = LX.hsb(cyanWave.getValuef(), 100, intns);
        }
        continue;
      }

      // --- Cheek: orange SinLFO ---
      if (component.tags.contains("Cheek")) {
        float intns = fillParam.getValuef();
        for (LXPoint point : component.points) {
          colors[point.index] = LX.hsb(orangeWave.getValuef(), 100, intns);
        }
        continue;
      }

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

}
