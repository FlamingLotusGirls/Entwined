package entwined.pattern.katie_murphy;

import entwined.core.CubeData;
import entwined.core.CubeManager;

import heronarts.lx.effect.LXEffect;
import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXModel;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.DiscreteParameter;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;


@LXCategory("Custom")
public class SpinningStainedEffect extends LXEffect {

    float sawHeight = 0.0f;
    float blenderPeriodMs = 1000;
    final SawLFO spinningWindowModulator = new SawLFO(0, 360, blenderPeriodMs);

    // 0 = 0ff, 1-3 = low/med/high
    public final DiscreteParameter cockatooWindowSpin = new DiscreteParameter("cWinSpin", 0, 4);

    // Cached list of points belonging to tagged sub-models
    private final List<LXPoint> taggedPoints = new ArrayList<>();
   

    public SpinningStainedEffect(LX lx) {
        super(lx);
        addModulator(spinningWindowModulator).start();
        addParameter("cockatooWindowSpin", cockatooWindowSpin);

        rebuildTaggedPoints();
    }

    private void rebuildTaggedPoints() {
        taggedPoints.clear();
        // model.sub(tag) returns all sub-models at any depth matching the tag
        for (LXModel sub : model.sub("CockatooSegment")) {
            if (sub.tags.contains("WindowPane")) {
                taggedPoints.addAll(Arrays.asList(sub.points));
                System.out.println("cockatoo window");
            }
        }
    }

  @Override
  public void run(double deltaMs, double enabledAmount) {
    float sawHeight = spinningWindowModulator.getValuef() * cockatooWindowSpin.getValuef();

    if (cockatooWindowSpin.getValue() > 0) {
      for (LXPoint point : taggedPoints) {
        int chamberIn = 4;
        final int[][] paletteSpinningStained = {{217,100,66},{202,97,87},{159,94,76},{23,100,94},{41,78,92}};
        final float chamberSizeSpinningStained = (float) (360.0/paletteSpinningStained.length);

        float cubeTheta = (float) ((CubeManager.getCube(lx, point.index).localTheta + sawHeight) % 360.0);
        if(cubeTheta<chamberSizeSpinningStained) {
            chamberIn = 0;
        } else if(cubeTheta < chamberSizeSpinningStained*2){
            chamberIn = 1;
        } else if(cubeTheta < chamberSizeSpinningStained*3){
            chamberIn = 2;
        } else if(cubeTheta < chamberSizeSpinningStained*4){
            chamberIn = 3;
        }

        colors[point.index] = LX.hsb(
                        paletteSpinningStained[chamberIn][0],
                        paletteSpinningStained[chamberIn][1],
                        paletteSpinningStained[chamberIn][2]);
      }
    }
  }
}
