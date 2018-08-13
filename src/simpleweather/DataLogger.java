/******************************************************************************
 
 Project Name: SimpleWeather
 File name:    DataLogger.java
 Version:      1.1.0   04/01/06
 $Id: DataLogger.java,v 1.1.1.1 2010/02/19 15:34:24 boris Exp $
 
 Copyright (C) 2005 by T. Bitson - All rights reserved.
 
 This class logs the weather data to a file on the user's hard drive
 
 *****************************************************************************/

package simpleweather;

import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;




public class DataLogger
{
  // class constants
  public static final char DELIM = ';';   // 
  
  // class variables
  private String pathFileName;
  
  
  public DataLogger(String fileName)
  {
    pathFileName = fileName;
    
    try
    {
      System.out.println("Creating Log File " + pathFileName);
      
      // print header to file
      FileOutputStream file = new FileOutputStream(pathFileName, true);
      file.write(("Weather Data log Started " + new Date() + "\r\n").getBytes());
      file.write(("Time" + DELIM).getBytes());
      file.write(("Temp1" + DELIM).getBytes());
      file.write(("Temp2" + DELIM).getBytes());
      file.write(("Temp3" + DELIM).getBytes());
      file.write(("Temp4" + DELIM).getBytes());
      file.write(("Temp5" + DELIM).getBytes());
      file.write(("Temp21" + DELIM).getBytes());
      file.write(("WSpd" + DELIM).getBytes());
      file.write(("WDir" + DELIM).getBytes());
      file.write(("Hum" + DELIM).getBytes());
      file.write(("Baro" + DELIM).getBytes());
      file.write(("Rain" + DELIM).getBytes());
      file.write(("Solar" + DELIM).getBytes());
//      file.write(("Ltng\r\n").getBytes());
      file.write(("\n").getBytes());
      
      file.close();
      
    }
    catch (java.io.IOException e)
    {
      System.out.println("Error Creating Log File" + e);
    }
  }
  
  
  
  public void logData(Date date, SimpleWeather sw)
  {
    FileOutputStream file;
    
    try
    {
      
      //System.out.println("Updating Log " + pathFileName + "\n");
      
      file = new FileOutputStream(pathFileName, true);
      
      // convert date to a String
      SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
      String dateStr = formatter.format(date);
      
      // write time stamp & weather data to log file
      file.write((dateStr + DELIM).getBytes());                         // time
      file.write((format(sw.temp1, 1) + DELIM).getBytes());             // temp1
      file.write((format(sw.temp2, 1) + DELIM).getBytes());             // temp2  
      
      file.write((format(sw.temp21, 1) + DELIM).getBytes());
      file.write((format(sw.windSpeed, 1) + DELIM).getBytes());         // wind speed
      file.write((Integer.toString(sw.windDir) + DELIM).getBytes());   // wind direction
      file.write((format(sw.humidity, 1) + DELIM).getBytes());         // humidity
      file.write((format(sw.pressure, 2) + DELIM).getBytes());         // pressure
      file.write((format(sw.rain, 2) + DELIM).getBytes());             // rain
      file.write((format(sw.solarLevel, 2) + DELIM).getBytes());             // rain
      file.write(("\n").getBytes());// lightning
      //file.write((Integer.toString(sw.lightning) + "\r\n").getBytes());// lightning
      
      file.close();
      file = null;
    }
    catch(Exception e)
    {
      System.out.println("LogData(): Error Writing Log " + e);
    }
  }
  
  
 
  
  // short routine format a double to 'digit' decimals and convert to string TBD
  public String format(float input, int digits)
  {
    
    String arg1;
    String arg2;
    StringTokenizer t;
    double roundVal;
    
    // check input
    if (digits < 1)
      digits = 1;
    
    if (digits > 3)
      digits = 3;
    
    // determine the adder
    if (digits == 1)
      roundVal = .05;
    else if (digits == 2)
      roundVal = .005;
    else // digits = 3
      roundVal = .0005;
    
    // add the rounder and convert to 2 strings
    t = new StringTokenizer(Double.toString(input + roundVal), ".");
    arg1 = t.nextToken();
    arg2 = t.nextToken();
    
    // see if it's close to zero to avoid formatting issues
    if (Math.abs(input + roundVal) < .001 && digits == 3)
      return ("0.000");
    else
      return arg1 + "." + arg2.substring(0, digits);
    
  }
}
