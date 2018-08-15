/******************************************************************************

Project Name: SimpleWeather
File name:    Wunderground.java
Version:      1.0.9 06/23/06
$Id: Wunderground.java,v 1.5 2010/02/25 16:33:07 boris Exp $

Copyright (C) 2006 by T. Bitson - All rights reserved.

This class provides the interface to the Weather Underground.

 *****************************************************************************/
package simpleweather;

import java.net.*;
import java.io.*;
//import java.util.*;
import boangri.*;
import java.util.Enumeration;
import simpleweather.sensor.ISensor;

public class Wunderground implements Runnable {
    // user constants

    // class variables
    private BufferedReader in;
    private PrintStream out;
    private final static boolean debugFlag = true; //SimpleWeather.debugFlag;
    private MyFifo fifo = new MyFifo();
    private String url;
    private final Thread t;
    public int hits;
    public int failures;
    private final SimpleWeather sw;
    private Enumeration sensors;
    
    Wunderground(SimpleWeather sw) {
        this.sw = sw;
        hits = failures = 0;
        t = new Thread(this, "Fifo processing");
        t.start();
    }

    public int getCount() {
        return fifo.getCount();
    }
    
    public void send() {
        //String url = "weatherstation.wunderground.com";
        StringBuffer sendUrl = new StringBuffer();
        url = sw.WWW;

        // build up wunderground message based on what sensors we have.
        // comment out the lines if you don't have that sensor


        sendUrl.append("GET " + sw.URL + "?");
        sendUrl.append("ID=" + sw.StationID);
        sendUrl.append("&ts=" + sw.timestamp);
        sendUrl.append("&mn=" + sw.measurement);

        sensors = sw.sensor_vector.elements();
        
        while(sensors.hasMoreElements()) {
            ISensor s = (ISensor) sensors.nextElement();
            sendUrl.append("&"+s.getLabel()+"="+s.getValue());
        } 
//        // Temperature
//        //sendUrl.append("&tempf=" + wc.getTemp());
//        if (sw.ts1ex) {
//            sendUrl.append("&temp1=" + sw.ts1.getTemp());
//        }
//        if (sw.ts2ex) {
//            sendUrl.append("&temp2=" + sw.ts2.getTemp());
//        }
//        // temp DS18B20
//        if (sw.ts21ex) {
//            sendUrl.append("&temp21=" + sw.ts21.getTemp());
//        }
//        if (sw.ts22ex) {
//            sendUrl.append("&temp22=" + sw.ts22.getTemp());
//        }
//        // Humidity
//        if (sw.hs1ex) {
//            sendUrl.append("&humidity=" + sw.hs1.getHum());
//            // Dewpoint
//            //sendUrl.append("&dewptf=" + wc.getDP());
//            // Solar radiation level, percent
//            //sendUrl.append("&solar=" + wc.getSolar());
//        }
//
//        // Wind Speed and Direction
//        if (sw.ws1ex) {
//            sendUrl.append("&wspd=" + sw.wss1.getWind());
//            sendUrl.append("&wspdpk=" + sw.wss1.getWindSigma());
//            sendUrl.append("&wdir=" + sw.wds1.getWindDirAvg());
//        }
//
//        // Baro Pressure
//        if (sw.bs1ex) {
//            sendUrl.append("&baromin=" + sw.bs1.getBaro());
//        }
//
//        // Rain
//        if (sw.rs1ex) {
//            sendUrl.append("&rainin=" + sw.rs1.getRainRate());
//            //sendUrl.append("&dailyrainin=" + wc.getRain24());
//            sendUrl.append("&raincnt=" + sw.rs1.getRain());
//            //sendUrl.append("&pulse=" + sw.pulse);
//        }

        // Software Type & action
        //sendUrl.append("&softwaretype=tws&action=updateraw HTTP//1.1\r\nConnection: keep-alive\r\n\r\n");
        sendUrl.append(" HTTP/1.0\r\n\r\n");
        push(sendUrl.toString());
    }

    private void push(String surl) {
        fifo.push(surl);
    }

    public void run() {
        String str;
        Boolean success = false;
        while (true) {
//            System.out.println("Process Fifo...");
            while ((str = (String) fifo.getfirst()) != null) {
//                System.out.println("run: got string"+str);
                try {
                    if (debugFlag) {
                        System.out.println("Updating webserver...");
                    }

                    // open a connection to wunderground
                    Socket s = new Socket(url, 80);
                    if (s != null) {
                        // set up buffered readers & writers to the socket
                        out = new PrintStream(s.getOutputStream());
                        in = new BufferedReader(new InputStreamReader(s.getInputStream()));

                        // send wunderground post string
                        send(str);
                        success = readResponse();
                        s.close();
                    }
                } catch (IOException exception) {
                    System.out.println("WU Error: " + exception);
                    success = false;
                }
                if (success) {
                    hits++;
                    fifo.pop(); //remove item if sent seccessfully
                } else {
                    failures++;
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                        System.out.println("Interrupted");
                    }
                }
            }  // while fifo is not empty
        }
    }

    private void send(String s) throws IOException {
        if (s != null) {
            if (debugFlag) {
                System.out.println("Sending " + s);
            }

            out.print(s);
        }
    }

    private Boolean readResponse() throws IOException {
        if (debugFlag) {
            System.out.println("Getting Response...");
        }

        // wait up to 10 seconds for reply
        int i = 0;
        while (i++ < 100) {
            if (in.ready()) {
                String line = in.readLine();
                if (line.toLowerCase().indexOf("success", 0) > -1) {
                    if (debugFlag) {
                        System.out.println("WU: " + line);
                    }
                    System.out.println("Web server updated");
                    return true;
                } else {
                    if (debugFlag) {
                        System.out.println("WU: " + line);
                    }
                }
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
        }
        System.out.println("WU: No Response");
        return false;
    }
}
