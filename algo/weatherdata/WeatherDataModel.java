package algo.weatherdata;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Object model for Weather data from Visby Weather Station
 */
public class WeatherDataModel {
    LocalDate date;
    LocalTime time;
    float airTemp;
    boolean approved;

    // Weather Data Object for Weather App   
    public WeatherDataModel(LocalDate date, LocalTime time, float temperature, boolean goodAirQuality){
        this.date = date;
        this.time = time;
        this.airTemp = temperature;
        this.approved = goodAirQuality;
    }

    @Override
    public String toString() {
        String airQuality = this.approved? "G" : "Y";
        return this.date.toString() + ";" + this.time.toString() + ";" + this.airTemp + ";" + airQuality;
    }
}
