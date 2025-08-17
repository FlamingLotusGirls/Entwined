package entwined.pattern.katie_murphy;

import java.util.ArrayList;
import java.util.List;

import entwined.core.CubeData;
import entwined.core.CubeManager;
import entwined.utils.EntwinedUtils;

import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXModel;
import heronarts.lx.LX;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.color.LXColor;

public class HavenDefaultInputsUtils {
    static final int MP_SWITCH_1_MASK = 0x01;
    static final int MP_SWITCH_2_MASK = 0x02;
    static final int MP_SWITCH_3_MASK = 0x04;
    static final int MP_SWITCH_4_MASK = 0x08;
    static final int MP_SWITCH_5_MASK = 0x10;
    static final int MP_SWITCH_6_MASK = 0x20;
    static final int MP_SWITCH_7_MASK = 0x40;
    static final int MP_SWITCH_8_MASK = 0x80;
    static final int MP_SWITCH_9_MASK = 0x100;
    static final int MP_SWITCH_10_MASK = 0x200;
    static final int MP_SWITCH_ALL_MASK = 0x3ff;

    ////////////////// SpinningStained functions
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

    ////////////////// Zebra functions
    static public int ZebraRun(LX lx, LXPoint cube, float position, float thickness, float saturationScale, float brightnessScale) {
        float saturation;
        float brightness = 1;

        if (((CubeManager.getCube(lx, cube.index).localY + position + CubeManager.getCube(lx, cube.index).localTheta) % 200) > thickness) {
          saturation=0;
          brightness=1;
        } else {
          saturation=1;
          brightness=0;
        }

        return LX.hsb(40, 100 * saturation * saturationScale, 100 * brightness * brightnessScale);
    }

    ////////////////// RingoDown functions
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

    ////////////////// ColorWave functions
    static public int ColorWaveRun(LX lx, float wave, LXPoint cube, float waveSlope, float minz, float maxz) {
        return LX.hsb( (wave + waveSlope * EntwinedUtils.map(cube.z, minz, maxz) ) % 360, 100, 100);
    }

    ////////////////// UpDown functions
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

    ////////////////// Wedges functions
    static public int WedgesRun(LX lx, LXPoint cube, float vCount, float vSat, float vHue, double rotation) 
    {
        double sections = Math.floor(1.0f + vCount * 10.0f);
        double quant = 360.0f/sections;

        return LXColor.hsb(
            Math.floor((rotation - CubeManager.getCube(lx, cube.index).localTheta) / quant) * quant + vHue * 360.0f,
            (1 - vSat) * 100, 100);
    }

    ////////////////// Multiprinter functions
    static public int MultiprinterRun(LX lx, LXPoint point, LXModel component, int multiPrinterMode, float multiPrinterSpeed, int multiPrinterSwitches, float positionSinLfo, double rotation)
    {
        int color = 0;

        // just play whatever the highest number switch is that's on
        if (multiPrinterMode >= 1) 
        {
            color = MultiPrinterRunWedges(lx, point, component, multiPrinterSpeed, multiPrinterSwitches, positionSinLfo, rotation);
        }
        else if (multiPrinterMode == 0)
        {
            color = MultiPrinterRunZebra(lx, point, component, multiPrinterSpeed, multiPrinterSwitches, positionSinLfo, rotation);
        }

      return color;
    }


    static public int MultiPrinterRunZebra(LX lx, LXPoint point, LXModel component, float multiPrinterSpeed, int multiPrinterSwitches, float position, double rotation)
    {
      // window1 is on by default

      boolean addWindow2 = (multiPrinterSwitches & MP_SWITCH_1_MASK) != 0;
      boolean addWindow3 = (multiPrinterSwitches & MP_SWITCH_2_MASK) != 0;
      boolean incBrightness1 = (multiPrinterSwitches & MP_SWITCH_3_MASK) != 0;
      boolean incThickness1 = (multiPrinterSwitches & MP_SWITCH_4_MASK) != 0;
      boolean partyTime1 = (multiPrinterSwitches & MP_SWITCH_5_MASK) != 0;
      boolean incBrightness2 = (multiPrinterSwitches & MP_SWITCH_6_MASK) != 0;
      boolean incThickness2 = (multiPrinterSwitches & MP_SWITCH_7_MASK) != 0;
      boolean partyTime2 = (multiPrinterSwitches & MP_SWITCH_8_MASK) != 0;
      boolean incBrightness3 = (multiPrinterSwitches & MP_SWITCH_9_MASK) != 0;
      boolean incThickness3 = (multiPrinterSwitches & MP_SWITCH_10_MASK) != 0;
      boolean partyTime3 = (multiPrinterSwitches == MP_SWITCH_ALL_MASK);

      
      float thickness1 = incThickness1 ? 160 : 100;
      float thickness2 = incThickness2 ? 160 : 100;
      float thickness3 = incThickness3 ? 160 : 100;
      float brightness1 = incBrightness1 ? 1 : 0.50f;
      float brightness2 = incBrightness2 ? 1 : 0.50f;
      float brightness3 = incBrightness3 ? 1 : 0.50f;

      int partyColors = SpinningStainedRun(lx, point, position);
      int zebra = 0;
      boolean partyTime = false;

      if (addWindow2 && (component.tags.contains("2"))) 
      {
        zebra = ZebraRun(lx, point, position, thickness2, 1f, brightness2);
        partyTime = partyTime2;
      }
      else if (addWindow3 && (component.tags.contains("3"))) 
      {
        zebra = ZebraRun(lx, point, position, thickness3, 1f, brightness3);
        partyTime = partyTime3;
      }
      else if (component.tags.contains("1"))
      {
        // window 1
        zebra = ZebraRun(lx, point, position, thickness1, 1f, brightness1);
        partyTime = partyTime1;
      }
      else
      {
        return 0;
      }

      return (partyTime ? (zebra & partyColors) : zebra);
    }

    static public int MultiPrinterRunWedges(LX lx, LXPoint point, LXModel component, float multiPrinterSpeed, int multiPrinterSwitches, float position, double rotation)
    {
      // window1 is on by default
      boolean addWindow2 = (multiPrinterSwitches & MP_SWITCH_1_MASK) != 0;
      boolean addWindow3 = (multiPrinterSwitches & MP_SWITCH_2_MASK) != 0;
      
      boolean incSaturation = (multiPrinterSwitches & MP_SWITCH_3_MASK) != 0;
      boolean incHue = (multiPrinterSwitches & MP_SWITCH_4_MASK) != 0;
      boolean addMorePoints = (multiPrinterSwitches & MP_SWITCH_5_MASK) != 0;
      boolean addEvenMorePoints = (multiPrinterSwitches & MP_SWITCH_6_MASK) != 0;
      
      boolean move = (multiPrinterSwitches & MP_SWITCH_7_MASK) != 0;
      boolean hueX2 = (multiPrinterSwitches & MP_SWITCH_8_MASK) != 0;
      boolean hueX3 = (multiPrinterSwitches & MP_SWITCH_9_MASK) != 0;
      boolean randomOff = (multiPrinterSwitches & MP_SWITCH_10_MASK) != 0;

      move &= (hueX2 || hueX2);

      float count = 1;
      count = hueX2 ? count + 1 : count;
      count = hueX3 ? count + 1 : count;
      count = (hueX2 && hueX3) ? count + 1 : count;
      count /= 10;

      float saturation = incSaturation ? 0.5f : 0.3f;
      float hue = incHue ? 0.55f : 0.33f;

      int color = 0;
      if (move)
      {
        color = WedgesRun(lx, point, count, saturation, hue, rotation);
      }

      if ((!addWindow2 && (component.tags.contains("2"))) ||
          (!addWindow3 && (component.tags.contains("3"))) ||
          (randomOff && (Math.random() < 0.01f)))
      {
        return 0;
      }

      if (((int)point.y % 9) == 0)
      {
        return (move ? color : LXColor.hsb(0, (1-saturation) * 100, 80));
      }

      if (addMorePoints && (((int)point.y % 3) == 0))
      {
        return (move ? color : LXColor.hsb(55, (1-saturation) * 100, 80));
      }

      if (addEvenMorePoints && (((int)point.y % 1) == 0))
      {
        return (move ? color : LXColor.hsb(35, (1-saturation) * 100, 80));
      }

      return 0;
    }
}

