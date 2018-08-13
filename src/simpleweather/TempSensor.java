/******************************************************************************
 
 Project Name: SimpleWeather
 File name:    TempSensor.java
 
 Copyright (C) 2006 by T. Bitson - All rights reserved.
 
 This class provides the interface to the 1-wire temperature sensor
 device.
 
 *****************************************************************************/

package simpleweather;

import com.dalsemi.onewire.*;
import com.dalsemi.onewire.adapter.*;
import com.dalsemi.onewire.container.*;

public class TempSensor extends AbstractSensor
{
  // class variables
  //private DSPortAdapter adapter;
  private OneWireContainer10 tempDevice = null;
//  private static boolean debugFlag = SimpleWeather.debugFlag;
//  private float sumTemp;
//  private int samples;
  
  public TempSensor(DSPortAdapter adapter, String deviceID, int num)
  {
    // get instances of the 1-wire devices
    tempDevice = new OneWireContainer10(adapter, deviceID);
	
    // does this temp sensor have greater than .5 deg resolution?
    try
    {
      if (tempDevice.hasSelectableTemperatureResolution())
      {
        // set resolution to max
        byte[] state = tempDevice.readDevice();
        tempDevice.setTemperatureResolution(OneWireContainer10.RESOLUTION_MAXIMUM, state);
        tempDevice.writeDevice(state);
        
        if (debugFlag)
          System.out.println("Temp Device Supports High Resolution");
      }
    }
    catch (OneWireException e)
    {
      System.out.println("Error Setting Resolution: " + e);
    }
  }
  
  public float getTemperature() throws SimpleWeatherException
  {
    float temperature;
    
    // make sure the temp device instance is not null
    if (tempDevice == null) {
        throw new SimpleWeatherException("temp device is null");
    }
    
    if (debugFlag)
    {
      System.out.print("Temperature: Device = " + tempDevice.getName());
      System.out.print("  ID = " + tempDevice.getAddressAsString() + "\n");
    }

    try
    {
      byte[] state = tempDevice.readDevice();
      tempDevice.doTemperatureConvert(state);

      state = tempDevice.readDevice();
      temperature = (float)tempDevice.getTemperature(state);

      update(temperature);
    }
    catch (OneWireException e)
    {
      //System.out.println("Error Reading Temperature: " + e);
      throw new SimpleWeatherException("" + e);
    }
    return temperature;
  }
  
  public String getTemp()
  { 
    return getAverage();
  }
}
