/******************************************************************************
 
 * Project Name: SimpleWeather File name: WindSensor.java Version: 1.0.3
 * 02/05/06 Copyright (C) 2006 by T. Bitson - All rights reserved.
 *
 * This class provides the interface to the 1-wire temperature sensor device.
 *
 ****************************************************************************
 */
package simpleweather.sensor;

import com.dalsemi.onewire.*;
import com.dalsemi.onewire.adapter.*;
import com.dalsemi.onewire.container.*;
import java.util.Properties;

public class WindSpeedSensor extends AbstractSensor {

    // calibration constants
    private float radius = 0.5f;//  effective radius of the wheel
    private final String WIND_RADIUS;
    // class variables
    private long lastCount = 0;
    private long lastTicks = 0;
    private OneWireContainer1D windSpdDevice = null;

    public WindSpeedSensor(DSPortAdapter adapter, String windSpdDeviceID, Properties ps) {
        // get instances of the 1-wire devices
        windSpdDevice = new OneWireContainer1D(adapter, windSpdDeviceID);
        WIND_RADIUS = ps.getProperty("WIND_RADIUS");
        if (WIND_RADIUS != null) {
            radius = Float.valueOf(WIND_RADIUS);
        }
        if (windSpdDevice != null) {
            lastTicks = System.currentTimeMillis();
            try {
                lastCount = windSpdDevice.readCounter(15);
            } catch (OneWireException e) {
                System.out.print("Can't create Conatiner20\n");
            }
            this.resetAverage();
        }
    }

    public float getWindSpeed() throws OneWireException {
        float windSpeed;

        if (debugFlag) {
            System.out.print("Wind Speed: Device = " + windSpdDevice.getName());
            System.out.print("  ID = " + windSpdDevice.getAddressAsString() + "\n");
        }

        // read wind counter & system time
        long currentCount = windSpdDevice.readCounter(15);
        long currentTicks = System.currentTimeMillis();
        System.out.println("Wind Count: " + currentCount);

        if (((currentCount - lastCount) >= 0) && ((currentTicks - lastTicks) >= 0)) {
            windSpeed = (float) (currentCount - lastCount) / (float) (currentTicks - lastTicks) * 1000f * 6.28f * radius;
        } else {
            throw new OneWireException("Could not get data");
        }

        if (debugFlag) {
            System.out.println("Count = " + (currentCount - lastCount) + " during "
                    + (currentTicks - lastTicks) + "ms calcs to " + windSpeed + " radius=" + radius);
        }

        lastCount = currentCount;
        lastTicks = currentTicks;

        return windSpeed;
    }

    public String getWind() {
        if (samples == 0) {
            return ("U");
        }
        double avgWind = Math.sqrt(sumSquares / samples);

        return this.formatValue((float) avgWind, 1);
    }

    public String getWindPk() {
        if (samples == 0) {
            return ("U");
        }
        float avg = sumValues / samples;
        float disp = sumSquares / samples;
        double pk = Math.sqrt(disp) + Math.sqrt(disp - avg * avg);

        return this.formatValue((float) pk, 1);
    }

    public void update() {
        try {
            float data = this.getWindSpeed();
            System.out.println("Wind speed = " + data + " M/sec");
            updateAverage(data);
        } catch (OneWireException e) {
            System.out.println("Error Reading Wind Speed: " + e);
        }
    }

    public Properties getResults() {
        Properties p = new Properties();
        p.setProperty("wspd", getWind());
        p.setProperty("wspdpk", getWindPk());

        return p;
    }
}
