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
  public static final String VERSION = "SimpleWeather 2.1.0 14.08.2018";
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
  
  // sensors
  public TempSensor ts1,ts2;
  public Temp2Sensor ts21,ts22;
  public WindSpeedSensor wss1;
  public WindDirSensor wds1;
  public HumiditySensor hs1;
  public BaroSensor bs1;
  public RainSensor rs1;
  
  
  // Flags - existance of the sensors
  public static Boolean ts1ex = false;  
  public static Boolean ts2ex = false;  
  public static Boolean ts3ex = false;  
  public static Boolean ts4ex = false;  
  public static Boolean ts5ex = false;  
  public static Boolean ts21ex = false;  
  public static Boolean ts22ex = false;  
  public static Boolean ts23ex = false;  
  public static Boolean ts24ex = false;  
  public static Boolean ts25ex = false;  
  public static Boolean ws1ex = false;  
  public static Boolean hs1ex = false;  
  public static Boolean bs1ex = false;  
  public static Boolean rs1ex = false;  
  public static Boolean urlex = false;  
  public static Boolean relayex = false;  
  
  public SimpleWeather(Properties ps)
  {
      this.ps = ps;
    
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
    //boolean relayOn = false;
    
    
    
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
        
        // get the weather
        if (ts1ex) {
            try {
		temp1 = ts1.getTemperature();
        	System.out.println("Temperature1 = " + temp1 + " degs C");
            } catch (SimpleWeatherException e) {
                System.out.println("Error Reading Temperature1: " + e);
            }
	}
        if (ts2ex) {
            try {
		temp2 = ts2.getTemperature();
        	System.out.println("Temperature2 = " + temp2 + " degs C");
            } catch (SimpleWeatherException e) {
                System.out.println("Error Reading Temperature2: " + e);
            }
	}
        
        if (ts21ex) {
            try {
		temp21 = ts21.getTemperature();
        	System.out.println("Temperature21 = " + temp21 + " degs C");
            } catch (SimpleWeatherException e) {
                System.out.println("Error Reading Temperature21: " + e);
            }
	}
        if (ts22ex) {
            try {
		temp22 = ts22.getTemperature();
        	System.out.println("Temperature22 = " + temp22 + " degs C");
            } catch (SimpleWeatherException e) {
                System.out.println("Error Reading Temperature22: " + e);
            }
	}
        
	if (ws1ex) {
            try {
        	windSpeed = wss1.getWindSpeed();
            } catch (SimpleWeatherException e) {
                System.out.println("Error Reading Wind Speed: " + e);
            }    
            windDir = wds1.getWindDirection();
            System.out.println("Wind Speed = " + windSpeed + " M/sec " +
            "from the " + wds1.getWindDirStr(windDir));
	}
        // get humidity
	if (hs1ex) {
	    try {
        	humidity = hs1.getHumidity();
        	System.out.println("Humidity = " + humidity + " %");
	    }
            catch (SimpleWeatherException e)
 	    {
		humidityErrorCnt += 1;
		System.out.println("Error Reading Humidity: " + e + "Errors: " +humidityErrorCnt);
	    }
//        // get solar level
//	    try {
//        	solarLevel = hs1.getSolarLevel();
//        	System.out.println("Solar Level = " + solarLevel + " %");
//            }
//	    catch (OneWireException e)
//	    {
//		solarErrorCnt += 1;
//		System.out.println("Error Reading Solar Sensor: " + e + "Errors: " + solarErrorCnt);
//	    }


//        // calculate dewpoint
//		if (ts2ex) {
//        		dewpoint = hs1.calcDewpoint(temp2, humidity);
//        		System.out.println("Dewpoint = " + dewpoint + " deg C");
//		}
	}
        // get barometric pressure
	if (bs1ex) {
            try {
        	pressure = bs1.getPressure();
        	System.out.println("Pressure = " + pressure + " inHg");
	    }
            catch (SimpleWeatherException e)
 	    {
		pressureErrorCnt += 1;
		System.out.println("Error Reading Pressure: " + e + "Errors: " +pressureErrorCnt);
	    }
	}
        // get rain count
	if (rs1ex) {
            try {
        	rain = rs1.getRainCount();
        	System.out.println("Rain = " + rain + " in");
            } catch (SimpleWeatherException e) {
                System.out.println("Error Reading Rain Counter: " + e);
            }
//        	pulse = rs1.getPulseCount();
//        	System.out.println("Pulse = " + pulse + " ");
	}  

        //wc.update();
        if (ws1ex) {
            wss1.update();
            wds1.update();
        }
        //logger.logData(date, this);
        
        // update the time
        lastMinute = minute;
        
        if ((minute % 5 == 0) && (second == 0)) {
            wu.send();
          
            if (ts1ex) {ts1.resetAverages();}
            if (ts2ex) {ts2.resetAverages();}
           
            if (ts21ex) {ts21.resetAverages();}
            if (ts22ex) {ts22.resetAverages();}
            
            if (ws1ex) {
                wss1.resetAverages();
                wds1.resetAverages();
            }
            if (bs1ex) {bs1.resetAverages();}
            if (hs1ex) {hs1.resetAverages();}
            if (rs1ex) {rs1.resetAverages();}
            
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
  
  private void init()
  {
      
    Enumeration pse = ps.keys();
    while (pse.hasMoreElements()) {
      String str = (String) pse.nextElement();
      System.out.println(str+"="+ps.getProperty(str));
    }
    
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
	ts1ex = true;
        ts1 = new TempSensor(adapter, TEMP_SENSOR1_ID, 1);
        ts1.resetAverages();
    }
    TEMP_SENSOR2_ID = ps.getProperty("TEMP_SENSOR2_ID"); // = "0800080189EB8F10";
    if (TEMP_SENSOR2_ID != null) {
	ts2ex = true;
        ts2 = new TempSensor(adapter, TEMP_SENSOR2_ID, 2);
        ts1.resetAverages();
    }
    TEMP1_ID = ps.getProperty("TEMP1_ID");
    if (TEMP1_ID != null) {
	ts21ex = true;
        ts21 = new Temp2Sensor(adapter, TEMP1_ID, 1);
    }
    TEMP2_ID = ps.getProperty("TEMP2_ID");
    if (TEMP2_ID != null) {
	ts22ex = true;
        ts22 = new Temp2Sensor(adapter, TEMP2_ID, 2);
    }
    HUMIDITY_SENSOR_ID = ps.getProperty("HUMIDITY_SENSOR_ID");
    if (HUMIDITY_SENSOR_ID != null) {
	hs1ex = true;
        hs1 = new HumiditySensor(adapter, HUMIDITY_SENSOR_ID);
    }
    WIND_SPD_ID = ps.getProperty("WIND_SPD_ID"); // = "1900000000F7C61D";
    if (WIND_SPD_ID != null) {
	ws1ex = true;
        wss1 = new WindSpeedSensor(adapter, WIND_SPD_ID);
    }
    WIND_DIR_ID = ps.getProperty("WIND_DIR_ID"); // = "D600000007293320";
    if (WIND_DIR_ID != null) {
	ws1ex = true;
        wds1 = new WindDirSensor(adapter, WIND_DIR_ID);
    }
    BARO_SENSOR_ID = ps.getProperty("BARO_SENSOR_ID");
    if (BARO_SENSOR_ID != null) {
	bs1ex = true;
        bs1 = new BaroSensor(adapter, BARO_SENSOR_ID);
    }
    RAIN_COUNTER_ID = ps.getProperty("RAIN_COUNTER_ID");
    if (RAIN_COUNTER_ID != null) {
	rs1ex = true;
        rs1 = new RainSensor(adapter, RAIN_COUNTER_ID, rain_offset);
    }
    RAIN_OFFSET = ps.getProperty("RAIN_OFFSET");
    
    NORTH_OFFSET = ps.getProperty("NORTH_OFFSET");
    MEASUREMENT_INTERVAL = ps.getProperty("MEASUREMENT_INTERVAL");
    
    WWW = ps.getProperty("WWW");
    URL = ps.getProperty("URL");
    if (URL != null) {
	urlex = true;
    }
    StationID = ps.getProperty("StationID");
    WIND_RADIUS = ps.getProperty("WIND_RADIUS");
    
    wind_radius = Float.valueOf(WIND_RADIUS);
    rain_offset = Float.valueOf(RAIN_OFFSET);

    measurement = 0;
    
    //if (ts1ex) {ts1.resetAverages();}
    //if (ts2ex) {ts2.resetAverages();}
    if (ts21ex) {ts21.resetAverages();}
    if (ts22ex) {ts22.resetAverages();}
    if (ws1ex) {
        wss1.resetAverages();
        wds1.resetAverages();
    }
  }
}
