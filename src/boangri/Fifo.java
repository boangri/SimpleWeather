/*
 Project Name: SimpleWeather
 File name:    Fifo.java
 $Id: Fifo.java,v 1.1 2010/02/24 15:15:31 boris Exp $
 
 Copyright (C) 2010 by Boris Gribovskiy - All rights reserved.
 
 This class defines the interface to the FIFO stack
 */

package boangri;

/**
 *
 * @author bgribovs
 */
public interface Fifo {
    public void push(Object o);
    public Object pop();
    public int getCount();
}

