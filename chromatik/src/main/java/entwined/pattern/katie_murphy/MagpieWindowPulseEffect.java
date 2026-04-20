package entwined.pattern.katie_murphy;

import entwined.utils.EntwinedUtils;

import heronarts.lx.effect.LXEffect;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXModel;
import heronarts.lx.parameter.BooleanParameter;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;


@LXCategory("Custom")
public class MagpieWindowPulseEffect extends LXEffect {

    // Phase durations in ms: dip down, oscillating pulse, return to normal
    private static final double DIP_MS        = 1000.0;
    private static final double PULSE_MS      = 3000.0;
    private static final double PULSE_HALF_MS =  500.0; // half-period of each up/down cycle
    private static final double RETURN_MS     = 1000.0;
    private static final double TOTAL_MS      = DIP_MS + PULSE_MS + RETURN_MS;

    private double pulseEnd = 0;
    private boolean pulseActive = false;

    final BooleanParameter magpieWindowPulseParam =
        new BooleanParameter("mWinPulse", false).setMode(BooleanParameter.Mode.MOMENTARY);

// Cached list of points belonging to MagpieSegment + WindowPane sub-models
    private final List<LXPoint> taggedPoints = new ArrayList<>();

    public MagpieWindowPulseEffect(LX lx) {
        super(lx);
        addParameter("magpieWindowPulse", magpieWindowPulseParam);
        magpieWindowPulseParam.addListener(p -> {
            if (magpieWindowPulseParam.getValueb() && !pulseActive) {
                pulseActive = true;
                pulseEnd = EntwinedUtils.millis() + TOTAL_MS;
            }
        });

        rebuildTaggedPoints();
    }

    private void rebuildTaggedPoints() {
        taggedPoints.clear();
        for (LXModel sub : model.sub("MagpieSegment")) {
            if (sub.tags.contains("WindowPane")) {
                taggedPoints.addAll(Arrays.asList(sub.points));
            }
        }
    }

    @Override
    public void run(double deltaMs, double enabledAmount) {
        if (EntwinedUtils.millis() > pulseEnd) {
            pulseActive = false;
        }

        if (!pulseActive) return;

        double elapsed = TOTAL_MS - (pulseEnd - EntwinedUtils.millis());
        elapsed = Math.max(0.0, Math.min(TOTAL_MS, elapsed));

        float brightnessOverride;
        if (elapsed < DIP_MS) {
            // Phase 1: fade down to 20 over DIP_MS
            float t = (float)(elapsed / DIP_MS);
            brightnessOverride = lerp(100f, 20f, t);
        } else if (elapsed < DIP_MS + PULSE_MS) {
            // Phase 2: oscillate between 20 and 100 with a 0.5s half-period (triangle wave)
            double pulseElapsed = elapsed - DIP_MS;
            double cyclePos = pulseElapsed % (PULSE_HALF_MS * 2);
            float t = (float)(cyclePos / PULSE_HALF_MS);
            if (t <= 1.0f) {
                // rising: 20 -> 100
                brightnessOverride = lerp(20f, 100f, t);
            } else {
                // falling: 100 -> 20
                brightnessOverride = lerp(100f, 20f, t - 1.0f);
            }
        } else {
            // Phase 3: return to normal (pass-through) over RETURN_MS
            float t = (float)((elapsed - DIP_MS - PULSE_MS) / RETURN_MS);
            // At t=0 we're still at full override (100), at t=1 we hand back fully
            // We'll apply a multiplier that goes from 1.0 to 0.0 on the override contribution
            for (LXPoint point : taggedPoints) {
                int c = colors[point.index];
                float b = LXColor.b(c);
                float blended = lerp(100f, b, t);
                colors[point.index] = LX.hsb(LXColor.h(c), LXColor.s(c), blended);
            }
            return;
        }

        for (LXPoint point : taggedPoints) {
            int c = colors[point.index];
            colors[point.index] = LX.hsb(LXColor.h(c), LXColor.s(c), brightnessOverride);
        }
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
