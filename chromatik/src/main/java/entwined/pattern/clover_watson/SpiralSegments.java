package entwined.pattern.clover_watson;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.pattern.LXPattern;

import entwined.utils.Vec3D;
import entwined.utils.EntwinedUtils;
import entwined.core.CubeData;
import entwined.core.CubeManager;
public class SpiralSegments extends LXPattern {
    int[][] palette = {{217,100,66},{202,97,87},{159,94,76},{23,100,94},{41,78,92}};
    float chamberSize = (float) (360.0/palette.length);
    float time = 0;
    Vec3D[] originVectors = new Vec3D[3];
    Vec3D[] segmentVectors = new Vec3D[3];
    float[] segmentLengths = new float[3];
    int[] segmentPalettes = {0, 0, 0};
    boolean[] segmentCanFade = {true, true, true};
    final double deviation = 0.05;
    final double minLineCenterY = 0;
    final double maxLineCenterY = model.yMax-model.yMin;
    final SawLFO spiralModulator = new SawLFO(0, 360, 3000);
    
    public SpiralSegments(LX lx) {
        super(lx);
        addModulator(spiralModulator).start();       
        for(LXModel component : model.children){
            if(component.tags.contains("SegmentOrigin")){
                if(component.tags.contains("OspreySegment")){
                    originVectors[0] = new Vec3D(component.xMin, component.zMin, 0);
                }
                else if(component.tags.contains("MagpieSegment")){
                    originVectors[1] = new Vec3D(component.xMin, component.zMin, 0);
                } else if(component.tags.contains("CockatooSegment")){
                    originVectors[2] = new Vec3D(component.xMin, component.zMin, 0);
                }
            }
            if(component.tags.contains("SegmentEndpoint")){
                if(component.tags.contains("OspreySegment")){
                    segmentVectors[0] = new Vec3D(component.xMin, component.zMin, 0);
                }
                else if(component.tags.contains("MagpieSegment")){
                    segmentVectors[1] = new Vec3D(component.xMin, component.zMin, 0);
                } else if(component.tags.contains("CockatooSegment")){
                    segmentVectors[2] = new Vec3D(component.xMin, component.zMin, 0);
                }
            }
        }
        for(int i = 0; i < 3; i++){
            segmentVectors[i] = segmentVectors[i].sub(originVectors[i]);
            segmentLengths[i] = segmentVectors[i].magnitude();
        }

        for(LXModel component : model.children){
            for(LXPoint point : component.points){
                CubeData cube = CubeManager.getCube(lx, point.index);
                int indexToUse = 0;
                if(component.tags.contains("OspreySegment")){
                    indexToUse = 0;
                }else if(component.tags.contains("MagpieSegment")){
                    indexToUse = 1;
                }else if(component.tags.contains("CockatooSegment")){
                    indexToUse = 2;
                }
                Vec3D tempVector = new Vec3D(point.x, point.z, 0);
                tempVector = tempVector.sub(originVectors[indexToUse]);
                cube.distancAlongLine = Math.abs(tempVector.dot(segmentVectors[indexToUse].getNormalized())/segmentLengths[indexToUse]);
            }
        }
    }

    @Override
    protected void run(double deltaMs) {
        for(int i = 0; i<3; i++){
            if(Math.random() >= 0.9){
                if(segmentCanFade[i]){
                    segmentPalettes[i] = (segmentPalettes[i] + 1) % 3;
                }
            }
        }
        float sawHeight = spiralModulator.getValuef();
        for (LXModel component : model.children) {
          if(!component.tags.contains("WindowPane")){
                for(LXPoint point : component.points) {
                    CubeData cube = CubeManager.getCube(lx, point.index);
                    colors[point.index] = LX.hsb((cube.distancAlongLine * 360 + sawHeight) % 360, 100, 100);
                    
                }
            }
        }
      }
}


