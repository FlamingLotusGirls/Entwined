package entwined.pattern.katie_murphy;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXModel;
import heronarts.lx.parameter.CompoundParameter;
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
    final double deviation = 0.05;
    final double minLineCenterY = 0 - 2 * deviation;
    final double maxLineCenterY = 1 + 2 * deviation;    
    final SinLFO upDownModulator = new SinLFO(minLineCenterY, maxLineCenterY, 5000);
    
    // SpinningStained
    float blenderPeriodMs = 1000;
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
                colors[point.index] = HavenDefaultInputsUtils.ZebraRun(lx, point, positionZebra.getValuef(), thicknessZebra.getValuef());
            }
            // Blender is triple window
            if ((component.tags.contains("WindowPane") && component.tags.contains("CockatooSegment")) && 
                (nodeEnabledMap.get("Blender WindowPane"))) {
                colors[point.index] = HavenDefaultInputsUtils.SpinningStainedRun(lx, point, sawHeight);
            }
            // Toaster is single window
            if (component.tags.contains("WindowPane") && component.tags.contains("OspreySegment")) 
            {
                if (nodeEnabledMap.get("Toaster WindowPane")) 
                {
                    colors[point.index] = HavenDefaultInputsUtils.RingoDownRun(lx, point, thick, brightness, pos, colorOfCubesRingoDown.getValuef());
                }
                else if (playToasterDone)
                {
                    colors[point.index] = HavenDefaultInputsUtils.ColorWaveRun(lx, waveColorWave.getValuef(), point, waveSlopeColorWave.getValuef(), minzColorWave, maxzColorWave);
                }
            }
            // CockatooCheeks is ??? TBD
            if (component.tags.contains("CockatooCheeks") && component.tags.contains("CockatooSegment"))
            //if (component.tags.contains("WindowPane") && component.tags.contains("CockatooSegment"))
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
            case 0:
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
            case 1:
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
}
