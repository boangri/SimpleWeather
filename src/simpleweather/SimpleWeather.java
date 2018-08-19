/******************************************************************************
 
 Project Name: SimpleWeather
 File name:    SimpleWeather.java
 Version:      1.0   12/17/05
 $Id: SimpleWeather.java,v 1.4 2010/02/28 09:58:54 boris Exp $
 Copyright (C) 2005 by T. Bitson - All rights reserved.
 
 
 *****************************************************************************/

package simpleweather;

import simpleweather.sensor.*;
import java.util.*;
import java.io.*;

import com.dalsemi.onewire.*;
import com.dalsemi.onewire.adapter.*;

public class SimpleWeather
{
  public static final String VERSION = "SimpleWeather 2.1.3 16.08.2018";
  public static String ONE_WIRE_SERIAL_PORT; 
  
  // 1-Wire Devices
  
  private static String RAIN_OFFSET;
  private static String MEASUREMENT_INTERVAL; //  Interval between measurements in seconds. Must divide 60.
  public static String WWW = "www.xland.ru";
  public static String URL = "/cgi-bin/meteo_upd";
  public static String StationID = "main";
  public static String WIND_RADIUS;
  public static String NORTH_OFFSET;
  public static String ADAPTER_TYPE ;
  
  public static boolean debugFlag = false;
  public long timestamp;
  public int measurement;
  public static Properties ps;
  public Enumeration sensors;
  public Vector sensor_vector = new Vector(10, 1);
  public int secs = 0;
  public int humidityErrorCnt = 0;
  public int pressureErrorCnt = 0;
  public int solarErrorCnt = 0;
  
  private DSPortAdapter adapter;
  private Wunderground wu;
  
  public SimpleWeather()
  {
      wu = new Wunderground(this);
  }
  
  public static void main(String[] args) throws Exception
  {
    StationProperties sp = new StationProperties("station.properties");

    ps = sp.getStationProperties();
    
    System.out.println("Starting " + VERSION);
    
    if (args.length != 0)
    {
      if (args[0].equals("-d"))
      {
        System.out.println("debug on");
        debugFlag = true;
      }
    }
    
    try
    {
      // get instances to the primary object
      
      SimpleWeather sw   = new SimpleWeather();
      // call the main program loop
      sw.init();
      sw.mainLoop();
    }
    catch(Throwable t)
    {
      System.out.println("Exception: Main() " + t);
    }
    finally
    {
      System.out.println("SimpleWeather Stopped");
      System.exit(0);
    }
  }
  
  private void init()
  {
      
    Enumeration pse = ps.keys();
    while (pse.hasMoreElements()) {
      String str = (String) pse.nextElement();
      System.out.println(str+"="+SimpleWeather.ps.getProperty(str));
    }
    ISensor s;
    String ID;
    
    ADAPTER_TYPE = ps.getProperty("ADAPTER_TYPE");
    ONE_WIRE_SERIAL_PORT = ps.getProperty("ONE_WIRE_SERIAL_PORT");
    // get the 1-wire adapter
    try
    {
      // get an instance of the 1-Wire adapter
      adapter = OneWireAccessProvider.getAdapter(ADAPTER_TYPE, ONE_WIRE_SERIAL_PORT);
      if (adapter != null)
      {
        System.out.println("Found Adapter: " + adapter.getAdapterName() +
                " on Port " + adapter.getPortName());
      }
      else
      {
        System.out.println("Error: Unable to find 1-Wire adapter!");
        System.exit(1);
      }
      
      // reset the 1-Wire bus
      resetBus();
    }
    catch (OneWireException e)
    {
      System.out.println("Error Finding Adapter: "+ e);
      System.exit(1);
    }
    
    ID = ps.getProperty("TEMP_SENSOR1_ID"); // = "A00008001B35DE10"; //WS-1  
    if (ID != null) {
        s = new TempSensor(adapter, ID, "temp1");
        sensor_vector.addElement(s);
    }
    ID = ps.getProperty("TEMP_SENSOR2_ID"); // = "A00008001B35DE10"; //WS-1  
    if (ID != null) {
        s = new TempSensor(adapter, ID, "temp2");
        sensor_vector.addElement(s);
    }
    ID = ps.getProperty("TEMP_SENSOR3_ID"); // = "A00008001B35DE10"; //WS-1  
    if (ID != null) {
        s = new TempSensor(adapter, ID, "temp3");
        sensor_vector.addElement(s);
    }
    ID = ps.getProperty("TEMP_SENSOR4_ID"); // = "A00008001B35DE10"; //WS-1  
    if (ID != null) {
        s = new TempSensor(adapter, ID, "temp4");
        sensor_vector.addElement(s);
    }
    ID = ps.getProperty("TEMP1_ID");
    if (ID != null) {
        s = new TempSensor(adapter, ID, "temp21");
        sensor_vector.addElement(s);
    }
    ID = ps.getProperty("TEMP2_ID");
    if (ID != null) {
        s = new TempSensor(adapter, ID, "temp22");
        sensor_vector.addElement(s);
    }
    ID = ps.getProperty("HUMIDITY_SENSOR_ID");
    if (ID != null) {
        s = new HumiditySensor(adapter, ID);
        sensor_vector.addElement(s);
    }
    ID = ps.getProperty("WIND_SPD_ID"); // = "1900000000F7C61D";
    if (ID != null) {
        WIND_RADIUS = ps.getProperty("WIND_RADIUS");
        s = new WindSpeedSensor(adapter, ID);
        sensor_vector.addElement(s);
    }
    ID = ps.getProperty("WIND_DIR_ID"); // = "D600000007293320";
    if (ID != null) {
        NORTH_OFFSET = ps.getProperty("NORTH_OFFSET");
        s = new WindDirSensor(adapter, ID);
        sensor_vector.addElement(s);
    }
    ID = ps.getProperty("BARO_SENSOR_ID");
    if (ID != null) {
        s = new BaroSensor(adapter, ID);
        sensor_vector.addElement(s);
    }
    ID = ps.getProperty("RAIN_COUNTER_ID");
    if (ID != null) {
        RAIN_OFFSET = ps.getProperty("RAIN_OFFSET");
        s = new RainSensor(adapter, ID, Float.valueOf(RAIN_OFFSET));
        sensor_vector.addElement(s);
    }
   
    MEASUREMENT_INTERVAL = ps.getProperty("MEASUREMENT_INTERVAL");
    WWW = ps.getProperty("WWW");
    URL = ps.getProperty("URL");
    
    StationID = ps.getProperty("StationID");
    
    
//    wind_radius = Float.valueOf(WIND_RADIUS);
    
    measurement = 0;
  }
  
  public void mainLoop() throws NumberFormatException
  {
    Date date = new Date();
    int second;
    int minute, lastMinute = -99;
    int hour;
    int interval = Integer.parseInt(MEASUREMENT_INTERVAL);
    boolean quit = false;
    InputStreamReader in = new InputStreamReader(System.in);
    
    // main program loop
    while(!quit)
    {
      // sleep for 1 second
      try
      {
        Thread.sleep(1000);
      }
      catch (InterruptedException e)
      {}
      
      // check current time
      timestamp = System.currentTimeMillis() / 1000;
      date.setTime(System.currentTimeMillis());
      second = date.getSeconds();
      minute = date.getMinutes();
      hour = date.getHours();
      
      // only loop once a minute
      //if (minute != lastMinute)
      if ((second % interval) == 0)
      {
        System.out.println("");
        System.out.println("Time = " + date);
	measurement += 1;
        System.out.println("ts = " + timestamp + ", measurement number = "+ measurement + " In q: " + wu.getCount());
        
        sensors = sensor_vector.elements();
        
        while(sensors.hasMoreElements()) {
            ISensor s = (ISensor) sensors.nextElement();
            s.update();
        } 
        
        
	
        // update the time
        lastMinute = minute;
        
        if ((minute % 5 == 0) && (second == 0)) {
            wu.send();
          
            sensors = sensor_vector.elements();
        
            while(sensors.hasMoreElements()) {
                ISensor s = (ISensor) sensors.nextElement();
                s.resetAverage();
            } 
            
	    secs = 0;
        }
      }
      
      // development use only - check for 'q' key press
      try
      {
        if (in.ready())
          if (in.read() == 'q')
          {
              quit = true;

              try
              {
                adapter.freePort();
              }
              catch (OneWireException e)
              {
                System.out.println("Error Finding Adapter: "+ e);
              }
          }
      }
      catch (IOException e)
      { } // don't care
    }
  }
  
  private void resetBus() // reset the 1-wire bus
  {
    System.out.println("Resetting 1-wire bus");
    
    try
    {
      int result = adapter.reset();
      
      if (result == 0)
        System.out.println("Warning: Reset indicates no Device Present");
      if (result == 3)
        System.out.println("Warning: Reset indicates 1-Wire bus is shorted");
    }
    catch (OneWireException e)
    {
      System.out.println("Exception Resetting the bus: " + e);
    }
  }
}
