package entwined.pattern.clover_watson;

import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.pattern.LXPattern;


import entwined.utils.EntwinedUtils;


public class UpFade extends LXPattern {
    boolean fading = false;
    boolean checkingFading = false;
    final SawLFO upFadeModulator = new SawLFO(-0.1, 1.1, 3000);
    
    public UpFade(LX lx) {
        super(lx);
        addModulator(upFadeModulator).start();
    }

    @Override
    protected void run(double deltaMs) {
        float scanHeight = upFadeModulator.getValuef();
        if(scanHeight >= 1 && checkingFading == true){
            if(fading == true){
                fading = false;
            }else if(fading == false){
                fading = true;
            }
            upFadeModulator.setValue(-0.1);
            checkingFading = false;
        }
        if(scanHeight <= 0){
            checkingFading = true;
        }
        for (LXModel component : model.children) {
          if (component.tags.contains("Cheek")) {
            // skip cockatoo cheek
            break;
          }

          for (LXPoint point : component.points) {
            // skip cockatoo cheek
            break;
          }

          for (LXPoint point : component.points) {
              float mappedHeight = EntwinedUtils.map(point.y, model.yMin, model.yMax);
              if(!fading){
                if(scanHeight>=1){
                    colors[point.index] = LX.hsb(
                      mappedHeight * 360,
                      100,
                    0);
                }
                else if(mappedHeight < scanHeight){
                  colors[point.index] = LX.hsb(
                      mappedHeight * 360,
                      100,
                      100);
                } else{
                    colors[point.index] = LX.hsb(
                      mappedHeight * 360,
                      100,
                    0);
                }
              } else if(fading){
                if(scanHeight >= 1){
                    colors[point.index] = LX.hsb(
                      mappedHeight * 360,
                      100,
                    100);
                }
                else if(scanHeight>= 0 && scanHeight<= 1){
                    colors[point.index] = LX.hsb(
                      mappedHeight*360,
                      100,
                       ((1-scanHeight)*100));
                    }else{
                    colors[point.index] = LX.hsb(
                      mappedHeight * 360,
                      100,
                    100);
                    }

                }
          }
        }
    }
}


