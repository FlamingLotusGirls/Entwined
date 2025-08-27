package entwined.pattern.eric_gauderman;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.pattern.LXPattern;
import entwined.utils.SimplexNoise;
import heronarts.lx.model.LXModel;

/**
 * Started out as a copy of Katie Murphy's Sunset
 */
public class HavenSolids extends LXPattern {

    final SinLFO redWave = new SinLFO(0, 20, 2200);
    final SinLFO yellowWave = new SinLFO(40, 50, 1800);
    final SinLFO orangeWave = new SinLFO(30, 45, 2000);
    final SinLFO greenWave = new SinLFO(140, 155, 1600);
    final SinLFO cyanWave = new SinLFO(170, 190, 2300);
    final SinLFO blueWave = new SinLFO(230, 256, 2400);
    final SinLFO purpleWave = new SinLFO(256, 270, 1700);
    final SinLFO spiralPortalWave = new SinLFO(10, 270, 4000);

    double time_ms = 0;

    float height = 0.0f;
    final CompoundParameter velocityParam = new CompoundParameter("VEL", 10f,
        -100f, 100f);
    final CompoundParameter blobWidthParam = new CompoundParameter("WID", 90f,
        10f, 400f);
    final CompoundParameter blobHeightParam = new CompoundParameter("HGT", 60f,
        10f, 400f);
    final CompoundParameter fillParam = new CompoundParameter("FIL", 25f,
        0.001f, 100f);

    public HavenSolids(LX lx) {
        super(lx);

        addParameter("velocity", velocityParam);
        addParameter("blob_width", blobWidthParam);
        addParameter("blob_height", blobHeightParam);
        addParameter("fill", fillParam);
        addModulator(redWave).start();
        addModulator(orangeWave).start();
        addModulator(yellowWave).start();
        addModulator(greenWave).start();
        addModulator(cyanWave).start();
        addModulator(blueWave).start();
        addModulator(purpleWave).start();
        addModulator(spiralPortalWave).start();
    }

    @Override
    public void run(double deltaMs) {
        // FreeFall parameter derivations
        float velocity = velocityParam.getValuef() / 200f;
        float fill = fillParam.getValuef() / 50f;
        float baseline = Math.max(0f, 1f - fill);
        float boost = Math.max(0f, fill - 1f);
        float blobWidth = blobWidthParam.getValuef();
        float blobHeight = blobHeightParam.getValuef();
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
                        hue = greenWave.getValuef();
                    } else if (component.tags.contains("2")) {
                        hue = yellowWave.getValuef();
                    } else if (component.tags.contains("3")) {
                        hue = blueWave.getValuef();
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
                float noise1 = 0.5f
                    + (float) SimplexNoise.noise(point.x / blobWidth,
                        point.z / blobWidth, height + point.y / blobHeight)
                        / 2f;

                float cutoffNoise1 = Math.min(1.0f, Math.max(0.0f,
                    (noise1 + boost - baseline) / (1.0f - boost - baseline)));
                float brightness = Float.max(40, cutoffNoise1 * 100f);

                colors[point.index] = LX.hsb(hue, 85, brightness);
            }
        }
    }
}
