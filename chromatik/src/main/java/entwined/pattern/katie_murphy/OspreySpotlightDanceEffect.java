package entwined.pattern.katie_murphy;

import entwined.utils.EntwinedUtils;

import heronarts.lx.effect.LXEffect;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXModel;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;


@LXCategory("Custom")
public class OspreySpotlightDanceEffect extends LXEffect {

    // Phase durations in ms
    private static final double DANCE_MS      = 3000.0;
    private static final double FADE_IN_MS    = 1000.0;
    private static final double TOTAL_MS      = DANCE_MS + FADE_IN_MS;

    // How often each point re-rolls its color (ms)
    private static final double TWINKLE_RATE_MS = 120.0;

    // Palettes: each row is { hue, sat, brt } triplets for 3 colors
    // Palette 1: orange, white, yellow
    // Palette 2: cyan, blue, white
    // Palette 3: teal, white, dim teal
    // Palette 4: burnt orange, yellow, white
    private static final float[][][] PALETTES = {
        { // 1: orange, white, yellow
            { 28f, 100f, 100f },
            {  0f,   0f, 100f },
            { 52f, 100f, 100f },
        },
        { // 2: cyan, blue, white
            { 185f, 100f, 100f },
            { 240f, 100f, 100f },
            {   0f,   0f, 100f },
        },
        { // 3: teal, white, dim teal
            { 172f, 90f, 100f },
            {   0f,  0f, 100f },
            { 172f, 90f,  40f },
        },
        { // 4: burnt orange, yellow, white
            {  18f, 100f, 100f },
            {  48f, 100f, 100f },
            {   0f,   0f, 100f },
        },
    };

    private double danceEnd = 0;
    private boolean danceActive = false;

    final BooleanParameter ospreySpotlightDanceParam =
        new BooleanParameter("oSpotDance", false).setMode(BooleanParameter.Mode.MOMENTARY);

    // 1-indexed palette selector to match labeling; internally we use value-1
    final DiscreteParameter paletteParam =
        new DiscreteParameter("palette1", 1, 1, PALETTES.length + 1);

    // Per-point twinkle state
    private float[] twinkleHue;
    private float[] twinkleSat;
    private float[] twinkleBrt;
    private double[] twinkleTimer;

    // Cached points for OspreySegment + Spotlight + NestSurface
    private final List<LXPoint> taggedPoints = new ArrayList<>();

    public OspreySpotlightDanceEffect(LX lx) {
        super(lx);
        addParameter("ospreySpotlightDance", ospreySpotlightDanceParam);
        addParameter("palette", paletteParam);

        ospreySpotlightDanceParam.addListener(p -> {
            if (ospreySpotlightDanceParam.getValueb() && !danceActive) {
                danceActive = true;
                danceEnd = EntwinedUtils.millis() + TOTAL_MS;
            }
        });

        rebuildTaggedPoints();

        int n = model.points.length;
        twinkleHue   = new float[n];
        twinkleSat   = new float[n];
        twinkleBrt   = new float[n];
        twinkleTimer = new double[n];
        for (int i = 0; i < n; i++) {
            twinkleHue[i]   = PALETTES[0][0][0];
            twinkleSat[i]   = PALETTES[0][0][1];
            twinkleBrt[i]   = PALETTES[0][0][2];
            twinkleTimer[i] = 0.0;
        }
    }

    private void rebuildTaggedPoints() {
        taggedPoints.clear();
        for (LXModel sub : model.sub("OspreySegment")) {
            if (sub.tags.contains("Spotlight") && sub.tags.contains("NestSurface")) {
                taggedPoints.addAll(Arrays.asList(sub.points));
            }
        }
    }

    @Override
    public void run(double deltaMs, double enabledAmount) {
        if (EntwinedUtils.millis() > danceEnd) {
            danceActive = false;
        }

        if (!danceActive) return;

        double elapsed = TOTAL_MS - (danceEnd - EntwinedUtils.millis());
        elapsed = Math.max(0.0, Math.min(TOTAL_MS, elapsed));

        float[][] palette = PALETTES[paletteParam.getValuei() - 1];

        // ── Phase 1: twinkling dance ──────────────────────────────────────────
        if (elapsed < DANCE_MS) {
            for (LXPoint pt : taggedPoints) {
                twinkleTimer[pt.index] -= deltaMs;
                if (twinkleTimer[pt.index] <= 0) {
                    float[] color = palette[(int)(Math.random() * palette.length)];
                    twinkleHue[pt.index]   = color[0];
                    twinkleSat[pt.index]   = color[1];
                    twinkleBrt[pt.index]   = color[2];
                    twinkleTimer[pt.index] = TWINKLE_RATE_MS * (0.5 + Math.random());
                }
                colors[pt.index] = LX.hsb(twinkleHue[pt.index], twinkleSat[pt.index], twinkleBrt[pt.index]);
            }
            return;
        }

        // ── Phase 2: fade back to whatever the pattern was doing ─────────────
        {
            float t = (float)((elapsed - DANCE_MS) / FADE_IN_MS);
            for (LXPoint pt : taggedPoints) {
                int c = colors[pt.index];
                float blendedH = lerp(twinkleHue[pt.index], LXColor.h(c), t);
                float blendedS = lerp(twinkleSat[pt.index], LXColor.s(c), t);
                float blendedB = lerp(twinkleBrt[pt.index], LXColor.b(c), t);
                colors[pt.index] = LX.hsb(blendedH, blendedS, blendedB);
            }
        }
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
