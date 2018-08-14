/******************************************************************************
 
 Project Name: SimpleWeather
 File name:    HumiditySensor.java
 Version:      1.1.2 06/17/06
 $Id: HumiditySensor.java,v 1.1.1.1 2010/02/19 15:34:24 boris Exp $
 
 Copyright (C) 2005 by T. Bitson - All rights reserved.
 
 This class provides the interface to the 1-wire humidity sensor
 device.
 
 *****************************************************************************/

package simpleweather;


import com.dalsemi.onewire.*;
import com.dalsemi.onewire.adapter.*;
import com.dalsemi.onewire.container.*;



public class HumiditySensor extends AbstractSensor
{
  // calibration constants
  private final float HUMIDITY_OFFSET = 0.0f;
  //private final float HUMIDITY_GAIN   = 0.927f;
  private final float HUMIDITY_GAIN   = 0.887f;
  private final float SOLAR_OFFSET = 0.0f;
  private final float SOLAR_GAIN   = 0.967f;
  
  // class variables
  //private DSPortAdapter adapter;
  private OneWireContainer26 humidityDevice = null;
  //private OneWireContainer26 solarDevice = null;
  //private static boolean debugFlag = SimpleWeather.debugFlag;

  // class constants
  private final int VDD_SENSE_AD = 0;
  private final int CURRENT_SENSE_AD = 2;

  public HumiditySensor(DSPortAdapter adapter, String deviceID)
  {
    // get an instance of the 1-wire device
    humidityDevice = new OneWireContainer26(adapter, deviceID);
  }
  
  public float getHumidity() throws SimpleWeatherException 
  {
    float humidity;
    
    if (humidityDevice == null) {
        throw new SimpleWeatherException("No humidity device");
    }
   
    if (debugFlag)
    {
      System.out.print("Humidity: Device = " + humidityDevice.getName());
      System.out.print("  ID = " + humidityDevice.getAddressAsString() + "\n");
    }
    try
    {
      // read 1-wire device's internal temperature sensor
      byte[] state = humidityDevice.readDevice();
      humidityDevice.doTemperatureConvert(state);
      double temp = humidityDevice.getTemperature(state);

      // Read humidity sensor's output voltage
      humidityDevice.doADConvert(OneWireContainer26.CHANNEL_VAD, state);
      double Vad = humidityDevice.getADVoltage(OneWireContainer26.CHANNEL_VAD, state);

      // Read the humidity sensor's power supply voltage
      humidityDevice.doADConvert(OneWireContainer26.CHANNEL_VDD, state);
      double Vdd = humidityDevice.getADVoltage(OneWireContainer26.CHANNEL_VDD, state);

      // calculate humidity
      double rh = (Vad/Vdd - 0.16) / 0.0062;
      humidity = (float)(rh / (1.0546 - 0.00216 * temp));

      // apply calibration
      humidity = humidity * HUMIDITY_GAIN + HUMIDITY_OFFSET;
      if (humidity > 100.0) {
//		OneWireException e = new OneWireException();
//		throw(e);
              humidity = 100.0f;
      }

      if (debugFlag)
      {
        System.out.println("Supply Voltage = " + Vdd + " Volts");
        System.out.println("Sensor Output  = " + Vad + " Volts");
        System.out.println("Temperature    = " + temp + " C / " + ((temp * 9/5) + 32) + " F");
        System.out.println("Uncomp RH      = " + rh + "%");
        System.out.println("Hum Gain       = " + HUMIDITY_GAIN);
        System.out.println("Hum Offset     = " + HUMIDITY_OFFSET);
        System.out.println("Calibrated RH   = " + humidity + "%\n");
      }
    }
    catch (OneWireException e)
    {
      throw new SimpleWeatherException("" + e);
    }

    this.update(humidity);
    return humidity;
  }
  
//  public float calcDewpoint(float temp, float hum)
//  {
//    // compute the dewpoint from relative humidity & temperature
//    
//    // if necessary, convert to degrees C
//    //temp = ((temp - 32.0f)/9.0f * 5.0f);
//    
//    // now convert to degrees K
//    double tempK = temp + 273.15;
//    
//    // calc dewpoint
//    double dp = tempK/((-0.0001846 * Math.log(hum/100.0) * tempK) + 1.0);
//    
//    // convert back to degrees C
//    dp = dp - 273.15;
//    
//    // and if necessary, convert back to degrees F
//    //dp = (dp * 9/5) + 32;
//    
//    return (float)dp;
//  }

//  public float getSolarLevel() throws OneWireException
//  {
//    double level = -999.9;
//
//    if (humidityDevice != null)
//    {
//      if (debugFlag)
//      {
//        System.out.print("Solar: Device = " + humidityDevice.getName());
//        System.out.print("  ID = " + humidityDevice.getAddressAsString() + "\n");
//      }
////      try
////      {
//        // get the current device state
//        byte[] state = humidityDevice.readDevice();
//
//        // Read moisture sensor's output voltage
//        humidityDevice.doADConvert(OneWireContainer26.CHANNEL_VSENSE, state);
//        double Vad = humidityDevice.getADVoltage(OneWireContainer26.CHANNEL_VSENSE, state);
//
//        // Read the moisture sensor's power supply voltage
//        humidityDevice.doADConvert(OneWireContainer26.CHANNEL_VDD,state);
//        double Vdd = humidityDevice.getADVoltage(OneWireContainer26.CHANNEL_VDD, state);
//
//        // Convert to percentage of full scale (2.5v)
//        // take absolute value in case the value rolls over
//        level = Math.abs(Vad) / .25  * 100.0;
//
//        // apply the calibration scale factor and offset
//        level = (level + SOLAR_OFFSET) * SOLAR_GAIN;
//
//        // round to 1 decimal place
//        level = ((int)(level * 10)) / 10.0;
//
//        if (debugFlag)
//        {
//          System.out.println("Supply Voltage = " + Vdd + " Volts");
//          System.out.println("Sensor Output  = " + Vad + " Volts");
//          System.out.println("Gain           = " + SOLAR_GAIN);
//          System.out.println("Offset         = " + SOLAR_OFFSET);
//          System.out.println("Comp Solar     = " + level + "%\n");
//        }
////      }
////      catch (OneWireException e)
////      {
////        System.out.println("Error Reading Solar Sensor: " + e);
////      }
//    }
//
//    // return a float
//    return (float)level;
//  }
  
    public String getHum()
    {
        return getAverage(1);
    }
}
