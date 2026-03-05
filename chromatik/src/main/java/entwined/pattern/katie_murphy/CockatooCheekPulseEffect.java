package entwined.pattern.katie_murphy;

import heronarts.lx.color.LXColor;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.utils.LXUtils;
import heronarts.lx.model.LXModel;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@LXCategory("Custom")
public class CockatooCheekPulseEffect extends LXEffect {

    private static final int MAX_PULSES = 4;
    private static final double PULSE_DURATION_MS = 3000;

    public final ColorParameter pulseColor = 
    new ColorParameter("Color", 0xFFFF0000);  // default red

    public final BooleanParameter trigger =
        new BooleanParameter("cCheek", false)
            .setMode(BooleanParameter.Mode.MOMENTARY)
            .setDescription("Trigger a brightness pulse");

    private int pulseCount = 0;
    private double pulseTimer = 0;
    private boolean isPulsing = false;

    // Cached list of points belonging to tagged sub-models
    private final List<LXPoint> taggedPoints = new ArrayList<>();

    private float currentPeakBrightness = 20f;
    private final java.util.Random random = new java.util.Random();

    public CockatooCheekPulseEffect(LX lx) {
        super(lx);
        addParameter("cockatooCheekTrigger", trigger);
        addParameter("pulseColor", this.pulseColor);

        rebuildTaggedPoints();
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == this.trigger && this.trigger.isOn()) {
        pulseCount = (pulseCount % MAX_PULSES) + 1;
        float stepT = (pulseCount - 1) / (float)(MAX_PULSES - 1);
        currentPeakBrightness = LXUtils.lerpf(20f, 100f, stepT);
        pulseTimer = 0;
        isPulsing = true;
      }
    }

    private void rebuildTaggedPoints() {
        taggedPoints.clear();
        // model.sub(tag) returns all sub-models at any depth matching the tag
        for (LXModel sub : model.sub("Cheek")) {
          taggedPoints.addAll(Arrays.asList(sub.points));
        }
    }

  @Override
  public void run(double deltaMs, double enabledAmount) {
    if (!isPulsing || taggedPoints.isEmpty()) return;

    pulseTimer += deltaMs;
    if (pulseTimer >= PULSE_DURATION_MS) {
        isPulsing = false;
        pulseTimer = 0;
        return;
    }

    float t = (float)(pulseTimer / PULSE_DURATION_MS);

    // Pulse envelope: sharp attack, long decay
    float pulseShape = t < 0.1f
        ? t / 0.1f
        : 1f - ((t - 0.1f) / 0.9f);

    int c = this.pulseColor.getColor();
    float h = LXColor.h(c);
    float s = LXColor.s(c);

    if (pulseCount == MAX_PULSES) {
        // Sparkle party — ignore envelope, run at full brightness for whole duration
        for (LXPoint p : taggedPoints) {
            if (random.nextFloat() < 0.15f) {
                // Sparkle point: bright flash with randomized hue
                float sparkleH = (h + random.nextFloat() * 60f - 30f + 360f) % 360f;
                float sparkleB = LXUtils.lerpf(60f, 100f, random.nextFloat()) * (float)enabledAmount;
                colors[p.index] = LXColor.hsb(sparkleH, s, sparkleB);
            } else {
                // Non-sparkle points: dim base so sparkles pop
                colors[p.index] = LXColor.hsb(h, s, 20f * (float)enabledAmount);
            }
        }
    } else {
        // Normal pulse: all points follow the brightness envelope
        float brightness = currentPeakBrightness * pulseShape * (float)enabledAmount;
        for (LXPoint p : taggedPoints) {
            colors[p.index] = LXColor.hsb(h, s, brightness);
        }
    }
  }
}
