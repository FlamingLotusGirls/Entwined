
package entwined.pattern.irenedesf;

import heronarts.lx.LX;
import heronarts.lx.LXCategory;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.pattern.LXPattern;

@LXCategory(LXCategory.TEST)
public class TimedISDEShrubSweep extends LXPattern {

    final BoundedParameter x;
    final BoundedParameter y;
    final BoundedParameter z;
    final BoundedParameter beam;

    public TimedISDEShrubSweep(LX lx) {
        super(lx);
        addParameter("x", x = new BoundedParameter("X", 0, model.xMin, model.xMax));
        // the following y param should light the two shortest rods of a shrub when the beam is set to 1
        // may be useful for adjusting the rotation of the shrubs in the JSON config
//        addParameter(y = new BoundedParameter("Y", 20.8, model.yMin, model.yMax));
        addParameter("y", y = new BoundedParameter("Y", 0, model.yMin, model.yMax));
        addParameter("z", z = new BoundedParameter("Z", 0, model.zMin, model.zMax));
        addParameter("beam", beam = new BoundedParameter("beam", 5, 1, 15));
        addParameter("rot", rot = new BoundedParameter( "Rot",0,0,360));
    }

    @Override
    public void run(double deltaMs) {
        if (getChannel().fader.getNormalized() == 0) return;

        float xVal = x.getValuef();
        float yVal = y.getValuef();
        float zVal = z.getValuef();

        for (LXPoint cube : model.points) {
            if (Math.abs(cube.x - xVal) < beam.getValuef() ||
                    Math.abs(cube.y - yVal) < beam.getValuef() ||
                    Math.abs(cube.z - zVal) < beam.getValuef()) {
                colors[cube.index] = LX.hsb(135, 100, 100);
            } else {
                colors[cube.index] = LX.hsb(135, 100, 0);
            }
        }
    }
}