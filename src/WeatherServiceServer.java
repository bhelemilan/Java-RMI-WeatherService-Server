// WeatherServiceImpl.java
// WeatherServiceImpl implements the WeatherService remote
// interface to provide a WeatherService remote object.

import java.io.*;
import java.net.URL;
import java.rmi.*;
import java.rmi.server.*;
import java.text.SimpleDateFormat;
import java.util.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class WeatherServiceServer implements WeatherService
{
    private List<List> weatherInformation=new ArrayList<>();  // WeatherBean objects
    static WeatherServiceServer obj;
    private int timeIntervalBetweenUpdates = 60*60*1000; //in milliseconds, 60*1000 milliseconds = 1 min

    private String[] links=new String[]{
            "https://forecast.weather.gov/product.php?site=CRH&product=SCS&issuedby=01",
            "https://forecast.weather.gov/product.php?site=CRH&product=SCS&issuedby=02",
            "https://forecast.weather.gov/product.php?site=CRH&product=SCS&issuedby=03",
            "https://forecast.weather.gov/product.php?site=CRH&product=SCS&issuedby=04"
    };

    public WeatherServiceServer ()
    {
        super();
        for (int i=0;i<links.length;i++)
        {
            updateWeatherConditions(i);
        }
    }

    private void updateWeatherConditions(int linkID)
    {
        try
        {
            System.err.println( "Updating Weather Information from . . .\n" + links[linkID] );

            URL url = new URL(links[linkID]);
            BufferedReader in = new BufferedReader(new InputStreamReader( url.openStream()));

            String separator = "TEMPERATURES INDICATE";
            String prevLine = "";
            String inputLine = "";
            String cityName = "";

            if (weatherInformation.size() <= 0)
            {
                weatherInformation.add(new ArrayList());
                weatherInformation.add(new ArrayList());
                weatherInformation.add(new ArrayList());
                weatherInformation.add(new ArrayList());
            }

            for ( inputLine = in.readLine(); !inputLine.startsWith(separator ); inputLine = in.readLine() )
            {
                //Skip Lines not starting with the separator
            }

            separator = "CITY";
            for ( inputLine = in.readLine(); !inputLine.startsWith(separator ); inputLine = in.readLine() )
            {
                //Skip Lines not starting with the separator
                prevLine = inputLine;
            }

            //Get Yesterday's, today's and tomorrow's Date and Day
            String yesterday = prevLine.trim().split("   ")[0];
            String yesterdayDay = yesterday.substring(0,3);
            String yesterdayDate = yesterday.substring(6);

            String today = prevLine.trim().split("   ")[1];
            String todayDay = today.substring(0,3);
            String todayDate=today.substring(7);

            String tomorrow = prevLine.trim().split("   ")[2];
            String tomorrowDay = tomorrow.substring(0,3);
            String tomorrowDate = tomorrow.substring(7);

            boolean is_LO_HI_format = inputLine.replace(" ","").contains("LO/HI");

            /*
            893
            FPUS20 KWBN 020050
            SCS01
            SELECTED CITIES WEATHER SUMMARY AND FORECASTS...PART 1 OF 4
            NWS/NDFD TELECOMMUNICATION OPERATIONS CENTER SILVER SPRING MD
            850 PM EDT SUN JUL 01 2018

            TEMPERATURES INDICATE NIGHTTIME LOW...DAYTIME HIGH
            B INDICATES TEMPERATURES BELOW ZERO
            PRECIPITATION FOR 24 HOURS ENDING AT 8 PM EDT

            FORECAST        FORECAST
                             SUN...JUL 01   MON....JUL 02   TUE....JUL 03
            CITY             LO/HI   PCPN   WEA     LO/HI   WEA     LO/HI

            ABILENE TX       71  99   .27   SUNNY   77/101  SUNNY   75/101
            COLUMBUS OH      70  94         TSTRMS  73/90   TSTRMS  73/90


            $$
            */

            // create WeatherBeans containing weather data and store in weatherInformation Vector
            inputLine = in.readLine();  // skip an empty line
            inputLine = in.readLine();  // first city info line

            // The portion of inputLine containing relevant data is
            // 45 characters long. If the line length is not at
            // least 10 characters long, done processing data.
            while ( inputLine.length() > 10 )
            {
                // Create WeatherBean object for city.
                // First 16 characters are city name.
                cityName = inputLine.substring( 0, 16 ).trim();

                WeatherParam yesterdayWeatherParam = new WeatherParam(
                        yesterdayDate,
                        yesterdayDay,
                        is_LO_HI_format?Integer.parseInt(inputLine.substring( 16, 19 ).trim()):Integer.parseInt(inputLine.substring( 20, 23 ).trim()),
                        is_LO_HI_format?Integer.parseInt(inputLine.substring( 20, 23 ).trim()):Integer.parseInt(inputLine.substring( 16, 19 ).trim()),
                        inputLine.substring(24,32).trim().equals("")?0:Float.parseFloat(inputLine.substring(24,32).trim()),
                        "");
                //System.out.println("YESTERDAY: " + yesterdayWeatherParam.toString());

                WeatherParam todayWeatherParam = new WeatherParam(
                        todayDate,
                        todayDay,
                        is_LO_HI_format?Integer.parseInt(inputLine.substring( 39, 42 ).trim()):Integer.parseInt(inputLine.substring( 43, 48 ).trim()),
                        is_LO_HI_format?Integer.parseInt(inputLine.substring( 43, 48 ).trim()):Integer.parseInt(inputLine.substring( 39, 42 ).trim()),
                        -5,
                        inputLine.substring( 32, 39 ).trim());
                //System.out.println("TODAY: " + todayWeatherParam.toString());

                WeatherParam tomorrowWeatherParam = new WeatherParam(
                        tomorrowDate,
                        tomorrowDay,
                        is_LO_HI_format?Integer.parseInt(inputLine.substring( 55, 58 ).trim()):Integer.parseInt(inputLine.substring( 59 ).trim()),
                        is_LO_HI_format?Integer.parseInt(inputLine.substring( 59 ).trim()):Integer.parseInt(inputLine.substring( 55, 58 ).trim()),
                        -5,
                        inputLine.substring( 48, 55 ).trim());
                //System.out.println("TOMORROW: " + tomorrowWeatherParam.toString());

                WeatherBean weatherBean = new WeatherBean(cityName, yesterdayWeatherParam, todayWeatherParam, tomorrowWeatherParam);

                weatherInformation.get(linkID).add( weatherBean );

                inputLine = in.readLine();
            }
            in.close();

            System.err.println( "Weather Information updated from \n"+links[linkID] );
        }
        catch( java.net.ConnectException connectException )
        {
            connectException.printStackTrace();
            System.exit( 1 );
        }
        catch( Exception exception )
        {
            exception.printStackTrace();
            System.exit( 1 );
        }
    }

    // implementation for WeatherService interface method
    public List<List> getWeatherInformation() throws RemoteException
    {
        return weatherInformation;
    }

    // launch WeatherService remote object
    public static void main( String args[] )
    {
        if (args.length!=2)
        {
            System.err.println("USAGE: java -Djava.security.policy=<Policy File> WeatherServiceServer <Host Name> <Port Number>");
            System.exit(1);
        }
        try
        {
            System.setSecurityManager (new SecurityManager());
            System.err.println( "Initializing WeatherService . . ." );

            obj = new WeatherServiceServer();

            Registry reg;
            reg = LocateRegistry.getRegistry(args[0], Integer.parseInt(args[1]));

            WeatherService stub;
            stub = (WeatherService) UnicastRemoteObject.exportObject(obj, 0);

            reg.rebind("WeatherService", stub);

            System.err.println( "WeatherService running." );
            System.err.println("Current Date/Time: " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()));
            System.err.println( "Weather Information next updates after "+(obj.timeIntervalBetweenUpdates / (60 * 1000))+" min(s)." );

            new Timer().scheduleAtFixedRate(new TimerTask()
            {
                @Override
                public void run()
                {
                    System.err.println("Current Date/Time: " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()));

                    if (obj.weatherInformation.size() >= 0)
                    {
                        for (int i=0;i<obj.links.length;i++)
                        {
                            obj.weatherInformation.get(i).clear();
                        }
                    }

                    for (int i=0;i<obj.links.length;i++)
                    {
                        obj.updateWeatherConditions(i);
                    }

                    System.err.println( "Weather Information next updates after "+(obj.timeIntervalBetweenUpdates / (60 * 1000))+" min(s)." );
                }
            }, obj.timeIntervalBetweenUpdates,obj.timeIntervalBetweenUpdates);
        }
        catch (Exception e)
        {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}