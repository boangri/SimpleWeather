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
  public static final String VERSION = "SimpleWeather 2.1.2 16.08.2018";
  public static String ONE_WIRE_SERIAL_PORT; 
  
  // 1-Wire Devices
  
  private static String TEMP1_ID; // DS18B20 
  private static String TEMP2_ID; // DS18B20 
  private static String TEMP_SENSOR1_ID; // WS-1 temp. sensor 
  private static String TEMP_SENSOR2_ID; // outdoor temp. (pagoda)
  private static String HUMIDITY_SENSOR_ID;    
  private static String WIND_SPD_ID;  
  private static String WIND_DIR_ID; 
  private static String BARO_SENSOR_ID;
  private static String RAIN_COUNTER_ID;
  private static String RAIN_OFFSET;
  private static String MEASUREMENT_INTERVAL; //  Interval between measurements in seconds. Must divide 60.
  public static String WWW = "www.xland.ru";
  public static String URL = "/cgi-bin/meteo_upd";
  public static String StationID = "main";
  public static String WIND_RADIUS;

  public static String ADAPTER_TYPE ;
  public Vector sensor_vector = new Vector(10, 1);
  
  
  // class variables
  public static boolean debugFlag = false;
  public float temp1, temp2;
  public float temp21, temp22;
  public float humidity;
  public float solarLevel;
  public float dewpoint;
  public float pressure;
  public float rain;
  public long pulse;
  public long timestamp;
  public int measurement;
  public float rain_offset;
  public float windSpeed;
  public int windDir;
  public static String NORTH_OFFSET;
  public Properties ps;
  public Enumeration sensors;
  public int secs = 0;
  public float wind_radius;
  public int humidityErrorCnt = 0;
  public int pressureErrorCnt = 0;
  public int solarErrorCnt = 0;
  
  private DSPortAdapter adapter;
  private Wunderground wu;
  
  public SimpleWeather(Properties ps)
  {
      this.ps = ps;
    
  }
  
  public static void main(String[] args) throws Exception
  {
    StationProperties sp = new StationProperties("station.properties");

    Properties ps = sp.getStationProperties();
    

    
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
      
      SimpleWeather sw   = new SimpleWeather(ps);
      Wunderground wu = new Wunderground(sw);
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
      System.out.println(str+"="+ps.getProperty(str));
    }
    ISensor s;
    
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
    
    TEMP_SENSOR1_ID = ps.getProperty("TEMP_SENSOR1_ID"); // = "A00008001B35DE10"; //WS-1  
    if (TEMP_SENSOR1_ID != null) {
        s = new TempSensor(adapter, TEMP_SENSOR1_ID, "t1");
        sensor_vector.addElement(s);
    }
    TEMP_SENSOR2_ID = ps.getProperty("TEMP_SENSOR2_ID"); // = "0800080189EB8F10";
    if (TEMP_SENSOR2_ID != null) {
        s = new TempSensor(adapter, TEMP_SENSOR2_ID, "t2");
        sensor_vector.addElement(s);
    }
    TEMP1_ID = ps.getProperty("TEMP1_ID");
    if (TEMP1_ID != null) {
        s = new TempSensor(adapter, TEMP1_ID, "t21");
        sensor_vector.addElement(s);
    }
    TEMP2_ID = ps.getProperty("TEMP2_ID");
    if (TEMP2_ID != null) {
        s = new TempSensor(adapter, TEMP2_ID, "t22");
        sensor_vector.addElement(s);
    }
    HUMIDITY_SENSOR_ID = ps.getProperty("HUMIDITY_SENSOR_ID");
    if (HUMIDITY_SENSOR_ID != null) {
        s = new HumiditySensor(adapter, HUMIDITY_SENSOR_ID);
        sensor_vector.addElement(s);
    }
    WIND_SPD_ID = ps.getProperty("WIND_SPD_ID"); // = "1900000000F7C61D";
    if (WIND_SPD_ID != null) {
        s = new WindSpeedSensor(adapter, WIND_SPD_ID);
        sensor_vector.addElement(s);
    }
    WIND_DIR_ID = ps.getProperty("WIND_DIR_ID"); // = "D600000007293320";
    if (WIND_DIR_ID != null) {
        s = new WindDirSensor(adapter, WIND_DIR_ID);
        sensor_vector.addElement(s);
    }
    BARO_SENSOR_ID = ps.getProperty("BARO_SENSOR_ID");
    if (BARO_SENSOR_ID != null) {
        s = new BaroSensor(adapter, BARO_SENSOR_ID);
        sensor_vector.addElement(s);
    }
    RAIN_COUNTER_ID = ps.getProperty("RAIN_COUNTER_ID");
    if (RAIN_COUNTER_ID != null) {
        s = new RainSensor(adapter, RAIN_COUNTER_ID, rain_offset);
        sensor_vector.addElement(s);
    }
    RAIN_OFFSET = ps.getProperty("RAIN_OFFSET");
    
    NORTH_OFFSET = ps.getProperty("NORTH_OFFSET");
    MEASUREMENT_INTERVAL = ps.getProperty("MEASUREMENT_INTERVAL");
    
    WWW = ps.getProperty("WWW");
    URL = ps.getProperty("URL");
    
    StationID = ps.getProperty("StationID");
    WIND_RADIUS = ps.getProperty("WIND_RADIUS");
    
    wind_radius = Float.valueOf(WIND_RADIUS);
    rain_offset = Float.valueOf(RAIN_OFFSET);

    
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
