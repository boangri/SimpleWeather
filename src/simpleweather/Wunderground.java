/**
 * ****************************************************************************
 *
 * Project Name: SimpleWeather File name: Wunderground.java Version: 1.0.9
 * 06/23/06
 *
 * Copyright (C) 2006 by T. Bitson - All rights reserved.
 *
 * This class provides the interface to the Weather Underground.
 *
 ****************************************************************************
 */
package simpleweather;

import java.net.*;
import java.io.*;
import java.util.*;
import boangri.*;
import java.util.Enumeration;
import simpleweather.sensor.ISensor;

public class Wunderground implements Runnable {

    private BufferedReader in;
    private PrintStream out;
    private final static boolean debugFlag = true; //SimpleWeather.debugFlag;
    private final MyFifo fifo;
    private String url;
    private final Thread t;
    public int hits;
    public int failures;
    private Enumeration<ISensor> sensors;

    Wunderground() {
        hits = failures = 0;
        fifo = new MyFifo();
        t = new Thread(this, "Fifo processing");
        t.start();
    }

    public int getCount() {
        return fifo.getCount();
    }

    public void send() {
        StringBuilder sendUrl = new StringBuilder();
        url = SimpleWeather.WWW;

        sendUrl.append("GET ").append(SimpleWeather.URL).append("?");
        sendUrl.append("ID=").append(SimpleWeather.StationID);
        sendUrl.append("&ts=").append(SimpleWeather.timestamp);
        sendUrl.append("&mn=").append(SimpleWeather.measurement);

        sensors = SimpleWeather.sensor_vector.elements();

        while (sensors.hasMoreElements()) {
            ISensor s = sensors.nextElement();
            Properties res = s.getResults();
            Enumeration<Object> er;
            er = res.keys();
            while (er.hasMoreElements()) {
                String key = (String) er.nextElement();
                String value = res.getProperty(key);
                sendUrl.append("&").append(key).append("=").append(value);
            }

        }

        sendUrl.append(" HTTP/1.0\r\n\r\n");
        push(sendUrl.toString());
    }

    private void push(String surl) {
        fifo.push(surl);
    }

    @Override
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
