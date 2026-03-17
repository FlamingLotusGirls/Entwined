package entwined.pattern.katie_murphy;

import java.util.ArrayList;
import java.util.List;

import entwined.core.CubeData;
import entwined.core.CubeManager;
import entwined.utils.EntwinedUtils;
import heronarts.lx.LX;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.model.LXModel;
import heronarts.lx.modulator.SinLFO;



public class HavenFire extends LXPattern {
  final CompoundParameter speed     = new CompoundParameter("SPEED", 0.5, 0.1, 2.0);
  final CompoundParameter intensity = new CompoundParameter("INTNS", 0.6, 0.0, 1.0);
  final CompoundParameter hue = new CompoundParameter("HUE", 10, 0, 360);
  final CompoundParameter hueRange  = new CompoundParameter("HRANG", 40,  0,   90);

  // Per-point noise state
  private float[] phase;       // slowly drifting phase per point
  private float[] phaseSpeed;  // individual drift rate per point
  private float[] flicker;     // current brightness value (smoothed)

  private static final float TWO_PI = (float)(Math.PI * 2);

  public HavenFire(LX lx) {
    super(lx);
    addParameter("speed",     speed);
    addParameter("intensity", intensity);
    addParameter("hue",       hue);
    addParameter("hueRange",  hueRange);

    int n = model.points.length;
    phase      = new float[n];
    phaseSpeed = new float[n];
    flicker    = new float[n];

    for (int i = 0; i < n; i++) {
      phase[i]      = EntwinedUtils.random(0, TWO_PI);
      phaseSpeed[i] = EntwinedUtils.random(0.6f, 1.4f); // variation between points
      flicker[i]    = 0;
    }
  }

  @Override
  public void run(double deltaMs) {
    float dt       = (float)(deltaMs / 1000.0);
    float spd      = speed.getValuef();
    float intns    = intensity.getValuef();
    float baseHue  = hue.getValuef();
    float hRange   = hueRange.getValuef();

    for (LXModel component : model.children) {
      String tag = getTag(component); // helper below

      for (LXPoint pt : component.points) {
        CubeData cdata = CubeManager.getCube(lx, pt.index);
        float yn = (cdata.localY - model.yMin) / (model.yMax - model.yMin); // 0=bottom,1=top

        // --- advance per-point phase ---
        phase[pt.index] += dt * spd * phaseSpeed[pt.index] * TWO_PI * 0.8f;

        // --- multi-octave smooth noise (sum of sines as cheap stand-in) ---
        float noise =  0.50f * EntwinedUtils.sin(phase[pt.index])
                     + 0.30f * EntwinedUtils.sin(phase[pt.index] * 2.1f + 1.3f)
                     + 0.20f * EntwinedUtils.sin(phase[pt.index] * 4.7f + 0.7f);
        noise = (noise + 1.0f) * 0.5f; // remap to [0,1]

        // --- smooth the flicker value (low-pass) ---
        float smoothing = getSmoothing(tag); // faster for bulbs, slower for windows
        flicker[pt.index] += (noise - flicker[pt.index]) * smoothing;

        // --- height bias: brighter at bottom, dimmer at top ---
        float heightBias = 1.0f - (yn * yn); // quadratic falloff

        // --- per-fixture-type brightness and hue treatment ---
        float brt, pointHue;
        switch (tag) {
          case "singleBulb":
            // Full on/off pulses — embrace the drama, just smooth it
            brt = flicker[pt.index] * heightBias * intns * 100;
            // Hue shifts toward orange/yellow at peak, red at low
            pointHue = (baseHue + flicker[pt.index] * 50) % 360;
            break;

          case "window":
            // Windows look better with a high floor — never fully dark
            brt = (0.3f + 0.7f * flicker[pt.index]) * heightBias * intns * 100;
            pointHue = (baseHue + (1.0f - yn) * 60) % 360; // cooler hue higher up
            break;

          case "spiralFixture":
            // Spiral: use position along strip (yn) to create a rising wave
            float wave = EntwinedUtils.sin(phase[pt.index] - yn * TWO_PI * 1.5f);
            wave = (wave + 1.0f) * 0.5f;
            brt = wave * heightBias * intns * 100;
            pointHue = (baseHue + wave * hRange) % 360;
            break;

          default:
            // Fallback: basic flicker
            brt = flicker[pt.index] * heightBias * intns * 100;
            pointHue = baseHue;
            break;
        }

        brt = EntwinedUtils.max(0, EntwinedUtils.min(100, brt));
        float sat = 100 - flicker[pt.index] * 15; // 85-100 range
        colors[pt.index] = LX.hsb(pointHue, sat, brt);
      }
    }
  }

  // Returns a smoothing factor per frame — higher = faster response (less smooth)
  private float getSmoothing(String tag) {
    switch (tag) {
      case "singleBulb":    return 0.12f; // medium — punchy but not harsh
      case "window":        return 0.05f; // slow — glowy, languid
      case "spiralFixture": return 0.08f;
      default:              return 0.08f;
    }
  }

  // Pull first recognized tag from component
  private String getTag(LXModel component) {
    if (component.tags.contains("Spotlight"))    return "singleBulb";
    if (component.tags.contains("WindowPane"))        return "window";
    if (component.tags.contains("SpiralPortal")) return "spiralFixture";
    // add more tags here
    return "default";
  }
}

