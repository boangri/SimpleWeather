/**
 * ****************************************************************************
 *
 * Project Name: SimpleWeather File name: RainSensor.java Version: 1.0.4
 * 02/07/06
 *
 * Copyright (C) 2006 by T. Bitson - All rights reserved.
 *
 * This class provides the interface to the 1-wire rain counter device.
 *
 ****************************************************************************
 */
package simpleweather.sensor;

import com.dalsemi.onewire.*;
import com.dalsemi.onewire.adapter.*;
import com.dalsemi.onewire.container.*;
import java.util.Properties;
import simpleweather.SimpleWeatherException;

public class RainSensor extends AbstractSensor {

    private OneWireContainer1D rainDevice = null;
    private long rain_offset = 0;
    private final String RAIN_OFFSET;
    private float rain_gain = 0.0114f; // inches/
    private final String RAIN_GAIN;
    private long lastCount = 0;
    private long currentCount = 0;
    private float rain;
    private float rainRate;

    public RainSensor(DSPortAdapter adapter, String deviceID, Properties ps) {
        // get instances of the 1-wire devices
        rainDevice = new OneWireContainer1D(adapter, deviceID);
        RAIN_OFFSET = ps.getProperty("RAIN_OFFSET");
        if (RAIN_OFFSET != null) {
            rain_offset = Long.valueOf(RAIN_OFFSET);
        }
        RAIN_GAIN = ps.getProperty("RAIN_GAIN");
        if (RAIN_GAIN != null) {
            rain_gain = Float.valueOf(RAIN_GAIN);
        }
        this.resetAverage();
    }

    public float getRainCount() throws SimpleWeatherException {
        try {
            if (debugFlag) {
                System.out.print("Rain: Device = " + rainDevice.getName());
                System.out.print("  ID = " + rainDevice.getAddressAsString() + "\n");
            }

            currentCount = rainDevice.readCounter(15) - rain_offset;

            rain = (float) currentCount * rain_gain;
            rainRate = rain - (float) lastCount * rain_gain;

            if (debugFlag) {
                System.out.println("Rain Count: " + currentCount);
            }
        } catch (OneWireException e) {
            throw new SimpleWeatherException("" + e);
        }
        return rain;
    }

//  public long getPulseCount()
//  {
//    long pulse;
//    
//    try
//    {
//      if (debugFlag)
//      {
//        System.out.print("Pulses: Device = " + rainDevice.getName());
//        System.out.print("  ID = " + rainDevice.getAddressAsString() + "\n");
//      }
//      
//      // read rain count from counter 14
//      pulse = rainDevice.readCounter(14);
//      
//      if (debugFlag)
//        System.out.println("Pulse Count: " + pulse + "\n");
//    }
//    catch (OneWireException e)
//    {
//      System.out.println("Error Reading Pulse Counter: " + e);
//      pulse = -1;
//    }
//    return pulse;
//  }
    public String getRain() {
        return this.formatValue(rain, 3);
    }

    public String getRainRate() {
        return this.formatValue(rainRate, 3);
    }

    /**
     *
     */
    public void resetAverage() {
        lastCount = currentCount;
    }

    public void update() {
        try {
            float data = this.getRainCount();
            System.out.println("Rain = " + data + "in");
            updateAverage(data);
        } catch (SimpleWeatherException e) {
            System.out.println("Error Reading Rain Counter: " + e);
        }
    }

    public Properties getResults() {
        Properties p = new Properties();
        p.setProperty("rainin", getRainRate());
        p.setProperty("raincnt", getRain());

        return p;
    }
}
