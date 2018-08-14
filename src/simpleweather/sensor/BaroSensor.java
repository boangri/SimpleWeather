/******************************************************************************
 
 Project Name: SimpleWeather
 File name:    BaroSensor.java
 Version:      1.0.2 02/21/06
 $Id: BaroSensor.java,v 1.1.1.1 2010/02/19 15:34:24 boris Exp $
 
 Copyright (C) 2005 by T. Bitson - All rights reserved.
 
 This class provides the interface to the 1-wire temperature sensor
 device.
 
 *****************************************************************************/

package simpleweather.sensor;


import com.dalsemi.onewire.*;
import com.dalsemi.onewire.adapter.*;
import com.dalsemi.onewire.container.*;
import simpleweather.SimpleWeatherException;

public class BaroSensor extends AbstractSensor
{
  // calibration constants
  private final double PRESSURE_GAIN   = 0.7171;
  //private final double PRESSURE_OFFSET = 26.2523;
  private final double PRESSURE_OFFSET = 26.2523 - 0.5962;
  
  // class variables
  //private DSPortAdapter adapter;
  private OneWireContainer26 baroDevice = null;
  //private static boolean debugFlag = SimpleWeather.debugFlag;
  
  
  public BaroSensor(DSPortAdapter adapter, String deviceID)
  {
    // get instances of the 1-wire devices
    baroDevice = new OneWireContainer26(adapter, deviceID);
  }
  
  
  
  public float getPressure() throws SimpleWeatherException
  {
    float pressure;
    
    if (debugFlag)
    {
      System.out.print("Pressure: Device = " + baroDevice.getName());
      System.out.print("  ID = " + baroDevice.getAddressAsString() + "\n");
    }
    
    try
    {
      byte[] state = baroDevice.readDevice();
      
      
      // Read pressure A to D output
      baroDevice.doADConvert(OneWireContainer26.CHANNEL_VAD, state);
      double Vad = baroDevice.getADVoltage(OneWireContainer26.CHANNEL_VAD, state);
      
      
      // Read Supply Voltage (for reference only)
      baroDevice.doADConvert(OneWireContainer26.CHANNEL_VDD, state);
      double Vdd = baroDevice.getADVoltage(OneWireContainer26.CHANNEL_VDD, state);
      
      
      // apply calibration
      pressure = (float)(Vad * PRESSURE_GAIN + PRESSURE_OFFSET);
      
      // scale to mb if required
      //pressure *= 33.8640;
      
      this.update(pressure);
      
      if (debugFlag)
      {
        System.out.println("Sensor Output  = " + Vad + " Volts");
        System.out.println("Supply Voltage = " + Vdd + " Volts");
        System.out.println("Scale Factor   = " + PRESSURE_GAIN);
        System.out.println("Offset         = " + PRESSURE_OFFSET);
        System.out.println("Baro Pressure  = " + pressure + "\n");
      }
      
    }
    catch (OneWireException e)
    {
        throw new SimpleWeatherException("" + e);
    }
    
    return pressure;
  }
  
  public String getBaro()
  { 
    return getAverage(1);
  }
}
