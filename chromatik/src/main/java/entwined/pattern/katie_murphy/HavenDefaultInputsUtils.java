package entwined.pattern.katie_murphy;

import java.util.ArrayList;
import java.util.List;

import entwined.core.CubeData;
import entwined.core.CubeManager;
import entwined.utils.EntwinedUtils;

import heronarts.lx.model.LXPoint;
import heronarts.lx.LX;
import heronarts.lx.modulator.SinLFO;

public class HavenDefaultInputsUtils {
    // SpinningStained functions
    static public int SpinningStainedRun(LX lx, LXPoint point, float sawHeight) {
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

        return LX.hsb(
                        paletteSpinningStained[chamberIn][0],
                        paletteSpinningStained[chamberIn][1],
                        paletteSpinningStained[chamberIn][2]);
    }

    // Zebra functions
    static public int ZebraRun(LX lx, LXPoint cube, float position, float thickness) {
        float saturation;
        float brightness = 1;

        if (((CubeManager.getCube(lx, cube.index).localY + position + CubeManager.getCube(lx, cube.index).localTheta) % 200) > thickness) {
          saturation=0;
          brightness=1;
        } else {
          saturation=1;
          brightness=0;
        }

        return LX.hsb(40, 100 * saturation, 100 * brightness);
    }

    // RingoDown functions
    static public int RingoDownRun(LX lx, LXPoint cube, double thick, double brightness, double pos, float color) {
        CubeData cubeData = CubeManager.getCube(lx, cube.index);
        double newPosition = (pos + (50 * Math.sin((cubeData.localX) / 100)));

        if (Math.abs(cubeData.localY - newPosition) < thick) {
            brightness = 100;
        } else if ((cubeData.localY - newPosition) > thick
            && (cubeData.localY - newPosition) < (5 * thick)) {
            brightness = 100
          - 100 * (cubeData.localY - newPosition - thick) / (4 * (thick));
        } else {
            brightness = 0;
        }

        return LX.hsb(color, 100, (float) brightness);
    }

    // ColorWave functions
    static public int ColorWaveRun(LX lx, float wave, LXPoint cube, float waveSlope, float minz, float maxz) {
        return LX.hsb( (wave + waveSlope * EntwinedUtils.map(cube.z, minz, maxz) ) % 360, 100, 100);
    }

    // UpDown functions
    static final double deviation = 0.05;
    static final double twoDeviationSquared = 2 * deviation * deviation;
    static private float gaussian(float value, float center) {
        return (float) Math.exp(-Math.pow(value - center, 2) / twoDeviationSquared);
    }

    // ymin and max are model.yMin and model.yMax
    static public int UpDownRun(LX lx, LXPoint point, float scanHeight, float yMin, float yMax) {
        CubeData cube = CubeManager.getCube(lx, point.index);
        return LX.hsb(
        cube.localTheta + 0 / 6000 * 360,
                      100,
                      100 * gaussian(EntwinedUtils.map(cube.localY, yMin, yMax), scanHeight));
    }
}

