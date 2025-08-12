// Fire pattern with tweaks for Haven installation
package entwined.pattern.katie_murphy;

import java.util.ArrayList;
import java.util.List;

import entwined.core.CubeData;
import entwined.core.CubeManager;
import entwined.core.TSTriggerablePattern;
import entwined.utils.EntwinedUtils;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.model.LXModel;
import heronarts.lx.modulator.SinLFO;



public class HavenFire extends TSTriggerablePattern {
  final CompoundParameter maxHeight = new CompoundParameter("HEIGHT", 5.0, 0.3, 12);
  final CompoundParameter flameSize = new CompoundParameter("SIZE", 30, 10, 75);
  final CompoundParameter flameCount = new CompoundParameter ("FLAMES", 75, 0, 75);
  final CompoundParameter hue = new CompoundParameter("HUE", 0, 0, 360);
  private LinearEnvelope fireHeight = new LinearEnvelope(0,0,500);

  private float height = 0;
  private int numFlames = 12;
  private List<Flame> flames;

  private class Flame {
    public float flameHeight = 0;
    public float theta = EntwinedUtils.random(0, 360);
    public LinearEnvelope decay = new LinearEnvelope(0,0,0);

    public Flame(float maxHeight, boolean groundStart){
      float flameHeight;
      if (EntwinedUtils.random(1) > .2f) {
        flameHeight = EntwinedUtils.pow(EntwinedUtils.random(0, 1), 3) * maxHeight * 0.3f;
      } else {
        flameHeight = EntwinedUtils.pow(EntwinedUtils.random(0, 1), 3) * maxHeight;
      }
      decay.setRange(model.yMin, (model.yMax * 0.9f) * flameHeight, EntwinedUtils.min(EntwinedUtils.max(200, 900 * flameHeight), 800));
      if (!groundStart) {
        decay.setBasis(EntwinedUtils.random(0,.8f));
      }
      addModulator(decay).start();
    }
  }

  public HavenFire(LX lx) {
    super(lx);

    patternMode = PATTERN_MODE_FIRED;

    addParameter("maxHeight", maxHeight);
    addParameter("flameSize", flameSize);
    addParameter("flameCount", flameCount);
    addParameter("hue", hue);
    addModulator(fireHeight);

    flames = new ArrayList<Flame>(numFlames);
    for (int i = 0; i < numFlames; ++i) {
      flames.add(new Flame(height, false));
    }
  }

  public void updateNumFlames(int numFlames) {
    for (int i = flames.size(); i < numFlames; ++i) {
      flames.add(new Flame(height, false));
    }
  }

  @Override
  public void run(double deltaMs) {
    //if (getChannel().fader.getNormalized() == 0) return;

    if (!triggered && flames.size() == 0) {
      enabled.setValue(false);
      // setCallRun(false);
    }

    if (!triggerableModeEnabled) {
      height = maxHeight.getValuef();
      numFlames = (int) (flameCount.getValue() / 75 * 30); // Convert for backwards compatibility
    } else {
      height = fireHeight.getValuef();
    }

    if (flames.size() != numFlames) {
      updateNumFlames(numFlames);
    }
    for (int i = 0; i < flames.size(); ++i) {
      if (flames.get(i).decay.finished()) {
        removeModulator(flames.get(i).decay);
        if (flames.size() <= numFlames) {
          flames.set(i, new Flame(height, true));
        } else {
          flames.remove(i);
          i--;
        }
      }
    }

    for (LXModel component : model.children) {

    for (LXPoint cube : component.points) {
      
      CubeData cdata = CubeManager.getCube(lx, cube.index);
      float yn = (cdata.localY - model.yMin) / model.yMax;

      if (component.tags.contains("PooferBird") || component.tags.contains("NestSurface")) {
        // kinda hacky, but flame math is height dependent and the nest rails
        // are modeled as straight lines.  idk how to change the model, so
        // just give them some fake height
        cdata.localY = cdata.localX;
      } else if (component.tags.contains("Cheek")) {
        // skip cockatoo cheek
        break;
      }

      float cBrt = 0;
      float cHue = 0;
      float flameWidth = flameSize.getValuef() / 2;
      for (int i = 0; i < flames.size(); ++i) {
        if (EntwinedUtils.abs(flames.get(i).theta - cdata.localTheta) < (flameWidth * (1- yn))) {
          cBrt = EntwinedUtils.min(
            100,
            EntwinedUtils.max(
              0,
              EntwinedUtils.max(
                cBrt,
                (100 - 2 * EntwinedUtils.abs(cdata.localY - flames.get(i).decay.getValuef()) - flames.get(i).decay.getBasisf() * 25) * EntwinedUtils.min(1, 2 * (1 - flames.get(i).decay.getBasisf()))
            )));
          cHue = EntwinedUtils.max(0,  (cHue + cBrt * 0.7f) * 0.5f);
        }
      }
      float h = (cHue + hue.getValuef()) % 360;
      float b = EntwinedUtils.min(100, cBrt + EntwinedUtils.pow(EntwinedUtils.max(0, (height - 0.3f) / 0.7f), 0.5f) * EntwinedUtils.pow(EntwinedUtils.max(0, 0.8f - yn), 2) * 75);
      colors[cube.index] = LX.hsb(h, 100, b);
    }
    } // end for component

  }

  @Override
  public void onTriggered() {
    super.onTriggered();

    fireHeight.setRange(1,0.6f);
    fireHeight.reset().start();
  };

  @Override
  public void onReleased() {
    super.onReleased();

    fireHeight.setRange(height, 0);
    fireHeight.reset().start();
  }
}

