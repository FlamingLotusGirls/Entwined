package entwined.pattern.clover_watson;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.pattern.LXPattern;


import entwined.utils.EntwinedUtils;

public class GradientUp extends LXPattern {
    float time = 0;
    final SawLFO gradientUpModulator = new SawLFO(0, 2, 3000);
    
    public GradientUp(LX lx) {
        super(lx);
        addModulator(gradientUpModulator).start();
    }

    @Override
    protected void run(double deltaMs) {
        float scanHeight = gradientUpModulator.getValuef();
        for (LXModel component : model.children) {
          for (LXPoint point : model.points) {
              float mappedHeight = EntwinedUtils.map(point.y, model.yMin, model.yMax);
                    colors[point.index] = LX.hsb(
                      scanHeight* mappedHeight * 360,
                      100,
                    100);
          }
        }
    }
}


