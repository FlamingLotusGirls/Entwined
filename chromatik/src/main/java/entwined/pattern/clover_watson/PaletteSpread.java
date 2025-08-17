package entwined.pattern.clover_watson;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.modulator.TriangleLFO;
import heronarts.lx.pattern.LXPattern;


import entwined.utils.EntwinedUtils;
import entwined.core.CubeData;
import entwined.core.CubeManager;
public class PaletteSpread extends LXPattern {
    int[][] palette = {{217,100,66},{202,97,87},{159,94,76},{23,100,94},{41,78,92}};
    float time = 0;
    boolean fading = false;
    boolean checkingFading = false;
    // All values below are on the scale from zero to one.
    // std dev of the gaussian function that determines the thickness of the line
    final double deviation = 0.05;
    final double minLineCenterY = 0;
    final double maxLineCenterY = model.yMax-model.yMin;
    final SinLFO upDownModulator = new SinLFO(0.1, 0.2, 3000);
    
    public PaletteSpread(LX lx) {
        super(lx);
        addModulator(upDownModulator).start();
    }
    
    int chamberIn(float pixelHeight, float chamberHeight){
        if(pixelHeight<chamberHeight){
            return 0;
        }
        if(pixelHeight<chamberHeight*2.0){
            return 1;
        }
        if(pixelHeight<chamberHeight*3.0){
            return 2;
        }
        if(pixelHeight<chamberHeight*4.0){
            return 3;
        }
        if(pixelHeight<chamberHeight*5.0){
            return 4;
        }
        return 4;
    }
    
    @Override
    protected void run(double deltaMs) {
        float scanHeight = upDownModulator.getValuef();
        // float scanHeight = (float) 0.2;
        for (LXModel component : model.children) {
          if (component.tags.contains("Cheek")) {
            // skip cockatoo cheek
            break;
          }

          for (LXPoint point : component.points) {
              CubeData cubeData = CubeManager.getCube(lx, point.index);
              float mappedHeight = EntwinedUtils.map(point.y, model.yMin, model.yMax);
              int chamber = chamberIn(mappedHeight, scanHeight);
                    colors[point.index] = LX.hsb(
                      palette[chamber][0],
                      palette[chamber][1],
                    palette[chamber][2]);
          }
        }
    }
}


