/******************************************************************************
 
 Project Name: SimpleWeather
 File name:    TempSensor2.java
 Version:      1.0.2 02/07/06
 $Id: TempSensor2.java,v 1.1.1.1 2010/02/19 15:34:24 boris Exp $
 
 Copyright (C) 2006 by T. Bitson - All rights reserved.
 
 This class provides the interface to the 1-wire DS18B20 temperature sensor
 device.
 
 *****************************************************************************/

package simpleweather;


import com.dalsemi.onewire.*;
import com.dalsemi.onewire.adapter.*;
import com.dalsemi.onewire.container.*;



public class TempSensor2
{
  // user constants
  private static final int TREND_SIZE = 20;
  // class variables
  private DSPortAdapter adapter;
  private OneWireContainer28 tempDevice = null;
  private static boolean debugFlag = SimpleWeather.debugFlag;
  private float sumTemp, tempHi, tempLo;
  private float[] tempTrend;
  private final int[] x;
  private int n;
  private int samples;
  
  public TempSensor2(DSPortAdapter adapter, String deviceID, int num)
  {
    n = num;
    // get instances of the 1-wire devices
    tempDevice = new OneWireContainer28(adapter, deviceID);
    // arrays for trend data
    x = new int[TREND_SIZE];
    tempTrend = new float[TREND_SIZE];
    // initialize trend values
    for (int i = 0; i < TREND_SIZE; i++)
    {
      tempTrend[i] = -1;
      x[i]         = i;
    }	
	
    // does this temp sensor have greater than .5 deg resolution?
    try
    {
      if (tempDevice.hasSelectableTemperatureResolution())
      {
        // set resolution to max
        byte[] state = tempDevice.readDevice();
        tempDevice.setTemperatureResolution(tempDevice.RESOLUTION_12_BIT, state);
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
  
  
  
  public float getTemperature()
  {
    float temperature = -999.9f;
    
    // make sure the temp device instance is not null
    if (tempDevice != null)
    {
      
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
        
        // convert to degs F - comment out to get degrees C
        //temperature = temperature * 9.0f/5.0f + 32f;
        
        samples++;
        sumTemp += temperature;
        //???updateTrendData(temperature, tempTrend);
        
      }
      catch (OneWireException e)
      {
        System.out.println("Error Reading Temperature: " + e);
      }
    }
    return temperature;
  }
  
  public void resetHighsAndLows()
  {
    // reset temperature
    tempHi = -999;
    tempLo = 999;
  }	
  
  public void resetAverages()
  {
    samples = 0;
    sumTemp = 0;
    
    if (debugFlag)
      System.out.println("Temperature Averages Reset");
  }
  
  public String getTemp()
  {
    if (samples == 0) return "U";
    
    float avgTemp = sumTemp/samples;
    
    if (avgTemp > tempHi)
      tempHi = avgTemp;
    
    if (avgTemp < tempLo)
      tempLo = avgTemp;
    
    return WeatherCruncher.formatValue(avgTemp, 1);
  }
  
  
}
