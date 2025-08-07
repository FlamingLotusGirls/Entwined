package entwined.pattern.katie_murphy;

import entwined.utils.SimplexNoise;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXModel;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.BoundedParameter;

import entwined.utils.EntwinedUtils;
import entwined.plugin.InputEventPattern;
import entwined.core.CubeData;
import entwined.core.CubeManager;

import java.util.HashMap;
import java.util.Map;

public class HavenDefaultInputs extends InputEventPattern {

    public final Map<String, Boolean> nodeEnabledMap = new HashMap<>();

    // UpDown 
    float time = 0;
    final double deviation = 0.05;
    final double minLineCenterY = 0 - 2 * deviation;
    final double maxLineCenterY = 1 + 2 * deviation;
    final SinLFO upDownModulator = new SinLFO(minLineCenterY, maxLineCenterY, 5000);
    final double twoDeviationSquared = 2 * deviation * deviation;

    // SpinningStained
    float blenderPeriodMs = 1000;
    int[][] paletteSpinningStained = {{217,100,66},{202,97,87},{159,94,76},{23,100,94},{41,78,92}};
    float chamberSizeSpinningStained = (float) (360.0/paletteSpinningStained.length);
    final SawLFO spinningWindowModulator = new SawLFO(0, 360, blenderPeriodMs);

    // RingoDown
    CompoundParameter thicknessRingoDown = new CompoundParameter("Thickness", 35, 0, 40);
    CompoundParameter colorOfCubesRingoDown = new CompoundParameter("Color", 40, 0, 360);
    CompoundParameter speedRingoDown = new CompoundParameter("speedRingoDown", 1850, 300, 2000);
    SawLFO positionRingoDown = new SawLFO(350, 0, speedRingoDown);

    // ColorWave
    private float minzColorWave = Float.MAX_VALUE;
    private float maxzColorWave = -Float.MAX_VALUE;
    private float speedMultColorWave = 1000;
    final BoundedParameter speedParamColorWave = new BoundedParameter("speedParamColorWave", .65, 20, .01);
    final BoundedParameter waveSlopeColorWave = new BoundedParameter("waveSlope", 360, 1, 720);
    final SawLFO waveColorWave = new SawLFO(0, 360, speedParamColorWave.getValuef() * speedMultColorWave);

    // Zebra
    CompoundParameter thicknessZebra =  new CompoundParameter ("THIC", 160, 0, 200);
    CompoundParameter  periodZebra = new CompoundParameter ("PERI", 1000, 300, 3000);
    SinLFO positionZebra = new SinLFO(0, 200, periodZebra);

    ///////// Fixture specific
    // Toaster stuff
    private boolean playToasterDone = false;
    private double toasterDoneRemainingTimeMs = 0;
    final double toasterPartyTimeMs = 3000;
    // CockatooCheeks
    private boolean playCheekParty = false;
    private double cheekPartyRemainingTimeMs = 0;
    final double cheekPartyTimeMs = 3000;
    final int cheekBrightnessMin = 20;
    private int cheekBrightness = cheekBrightnessMin;
    final SinLFO cheekSaturationWave = new SinLFO(0, 100, 700);

    public HavenDefaultInputs(LX lx) {
        super(lx);
        nodeEnabledMap.put("MagpieSegment WindowPane", false);
        nodeEnabledMap.put("Blender WindowPane", false);  // SpinningStained
        nodeEnabledMap.put("Toaster WindowPane", false);  // RingoDown
        nodeEnabledMap.put("CockatooCheeks", false);  // RingoDown

        addModulator(upDownModulator).start();
        addModulator(spinningWindowModulator).start();
        addParameter("thickness", thicknessRingoDown);
        addParameter("color", colorOfCubesRingoDown);
        addParameter("speedRingoDown", speedRingoDown);
        addModulator(positionRingoDown).start();

        addModulator(waveColorWave).start();
        addParameter("waveslope", waveSlopeColorWave);
        addParameter("speedParamColorWave", speedParamColorWave);
        for (LXPoint cube : model.points) {
            if (cube.z < minzColorWave) {minzColorWave = cube.z;}
            if (cube.z > maxzColorWave) {maxzColorWave = cube.z;}
        }        

        addParameter("thicknessZebra", thicknessZebra);
        addParameter("periodZebra", periodZebra);
        addModulator(positionZebra).start();

        addModulator(cheekSaturationWave).start();
    }

    @Override
    protected void run(double deltaMs) {
        // UpDown
        float scanHeight = upDownModulator.getValuef();
        // SpinningStained
        float sawHeight = spinningWindowModulator.getValuef();       
        // RingoDown
        double thick = thicknessRingoDown.getValue();
        double brightness = 0;
        double pos = positionRingoDown.getValue();
        // ColorWave
        waveColorWave.setPeriod(speedParamColorWave.getValuef() * speedMultColorWave);


        for (LXModel component : model.children) {          
          for (LXPoint point : component.points) {
            // Magpie is double window
            if ((component.tags.contains("WindowPane") && component.tags.contains("MagpieSegment")) && 
                (nodeEnabledMap.get("MagpieSegment WindowPane"))) {                 
                // UpDownRun(point, scanHeight);
                ZebraRun(point, positionZebra.getValuef(), thicknessZebra.getValuef());
            }
            // Blender is triple window
            if ((component.tags.contains("WindowPane") && component.tags.contains("CockatooSegment")) && 
                (nodeEnabledMap.get("Blender WindowPane"))) {
                 SpinningStainedRun(point, sawHeight);
            }
            // Toaster is single window
            if (component.tags.contains("WindowPane") && component.tags.contains("OspreySegment")) 
            {
                // TODO: toaster is opposite active signal--assuming that's handled on ESP32
                // and 1 here means button active
                if (nodeEnabledMap.get("Toaster WindowPane")) 
                {
                    RingoDownRun(point, thick, brightness, pos, colorOfCubesRingoDown.getValuef());
                }
                else if (playToasterDone)
                {
                    ColorWaveRun(waveColorWave.getValuef(), point, waveSlopeColorWave.getValuef(), minzColorWave, maxzColorWave);
                }
            }
            // CockatooCheeks is ??? TBD
            //if ((component.tags.contains("CockatooCheeks") && component.tags.contains("CockatooSegment"))
            if (component.tags.contains("CocktaooCheeks") && component.tags.contains("CockatooSegment"))
            { 
                if (playCheekParty)
                {
                    colors[point.index] = LX.hsb(0, cheekSaturationWave.getValuef(), (float)cheekBrightness);
                }
                else
                {
                    colors[point.index] = LX.hsb(0, 100, (float)cheekBrightness);
                }
            }
          }
        }

        // Time track the things that are time dependent
        if (playToasterDone)
        {
            if (toasterDoneRemainingTimeMs > 0)
            {
                toasterDoneRemainingTimeMs -= deltaMs;
            }
            else
            {
                playToasterDone = false;
            } 
        }
        if (playCheekParty)
        {
            if (cheekPartyRemainingTimeMs > 0)
            {
                cheekPartyRemainingTimeMs -= deltaMs;
            }
            else
            {
                playCheekParty = false;
                cheekBrightness = cheekBrightnessMin;
            } 
        }
    }

    @Override
    public void onInputEvent(Map<String, Object> params) {
        // do pattern for button
        double status = (double) params.get("buttonStatus");
        double nodeId = (double) params.get("inputNodeId");
        
        boolean statusEnabled = status != 0.0;
        int node = (int)nodeId;

        switch (node) {
            case 1:
                nodeEnabledMap.put("MagpieSegment WindowPane", statusEnabled);
                break;
            case 2:
                nodeEnabledMap.put("Blender WindowPane", statusEnabled);
                if (params.containsKey("speed"))
                {
                    blenderPeriodMs = EntwinedUtils.min((float)params.get("speed"), 100);
                    blenderPeriodMs = EntwinedUtils.max((float)params.get("speed"), 2000);
                }
                break;
            case 3:
                nodeEnabledMap.put("Toaster WindowPane", statusEnabled);
                if (!statusEnabled)
                {
                    playToasterDone = true;
                    toasterDoneRemainingTimeMs = toasterPartyTimeMs;
                }
                break;
            case 4:
                nodeEnabledMap.put("CocktaooCheeks", statusEnabled);
                if (statusEnabled && !playCheekParty) // don't interrupt the party
                {
                    cheekBrightness = EntwinedUtils.min(cheekBrightness + 10, 100);
                    if (cheekBrightness == 100)
                    {
                        playCheekParty = true;
                        cheekPartyRemainingTimeMs = cheekPartyTimeMs;
                    }
                }
                break;
            default:
                break;
        }
    }

    // UpDown functions
    float gaussian(float value, float center) {
        return (float) Math.exp(-Math.pow(value - center, 2) / twoDeviationSquared);
    }

    private void UpDownRun(LXPoint point, float scanHeight) {
        CubeData cube = CubeManager.getCube(lx, point.index);
        colors[point.index] = LX.hsb(
        cube.localTheta + time / 6000 * 360,
                      100,
                      100 * gaussian(EntwinedUtils.map(cube.localY, model.yMin, model.yMax), scanHeight));
    }

    // SpinningStained functions
    private void SpinningStainedRun(LXPoint point, float sawHeight) {
        int chamberIn = 4;
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

    // RingoDown functions
    private void RingoDownRun(LXPoint cube, double thick, double brightness, double pos, float color) {
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

        colors[cube.index] = LX.hsb(color, 100,
        (float) brightness);
    }

    // ColorWave functions
    private void ColorWaveRun(float wave, LXPoint cube, float waveSlope, float minz, float maxz) {
        colors[cube.index] = LX.hsb( (wave + waveSlope * EntwinedUtils.map(cube.z, minz, maxz) ) % 360, 100, 100);
    }

    // Zebra functions
    public void ZebraRun(LXPoint cube, float position, float thickness) {
        float saturation;
        float brightness = 1;

        if (((CubeManager.getCube(lx, cube.index).localY + position + CubeManager.getCube(lx, cube.index).localTheta) % 200) > thickness) {
          saturation=0;
          brightness=1;
        } else {
          saturation=1;
          brightness=0;
        }

        colors[cube.index] = LX.hsb(40, 100 * saturation, 100 * brightness);
    }
}
