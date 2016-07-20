/******************************************************************************
 
 Project Name: SimpleWeather
 File name:    OWSwitch.java
 Version:      1.0.2 07/01/06
 $Id: OWSwitch.java,v 1.1.1.1 2010/02/19 15:34:24 boris Exp $
 
 Copyright (C) 2006 by T. Bitson - All rights reserved.
 
 This class provides the interface to the 1-wire DS2405 switch.
 
 *****************************************************************************/

package simpleweather;


import com.dalsemi.onewire.*;
import com.dalsemi.onewire.adapter.*;
import com.dalsemi.onewire.container.*;



public class OWSwitch implements Runnable
{
  // user constants
  public static final int NUM_SWITCHES = 1;
  
  
  // class variables
  private DSPortAdapter adapter;
  private OneWireContainer12 switchDevice = null;
  private String[] deviceID;
  private int[] runTime;
  private static boolean debugFlag = SimpleWeather.debugFlag;
  private boolean prevstate; // previous internal state of the switch 
  private boolean intstate; // internal state of the switch  
  public boolean stateKnown; // true when state of the switch is known  
  
  public OWSwitch(DSPortAdapter adapter)
  {
    this.adapter = adapter;
    deviceID = new String[NUM_SWITCHES];
    runTime = new int[NUM_SWITCHES];
    intstate = false;
    prevstate = false;
    stateKnown = false;
  }
  
  public void setSwitchState(String deviceID, boolean latchState)
  {
    // get instances of the 1-wire devices
    switchDevice = new OneWireContainer12(adapter, deviceID);
      
    try
    {
      if (debugFlag)
      {
        System.out.print("OWSwitch: Device = " + switchDevice .getName());
        System.out.print("  ID = " + switchDevice.getAddressAsString() + "\n");
        System.out.println("Setting to " + latchState);
      }
      prevstate = intstate; 
      byte[] state = switchDevice.readDevice();
      switchDevice.setLatchState(0, latchState, false, state);
      switchDevice.writeDevice(state);
      intstate = latchState;
      stateKnown = true; 
    }
    catch (OneWireException e)
    {
      intstate = prevstate;
      stateKnown = false; 
      System.out.println("Error Setting Switch: " + e);
    }
  }
  
  
  
  public boolean getSwitchState(String deviceID)
  {
    
    // get instances of the 1-wire devices
    switchDevice = new OneWireContainer12(adapter, deviceID);
    
    try
    {
      if (debugFlag)
      {
        System.out.print("OWSwitch: Device = " + switchDevice .getName());
        System.out.print("  ID = " + switchDevice.getAddressAsString() + "\n");
      }
      
      byte[] state = switchDevice.readDevice();
      
      boolean latchState = switchDevice.getLatchState(0, state);
      
      if (debugFlag)
        System.out.println("The Latch Reads: " + latchState);
      
      return latchState;
      
    }
    catch (OneWireException e)
    {
      System.out.println("Error Reading Switch: " + e);
      stateKnown = false; 
      return intstate;
    }
  }
  
  
  
  public void setDeviceTime(int deviceNum, String serialNum, int minutes)
  {
    if (minutes < 0)
      minutes = 0;
    
    deviceID[deviceNum] = serialNum;
    runTime[deviceNum] = minutes;
  }
  
  
  
  public void run()
  {
    int i;
    
    for (i=0; i< NUM_SWITCHES; i++)
    {
      setSwitchState(deviceID[i], true);
      
      try
      {
        //convert time to minutes and sleep
        Thread.sleep(runTime[i] * 1000 * 60);
      }
      catch (InterruptedException e)
      {}
      
      setSwitchState(deviceID[i], false);
    }
  }
}
