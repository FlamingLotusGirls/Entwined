package entwined.pattern.katie_murphy;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.model.LXModel;

import entwined.utils.SimplexNoise;

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

    float height = 0.0f;
    double time = 0;

    // discrete/continuous triggers
    public final DiscreteParameter cockatooWindowPaletteParam = new DiscreteParameter("cWinPalette", 0, 3);
    final CompoundParameter cockatooJellyBrightnessParam = new CompoundParameter("cChndBrt", 40, 40, 85);

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
        addEffect(new CockatooCheekPulseEffect(lx));
        addEffect(new SpinningStainedEffect(lx));
        addEffect(new CockatooJellyChandelierEffect(lx));
        addEffect(new MagpieWindowPulseEffect(lx));
    }

    @Override
    public void run(double deltaMs) {
        setCockatooWindowDefaults();

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


}
