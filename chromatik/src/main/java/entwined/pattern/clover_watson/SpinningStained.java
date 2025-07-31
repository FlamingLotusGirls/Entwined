package entwined.pattern.clover_watson;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.pattern.LXPattern;

import entwined.core.CubeManager;

/**
 * The color palette spins around the windows
 */
public class SpinningStained extends LXPattern {
    // The palette for the lights of the sculpture
    int[][] palette = {{217,100,66},{202,97,87},{159,94,76},{23,100,94},{41,78,92}};

    // The size of each chamber
    float chamberSize = (float) (360.0/palette.length);
    final SawLFO spinningWindowModulator = new SawLFO(0, 360, 3000);
    
    public SpinningStained(LX lx) {
        super(lx);
        addModulator(spinningWindowModulator).start();
    }

    @Override
    protected void run(double deltaMs) {
        float sawHeight = spinningWindowModulator.getValuef();
        for (LXModel component : model.children) {

            boolean desiredFixtureType = false;

            // If the component has the proper tag, then we update the colors
            if(component.tags.contains("WindowPane")){
                desiredFixtureType = true;
            }
            if(desiredFixtureType){
                for(LXPoint point : component.points) {
                    int chamberIn = 4;
                    float cubeTheta = (float) ((CubeManager.getCube(lx, point.index).localTheta + sawHeight) % 360.0);
                    if(cubeTheta<chamberSize) {
                        chamberIn = 0;
                    } else if(cubeTheta < chamberSize*2){
                        chamberIn = 1;
                    } else if(cubeTheta < chamberSize*3){
                        chamberIn = 2;
                    } else if(cubeTheta < chamberSize*4){
                        chamberIn = 3;
                    }
                    colors[point.index] = LX.hsb(
                        palette[chamberIn][0],
                        palette[chamberIn][1],
                        palette[chamberIn][2]);
                }
            }
        }
    }
}