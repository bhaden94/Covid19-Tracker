package io.javabrains.coronavirustracker.services;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import io.javabrains.coronavirustracker.models.GraphStats;
import io.javabrains.coronavirustracker.models.LocationStats;

/*
 * This class holds our stats and fetches the data from the github repo for us.
 * https://github.com/CSSEGISandData/COVID-19
 * 
 * Brady Haden
 */

@Service // start when application does (creates instance of class on start)
public class CoronaVirusDataServices
{
  // url for data
  private static String VIRUS_DATA_CONFIRMED_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
  private static String VIRUS_DATA_DEATH_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_deaths_global.csv";
  private static String VIRUS_DATA_RECOVERED_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_recovered_global.csv";
  private static String VIRUS_DATA_GRAPH_URL = "https://raw.githubusercontent.com/datasets/covid-19/master/data/worldwide-aggregated.csv";

  private List<LocationStats> confirmedStats = new ArrayList<>();
  private List<LocationStats> deathStats = new ArrayList<>();
  private List<LocationStats> recoveredStats = new ArrayList<>();
  private List<GraphStats> totalCasesPerDayStats = new ArrayList<>();

  public List<GraphStats> getTotalCasesPerDayStats()
  {
    return totalCasesPerDayStats;
  }

  public List<LocationStats> getRecoveredStats()
  {
    return recoveredStats;
  }

  public List<LocationStats> getDeathStats()
  {
    return deathStats;
  }

  public List<LocationStats> getConfirmedStats()
  {
    return confirmedStats;
  }

  // tells spring to execute method after instance of class is
  // created
  @PostConstruct
  // scheduled layout is second, minute, hour, day, month, year
  // * is used for every iteration call (ie: every second if all *'s)
  // must use @EnableScheduling in main class
  // currently it is set to run on the first hour of everyday
  @Scheduled(cron = "0 0 15-18 * * ?")
  public void fetchConfirmedVirusData() throws IOException, InterruptedException
  {

    // creating this new list allows the user to see the old data
    // (confirmedStats)
    // while newStats is being populated
    List<LocationStats> newStats = new ArrayList<>();
    HttpClient client = HttpClient.newHttpClient();

    // request to the given url to get data
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(VIRUS_DATA_CONFIRMED_URL)).build();

    HttpResponse<String> httpResponse = client.send(request,
        HttpResponse.BodyHandlers.ofString());

    // outputs the body of the http page linked
    // org.apache.commons Maven dependency added to parse values
    // https://commons.apache.org/proper/commons-csv/user-guide.html

    StringReader csvBodyReader = new StringReader(httpResponse.body());
    // Reader csvBodyReader = new FileReader(httpResponse.body());
    // our raw file has the header as the first line so we use header auto
    // detection
    Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader()
        .parse(csvBodyReader);
    for (CSVRecord record : records)
    {
      LocationStats locationStat = new LocationStats();
      locationStat.setState(record.get("Province/State"));
      locationStat.setCountry(record.get("Country/Region"));
      // our read value is String so we must parse
      // the most recent data will be added as a new column so we must grab the
      // last available column
      int latestCases = 0;
      int previousDayCases = 0;

      latestCases = Integer.parseInt(record.get(record.size() - 1));
      previousDayCases = Integer.parseInt(record.get(record.size() - 2));

      locationStat.setLatestTotalCases(latestCases);
      locationStat.setDiffFromPrevDay(latestCases - previousDayCases);
      newStats.add(locationStat);
    }
    this.confirmedStats = newStats;
  }

  // tells spring to execute method after instance of class is
  // created
  @PostConstruct
  // scheduled layout is second, minute, hour, day, month, year
  // * is used for every iteration call (ie: every second if all *'s)
  // must use @EnableScheduling in main class
  // currently it is set to run on the first hour of everyday
  @Scheduled(cron = "0 0 15-18 * * ?")
  public void fetchDeathVirusData() throws IOException, InterruptedException
  {
    // creating this new list allows the user to see the old data
    // (deathStats)
    // while newStats is being populated
    List<LocationStats> newStats = new ArrayList<>();
    HttpClient client = HttpClient.newHttpClient();

    // request to the given url to get data
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(VIRUS_DATA_DEATH_URL)).build();

    HttpResponse<String> httpResponse = client.send(request,
        HttpResponse.BodyHandlers.ofString());

    // outputs the body of the http page linked
    // org.apache.commons Maven dependency added to parse values
    // https://commons.apache.org/proper/commons-csv/user-guide.html

    StringReader csvBodyReader = new StringReader(httpResponse.body());
    // Reader csvBodyReader = new FileReader(httpResponse.body());
    // our raw file has the header as the first line so we use header auto
    // detection
    Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader()
        .parse(csvBodyReader);
    for (CSVRecord record : records)
    {
      LocationStats locationStat = new LocationStats();
      locationStat.setState(record.get("Province/State"));
      locationStat.setCountry(record.get("Country/Region"));
      // our read value is String so we must parse
      // the most recent data will be added as a new column so we must grab the
      // last available column
      int latestCases = 0;
      int previousDayCases = 0;

      latestCases = Integer.parseInt(record.get(record.size() - 1));
      previousDayCases = Integer.parseInt(record.get(record.size() - 2));

      locationStat.setLatestTotalCases(latestCases);
      locationStat.setDiffFromPrevDay(latestCases - previousDayCases);
      newStats.add(locationStat);
    }
    this.deathStats = newStats;
  }

  // tells spring to execute method after instance of class is
  // created
  @PostConstruct
  // scheduled layout is second, minute, hour, day, month, year
  // * is used for every iteration call (ie: every second if all *'s)
  // must use @EnableScheduling in main class
  // currently it is set to run on the first hour of everyday
  @Scheduled(cron = "0 0 15-18 * * ?")
  public void fetchRecoveredVirusData() throws IOException, InterruptedException
  {
    // creating this new list allows the user to see the old data
    // (recoveredStats)
    // while newStats is being populated
    List<LocationStats> newStats = new ArrayList<>();
    HttpClient client = HttpClient.newHttpClient();

    // request to the given url to get data
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(VIRUS_DATA_RECOVERED_URL)).build();

    HttpResponse<String> httpResponse = client.send(request,
        HttpResponse.BodyHandlers.ofString());

    // outputs the body of the http page linked
    // org.apache.commons Maven dependency added to parse values
    // https://commons.apache.org/proper/commons-csv/user-guide.html

    StringReader csvBodyReader = new StringReader(httpResponse.body());
    // Reader csvBodyReader = new FileReader(httpResponse.body());
    // our raw file has the header as the first line so we use header auto
    // detection
    Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader()
        .parse(csvBodyReader);
    for (CSVRecord record : records)
    {
      LocationStats locationStat = new LocationStats();
      locationStat.setState(record.get("Province/State"));
      locationStat.setCountry(record.get("Country/Region"));
      // our read value is String so we must parse
      // the most recent data will be added as a new column so we must grab the
      // last available column
      int latestCases = 0;
      int previousDayCases = 0;

      latestCases = Integer.parseInt(record.get(record.size() - 1));
      previousDayCases = Integer.parseInt(record.get(record.size() - 2));

      locationStat.setLatestTotalCases(latestCases);
      locationStat.setDiffFromPrevDay(latestCases - previousDayCases);
      newStats.add(locationStat);
    }
    this.recoveredStats = newStats;
  }

  // tells spring to execute method after instance of class is
  // created
  @PostConstruct
  public void fetchTotalCasesData() throws IOException, InterruptedException
  {
    // creating this new list allows the user to see the old data
    // (recoveredStats)
    // while newStats is being populated
    List<GraphStats> newStats = new ArrayList<>();
    HttpClient client = HttpClient.newHttpClient();

    // request to the given url to get data
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(VIRUS_DATA_GRAPH_URL)).build();

    HttpResponse<String> httpResponse = client.send(request,
        HttpResponse.BodyHandlers.ofString());

    // outputs the body of the http page linked
    // org.apache.commons Maven dependency added to parse values
    // https://commons.apache.org/proper/commons-csv/user-guide.html

    StringReader csvBodyReader = new StringReader(httpResponse.body());
    Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader()
        .parse(csvBodyReader);
    for (CSVRecord record : records)
    {
      // date,confirmed,deaths
      GraphStats currentStats = new GraphStats();
      String date = record.get("Date");
      currentStats.setDate(Date.valueOf(date));

      int confirmedCasesThisDay = Integer.parseInt(record.get("Confirmed"));
      int recoveredThisDay = Integer.parseInt(record.get("Recovered"));
      int deathsThisDay = Integer.parseInt(record.get("Deaths"));

      currentStats.setCasesThisDay(confirmedCasesThisDay);
      currentStats.setRecoveredThisDay(recoveredThisDay);
      currentStats.setDeathsThisDay(deathsThisDay);
      newStats.add(currentStats);
    }
    this.totalCasesPerDayStats = newStats;
  }

}
