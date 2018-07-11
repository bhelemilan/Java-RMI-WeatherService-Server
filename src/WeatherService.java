// WeatherService.java
// WeatherService interface declares a method for obtaining weather information.

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface WeatherService extends Remote
{
   // obtain Vector of WeatherBean objects from server
   List<List> getWeatherInformation() throws RemoteException;
}