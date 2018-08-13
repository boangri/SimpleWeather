/******************************************************************************
 
 Project Name: SimpleWeather
 File name:    WeatherCruncher.java
 Version:      1.1.2 03/22/06
 $Id: WeatherCruncher.java,v 1.1.1.1 2010/02/19 15:34:24 boris Exp $
 
 Copyright (C) 2006 by T. Bitson - All rights reserved.
 
 
 *****************************************************************************/


package simpleweather;


import java.util.*;
//import java.lang.Math;
//import com.dalsemi.onewire.OneWireException;


public class WeatherCruncher
{
  // user constants
  private static final int TREND_SIZE = 20;
  
  // class variables
  private float sumTemp, tempHi, tempLo;
  private float sumHum, humHi, humLo;
  private float sumSolar, solarHi, solarLo;
  private float sumDP, dpHi, dpLo;
  private float sumWind, windHi, windPk;
  private float sumPress, baroHi, baroLo;
  private float rain, rain24, rainRate, lastRain, rainOffset;
  private float sumLight, lightHi;
  private int samplesTemp;
  private int samplesPress;
  private int samplesHum;
  private int samplesSolar;
  private int samplesDP;
  private int samplesWindDir;
  private int samplesWindSpd;
  private int samplesLight;
  
  private boolean firstTime = true;
  private static boolean debugFlag;
  private SimpleWeather sw;
  
  private final int[] x;
  private float[] tempTrend, temp2Trend, humTrend, windTrend;
  private float[] dpTrend, baroTrend, ltngTrend;
  private double sumSin, sumCos;
  private long lastTime;
  private static final int MS_PER_HOUR = 1000 * 60 * 60 ;
  
  
  
  
  public WeatherCruncher(SimpleWeather sw)
  {
    debugFlag = SimpleWeather.debugFlag;
    this.sw = sw;
    
    // arrays for trend data
    x = new int[TREND_SIZE];
    tempTrend = new float[TREND_SIZE];
    humTrend  = new float[TREND_SIZE];
    dpTrend   = new float[TREND_SIZE];
    windTrend = new float[TREND_SIZE];
    baroTrend = new float[TREND_SIZE];
    ltngTrend = new float[TREND_SIZE];
    
    // initialize trend values
    for (int i = 0; i < TREND_SIZE; i++)
    {
      tempTrend[i] = -1;
      humTrend[i]  = -1;
      dpTrend[i]   = -1;
      windTrend[i] = -1;
      baroTrend[i] = -1;
      ltngTrend[i] = -1;
      x[i]         = i;
    }
  }
  
  
  
  public void resetHighsAndLows()
  {
    // reset temperature
    tempHi = -999;
    tempLo = 999;
    
    // reset humiidty
    humHi = -999;
    humLo = 999;
    
    // reset solar
    solarHi = -999;
    solarLo = 999;
    
    // reset dewpoint
    dpHi = -999;
    dpLo = 999;
    
    // rest sumWind speed
    windHi = 0;
    
    // reset barometric pressure
    baroHi = -999;
    baroLo = 999;
    
    // reset rain
    rainOffset = sw.rain;
    rainRate = 0;
    
    // reset lightning
    lightHi = 0;
  }
  
  
  public void resetAverages()
  {
    samplesTemp = 0;
    samplesPress = 0;
    samplesHum = 0;
    samplesSolar = 0;
    samplesDP = 0;
    samplesWindSpd = 0;
    samplesWindDir = 0;
    samplesLight = 0;
    sumTemp = 0;
    sumHum = 0;
    sumSolar = 0;
    sumWind = 0;
    sumDP = 0;
    sumPress = 0;
    sumLight = 0;
    sumSin = 0;
    sumCos = 0;
    windPk = 0;
    
    if (debugFlag)
      System.out.println("Averages Reset");
  }
  
  
  public void update()
  {
    // increment the sample counter
    //samples++;
    
    //if (debugFlag)
      //System.out.println("Sample #" + samples);
    
    // update temperature
    if (sw.temp1 >= -100 ) {
	samplesTemp++;
    	sumTemp += sw.temp1;
    	updateTrendData(sw.temp1, tempTrend);
    }
    
    // update humidity
    if (sw.humidity >= 0.0 ) {
    	samplesHum++;
    	sumHum += sw.humidity;
    	updateTrendData(sw.humidity, humTrend);
    }
    
    // update solar level
    if (sw.solarLevel >= 0.0 ) {
    	samplesSolar++;
    	sumSolar += sw.solarLevel;
    }
    
    // update dewpoint
    if (sw.dewpoint >= -100 ) {
    	samplesDP++;
    	sumDP += sw.dewpoint;
    	updateTrendData(sw.dewpoint, dpTrend);
    }
    
    // update sumWind speed
    if (sw.windSpeed >= 0.0 ) {
    	samplesWindSpd++;
    	sumWind += sw.windSpeed * sw.windSpeed;

    	if (sw.windSpeed > windPk)
            windPk = sw.windSpeed;
    
    	updateTrendData(sw.windSpeed, windTrend);
    }
       
    
    // update sumWind direction
    if ((sw.windDir < 16) && (sw.windDir >=0)) {
	samplesWindDir++;
    	updateWindAvg(sw.windDir);
    }
    
    // update pressure
    if (sw.pressure > 0.0) {
    	samplesPress++;
    	sumPress += sw.pressure;
    	updateTrendData(sw.pressure, baroTrend);
    }
    
    // update rain
    rain = sw.rain;
    rain24 = sw.rain - rainOffset;
    
    
    // update lightning
    //sumLight += sw.lightning;
    
    //if (sw.lightning > lightHi)
      //lightHi = sw.lightning;
    
    //updateTrendData(sw.lightning, ltngTrend);
  }
  
  
  
  private void updateTrendData(float value, float[] yArray)
  {
    // shift trend data down one
    for (int i = 0; i < TREND_SIZE-1; i++)
      yArray[i] = yArray[i+1];
    
    // add new data to the end of the list
    yArray[TREND_SIZE-1] = value;
  }
  
  
  
  private String leastSquaresSlope(int numElements, int[] xArray, float[] yArray)
  {
    double sumX = 0;
    double sumXY = 0;
    double sumY = 0;
    double sumXX = 0;
    
    // check if the array is full
    if (yArray[0] == -1)
      return " - ";
    
    // calculate the Least Squares Fit
    for (int i=0; i<numElements; i++)
    {
      sumX += xArray[i];
      sumY += yArray[i];
      sumXY += xArray[i] * yArray[i];
      sumXX += xArray[i] * xArray[i];
    }
    
    double a = (sumX * sumY) - (sumXY * numElements);
    double d = (sumX * sumX) - (sumXX * numElements);
    
    if (debugFlag)
    {
      System.out.println("Least Squares Input:");
      for (int i=0; i<numElements; i++)
        System.out.println("x[" + i + "] = " + xArray[i] + "   y[" + i + "] = " + yArray[i]);
      System.out.println("Least Squares Slope = " + (a/d));
      System.out.println("LSS Adjusted for 1 Hour Rate: " + (a/d * 60));
    }
    
    // convert results to string
    return formatValue((float)(a/d * 60), 2);
  }
  
  
  
  private void updateWindAvg(int windDir)
  {
    
    // convert sumWind direction to radians
    double angle =  Math.toRadians(windDir * 22.5);
    
    sumSin += Math.sin(angle);
    sumCos += Math.cos(angle);
  }
  
  
  
//  public String getWindDirAvg()
//  {
//    // divide by the number of samples to get average
//    double avgSin = sumSin/samplesWindDir;
//    double avgCos = sumCos/samplesWindDir;
//    
//    // convert average to degrees
//    float angle = (float)Math.toDegrees(Math.asin(avgSin));
//    
//    // correct for the quadrant
//    if (avgCos < 0)
//      angle = 180 - angle;
//    
//    else if (avgSin < 0)
//      angle = 360 + angle;
//    
//    // convert back to 0 to 15 value
//    //int dir = (int)((angle + 0.5)/22.5);
//    
//    if (debugFlag)
//      System.out.println("Avg Wind Angle = " + angle);
//    
//    return formatValue(angle, 0);
//  }
  
  
  
  
  // short routine format a float to 'digit' decimals and convert to string
  public static String formatValue(float input, int digits)
  {
    
    try
    {
      
      String arg1;
      String arg2;
      StringTokenizer t;
      
      if (digits < 1)
        digits = 1;
      
      if (digits > 3)
        digits = 3;
      
      
      float roundVal;
      
      if (digits == 1)
        roundVal = .05f;
      else if (digits == 2)
        roundVal = .005f;
      else // digits = 3
        roundVal = .0005f;
      
      t = new StringTokenizer(Double.toString(input + roundVal), ".");
      
      arg1 = t.nextToken();
      arg2 = t.nextToken();
      
      if (Math.abs(input + roundVal) < .001 && digits == 3)
        return ("0.000");
      else
        return arg1 + "." + arg2.substring(0, digits);
    }
    catch (Exception e)
    {
      return "error";
    }
  }
  
    
//  // temperature --------------------------------
//  public String getTemp()
//  {
//    float avgTemp = sumTemp/samplesTemp;
//    
//    if (avgTemp > tempHi)
//      tempHi = avgTemp;
//    
//    if (avgTemp < tempLo)
//      tempLo = avgTemp;
//    
//    return formatValue(avgTemp, 1);
//  }
//  
//  // temperature -------------------------------
//  
//  public String getTempHi()
//  {
//    return formatValue(tempHi, 1);
//  }
//  
//  
//    
//  public String getTempLo()
//  {
//    return formatValue(tempLo, 1);
//  }  
//  
//  public String getTempTrend()
//  {
//    return leastSquaresSlope(TREND_SIZE, x, tempTrend);
//  }
//
//  // Wind Speed ---------------------------------
//  public String getWind()
//  {
//    if (samplesWindSpd == 0) return ("error");
//
//    float avgWind = (float)Math.sqrt(sumWind/samplesWindSpd);
//
//    if (avgWind > windHi)
//      windHi = avgWind;
//
//    return WeatherCruncher.formatValue(avgWind, 1);
//  }
//  
//  public String getWindPk()
//  {
//    return WeatherCruncher.formatValue(windPk, 1);
//  }
//
//  // Wind Direction -----------------------------

  
  // Humidity -----------------------------------
  public String getHum()
  {
    float avgHum;
    if (samplesHum > 0) {
	avgHum = sumHum/samplesHum;
    } else {
	return "error";
    }
    if (sumHum > humHi)
      humHi = avgHum;
    
    if (sumHum < humLo)
      humLo = avgHum;
    
    return formatValue(avgHum, 1);
  }
  
  
//  public String getHumHi()
//  {
//    return formatValue(humHi, 1);
//  }
//  
//  
//  public String getHumLo()
//  {
//    return formatValue(humLo, 1);
//  }
//  
//  
//  public String getHumTrend()
//  {
//    return leastSquaresSlope(TREND_SIZE, x, humTrend);
//  }
//  
//  
//  
  // Dewpoint -----------------------------------
  public String getDP()
  {
    float avgDP;
    if (samplesDP > 0) {
	avgDP = sumDP/samplesDP;
    } else {
	return "error";
    }
    
    if (avgDP > dpHi)
      dpHi = avgDP;
    
    if (avgDP < dpLo)
      dpLo = avgDP;
    
    return formatValue(avgDP, 1);
  }
  
  
//  public String getDPHi()
//  {
//    return formatValue(dpHi, 1);
//  }
//  
//  
//  public String getDPLo()
//  {
//    return formatValue(dpLo, 1);
//  }
//  
//  
//  public String getDPTrend()
//  {
//    return leastSquaresSlope(TREND_SIZE, x, dpTrend);
//  }
//  
  // Solar level  -----------------------------------
  public String getSolar()
  {
    float avgSolar;
    if (samplesSolar > 0) {
	avgSolar = sumSolar/samplesSolar;
    } else {
	return "error";
    }
    if (sumSolar > solarHi)
      solarHi = avgSolar;
    
    if (sumSolar < solarLo)
      solarLo = avgSolar;
    
    return formatValue(avgSolar, 2);
  }
  
//  // Wind
//  public String getWindTrend()
//  {
//    return leastSquaresSlope(TREND_SIZE, x, windTrend);
//  }
//  
//  
  // Baro Pressure ------------------------------
  public String getBaro()
  {
    float avgBaro; 
    if (samplesPress > 0) { 
        avgBaro = sumPress/samplesPress;
    } else {
        return "error";
    }
    
    if (avgBaro > baroHi)
      baroHi = avgBaro;
    
    if (avgBaro < baroLo)
      baroLo = avgBaro;
    
    return formatValue(avgBaro, 2);
  }
  
  
//  public String getBaroHi()
//  {
//    return formatValue(baroHi, 2);
//  }
//  
//  
//  public String getBaroLo()
//  {
//    return formatValue(baroLo, 2);
//  }
//  
//  
//  public String getBaroTrend()
//  {
//    return leastSquaresSlope(TREND_SIZE, x, baroTrend);
//  }
  
  
  
  // Rain ---------------------------------------
  public String getRain()
  {
    return formatValue(rain, 2);
  }
  
  
  public String getRain24()
  {
    return formatValue(rain24, 2);
  }
  
  
  public String getRainRate()
  {
    long time = System.currentTimeMillis();
    rainRate = (rain - lastRain) / (time - lastTime) * MS_PER_HOUR;
    
    lastRain = rain;
    lastTime = time;
    return formatValue(rainRate, 2);
  }
  
  
  
  //Lightning -----------------------------------
  public String getLightning()
  {
    return formatValue(sumLight/samplesLight, 1);
  }
  
  
  public String getLightningHi()
  {
    return formatValue(lightHi, 1);
  }
  
  
  public String getLightningTrend()
  {
    return leastSquaresSlope(TREND_SIZE, x, ltngTrend);
  }
}
