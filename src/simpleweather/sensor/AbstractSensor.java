package simpleweather.sensor;

import com.dalsemi.onewire.*;
import com.dalsemi.onewire.adapter.*;
import com.dalsemi.onewire.container.*;
import java.util.StringTokenizer;
import java.util.Properties;
import simpleweather.SimpleWeather;

/**
 *
 * @author Boris
 */
public abstract class AbstractSensor implements ISensor {

    protected static boolean debugFlag = SimpleWeather.debugFlag;
    protected float sumValues;
    protected float sumSquares;
    protected int samples;
    protected String name;
    protected OneWireContainer device;
    protected boolean ready;

    public boolean isReady() {
        return this.ready;
    }
    
    public boolean checkSensor() {
        try {
            this.ready = device.isPresent();
        } catch (OneWireException e) {
            this.ready = false;
        }
        return this.ready;
    }
    
    abstract public void update();

    abstract public Properties getResults();

    public void resetAverage() {
        samples = 0;
        sumValues = 0f;
        sumSquares = 0f;

        if (debugFlag) {
            System.out.println("Averages Reset");
        }
    }

    protected void updateAverage(float value) {
        samples++;
        sumValues += value;
        sumSquares += value * value;
    }

    public String getAverage(int digits) {
        if (samples == 0) {
            return "U";
        }

        float avg = sumValues / samples;

        return this.formatValue(avg, digits);
    }

    public String getSigma(int digits) {
        if (samples == 0) {
            return "U";
        }

        float avg = sumValues / samples;
        float disp = sumSquares / samples;
        float sigma = (float) Math.sqrt(disp - avg * avg);

        return this.formatValue(avg, digits);
    }

    // short routine format a float to 'digit' decimals and convert to string
    public String formatValue(float input, int digits) {

        try {

            String arg1;
            String arg2;
            StringTokenizer t;

            if (digits < 1) {
                digits = 1;
            }

            if (digits > 3) {
                digits = 3;
            }

            float roundVal;

            if (digits == 1) {
                roundVal = .05f;
            } else if (digits == 2) {
                roundVal = .005f;
            } else // digits = 3
            {
                roundVal = .0005f;
            }

            t = new StringTokenizer(Double.toString(input + roundVal), ".");

            arg1 = t.nextToken();
            arg2 = t.nextToken();

            if (Math.abs(input + roundVal) < .001 && digits == 3) {
                return ("0.000");
            } else {
                return arg1 + "." + arg2.substring(0, digits);
            }
        } catch (Exception e) {
            return "error";
        }
    }

}
