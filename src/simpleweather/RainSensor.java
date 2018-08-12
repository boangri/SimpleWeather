/******************************************************************************
 
 Project Name: SimpleWeather
 File name:    RainSensor.java
 Version:      1.0.4 02/07/06
 $Id: RainSensor.java,v 1.1.1.1 2010/02/19 15:34:24 boris Exp $
 
 Copyright (C) 2006 by T. Bitson - All rights reserved.
 
 This class provides the interface to the 1-wire rain counter device.
 
 *****************************************************************************/

package simpleweather;


import com.dalsemi.onewire.*;
import com.dalsemi.onewire.adapter.*;
import com.dalsemi.onewire.container.*;



public class RainSensor
{
  // calibration constants
  // private final float RAIN_OFFSET = 781;
  
  
  // class variables
  private DSPortAdapter adapter;
  private OneWireContainer1D rainDevice = null;
  private static boolean debugFlag = SimpleWeather.debugFlag;
  private float RAIN_OFFSET;
  
  
  public RainSensor(DSPortAdapter adapter, String deviceID, float rain_offset)
  {
    // get instances of the 1-wire devices
    rainDevice = new OneWireContainer1D(adapter, deviceID);
    RAIN_OFFSET = rain_offset;
  }
  
  
  
  public float getRainCount()
  {
    long rain;
    
    try
    {
      if (debugFlag)
      {
        System.out.print("Rain: Device = " + rainDevice.getName());
        System.out.print("  ID = " + rainDevice.getAddressAsString() + "\n");
      }
      
      
      // read rain count from counter 15 and subtract offset
      rain = rainDevice.readCounter(15) - (long)RAIN_OFFSET;
      
      // convert to inches
      
      
      // convert to centimeters if required
      //rain *= 2.54f;
      
      //if (debugFlag)
        System.out.println("Rain Count: " + rain);
    }
    catch (OneWireException e)
    {
      System.out.println("Error Reading Rain Counter: " + e);
      rain = -9999;
    }
    return (float)rain/100f;
  }

  public long getPulseCount()
  {
    long pulse;
    
    try
    {
      if (debugFlag)
      {
        System.out.print("Pulses: Device = " + rainDevice.getName());
        System.out.print("  ID = " + rainDevice.getAddressAsString() + "\n");
      }
      
      // read rain count from counter 14
      pulse = rainDevice.readCounter(14);
      
      if (debugFlag)
        System.out.println("Pulse Count: " + pulse + "\n");
    }
    catch (OneWireException e)
    {
      System.out.println("Error Reading Pulse Counter: " + e);
      pulse = -1;
    }
    return pulse;
  }
}
