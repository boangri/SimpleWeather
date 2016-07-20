/*
 Project Name: SimpleWeather
 File name:    MyFifo.java
 $Id: MyFifo.java,v 1.2 2010/02/25 16:33:57 boris Exp $
 
 Copyright (C) 2010 by Boris Gribovskiy - All rights reserved.
 
 This class implements the interface to the FIFO stack

 */

package boangri;

/**
 *
 * @author bgribovs
 */
public class MyFifo implements Fifo {
    private MyFifoItem head, tail;
    private int count;
    
    public MyFifo () {
        /* create empty Fifo */
        count = 0;
        head = tail = null;
    }
    
    synchronized public void push(Object o) {
        MyFifoItem item = new MyFifoItem(o);
        if (count == 0) {
            head = item;
        } else {
            tail.next = item;
        }
        tail = item;
        count++;
        notify();
//        System.out.println("MyFifo: Pushed: " + o);
    }
    
    synchronized public Object pop(){
        Object o = null;
        if (count > 0) {
            count--;
            o = head.getObject();
            head = head.next;
            if (count == 0) tail = null;
        }
//        System.out.println("MyFifo: Poped: " + o);        
        return o;
    }
    
    synchronized public Object getfirst() {
        if (count == 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("InterruptedException: "+e);
            }
        }
//        System.out.println("Fifo size="+count);
        if (count == 0) {
            return null;
        } else {
            return head.getObject();
        }
    }    
}
