/******************************************************************************
 
 Project Name: SimpleWeather
 File name:    RainSensor.java
 Version:      1.0.4 02/07/06
 $Id: RainSensor.java,v 1.1.1.1 2010/02/19 15:34:24 boris Exp $
 
 Copyright (C) 2006 by T. Bitson - All rights reserved.
 
 This class provides the interface to the 1-wire rain counter device.
 
 *****************************************************************************/

package simpleweather.sensor;


import com.dalsemi.onewire.*;
import com.dalsemi.onewire.adapter.*;
import com.dalsemi.onewire.container.*;
import simpleweather.SimpleWeatherException;



public class RainSensor extends AbstractSensor
{
  
  // class variables
  private OneWireContainer1D rainDevice = null;
  private float RAIN_OFFSET;
  private float RAIN_GAIN = 0.01f;
  private long lastCount = 0;
  private long currentCount = 0;
  private float rain;
  private float rainRate;
  
  public RainSensor(DSPortAdapter adapter, String deviceID, float rain_offset)
  {
    // get instances of the 1-wire devices
    rainDevice = new OneWireContainer1D(adapter, deviceID);
    RAIN_OFFSET = rain_offset;
    
  }
  
  public float getRainCount() throws SimpleWeatherException
  {
    
    
    try
    {
      if (debugFlag)
      {
        System.out.print("Rain: Device = " + rainDevice.getName());
        System.out.print("  ID = " + rainDevice.getAddressAsString() + "\n");
      }
      
      
      // read rain count from counter 15 and subtract offset
      currentCount = rainDevice.readCounter(15) - (long)RAIN_OFFSET;
      
      rain = (float)currentCount * RAIN_GAIN;
      rainRate = rain - (float)lastCount * RAIN_GAIN;
      
      if (debugFlag)
        System.out.println("Rain Count: " + currentCount);
    }
    catch (OneWireException e)
    {
        throw new SimpleWeatherException("" + e);
//      System.out.println("Error Reading Rain Counter: " + e);
//      rain = -9999;
    }
    return rain;
  }

//  public long getPulseCount()
//  {
//    long pulse;
//    
//    try
//    {
//      if (debugFlag)
//      {
//        System.out.print("Pulses: Device = " + rainDevice.getName());
//        System.out.print("  ID = " + rainDevice.getAddressAsString() + "\n");
//      }
//      
//      // read rain count from counter 14
//      pulse = rainDevice.readCounter(14);
//      
//      if (debugFlag)
//        System.out.println("Pulse Count: " + pulse + "\n");
//    }
//    catch (OneWireException e)
//    {
//      System.out.println("Error Reading Pulse Counter: " + e);
//      pulse = -1;
//    }
//    return pulse;
//  }
  
  public String getRain()
  {
      return this.formatValue(rain, 3);
  }
  
  public String getRainRate()
  {
      return this.formatValue(rainRate, 3);
  }
  
  /**
   *
   */
  public void resetAverages()
  {
    lastCount = currentCount;
  }
}
