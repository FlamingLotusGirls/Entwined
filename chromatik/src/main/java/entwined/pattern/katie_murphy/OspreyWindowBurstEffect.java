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
public class OspreyWindowBurstEffect extends LXEffect {

    // Phase durations in ms
    private static final double FADE_OUT_MS  = 1000.0; // fade whatever pattern to black
    private static final double PAUSE_MS     = 500.0; // hold black
    private static final double RISE_MS      =  330.0; // blue wipe up from bottom
    private static final double DANCE_MS     = 2000.0; // twinkling blue/cyan/amber
    private static final double FADE_IN_MS   = 1000.0; // return to pattern
    private static final double TOTAL_MS     = FADE_OUT_MS + PAUSE_MS + RISE_MS + DANCE_MS + FADE_IN_MS;

    // How often each point re-rolls its color (ms)
    private static final double TWINKLE_RATE_MS = 120.0;

    // Hues
    private static final float HUE_BLUE  = 240f;
    private static final float HUE_CYAN  = 185f;
    private static final float HUE_AMBER =  38f;

    private double burstEnd = 0;
    private boolean burstActive = false;

    // Per-point twinkle state for phase 4
    private float[] twinkleHue;
    private double[] twinkleTimer;

    final BooleanParameter ospreyWindowBurstParam =
        new BooleanParameter("oWinBurst", false).setMode(BooleanParameter.Mode.MOMENTARY);

    // Cached sub-models (not just points) so we can use yMin/yMax for the wipe
    private final List<LXModel> taggedComponents = new ArrayList<>();
    // Flat point list used for phases that don't need per-component geometry
    private final List<LXPoint> taggedPoints = new ArrayList<>();

    public OspreyWindowBurstEffect(LX lx) {
        super(lx);
        addParameter("ospreyWindowBurst", ospreyWindowBurstParam);
        ospreyWindowBurstParam.addListener(p -> {
            if (ospreyWindowBurstParam.getValueb() && !burstActive) {
                burstActive = true;
                burstEnd = EntwinedUtils.millis() + TOTAL_MS;
            }
        });

        rebuildTaggedPoints();

        int n = model.points.length;
        twinkleHue   = new float[n];
        twinkleTimer = new double[n];
        for (int i = 0; i < n; i++) {
            twinkleHue[i]   = HUE_BLUE;
            twinkleTimer[i] = 0.0;
        }
    }

    private void rebuildTaggedPoints() {
        taggedComponents.clear();
        taggedPoints.clear();
        for (LXModel sub : model.sub("OspreySegment")) {
            if (sub.tags.contains("WindowPane")) {
                taggedComponents.add(sub);
                taggedPoints.addAll(Arrays.asList(sub.points));
            }
        }
    }

    @Override
    public void run(double deltaMs, double enabledAmount) {
        if (EntwinedUtils.millis() > burstEnd) {
            burstActive = false;
        }

        if (!burstActive) return;

        double elapsed = TOTAL_MS - (burstEnd - EntwinedUtils.millis());
        elapsed = Math.max(0.0, Math.min(TOTAL_MS, elapsed));

        // ── Phase 1: fade pattern brightness to 0 ────────────────────────────
        if (elapsed < FADE_OUT_MS) {
            float t = (float)(elapsed / FADE_OUT_MS);
            for (LXPoint pt : taggedPoints) {
                int c = colors[pt.index];
                float b = lerp(LXColor.b(c), 0f, t);
                colors[pt.index] = LX.hsb(LXColor.h(c), LXColor.s(c), b);
            }
            return;
        }

        // ── Phase 2: hold black ───────────────────────────────────────────────
        if (elapsed < FADE_OUT_MS + PAUSE_MS) {
            for (LXPoint pt : taggedPoints) {
                colors[pt.index] = LX.hsb(HUE_BLUE, 100f, 0f);
            }
            return;
        }

        // ── Phase 3: blue wipe upward from bottom of each component ──────────
        if (elapsed < FADE_OUT_MS + PAUSE_MS + RISE_MS) {
            float t = (float)((elapsed - FADE_OUT_MS - PAUSE_MS) / RISE_MS); // 0→1
            for (LXModel comp : taggedComponents) {
                float yMin = comp.yMin;
                float yMax = comp.yMax;
                float yRange = yMax - yMin;
                float wipeFront = yMin + yRange * t; // y level of the leading edge
                for (LXPoint pt : comp.points) {
                    if (pt.y <= wipeFront) {
                        // lit — brightness also ramps up as wipe passes
                        float localT = (yRange > 0f) ? Math.min(1f, (wipeFront - pt.y) / (yRange * 0.25f)) : 1f;
                        colors[pt.index] = LX.hsb(HUE_BLUE, 100f, localT * 100f);
                    } else {
                        colors[pt.index] = LX.hsb(HUE_BLUE, 100f, 0f);
                    }
                }
            }
            return;
        }

        // ── Phase 4: twinkling blue / cyan / amber dance ─────────────────────
        if (elapsed < FADE_OUT_MS + PAUSE_MS + RISE_MS + DANCE_MS) {
            float[] danceHues = { HUE_BLUE, HUE_CYAN, HUE_AMBER };
            for (LXPoint pt : taggedPoints) {
                twinkleTimer[pt.index] -= deltaMs;
                if (twinkleTimer[pt.index] <= 0) {
                    twinkleHue[pt.index]  = danceHues[(int)(Math.random() * danceHues.length)];
                    twinkleTimer[pt.index] = TWINKLE_RATE_MS * (0.5 + Math.random());
                }
                colors[pt.index] = LX.hsb(twinkleHue[pt.index], 100f, 100f);
            }
            return;
        }

        // ── Phase 5: fade from full blue back to the pattern ─────────────────
        {
            float t = (float)((elapsed - FADE_OUT_MS - PAUSE_MS - RISE_MS - DANCE_MS) / FADE_IN_MS);
            for (LXPoint pt : taggedPoints) {
                int c = colors[pt.index]; // current pattern color (effect runs on top)
                float patternB = LXColor.b(c);
                float patternH = LXColor.h(c);
                float patternS = LXColor.s(c);
                // blend from full-blue to whatever the pattern is producing
                float blendedH = lerp(HUE_BLUE, patternH, t);
                float blendedS = lerp(100f, patternS, t);
                float blendedB = lerp(100f, patternB, t);
                colors[pt.index] = LX.hsb(blendedH, blendedS, blendedB);
            }
        }
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
