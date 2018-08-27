/******************************************************************************
 
 Project Name: SimpleWeather
 File name:    SimpleWeatherException.java
 Version:      1.0   12/17/05
 Copyright (C) 2005 by T. Bitson - All rights reserved.
 
 
 *****************************************************************************/

package simpleweather;

public class SimpleWeatherException
        extends Exception {

    public SimpleWeatherException() {
        super();
    }

    public SimpleWeatherException(String desc) {
        super(desc);
    }
}
