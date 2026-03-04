package entwined.pattern.katie_murphy;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.LXLayer;
import heronarts.lx.LXLayeredComponent;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.model.LXModel;

import entwined.pattern.irene_zhou.Bubbles;
import entwined.utils.SimplexNoise;
import entwined.utils.EntwinedUtils;


/**
 * Started out as a copy of Eric Gauderman's HavenSolids,
 * intended to be used with OSC found object triggers
 */
public class HavenSolidsPlus extends LXPattern {

    // default params
    private static final float velocity = 10f / 200f;
    private static final float blobWidth = 90f;
    private static final float blobHeight = 60f;
    private static final float fill = 25f / 50f;

    final SinLFO redWave = new SinLFO(0, 20, 2200);
    final SinLFO yellowWave = new SinLFO(40, 50, 1800);
    final SinLFO orangeWave = new SinLFO(30, 45, 2000);
    final SinLFO greenWave = new SinLFO(140, 155, 1600);
    final SinLFO cyanWave = new SinLFO(170, 190, 2300);
    final SinLFO blueWave = new SinLFO(230, 256, 2400);
    final SinLFO purpleWave = new SinLFO(256, 270, 1700);
    final SinLFO spiralPortalWave = new SinLFO(10, 270, 4000);
    final SinLFO ospreyWave = new SinLFO(50, 60, 3000);

    private SinLFO window1wave = greenWave;
    private SinLFO window2wave = yellowWave;
    private SinLFO window3wave = blueWave;

    private final int[] jellyPalette = new int[] {
        LXColor.rgb(180, 0, 180),
        LXColor.rgb(255, 105, 180),
        LXColor.rgb(0, 255, 255),
    };

    float height = 0.0f;
    double time = 0;

    // found object one-shot triggers
    private boolean cockatooJellyActive = false;
    private double cockatooJellyEnd = 0;
    private double cockatooJellyDurationMs = 5000;
    private boolean magpieSpiralActive = false;
    private double magpieSpiralEnd = 0;
    private double magpieSpiralDurationMs = 5000;
    final BooleanParameter cockatooJellyChandelierParam = new BooleanParameter("cChand", false).setMode(BooleanParameter.Mode.MOMENTARY);
    final BooleanParameter magpieSpiralChandelierParam = new BooleanParameter("mChand", false).setMode(BooleanParameter.Mode.MOMENTARY);
    
    // discrete/continuous triggers
    public final DiscreteParameter cockatooWindowPaletteParam = new DiscreteParameter("cWinPalette", 0, 3);
    final CompoundParameter cockatooJellyBrightnessParam = new CompoundParameter("cChndBrt", 40, 40, 85);

    private final Bubbles magpieSpiralEffect;
    private java.lang.reflect.Method runMethod;
    private java.lang.reflect.Field colorsField;

    public HavenSolidsPlus(LX lx) {
        super(lx);

        // defaults
        addModulator(redWave).start();
        addModulator(orangeWave).start();
        addModulator(yellowWave).start();
        addModulator(greenWave).start();
        addModulator(cyanWave).start();
        addModulator(blueWave).start();
        addModulator(purpleWave).start();
        addModulator(spiralPortalWave).start();
        addModulator(ospreyWave).start();
    
        // discrete/continuous settings
        addParameter("cockatooWindowPalette", cockatooWindowPaletteParam);
        addParameter("cockatooJellyBrightness", cockatooJellyBrightnessParam);

        // found object OSC triggers
        addParameter("cockatooJellyTwinkleTrigger", cockatooJellyChandelierParam);
        cockatooJellyChandelierParam.addListener(p -> {
            if (cockatooJellyChandelierParam.getValueb() && !cockatooJellyActive) {
                cockatooJellyActive = true;
                cockatooJellyEnd = EntwinedUtils.millis() + cockatooJellyDurationMs;
            }
        });
        addParameter("magpieSpiralTrigger", magpieSpiralChandelierParam);
        magpieSpiralChandelierParam.addListener(p -> {
            if (magpieSpiralChandelierParam.getValueb() && !magpieSpiralActive) {
                magpieSpiralActive = true;
                magpieSpiralEnd = EntwinedUtils.millis() + magpieSpiralDurationMs;
            }
        });

        magpieSpiralEffect = new Bubbles(lx);
        magpieSpiralEffect.ballCount.setValue(149);

        try {
            runMethod = LXPattern.class.getDeclaredMethod("run", double.class);
            runMethod.setAccessible(true);

            colorsField = LXLayeredComponent.class.getDeclaredField("colors");
            colorsField.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void renderDefault(double deltaMs) {
        // FreeFall parameter derivations
        float baseline = Math.max(0f, 1f - fill);
        float boost = Math.max(0f, fill - 1f);
        height += deltaMs * velocity / blobHeight;

        for (LXModel component : model.children) {
            // Assign colors to fixtures
            float hue = blueWave.getValuef();
            if (component.tags.contains("Cheek")) {
                hue = orangeWave.getValuef();
            } else if (component.tags.contains("Eye")) {
                if (component.tags.contains("CockatooSegment")) {
                    hue = purpleWave.getValuef();
                } else if (component.tags.contains("MagpieSegment")) {
                    hue = blueWave.getValuef();
                }
            } else if (component.tags.contains("Body")) {
                hue = cyanWave.getValuef();
            } else if (component.tags.contains("SpiralPortal")) {
                hue = spiralPortalWave.getValuef();
            } else if (component.tags.contains("WindowPane")) {
                if (component.tags.contains("CockatooSegment")) {
                    if (component.tags.contains("1")) {
                        hue = window1wave.getValuef();
                    } else if (component.tags.contains("2")) {
                        hue = window2wave.getValuef();
                    } else if (component.tags.contains("3")) {
                        hue = window3wave.getValuef();
                    }
                } else if (component.tags.contains("MagpieSegment")) {
                    if (component.tags.contains("1")) {
                        hue = yellowWave.getValuef();
                    } else if (component.tags.contains("2")) {
                        hue = blueWave.getValuef();
                    }
                } else if (component.tags.contains("OspreySegment")) {
                    if (component.tags.contains("1")) {
                        hue = greenWave.getValuef();
                    } else if (component.tags.contains("2")) {
                        hue = blueWave.getValuef();
                    }
                }
            } else if (component.tags.contains("Spotlight")) {
                if (component.tags.contains("Backlight")) {
                    if (component.tags.contains("StainedGlass")) {
                        if (component.tags.contains("CockatooSegment")) {
                            hue = redWave.getValuef();
                        } else if (component.tags.contains("MagpieSegment")) {
                            hue = blueWave.getValuef();
                        } else if (component.tags.contains("OspreySegment")) {
                            hue = blueWave.getValuef();
                        }
                    } else if (component.tags.contains("CutoutWindow")) {
                        if (component.tags.contains("CockatooSegment")) {
                            hue = redWave.getValuef();
                        } else if (component.tags.contains("MagpieSegment")) {
                            hue = greenWave.getValuef();
                        } else if (component.tags.contains("OspreySegment")) {
                            hue = blueWave.getValuef();
                        }
                    }
                } else if (component.tags.contains("Osprey")) {
                    hue = ospreyWave.getValuef();
                } else if (component.tags.contains("1")) {
                    hue = greenWave.getValuef();
                } else if (component.tags.contains("2")) {
                    hue = blueWave.getValuef();
                } else if (component.tags.contains("3")) {
                    hue = redWave.getValuef();
                } else if (component.tags.contains("4")) {
                    hue = yellowWave.getValuef();
                } else if (component.tags.contains("5")) {
                    hue = greenWave.getValuef();
                } else if (component.tags.contains("6")) {
                    hue = blueWave.getValuef();
                } else if (component.tags.contains("7")) {
                    hue = redWave.getValuef();
                } else if (component.tags.contains("8")) {
                    hue = yellowWave.getValuef();
                } else if (component.tags.contains("9")) {
                    hue = greenWave.getValuef();
                } else if (component.tags.contains("10")) {
                    hue = blueWave.getValuef();
                } else if (component.tags.contains("11")) {
                    hue = redWave.getValuef();
                } else if (component.tags.contains("12")) {
                    hue = yellowWave.getValuef();
                }

                // FreeFall math to give it a bit of glimmer
                for (LXPoint point : component.points) {
                    float noise1 = 0.5f
                        + (float) SimplexNoise.noise(point.x / blobWidth,
                            point.z / blobWidth, height + point.y / blobHeight)
                            / 2f;

                    float cutoffNoise1 = Math.min(1.0f,
                        Math.max(0.0f, (noise1 + boost - baseline)
                            / (1.0f - boost - baseline)));
                    float brightness = Float.max(40, cutoffNoise1 * 100f);

                    colors[point.index] = LX.hsb(hue, 40, brightness);
                }
                continue;
            } else if (component.tags.contains("Egg")) {
                if (component.tags.contains("1")) {
                    hue = yellowWave.getValuef();
                } else if (component.tags.contains("2")) {
                    hue = redWave.getValuef();
                } else if (component.tags.contains("3")) {
                    hue = orangeWave.getValuef();
                }
            } else if (component.tags.contains("PooferBird")) {
                if (component.tags.contains("CockatooSegment")) {
                    hue = orangeWave.getValuef();
                } else if (component.tags.contains("MagpieSegment")) {
                    hue = blueWave.getValuef();
                } else if (component.tags.contains("OspreySegment")) {
                    hue = yellowWave.getValuef();
                }
            } else if (component.tags.contains("BirdBath")) {
                hue = yellowWave.getValuef();
            } else if (component.tags.contains("ZenGarden")) {
                hue = blueWave.getValuef();
                for (LXPoint point : component.points) {
                    colors[point.index] = LX.hsb(hue, 85, 50);
                }
                continue;
            }

            // FreeFall math to give it a bit of glimmer
            for (LXPoint point : component.points) {
                float brightnessMin = 40f;
                if (component.tags.contains("JellyChandelier")) {
                    brightnessMin = cockatooJellyBrightnessParam.getValuef();
                }

                float noise1 = 0.5f
                    + (float) SimplexNoise.noise(point.x / blobWidth,
                        point.z / blobWidth, height + point.y / blobHeight)
                        / 2f;

                float cutoffNoise1 = Math.min(1.0f, Math.max(0.0f,
                    (noise1 + boost - baseline) / (1.0f - boost - baseline)));
                float brightness = Float.max(brightnessMin, cutoffNoise1 * 100f);

                colors[point.index] = LX.hsb(hue, 85, brightness);
            }
        }
    }

    @Override
    public void run(double deltaMs) {
        checkEndEffects();

        // effects which setup the default
        setCockatooWindowDefaults();
        
        renderDefault(deltaMs);
        
        // one shot effects that replace the default
        runCockatooJelly(deltaMs);
        runMagpieSpiral(deltaMs);

        
    }

    ////////////// default setups
    private void setCockatooWindowDefaults() {
        int paletteNum = cockatooWindowPaletteParam.getValuei();
        switch (paletteNum) {
          case 0:
            window1wave = greenWave;
            window2wave = yellowWave;
            window3wave = blueWave;
            break;
          case 1:
            window1wave = blueWave;
            window2wave = greenWave;
            window3wave = yellowWave;
            break;
          case 2:
            window1wave = yellowWave;
            window2wave = blueWave;
            window3wave = greenWave;
            break;
          default:
            // same as 0 for now
            window1wave = greenWave;
            window2wave = yellowWave;
            window3wave = blueWave;
            break;
        }
    }

    ////////////// conclude one shots
    private void checkEndEffects() {
        if (EntwinedUtils.millis() > cockatooJellyEnd) {
            cockatooJellyActive = false;
        }
        if (EntwinedUtils.millis() > magpieSpiralEnd) {
            magpieSpiralActive = false;
        }

    }

    ///////////// one shots
    private void runCockatooJelly(double deltaMs) {
        if (cockatooJellyActive) {
            time += deltaMs / 1000;
            double speed = 1.25;
            double f = (time * speed) % 1.0;
            double palettePos = f * jellyPalette.length;
            int idx0 = (int)Math.floor(palettePos) % jellyPalette.length;
            int idx1 = (idx0 + 1) % jellyPalette.length;
            double mix = palettePos - Math.floor(palettePos);
            int color = LXColor.lerp(jellyPalette[idx0], jellyPalette[idx1], mix);

            for (LXModel component : model.children) {
                if (component.tags.contains("JellyChandelier")) {    
                    for (LXPoint point : component.points) {
                        colors[point.index] = color;
                    }
                }
            }
        }
    }

    private void runMagpieSpiral(double deltaMs) {
        if (magpieSpiralActive) {
            try {
                // Save current colors for non-tagged points
                int[] saved = java.util.Arrays.copyOf(this.colors, this.colors.length);
  
                // Let overlay write to our colors[]
                colorsField.set(magpieSpiralEffect, this.colors);
                runMethod.invoke(magpieSpiralEffect, deltaMs);
  
                // Restore non-tagged points back to default
                for (LXModel component : model.children) {
                    if (!component.tags.contains("SpiralPortal")) {    
                        for (LXPoint point : component.points) {
                            colors[point.index] = saved[point.index];
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
