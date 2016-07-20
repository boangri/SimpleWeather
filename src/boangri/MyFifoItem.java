/*
 Project Name: SimpleWeather
 File name:    MyFifo.java
 $Id: MyFifoItem.java,v 1.1 2010/02/24 15:15:31 boris Exp $
 
 Copyright (C) 2010 by Boris Gribovskiy - All rights reserved.
 
 This class implements the item of FIFO stack
 */

package boangri;

/**
 *
 * @author bgribovs
 */
public class MyFifoItem {
    public MyFifoItem next;
    public Object object;
    
    MyFifoItem (Object o) {
        next = null;
        object = o;
    }

    public Object getObject() {
        return object;
    } 
}
