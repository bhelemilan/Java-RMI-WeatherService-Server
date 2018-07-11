import java.io.Serializable;

public class WeatherParam implements Serializable
{
    private String day;
    private String date;
    private int lowTemp;
    private int highTemp;
    private float precipitation;
    private String weatherForecast; // SUNNY, TSTRMS, PTCLDY, . . .

    public WeatherParam(String date, String day, int lowTemp, int highTemp, float precipitation, String weatherForecast)
    {
        this.date=date;
        this.day=day;
        this.lowTemp=lowTemp;
        this.highTemp=highTemp;
        this.precipitation=precipitation;
        this.weatherForecast=weatherForecast;
    }

    public String getDate() {
        return date;
    }

    public String getDay() {
        return day;
    }

    public int getLowTemp() {
        return lowTemp;
    }

    public int getHighTemp() {
        return highTemp;
    }

    public float getPrecipitation() {
        return precipitation;
    }

    public String getWeatherForecast() {
        return weatherForecast;
    }

    @Override
    public String toString()
    {
        return date + " [" + day + "], " +
                "Hi/Low: " + highTemp + "/" + lowTemp +
                ", Precipitation: " + ((precipitation >= 0) ? Float.toString(precipitation) : "-") +
                (weatherForecast.isEmpty() ? "" : ", Forecast: " + weatherForecast);
    }
}