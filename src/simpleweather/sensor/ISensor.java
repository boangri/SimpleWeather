/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package simpleweather.sensor;
import java.util.Properties;

/**
 *
 * @author Boris
 */
public interface ISensor {
    public void update();
    public void resetAverage();
//    public String getLabel();
//    public String getValue();
    public Properties getResults();
}
