/**
 * ****************************************************************************
 *
 * Project Name: SimpleWeather File name: SimpleWeather.java Version: 1.0
 * 12/17/05 $Id: SimpleWeather.java,v 1.4 2010/02/28 09:58:54 boris Exp $
 * Copyright (C) 2005 by T. Bitson - All rights reserved.
 *
 *
 ****************************************************************************
 */
package simpleweather;

import simpleweather.sensor.*;
import java.util.*;
import java.io.*;

import com.dalsemi.onewire.*;
import com.dalsemi.onewire.adapter.*;

public class SimpleWeather {

    public static final String VERSION = "SimpleWeather 2.1.12 28.08.2018";
    private static String MEASUREMENT_INTERVAL; //  Interval between measurements in seconds. Must divide 60.
    public static String WWW = "www.xland.ru";
    public static String URL = "/cgi-bin/meteo_upd";
    public static String StationID = "main";
    public static boolean debugFlag = false;
    public static long timestamp;
    public static int measurement;
    public static Properties ps;
    public Enumeration<ISensor> sensors;
    public static Vector<ISensor> sensor_vector = new Vector<ISensor>(10, 1);
    public int humidityErrorCnt = 0;
    public int pressureErrorCnt = 0;
    public int solarErrorCnt = 0;
    private DSPortAdapter adapter;
    private final Wunderground wu;

    public SimpleWeather() {
        wu = new Wunderground();
    }

    public static void main(String[] args) throws Exception {
        StationProperties sp = new StationProperties("station.properties");

        ps = sp.getStationProperties();

        System.out.println("Starting " + VERSION);

        if (args.length != 0) {
            if (args[0].equals("-d")) {
                System.out.println("debug on");
                debugFlag = true;
            }
        }

        try {
      // get instances to the primary object

            SimpleWeather sw = new SimpleWeather();
            // call the main program loop
            sw.init();
            sw.mainLoop();
        } catch (Throwable t) {
            System.out.println("Exception: Main() " + t);
        } finally {
            System.out.println("SimpleWeather Stopped");
            System.exit(0);
        }
    }

    private void init() {
        String ADAPTER_TYPE = ps.getProperty("ADAPTER_TYPE");
        String ONE_WIRE_SERIAL_PORT = ps.getProperty("ONE_WIRE_SERIAL_PORT");

        Enumeration<Object> pse = ps.keys();
        while (pse.hasMoreElements()) {
            String str = (String) pse.nextElement();
            System.out.println(str + "=" + SimpleWeather.ps.getProperty(str));
        }
        ISensor s;
        String ID;

        // get the 1-wire adapter
        try {
            // get an instance of the 1-Wire adapter
            adapter = OneWireAccessProvider.getAdapter(ADAPTER_TYPE, ONE_WIRE_SERIAL_PORT);
            if (adapter != null) {
                System.out.println("Found Adapter: " + adapter.getAdapterName()
                        + " on Port " + adapter.getPortName());
            } else {
                System.out.println("Error: Unable to find 1-Wire adapter!");
                System.exit(1);
            }
            // reset the 1-Wire bus
            resetBus();
        } catch (OneWireException e) {
            System.out.println("Error Finding Adapter: " + e);
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
        ID = ps.getProperty("TEMP3_ID");
        if (ID != null) {
            s = new TempSensor(adapter, ID, "temp23");
            sensor_vector.addElement(s);
        }
        ID = ps.getProperty("TEMP4_ID");
        if (ID != null) {
            s = new TempSensor(adapter, ID, "temp24");
            sensor_vector.addElement(s);
        }
        ID = ps.getProperty("HUMIDITY_SENSOR_ID");
        if (ID != null) {
            s = new HumiditySensor(adapter, ID, ps);
            sensor_vector.addElement(s);
        }
        ID = ps.getProperty("WIND_SPD_ID"); // = "1900000000F7C61D";
        if (ID != null) {
            s = new WindSpeedSensor(adapter, ID, ps);
            sensor_vector.addElement(s);
        }
        ID = ps.getProperty("WIND_DIR_ID"); // = "D600000007293320";
        if (ID != null) {
            s = new WindDirSensor(adapter, ID, ps);
            sensor_vector.addElement(s);
        }
        ID = ps.getProperty("BARO_SENSOR_ID");
        if (ID != null) {
            s = new BaroSensor(adapter, ID, ps);
            sensor_vector.addElement(s);
        }
        ID = ps.getProperty("RAIN_COUNTER_ID");
        if (ID != null) {
            s = new RainSensor(adapter, ID, ps);
            sensor_vector.addElement(s);
        }

        MEASUREMENT_INTERVAL = ps.getProperty("MEASUREMENT_INTERVAL");
        WWW = ps.getProperty("WWW");
        URL = ps.getProperty("URL");
        StationID = ps.getProperty("StationID");
        measurement = 0;
    }

    public void mainLoop() throws NumberFormatException {
        Date date;
        int interval = Integer.parseInt(MEASUREMENT_INTERVAL);
        boolean quit = false;
        InputStreamReader in = new InputStreamReader(System.in);

        // main program loop
        while (!quit) {
            // sleep for 1 second
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }

            // check current time
            timestamp = System.currentTimeMillis() / 1000;
            date = new Date();
            
            if ((timestamp % interval) == 0) {
                System.out.println("");
                System.out.println("Time = " + date);
                measurement += 1;
                System.out.println("ts = " + timestamp + ", measurement number = " + measurement + " In q: " + wu.getCount());

                sensors = sensor_vector.elements();

                while (sensors.hasMoreElements()) {
                    ISensor s = sensors.nextElement();
                    if (s.isReady()) {
                        s.update();
                    }
                }

                if ((timestamp % 300) == 0)  {
                    wu.send();

                    sensors = sensor_vector.elements();

                    while (sensors.hasMoreElements()) {
                        ISensor s = sensors.nextElement();
                        s.resetAverage();
                        if (!s.isReady()) {
                            s.checkSensor();
                        }
                    }
                }
            }

            // development use only - check for 'q' key press
            try {
                if (in.ready()) {
                    if (in.read() == 'q') {
                        quit = true;

                        try {
                            adapter.freePort();
                        } catch (OneWireException e) {
                            System.out.println("Error Finding Adapter: " + e);
                        }
                    }
                }
            } catch (IOException e) {
            } // don't care
        }
    }

    private void resetBus() // reset the 1-wire bus
    {
        System.out.println("Resetting 1-wire bus");
        try {
            int result = adapter.reset();
            
            if (result == 0) {
                System.out.println("Warning: Reset indicates no Device Present");
            }
            if (result == 3) {
                System.out.println("Warning: Reset indicates 1-Wire bus is shorted");
            }
        } catch (OneWireException e) {
            System.out.println("Exception Resetting the bus: " + e);
        }
    }
}
