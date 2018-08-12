/******************************************************************************
 
 Project Name: SimpleWeather
 File name:    SimpleWeatherException.java
 Version:      1.0   12/17/05
 Copyright (C) 2005 by T. Bitson - All rights reserved.
 
 
 *****************************************************************************/

package simpleweather;

// imports
import java.lang.Exception;


/**
 * This is the general exception thrown by the iButton and 1-Wire
 * operations.
 *
 * @version    0.00, 21 August 2000
 * @author     DS
 */
public class SimpleWeatherException
   extends Exception
{

   //--------
   //-------- Contructor
   //--------

   /**
    * Constructs a <code>OneWireException</code> with no detail message.
    */
   public SimpleWeatherException ()
   {
      super();
   }

   /**
    * Constructs a <code>OneWireException</code> with the specified detail message.
    *
    * @param  desc   the detail message description
    */
   public SimpleWeatherException (String desc)
   {
      super(desc);
   }
}
