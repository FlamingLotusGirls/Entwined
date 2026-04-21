package entwined.pattern.katie_murphy;

import heronarts.lx.LX;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.model.LXModel;

public class HavenFire extends LXPattern {

    final CompoundParameter speed     = new CompoundParameter("SPEED", 0.5, 0.1, 2.0);
    final CompoundParameter intensity = new CompoundParameter("INTNS", 0.6, 0.0, 1.0);
    final CompoundParameter hue       = new CompoundParameter("HUE",   10,  0,   360);
    final CompoundParameter hueRange  = new CompoundParameter("HRANG", 40,  0,   90);
    final CompoundParameter hueWobbleMult  = new CompoundParameter("HWOBB", 1.3,  0.5, 2.0);

    // Spotlight: fades between white (hue ~60, low sat) and yellow (hue ~50, full sat)
    // Period 2000ms; we'll use a SinLFO on saturation: 0 = white, 100 = yellow
    final SinLFO spotlightFade = new SinLFO(0, 100, 2000);

    // Eye + CockatooSegment: purple wave
    final SinLFO purpleWave = new SinLFO(256, 270, 1700);
    // Eye + MagpieSegment: blue wave
    final SinLFO blueWave   = new SinLFO(230, 256, 2400);
    // Body: cyan wave
    final SinLFO cyanWave   = new SinLFO(170, 190, 2300);
    // Cheek: orange wave
    final SinLFO orangeWave = new SinLFO(30,  45,  2000);

    private float[] phase;
    private float[] phaseSpeed;
    private float[] flicker;
    private float[] huePhase;

    private static final float TWO_PI = (float)(Math.PI * 2);

    public HavenFire(LX lx) {
        super(lx);
        addParameter("speed",     speed);
        addParameter("intensity", intensity);
        addParameter("hue",       hue);
        addParameter("hueRange",  hueRange);
        addParameter("hueWobbleMultiplier",  hueWobbleMult);

        startModulator(spotlightFade);
        startModulator(purpleWave);
        startModulator(blueWave);
        startModulator(cyanWave);
        startModulator(orangeWave);

        int n = model.points.length;
        phase      = new float[n];
        phaseSpeed = new float[n];
        flicker    = new float[n];
        huePhase = new float[n];

        for (int i = 0; i < n; i++) {
            phase[i]      = (float)(Math.random() * TWO_PI);
            phaseSpeed[i] = 0.6f + (float)(Math.random() * 0.8f); // 0.6 to 1.4
            flicker[i]    = 0;
            huePhase[i] = (float)(Math.random() * TWO_PI);
        }

        // found object OSC triggers
        addEffect(new CockatooCheekPulseEffect(lx));
        addEffect(new SpinningStainedEffect(lx));
        addEffect(new CockatooJellyChandelierEffect(lx));
        addEffect(new MagpieWindowPulseEffect(lx));
        addEffect(new OspreyWindowBurstEffect(lx));
    }

    @Override
    public void run(double deltaMs) {
        float dt      = (float)(deltaMs / 1000.0);
        float spd     = speed.getValuef();
        float intns   = intensity.getValuef();
        float baseHue = hue.getValuef();
        float hRange  = hueRange.getValuef();
        float hWobbMult = hueWobbleMult.getValuef();

        // Spotlight: sine from 0→100 over 2s drives saturation; hue fixed at ~55 (yellow)
        float spotSat = spotlightFade.getValuef(); // 0 = white, 100 = yellow
        float spotHue = 55f;

        // Purple wave hue for Eye + CockatooSegment
        float purpleHue = purpleWave.getValuef();
        // Blue wave hue for Eye + MagpieSegment
        float blueHue   = blueWave.getValuef();
        // Cyan wave hue for Body
        float cyanHue   = cyanWave.getValuef();
        // Orange wave hue for Cheek
        float orangeHue = orangeWave.getValuef();

        for (LXModel component : model.children) {
            String tag = getTag(component);
            int nPts = component.points.length;
            if (nPts == 0) continue;

            // --- Spotlight: white <-> yellow fade, skip fire logic ---
            if (tag.equals("Spotlight")) {
                for (int j = 0; j < nPts; j++) {
                    LXPoint pt = component.points[j];
                    colors[pt.index] = LX.hsb(spotHue, spotSat, intns * 100);
                }
                continue;
            }

            // --- Eye + CockatooSegment: purple SinLFO ---
            if (component.tags.contains("Eye") && component.tags.contains("CockatooSegment")) {
                for (int j = 0; j < nPts; j++) {
                    LXPoint pt = component.points[j];
                    colors[pt.index] = LX.hsb(purpleHue, 100, intns * 100);
                }
                continue;
            }

            // --- Eye + MagpieSegment: blue SinLFO ---
            if (component.tags.contains("Eye") && component.tags.contains("MagpieSegment")) {
                for (int j = 0; j < nPts; j++) {
                    LXPoint pt = component.points[j];
                    colors[pt.index] = LX.hsb(blueHue, 100, intns * 100);
                }
                continue;
            }

            // --- Body: cyan SinLFO ---
            if (component.tags.contains("Body")) {
                for (int j = 0; j < nPts; j++) {
                    LXPoint pt = component.points[j];
                    colors[pt.index] = LX.hsb(cyanHue, 100, intns * 100);
                }
                continue;
            }

            // --- Cheek: orange SinLFO ---
            if (component.tags.contains("Cheek")) {
                for (int j = 0; j < nPts; j++) {
                    LXPoint pt = component.points[j];
                    colors[pt.index] = LX.hsb(orangeHue, 100, intns * 100);
                }
                continue;
            }

            // --- Fire logic for all other tags ---
            for (int j = 0; j < nPts; j++) {
                LXPoint pt = component.points[j];

                // Position along strip: 0 = base, 1 = tip
                float yn = (nPts == 1)
                  ? 0.0f
                  : isFlipped(tag)
                      ? 1.0f - (float)j / (float)(nPts - 1)
                      : (float)j / (float)(nPts - 1);

                // Height bias: base is bright, tip fades — floor of 0.4 so tip never goes dark
                float heightBias = 0.4f + 0.6f * (1.0f - (yn * yn));

                // Advance per-point phase
                phase[pt.index] += dt * spd * phaseSpeed[pt.index] * TWO_PI * 2.0f;
                huePhase[pt.index] += dt * spd * phaseSpeed[pt.index] * TWO_PI * hWobbMult;

                // Multi-octave smooth noise from sum of sines
                float noise =  0.50f * (float)Math.sin(phase[pt.index])
                             + 0.30f * (float)Math.sin(phase[pt.index] * 2.1f + 1.3f)
                             + 0.20f * (float)Math.sin(phase[pt.index] * 4.7f + 0.7f);
                noise = (noise + 1.0f) * 0.5f; // remap to [0, 1]

                // Low-pass smooth toward noise target
                float smoothing = getSmoothing(tag) * spd;
                flicker[pt.index] += (noise - flicker[pt.index]) * smoothing;

                float brt, pointHue, hueWobble;
                switch (tag) {
                    case "WindowPane":
                        brt = (0.3f + 0.7f * flicker[pt.index]) * heightBias * intns * 100;
                        hueWobble = ((float)Math.sin(huePhase[pt.index]) + 1.0f) * 0.5f;
                        pointHue = (baseHue + hueWobble * hRange) % 360;
                        break;

                    case "SpiralPortal":
                        float wave = (float)Math.sin(phase[pt.index] - yn * TWO_PI * 1.5f);
                        wave = (wave + 1.0f) * 0.5f;
                        brt = wave * heightBias * intns * 100;
                        hueWobble = ((float)Math.sin(huePhase[pt.index]) + 1.0f) * 0.5f;
                        pointHue = (baseHue + hueWobble * hRange) % 360;
                        break;

                    default:
                        brt = flicker[pt.index] * heightBias * intns * 100;
                        hueWobble = ((float)Math.sin(huePhase[pt.index]) + 1.0f) * 0.5f;
                        pointHue = (baseHue + hueWobble * hRange) % 360;
                        break;
                }

                brt = Math.max(0, Math.min(100, brt));
                float sat = 100 - flicker[pt.index] * 15;
                colors[pt.index] = LX.hsb(pointHue, sat, brt);
            }
        }
    }

    // Higher = snappier flicker, lower = slower/glowier
    private float getSmoothing(String tag) {
        switch (tag) {
            case "Spotlight":    return 0.12f;
            case "WindowPane":   return 0.05f;
            case "SpiralPortal": return 0.08f;
            default:             return 0.08f;
        }
    }

    // Return true if this fixture type is wired tip-to-base instead of base-to-tip
    private boolean isFlipped(String tag) {
        switch (tag) {
            case "SpiralPortal": return false;
            default:             return false;
        }
    }

    // Map component tags to fixture types
    private String getTag(LXModel component) {
        if (component.tags.contains("Spotlight"))       return "Spotlight";
        if (component.tags.contains("WindowPane"))      return "WindowPane";
        if (component.tags.contains("SpiralPortal"))    return "SpiralPortal";

        return "default";
    }
}
