/******************************************************************************
 
 Project Name: SimpleWeather
 File name:    WindSensor.java
 Version:      1.0.3 02/05/06
 Copyright (C) 2006 by T. Bitson - All rights reserved.
 
 This class provides the interface to the 1-wire temperature sensor
 device.
 
 *****************************************************************************/

package simpleweather.sensor;


import com.dalsemi.onewire.*;
import com.dalsemi.onewire.adapter.*;
import com.dalsemi.onewire.container.*;
import java.util.Properties;
import simpleweather.SimpleWeather;
import simpleweather.SimpleWeatherException;



public class WindSpeedSensor extends AbstractSensor
{
  // calibration constants
  private final float radius = Float.parseFloat(SimpleWeather.WIND_RADIUS); // effective radius of the wheel
  
  // class variables
  private long lastCount = 0;
  private long lastTicks = 0;
  private OneWireContainer1D   windSpdDevice = null;
  public float windSpeed = 0f;
  private float sumWind;
  private double sumSin, sumCos;
 
  public WindSpeedSensor(DSPortAdapter adapter, String windSpdDeviceID)
  {
    // get instances of the 1-wire devices
    windSpdDevice   = new OneWireContainer1D(adapter, windSpdDeviceID);
    
    if (windSpdDevice != null)
    {
      lastTicks = System.currentTimeMillis();
      try
      {
        lastCount = windSpdDevice.readCounter(15);
      } 
      catch (OneWireException e)
      {
         System.out.print("Can't create Conatiner20\n");
      }
      this.resetAverage();
    }  
  }
 
 
  public float getWindSpeed() throws OneWireException
  {
    //float windSpeed = 0f;
    
    
      if (debugFlag)
      {
        System.out.print("Wind Speed: Device = " + windSpdDevice.getName());
        System.out.print("  ID = " + windSpdDevice.getAddressAsString() + "\n");
      }

      // read wind counter & system time
      long currentCount = windSpdDevice.readCounter(15);
      long currentTicks = System.currentTimeMillis();
      System.out.println("Wind Count: " + currentCount);

      if (lastTicks != 0)
      {
        // calculate the wind speed based on the revolutions per second
        //windSpeed = ((currentCount-lastCount)/((currentTicks-lastTicks)/1000f)) / 2.0f * 2.453f;   // MPH
        //windSpeed = ((currentCount-lastCount)/((currentTicks-lastTicks)/1000f)) / 2.0f * 2.453f * 1609.0f / 3600.0f;   // Met/sec
        windSpeed = (float)(currentCount-lastCount)/(float)(currentTicks-lastTicks)*1000f*6.28f*radius;
      }

      if (debugFlag)
        System.out.println("Count = " + (currentCount-lastCount) + " during " +
                (currentTicks-lastTicks) + "ms calcs to " + windSpeed + " radius="+radius);

      // remember count & time
      lastCount = currentCount;
      lastTicks = currentTicks;

    return windSpeed;
  }
  
  public void updateAverage()
  { 
    samples++;
    
    if (debugFlag)
      System.out.println("Sample #" + samples);
    
    // update sumWind speed
    sumWind += windSpeed;
    sumSquares += windSpeed*windSpeed;
  }
  
  public String getWind()
  {
    if (samples == 0) return ("U");
    
    double avgWind = Math.sqrt(sumSquares/samples);
    
    return this.formatValue((float)avgWind, 1);
  } 
  
  public String getWindSigma()
  {
    if (samples == 0) return ("U");
    
    float avg = sumWind/samples;
    float disp = sumSquares/samples;
    double sigma = Math.sqrt(disp - avg*avg);
    
    return this.formatValue((float)sigma, 1);
  } 
  
  public String getWindPk()
  {
    if (samples == 0) return ("U");
    
    float avg = sumWind/samples;
    float disp = sumSquares/samples;
    float sigma = (float)Math.sqrt(disp - avg*avg);
    
    return this.formatValue(avg + sigma, 1);
  } 
  
  public void update()
  {
    try {
        float data = this.getWindSpeed();
        System.out.println("Wind speed = " + data + " M/sec");
        updateAverage(data);
    } catch (OneWireException e) {
        System.out.println("Error Reading Wind Speed: " + e);
    }
  }
  
  public Properties getResults()
  {
//            sendUrl.append("&wspd=" + sw.wss1.getWind());
//            sendUrl.append("&wspdpk=" + sw.wss1.getWindSigma());
      Properties p = new Properties();
      p.setProperty("wspd", getWind());
      p.setProperty("wspdpk", getWindPk());
      
      return p;
  }
}
