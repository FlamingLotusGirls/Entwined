package entwined.pattern.katie_murphy;

import entwined.utils.EntwinedUtils;

import heronarts.lx.color.LXColor;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.model.LXModel;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;


@LXCategory("Custom")
public class CockatooJellyChandelierEffect extends LXEffect {

    private final int[] jellyPalette = new int[] {
        LXColor.rgb(180, 0, 180),
        LXColor.rgb(255, 105, 180),
        LXColor.rgb(0, 255, 255),
    };
    private double time = 0;
    private double cockatooJellyDurationMs = 5000;
    private double cockatooJellyEnd = 0;
    private boolean cockatooJellyActive = false;
    final BooleanParameter cockatooJellyChandelierParam = new BooleanParameter("cChand", false).setMode(BooleanParameter.Mode.MOMENTARY);

    // Cached list of points belonging to tagged sub-models
    private final List<LXPoint> taggedPoints = new ArrayList<>();

    public CockatooJellyChandelierEffect(LX lx) {
        super(lx);
        addParameter("cockatooJellyTwinkleTrigger", cockatooJellyChandelierParam);
        cockatooJellyChandelierParam.addListener(p -> {
            if (cockatooJellyChandelierParam.getValueb() && !cockatooJellyActive) {
                cockatooJellyActive = true;
                cockatooJellyEnd = EntwinedUtils.millis() + cockatooJellyDurationMs;
            }
        });

        rebuildTaggedPoints();
    }

    private void rebuildTaggedPoints() {
        taggedPoints.clear();
        // model.sub(tag) returns all sub-models at any depth matching the tag
        for (LXModel sub : model.sub("JellyChandelier")) {
          taggedPoints.addAll(Arrays.asList(sub.points));
        }
    }

  @Override
  public void run(double deltaMs, double enabledAmount) {
    if (EntwinedUtils.millis() > cockatooJellyEnd) {
        cockatooJellyActive = false;
    }

    if (cockatooJellyActive) {
        for (LXPoint point : taggedPoints) {
            time += deltaMs / 1000;
            double speed = 0.1;
            double f = (time * speed) % 1.0;
            double palettePos = f * jellyPalette.length;
            int idx0 = (int)Math.floor(palettePos) % jellyPalette.length;
            int idx1 = (idx0 + 1) % jellyPalette.length;
            double mix = palettePos - Math.floor(palettePos);
            int color = LXColor.lerp(jellyPalette[idx0], jellyPalette[idx1], mix);

            colors[point.index] = color;
        }
    }
  }

}
