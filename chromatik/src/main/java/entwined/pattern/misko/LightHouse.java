package entwined.pattern.misko;

import entwined.core.CubeManager;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.pattern.LXPattern;

public class LightHouse extends LXPattern {

  private float speedMult = 1000;
  final BoundedParameter hue = new BoundedParameter("hue", 50, 0, 360);
  final BoundedParameter width = new BoundedParameter("width", 45, 0, 100);
  final BoundedParameter globalTheta = new BoundedParameter("globalTheta", 1.0, 0, 1.0);
  final BoundedParameter colorSpeed = new BoundedParameter("colorSpeed", 0, 0, 200);
  final BoundedParameter speedParam = new BoundedParameter("Speed", 5, 20, .01);
  final BoundedParameter glow = new BoundedParameter("glow", 0.1, 0.0, 1.0);
  final SawLFO wave = new SawLFO(0, 360, 1000);
  float total_ms=0;
  int shrub_offset[];

  public LightHouse(LX lx) {
      super(lx);
      addModulator(wave).start();
    addParameter("hue", hue);
    addParameter("globalTheta", globalTheta);
    addParameter("speedParam", speedParam);
    addParameter("colorSpeed", colorSpeed);
    addParameter("glow", glow);
    addParameter("width", width);

  }

  @Override
  public void run(double deltaMs) {
    if (getChannel().fader.getNormalized() == 0) return;

    wave.setPeriod(speedParam.getValuef() * speedMult  );
    total_ms+=deltaMs*speedParam.getValuef();
    //float offset = (wave.getValuef()+360.0f)%360.0f;
    for (LXPoint cube : model.points) {
      float diff = (360.0f+(wave.getValuef() - CubeManager.getCube(lx, cube.index).theta)%360.0f)%360.f; // smallest positive representation modulo 360
      if ((360-diff)<diff) {
        diff=360-diff;
      }
      float b = diff<width.getValuef() ? 100.0f : 0.0f;
      float h = (360+(hue.getValuef() +
      total_ms*colorSpeed.getValuef()/10000)%360)%360;
      colors[cube.index] = LX.hsb(h,
        100,
        b);
    }
  }
}
