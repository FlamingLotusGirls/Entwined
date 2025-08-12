package entwined.pattern.katie_murphy;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;
import entwined.utils.SimplexNoise;
import heronarts.lx.model.LXModel;

import entwined.core.CubeManager;




public class Sunset extends LXPattern {

  public enum FixtureType {
    UNKNOWN,
    STAINED_GLASS,
    WINDOW_PANE,
    POOFER_BIRD,
    NEST_SURFACE
  };

  final SinLFO brightnessWave = new SinLFO(0, 100, 2000);
  final SinLFO blueWave = new SinLFO(210, 256, 2000);
  final SinLFO greenWave = new SinLFO(140, 155, 2000);
  final SinLFO yellowWave = new SinLFO(40, 50, 2000);
  final SinLFO redWave = new SinLFO(0, 20, 2000);
        
  double time_ms = 0;

      private float minz = Float.MAX_VALUE;
    private float maxz = -Float.MAX_VALUE;
    // private float waveWidth = 1;
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

  public Sunset(LX lx) {
    super(lx);
    
  // addParameter("blue", blueParam);
    addModulator(brightnessWave).start();
    addParameter("velocity", velocityParam);
    addParameter("blob_width", blobWidthParam);
    addParameter("blob_height", blobHeightParam);
    addParameter("fill", fillParam);
    addModulator(blueWave).start();
    addModulator(greenWave).start();
    addModulator(yellowWave).start();
    addModulator(redWave).start();
  }

  public FixtureType getFixtureType(LXModel component){
    if (component.tags.contains("StainedGlass")) {
      return FixtureType.STAINED_GLASS;
    } else if (component.tags.contains("WindowPane")) {
      return FixtureType.WINDOW_PANE;
    } else if (component.tags.contains("PooferBird")) {
      return FixtureType.POOFER_BIRD;
    } else if (component.tags.contains("NestSurface")) {
      return FixtureType.NEST_SURFACE;
    } else {
      // error msg
      return FixtureType.UNKNOWN;
    }
  }

  @Override
  public void run(double deltaMs) {
        float velocity = velocityParam.getValuef() / 200f;
        float fill = fillParam.getValuef() / 50f;
        float blobWidth = blobWidthParam.getValuef();
        float blobHeight = blobHeightParam.getValuef();
        
        height += deltaMs * velocity / blobHeight;

        float baseline = Math.max(0f, 1f - fill);
        float boost = Math.max(0f, fill - 1f);

        float pointnum = 0;
        for (LXModel component : model.children) {

          FixtureType fixture = getFixtureType(component);
          float componentHeight = component.yMax - component.yMin;
          float componentWidth = component.zMax - component.zMin;
          
          for (LXPoint point : component.points) {
            float hue = 0;
            if (fixture == FixtureType.WINDOW_PANE) {
              float y = CubeManager.getCube(lx, point.index).localY;
              if (y >= component.yMin + (componentHeight * .70f)) {
                hue = blueWave.getValuef();
              } else if (y >= component.yMin + (componentHeight * .55f)) {
                hue = greenWave.getValuef();
              } else if (y >= component.yMin + (componentHeight * .25f)) {
                hue = yellowWave.getValuef();
              } else {
                hue = redWave.getValuef();
              }
            } else if (component.tags.contains("Cheek")) {
              // skip cockatoo cheek
              break;
            }
            else {
              hue = blueWave.getValuef();
            }

            pointnum++;

            // FreeFall math to give it a bit of glimmer
            float noise1 = 0.5f
                + (float) SimplexNoise.noise(point.x / blobWidth,
                    point.z / blobWidth, height + point.y / blobHeight) / 2f;

            float cutoffNoise1 = Math.min(1.0f, Math.max(0.0f,
                (noise1 + boost - baseline) / (1.0f - boost - baseline)));
            float brightness = Float.max(40, cutoffNoise1 * 100f);

            colors[point.index] = LX.hsb(hue, 85, brightness);
          }
        }
    }
}
