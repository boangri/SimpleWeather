
package simpleweather;

import com.dalsemi.onewire.*;
import com.dalsemi.onewire.adapter.*;
import com.dalsemi.onewire.container.*;

/**
 *
 * @author Boris
 */
public abstract class AbstractSensor {
  protected DSPortAdapter adapter;
  protected static boolean debugFlag = SimpleWeather.debugFlag;
  protected float sumValues;
  protected float sumSquares;
  protected int samples;
  
  public void resetAverages()
  {
    samples = 0;
    sumValues = 0f;
    sumSquares = 0f;
    
    if (debugFlag)
      System.out.println("Averages Reset");
  }
  
  protected void update(float value)
  {
      samples++;
      sumValues += value;
      sumSquares += value*value;
  }
  
  public String getAverage()
  {
    if (samples == 0) return "U";
    
    float avg = sumValues/samples;
    
    return WeatherCruncher.formatValue(avg, 1);
  }
  
  public String getSigma()
  {
    if (samples == 0) return "U";
    
    float avg = sumValues/samples;
    float disp = sumSquares/samples;
    float sigma = (float)Math.sqrt(disp - avg*avg);
    
    return WeatherCruncher.formatValue(avg, 1);
  }
  
}
