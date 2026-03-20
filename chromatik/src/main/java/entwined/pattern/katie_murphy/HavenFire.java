package entwined.pattern.katie_murphy;

import heronarts.lx.LX;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.model.LXModel;

public class HavenFire extends LXPattern {

    final CompoundParameter speed     = new CompoundParameter("SPEED", 0.5, 0.1, 2.0);
    final CompoundParameter intensity = new CompoundParameter("INTNS", 0.6, 0.0, 1.0);
    final CompoundParameter hue       = new CompoundParameter("HUE",   10,  0,   360);
    final CompoundParameter hueRange  = new CompoundParameter("HRANG", 40,  0,   90);
    final CompoundParameter hueWobbleMult  = new CompoundParameter("HWOBB", 1.3,  0.5, 2.0);

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
        addEffect(new MagpieSpiralChandelierEffect(lx));
    }

    @Override
    public void run(double deltaMs) {
        float dt      = (float)(deltaMs / 1000.0);
        float spd     = speed.getValuef();
        float intns   = intensity.getValuef();
        float baseHue = hue.getValuef();
        float hRange  = hueRange.getValuef();
        float hWobbMult = hueWobbleMult.getValuef();

        for (LXModel component : model.children) {
            String tag = getTag(component);
            int nPts = component.points.length;
            if (nPts == 0) continue;

            for (int j = 0; j < nPts; j++) {
                LXPoint pt = component.points[j];

                // Position along strip: 0 = base, 1 = tip
                // Flip per fixture type in isFlipped() if strip is wired backwards
                float yn = (nPts == 1)
                  ? 0.0f
                  : isFlipped(tag)
                      ? 1.0f - (float)j / (float)(nPts - 1)
                      : (float)j / (float)(nPts - 1);

                // Height bias: base is bright, tip fades — floor of 0.4 so tip never goes dark
                float heightBias = 0.4f + 0.6f * (1.0f - (yn * yn));

                // Advance per-point phase
                phase[pt.index] += dt * spd * phaseSpeed[pt.index] * TWO_PI * 2.0f;
                //
                huePhase[pt.index] += dt * spd * phaseSpeed[pt.index] * TWO_PI * hWobbMult; // slightly different rate to flicker

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
                    case "Spotlight":
                      brt = flicker[pt.index] * heightBias * intns * 100;
                      //pointHue = (baseHue + flicker[pt.index] * hRange) % 360;
                      hueWobble = ((float)Math.sin(huePhase[pt.index]) + 1.0f) * 0.5f; // 0 to 1
                      pointHue = (baseHue + hueWobble * hRange) % 360;
                      break;
                    case "WindowPane":
                      brt = (0.3f + 0.7f * flicker[pt.index]) * heightBias * intns * 100;
                      //pointHue = (baseHue + (1.0f - yn) * hRange) % 360;
                      hueWobble = ((float)Math.sin(huePhase[pt.index]) + 1.0f) * 0.5f; // 0 to 1
                      pointHue = (baseHue + hueWobble * hRange) % 360;
                      break;

                    case "SpiralPortal":
                        // Traveling wave along strip index
                        float wave = (float)Math.sin(phase[pt.index] - yn * TWO_PI * 1.5f);
                        wave = (wave + 1.0f) * 0.5f;
                        brt = wave * heightBias * intns * 100;
                        //pointHue = (baseHue + wave * hRange) % 360;
                        hueWobble = ((float)Math.sin(huePhase[pt.index]) + 1.0f) * 0.5f; // 0 to 1
                        pointHue = (baseHue + hueWobble * hRange) % 360;
                        break;

                    default:
                        brt = flicker[pt.index] * heightBias * intns * 100;
                        //pointHue = (baseHue + flicker[pt.index] * hRange) % 360;
                        hueWobble = ((float)Math.sin(huePhase[pt.index]) + 1.0f) * 0.5f; // 0 to 1
                        pointHue = (baseHue + hueWobble * hRange) % 360;
                        break;
                }

                brt = Math.max(0, Math.min(100, brt));
                float sat = 100 - flicker[pt.index] * 15; // slight desaturation at peaks
                colors[pt.index] = LX.hsb(pointHue, sat, brt);
            }
        }
    }

    // Higher = snappier flicker, lower = slower/glowier
    private float getSmoothing(String tag) {
        switch (tag) {
            case "Spotlight":    return 0.12f;
            case "WindowPane":        return 0.05f;
            case "SpiralPortal": return 0.08f;
            default:              return 0.08f;
        }
    }

    // Return true if this fixture type is wired tip-to-base instead of base-to-tip
    private boolean isFlipped(String tag) {
        switch (tag) {
            case "SpiralPortal": return false; // change to true if spiral looks upside down
            default:              return false;
        }
    }

    // Map component tags to fixture types
    private String getTag(LXModel component) {
        if (component.tags.contains("Spotlight"))    return "Spotlight";
        if (component.tags.contains("WindowPane"))   return "WindowPane";
        if (component.tags.contains("SpiralPortal")) return "SpiralPortal";
        return "default";
    }
}
