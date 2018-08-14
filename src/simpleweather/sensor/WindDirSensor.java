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



public class WindDirSensor extends AbstractSensor
{
  // calibration constants
  private static final int NORTH_OFFSET = Integer.parseInt(SimpleWeather.NORTH_OFFSET);
  
  // class variables
  //private DSPortAdapter adapter;
  private long lastCount = 0;
  private long lastTicks = 0;
  private OneWireContainer20   windDirDevice = null;
  public float windSpeed = 0f;
  public int windDir = 16;
  private double sumSin, sumCos;
 
  public WindDirSensor(DSPortAdapter adapter, String windDirDeviceID)
  {
    // get instances of the 1-wire devices
    windDirDevice   = new OneWireContainer20(adapter, windDirDeviceID);
  }
 
  /**
   * 
   * @return 
   */
  public int getWindDirection() 
  {
    
    try
    {
      if (debugFlag)
      {
        System.out.print("Wind Dir: Device = " + windDirDevice.getName());
        System.out.print("  ID = " + windDirDevice.getAddressAsString() + "\n");
      }
      
      // set up A to D for 8 bit readings, 5.12 V full-scale
      byte[] state = windDirDevice.readDevice();
      
      windDirDevice.setADResolution(OneWireContainer20.CHANNELA, 8, state);
      windDirDevice.setADResolution(OneWireContainer20.CHANNELB, 8, state);
      windDirDevice.setADResolution(OneWireContainer20.CHANNELC, 8, state);
      windDirDevice.setADResolution(OneWireContainer20.CHANNELD, 8, state);
      
      windDirDevice.setADRange(OneWireContainer20.CHANNELA, 5.12, state);
      windDirDevice.setADRange(OneWireContainer20.CHANNELB, 5.12, state);
      windDirDevice.setADRange(OneWireContainer20.CHANNELC, 5.12, state);
      windDirDevice.setADRange(OneWireContainer20.CHANNELD, 5.12, state);
      windDirDevice.writeDevice(state);
      
      // command each channel to read voltage
      windDirDevice.doADConvert(OneWireContainer20.CHANNELA, state);
      windDirDevice.doADConvert(OneWireContainer20.CHANNELB, state);
      windDirDevice.doADConvert(OneWireContainer20.CHANNELC, state);
      windDirDevice.doADConvert(OneWireContainer20.CHANNELD, state);
      
      // read results
      float chAVolts = (float)windDirDevice.getADVoltage(OneWireContainer20.CHANNELA, state);
      float chBVolts = (float)windDirDevice.getADVoltage(OneWireContainer20.CHANNELB, state);
      float chCVolts = (float)windDirDevice.getADVoltage(OneWireContainer20.CHANNELC, state);
      float chDVolts = (float)windDirDevice.getADVoltage(OneWireContainer20.CHANNELD, state);
      
      // convert the 4 A to D voltages to a wind direction
      windDir = lookupWindDir(chAVolts, chBVolts, chCVolts, chDVolts);
      
      if (windDir == 16)
        System.out.println("Wind Direction Error: ");
      
      if (debugFlag || windDir == 16)
      {
        System.out.println("Wind Dir AtoD Ch A = " + chAVolts);
        System.out.println("Wind Dir AtoD Ch B = " + chBVolts);
        System.out.println("Wind Dir AtoD Ch C = " + chCVolts);
        System.out.println("Wind Dir AtoD Ch D = " + chDVolts);
        System.out.println("Wind Direction     = " + windDir + "\n");
      }
    }
    catch (OneWireException e)
    {
      System.out.println("Error Reading Wind Direction: " + e);
      windDir = 16;
    }
    
	if (windDir < 16) 
		windDir = (windDir + NORTH_OFFSET) % 16;
	
    this.update();
    return windDir;
  }
  
  // convert wind direction A to D results to direction value
  private int lookupWindDir(float a, float b, float c, float d)
  {
    int i;
    int direction = 16;
    
    
    for (i=0; i<16; i++)
    {
      if(((a <= lookupTable[i][0] +1.0) && (a >= lookupTable[i][0] -1.0)) &&
         ((b <= lookupTable[i][1] +1.0) && (b >= lookupTable[i][1] -1.0)) &&
         ((c <= lookupTable[i][2] +1.0) && (c >= lookupTable[i][2] -1.0)) &&
         ((d <= lookupTable[i][3] +1.0) && (d >= lookupTable[i][3] -1.0)) )
      {
        direction = i;
        break;
      }
    }
    return direction;
  }
  
  
  static final float lookupTable[][] = {
    {4.5F, 4.5F, 2.5F, 4.5F}, // N   0
    {4.5F, 2.5F, 2.5F, 4.5F}, // NNE 1
    {4.5F, 2.5F, 4.5F, 4.5F}, // NE  2
    {2.5F, 2.5F, 4.5F, 4.5F}, // ENE 3
    {2.5F, 4.5F, 4.5F, 4.5F}, // E   4
    {2.5F, 4.5F, 4.5F, 0.0F}, // ESE 5
    {4.5F, 4.5F, 4.5F, 0.0F}, // SE  6
    {4.5F, 4.5F, 0.0F, 0.0F}, // SSE 7
    {4.5F, 4.5F, 0.0F, 4.5F}, // S   8
    {4.5F, 0.0F, 0.0F, 4.5F}, // SSW 9
    {4.5F, 0.0F, 4.5F, 4.5F}, // SW  10
    {0.0F, 0.0F, 4.5F, 4.5F}, // WSW 11
    {0.0F, 4.5F, 4.5F, 4.5F}, // W   12
    {0.0F, 4.5F, 4.5F, 2.5F}, // WNW 13
    {4.5F, 4.5F, 4.5F, 2.5F}, // NW  14
    {4.5F, 4.5F, 2.5F, 2.5F}, // NNW 15
  };
  
// convert direction value into compass direction string
  public String getWindDirStr(int input)
  {
    String[] direction = {" N ", "NNE", "NE ", "ENE",
                          " E ", "ESE", "SE ", "SSE",
                          " S ", "SSW", "SW ", "WSW",
                          " W ", "WNW", "NW ", "NNW",
                          " ERR"};
    
    if (debugFlag)
      System.out.println("GetWindDirectionString input = " +
              input + " and cal = " + NORTH_OFFSET);
    
    // valid inputs 0 thru 16
    if (input < 0 || input >= 16)
      input = 16;
    
    if (debugFlag)
      System.out.println("Wind Direction Decoded = " +
              input + " = " + direction[input]);
    
    return direction[input];
  }
    
  public void resetAverages()
  {
    samples = 0;
    sumSin = 0;
    sumCos = 0;
    
    if (debugFlag)
      System.out.println("Wind Direction Averages Reset");
  }
  
  public void update()
  { 
    if (windDir == 16) return;
    
    // increment the sample counter
    samples++;
    
    if (debugFlag)
      System.out.println("Sample #" + samples);
    
    // convert sumWind direction to radians
    double angle =  Math.toRadians(windDir * 22.5);
    
    sumSin += Math.sin(angle);
    sumCos += Math.cos(angle);
  }
  
  public String getWindDirAvg()
  {
    if (samples == 0) return "U";
    
    double r = Math.sqrt(sumSin*sumSin + sumCos*sumCos);
    double avgSin = sumSin/r;
    double avgCos = sumCos/r;
    
    // convert average to degrees
    float angle = (float)Math.toDegrees(Math.asin(avgSin));
    
    // correct for the quadrant
    if (avgCos < 0)
      angle = 180 - angle;
    
    else if (avgSin < 0)
      angle = 360 + angle;
    
    // convert back to 0 to 15 value
    //int dir = (int)((angle + 0.5)/22.5);
    
    if (debugFlag)
      System.out.println("Avg Wind Angle = " + angle);
    
    return this.formatValue(angle, 1);
  }
}
