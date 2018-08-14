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
import simpleweather.SimpleWeather;
import simpleweather.SimpleWeatherException;



public class WindSpeedSensor extends AbstractSensor
{
  // calibration constants
  private static final float radius = Float.parseFloat(SimpleWeather.WIND_RADIUS); // effective radius of the wheel
  private static final int NORTH_OFFSET = Integer.parseInt(SimpleWeather.NORTH_OFFSET);
  
  // class variables
  private DSPortAdapter adapter;
  private long lastCount = 0;
  private long lastTicks = 0;
  private OneWireContainer1D   windSpdDevice = null;
  private OneWireContainer20   windDirDevice = null;
  public float windSpeed = 0f;
  public int windDir = 16;
  private float sumWind, sumSquares;
  private int samples;
  private double sumSin, sumCos;
  private static final boolean debugFlag = SimpleWeather.debugFlag;
 
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
      this.resetAverages();
    }  
  }
 
 
  public float getWindSpeed() throws SimpleWeatherException
  {
    //float windSpeed = 0f;
    
    if (windSpdDevice == null) {
        throw new SimpleWeatherException("No wind speed device found");
    }
    
    try
    {
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
    }
    catch (OneWireException e)
    {
      throw new SimpleWeatherException("" + e);
    }

    return windSpeed;
  }
  
  public void update()
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
}
