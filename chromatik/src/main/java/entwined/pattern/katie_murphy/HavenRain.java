package entwined.pattern.katie_murphy;

import entwined.utils.SimplexNoise;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BoundedParameter;

import entwined.utils.EntwinedUtils;

public class HavenRain extends LXPattern {

    private float minz = Float.MAX_VALUE;
    private float maxz = -Float.MAX_VALUE;
    private float speedMult = 1000;

    float height = 0.0f;
    final CompoundParameter velocityParam = new CompoundParameter("VEL", 10f,
        -100f, 100f);
    final CompoundParameter blobWidthParam = new CompoundParameter("WID", 90f,
        10f, 400f);
    final CompoundParameter blobHeightParam = new CompoundParameter("HGT", 60f,
        10f, 400f);
    final CompoundParameter fillParam = new CompoundParameter("FIL", 25f, 0.001f,
        100f);
    final CompoundParameter hueParam = new CompoundParameter("HUE", 220f, 200f,
        256f);
    final SinLFO saturationWave = new SinLFO(70, 100, 2000);


    public HavenRain(LX lx) {
        super(lx);

        addParameter("velocity", velocityParam);
        addParameter("blob_width", blobWidthParam);
        addParameter("blob_height", blobHeightParam);
        addParameter("fill", fillParam);
        addParameter("hue", hueParam);
        addModulator(saturationWave).start();
    }

    @Override
    protected void run(double deltaMs) {
        float velocity = velocityParam.getValuef() / 200f;
        float fill = fillParam.getValuef() / 50f;
        float blobWidth = blobWidthParam.getValuef();
        float blobHeight = blobHeightParam.getValuef();
        
        height += deltaMs * velocity / blobHeight;

        float baseline = Math.max(0f, 1f - fill);
        float boost = Math.max(0f, fill - 1f);

        float pointnum = 0;
        for (LXPoint point : model.points) {
            float hue = (hueParam.getValuef() + pointnum++) % 360;
            hue = Float.max(hue, 200);
            hue = Float.min(hue, 256);

            float noise1 = 0.5f
                + (float) SimplexNoise.noise(point.x / blobWidth,
                    point.z / blobWidth, height + point.y / blobHeight) / 2f;

            float cutoffNoise1 = Math.min(1.0f, Math.max(0.0f,
                (noise1 + boost - baseline) / (1.0f - boost - baseline)));

            colors[point.index] = LX.hsb(hue, saturationWave.getValuef(), cutoffNoise1 * 100f);
        }
    }
}
