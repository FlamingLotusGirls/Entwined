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

    // debug
    double debugTime = 0;
    double lastTime = 0;

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
    float cheekBrightnessDecaying = 0;
    private boolean playCheekParty = false;
    private double cheekPartyRemainingTimeMs = 0;
    final double cheekPartyTimeMs = 3000;
    final int cheekBrightnessMin = 20;
    private int cheekBrightnessSetting = cheekBrightnessMin;
    final SinLFO cheekSaturationWave = new SinLFO(0, 100, 700);
    final double cheekDecayPeriodMs = 3000;
    // Multiprinter
    private int multiPrinterMode = 0; // top row of buttons and switches
    private float multiPrinterSpeed = 0; // 0 - 10 dial
    private int multiPrinterSwitches = 0; // 1-10
    CompoundParameter  periodMP = new CompoundParameter ("periodMP", 200, 0, 10000);
    SinLFO positionMP = new SinLFO(0, 200, periodMP);
    private double rotation = 0;

    public HavenDefaultInputs(LX lx) {
        super(lx);

        nodeEnabledMap.put("Blender WindowPane", false);  // SpinningStained
        nodeEnabledMap.put("Toaster WindowPane", false);  // RingoDown
        nodeEnabledMap.put("CockatooCheeks", false);  // RingoDown
        nodeEnabledMap.put("Multiprinter", false);  // RingoDown

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

        //multiprinter
        addParameter("periodMP", periodMP);
        addModulator(positionMP).start();
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
        // Cockatoo cheeks
        cheekBrightnessDecaying = (float) Math.exp(Math.log(cheekBrightnessDecaying / 100)
                    - deltaMs / (cheekDecayPeriodMs * 0.5)) * 100;
        // multiprinter 
        rotation += deltaMs/1000.0f * (2 * ((multiPrinterSpeed/10.0f) - .5f) * 360.0f * 1.0f);
        rotation = rotation % 360.0f;

        for (LXModel component : model.children) {          
          for (LXPoint point : component.points) {
            // Blender is double window
            if ((component.tags.contains("WindowPane") && component.tags.contains("MagpieSegment")) && 
                (nodeEnabledMap.get("Blender WindowPane"))) {                 
                colors[point.index] = HavenDefaultInputsUtils.SpinningStainedRun(lx, point, sawHeight);
            }
            // Multiprinter is triple window
            if ((component.tags.contains("WindowPane") && component.tags.contains("CockatooSegment")) && 
                (nodeEnabledMap.get("Multiprinter"))) {
                colors[point.index] = HavenDefaultInputsUtils.MultiprinterRun(lx, point, component, multiPrinterMode, periodMP.getValuef(), multiPrinterSwitches, positionMP.getValuef(), rotation);
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
            // CockatooCheeks
            if (component.tags.contains("Cheek") && component.tags.contains("CockatooSegment"))
            { 
                if (playCheekParty)
                {
                    colors[point.index] = LX.hsb(0, cheekSaturationWave.getValuef(), (float)cheekBrightnessSetting);
                }
                else
                {
                    cheekBrightnessDecaying = EntwinedUtils.max(0, cheekBrightnessDecaying);
                    colors[point.index] = LX.hsb(0, 100, cheekBrightnessDecaying);
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
                cheekBrightnessSetting = cheekBrightnessMin;
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
                nodeEnabledMap.put("Blender WindowPane", statusEnabled);
                if (params.containsKey("speed"))
                {
                    blenderPeriodMs = EntwinedUtils.min((float)params.get("speed"), 100);
                    blenderPeriodMs = EntwinedUtils.max((float)params.get("speed"), 2000);
                }
                break;
            case 2:
                nodeEnabledMap.put("Toaster WindowPane", statusEnabled);
                if (!statusEnabled)
                {
                    playToasterDone = true;
                    toasterDoneRemainingTimeMs = toasterPartyTimeMs;
                }
                break;
            case 3:
                nodeEnabledMap.put("CocktaooCheeks", statusEnabled);
                if (statusEnabled && !playCheekParty) // don't interrupt the party
                {
                    cheekBrightnessSetting = EntwinedUtils.min(cheekBrightnessSetting + 10, 100);
                    cheekBrightnessDecaying = cheekBrightnessSetting;
                    if (cheekBrightnessSetting == 100)
                    {
                        playCheekParty = true;
                        cheekPartyRemainingTimeMs = cheekPartyTimeMs;
                    }
                }
                break;
            case 4:
                // In this approach, each button/lever on the top row has a mode value, and
                // switches 1-10 are effects.  
                nodeEnabledMap.put("Multiprinter", statusEnabled); // any of the green push buttons
                multiPrinterMode = (int) params.get("modeId"); // top row of buttons and switches
                multiPrinterSpeed = (float) params.get("speed"); // 0 - 10 dial
                multiPrinterSwitches = (int)params.get("switches"); // 1-10

                if ((int)multiPrinterSpeed == 0)
                {
                    periodMP.setValue(0);
                }
                else
                {
                    periodMP.setValue(2000 / multiPrinterSpeed);
                }
            default:
                break;
        }
    }
}
