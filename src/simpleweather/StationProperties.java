/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package simpleweather;

import java.io.*;
import java.util.*;

/**
 *
 * @author Boris
 */
public class StationProperties {
    private final Properties ps;
    public StationProperties(String filename) throws FileNotFoundException, IOException
    {
        int lines;
        ps = new Properties();
        
        InputStream f = new FileInputStream(filename);
        InputStreamReader r = new InputStreamReader(f);
        LineNumberReader l = new LineNumberReader(r);
        String s ;

        for (lines = 1; (s = l.readLine()) != null; lines++) {
          String key;
          if (s.charAt(0) == '#') continue;
          StringTokenizer tok = new StringTokenizer(s, "=");
          key = tok.nextToken();
          ps.put(key, tok.nextToken());
        }
        f.close();
    }
    
    public Properties getStationProperties()
    {
        return ps;
    }
}
