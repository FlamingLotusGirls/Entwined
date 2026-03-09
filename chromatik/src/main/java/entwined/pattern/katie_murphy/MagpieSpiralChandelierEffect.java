package entwined.pattern.katie_murphy;

import entwined.utils.EntwinedUtils;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.LXComponent;
import heronarts.lx.color.LXColor;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.utils.LXUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@LXCategory("Custom")
public class MagpieSpiralChandelierEffect extends LXEffect {

    // Speed of the breathing oscillation
    public final CompoundParameter speed =
        new CompoundParameter("Speed", 1.0, 0.1, 5.0)
            .setDescription("Speed of bubble breathing");

    // How far the bubbles travel up the spiral
    public final CompoundParameter spread =
        new CompoundParameter("Spread", 0.3, 0.0, 1.0)
            .setDescription("Vertical spread of each bubble");

    // How many bubble waves at once
    public final CompoundParameter waves =
        new CompoundParameter("Waves", 4.79, 1.0, 8.0)
            .setDescription("Number of bubble waves");

    // Brightness of the bubble peaks
    public final CompoundParameter intensity =
        new CompoundParameter("Intensity", 80.0, 0.0, 100.0)
            .setDescription("Peak brightness of bubbles");

    private double time = 0;
    private double magpieSpiralDurationMs = 5000;
    private double magpieSpiralEnd = 0;
    private boolean magpieSpiralActive = false;
    final BooleanParameter magpieSpiralChandelierParam = new BooleanParameter("mChand", false).setMode(BooleanParameter.Mode.MOMENTARY);

    // Cached list of points belonging to tagged sub-models
    private final List<LXPoint> taggedPoints = new ArrayList<>();

    public MagpieSpiralChandelierEffect(LX lx) {
        super(lx);
        addParameter("speed", this.speed);
        addParameter("spread", this.spread);
        addParameter("waves", this.waves);
        addParameter("intensity", this.intensity);

        addParameter("magpieSpiralTrigger", magpieSpiralChandelierParam);
        magpieSpiralChandelierParam.addListener(p -> {
            if (magpieSpiralChandelierParam.getValueb() && !magpieSpiralActive) {
                magpieSpiralActive = true;
                magpieSpiralEnd = EntwinedUtils.millis() + magpieSpiralDurationMs;
            }
        });

        rebuildTaggedPoints();
    }

    private void rebuildTaggedPoints() {
        taggedPoints.clear();
        // model.sub(tag) returns all sub-models at any depth matching the tag
        for (LXModel sub : model.sub("SpiralPortal")) {
          taggedPoints.addAll(Arrays.asList(sub.points));
        }
    }

  @Override
  public void run(double deltaMs, double enabledAmount) {
    if (EntwinedUtils.millis() > magpieSpiralEnd) {
            magpieSpiralActive = false;
    }

    time += deltaMs * speed.getValue();

    float numWaves = waves.getValuef();
    float spreadVal = spread.getValuef();
    float intensityVal = intensity.getValuef();
    float timePhase = (float)(time * 0.001f);

    if (magpieSpiralActive) {
      int numPoints = taggedPoints.size();
      for (int i = 0; i < numPoints; i++) {
        LXPoint p = taggedPoints.get(i);
        int base = colors[p.index];

        // Use normalized position along the spiral arc (0-1)
        // instead of yn which is just vertical height
        float arcPos = (float)i / (numPoints - 1);

        float wavePhase = (arcPos * numWaves) - timePhase;
        float breathe = (float)(Math.sin(wavePhase * Math.PI * 2.0) * 0.5 + 0.5);
        float bubble = (float)Math.pow(breathe, 1.0f / spreadVal);

        float targetB = intensityVal * bubble;
        float b = LXUtils.lerpf(LXColor.b(base), targetB, (float)enabledAmount);

        colors[p.index] = LXColor.hsb(LXColor.h(base), LXColor.s(base), b);
      }
    }
  }

}
