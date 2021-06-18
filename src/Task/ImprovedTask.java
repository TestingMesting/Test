package Task;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ImprovedTask {
    public static void main(String[] args) {

        List<Person> salesPeople = retrieveSalesPeopleJsonFile();
        ReportDefinition reportDefinition = retrieveReportDefinitionJsonFile();
        Map<String, Double> bestPerformers = addBestPerformers(salesPeople, reportDefinition);

        try {
            exportResultToCsvFile(bestPerformers);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private static void exportResultToCsvFile(Map<String, Double> bestPerformers) throws IOException {
        if (bestPerformers.isEmpty()) {
            System.out.println("You have set too high standards. Nobody meets them.");
        } else {
            createCSVFile(bestPerformers);
        }
    }

    public static void createCSVFile(Map<String, Double> bestPerformers) throws IOException {
        FileWriter out = new FileWriter("book_new.csv");

        //  CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader("Name, Score"));
        CSVPrinter printer = CSVFormat.DEFAULT.withHeader("Name", "Score").print(out);

        //  CSVPrinter printer = CSVFormat.DEFAULT.withHeader("Name, Score").withDelimiter(',').withDelimiter(' ').print(out);

        bestPerformers.forEach((name, score) ->
        {
            try {
                printer.printRecord(name, score);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        printer.close();

    }

    private static Map<String, Double> addBestPerformers(List<Person> salesPeople,
                                                         ReportDefinition reportDefinition) {

        Map<String, Double> bestSalesPeople =
                addAllSalesPeopleWithLowerOrEqualSalesPeriodThanPeriodLimit(salesPeople, reportDefinition);

        bestSalesPeople = sortByScore(bestSalesPeople);

        double theLengthOfEndResult =
                Math.floor(bestSalesPeople.size() * (reportDefinition.getTOP_PERFORMERS_THRESHOLD() * 1.0 / 100)); // moje bi trqbva da gleda vsi4ki

        if (theLengthOfEndResult == 0) {
            return new HashMap<>();
        } else {
            bestSalesPeople = bestSalesPeople.entrySet()
                    .stream()
                    .limit((long) theLengthOfEndResult)
                    .collect(Collectors.toMap
                            (Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

            return bestSalesPeople;
        }


    }

    private static Map<String, Double> addAllSalesPeopleWithLowerOrEqualSalesPeriodThanPeriodLimit
            (List<Person> salesPeople, ReportDefinition reportDefinition) {

        Map<String, Double> bestSalesPeopleOneFilter = new HashMap<>();
        for (Person salesPerson : salesPeople) {
            Person person = salesPerson;

            if (person.getSalesPeriod() <= reportDefinition.getPERIOD_LIMIT()) {
                double score = calculateScore(reportDefinition, person);

                if (!bestSalesPeopleOneFilter.containsKey(person.getName())) {
                    bestSalesPeopleOneFilter.put(person.getName(), score);
                } else {
                    System.out.printf
                            ("%s is found more than once. Please enter each person only 1 time!", person.getName());
                }
            }
        }
        return bestSalesPeopleOneFilter;
    }


    public static Map<String, Double> sortByScore(final Map<String, Double> wordCounts) {

        return wordCounts.entrySet()
                .stream()
                .sorted((Map.Entry.<String, Double>comparingByValue().reversed()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    private static double calculateScore(ReportDefinition reportDefinition, Person person) {
        double score;
        if (reportDefinition.isUSER_EXPERIENCE_MULTIPLIER()) {
            score = (person.totalSales * 1.0 / person.salesPeriod) * person.experienceMultiplier;
        } else {
            score = (person.totalSales * 1.0 / person.salesPeriod);
        }
        return score;
    }

    private static ReportDefinition retrieveReportDefinitionJsonFile() {
        ReportDefinition reReportDefinition = null;

        JSONParser jsonP = new JSONParser();

        try (FileReader reader = new FileReader("reportDefinition.json")) {
            Object obj = jsonP.parse(reader);

            JSONObject jsonObject = (JSONObject) obj;

            long topPerformerThreshold = (long) jsonObject.get("topPerformersThreshold");
            boolean useExprienceMultiplier = (boolean) jsonObject.get("useExprienceMultiplier");
            long periodLimit = (long) jsonObject.get("periodLimit");

            reReportDefinition = new ReportDefinition(topPerformerThreshold, useExprienceMultiplier, periodLimit);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return reReportDefinition;
    }

    private static List<Person> retrieveSalesPeopleJsonFile() {
        JSONParser jsonP = new JSONParser();

        List<Person> salesPeople = new ArrayList<>();

        try (FileReader reader = new FileReader("salesPeople.json")) {
            Object obj = jsonP.parse(reader);

            JSONArray array = (JSONArray) obj;

            for (int i = 0; i < array.size(); i++) {
                Object o = array.get(i);
                JSONObject jsonObject = (JSONObject) o;

                String name = (String) jsonObject.get("name");
                long totalSales = (long) jsonObject.get("totalSales");
                long salesPeriod = (long) jsonObject.get("salesPeriod");
                double experienceMultiplier = (double) jsonObject.get("experienceMultiplier");

                Person person = new Person(name, totalSales, salesPeriod, experienceMultiplier);
                salesPeople.add(person);
            }

        } catch (FileNotFoundException e) {
            System.out.println("The file doesn't exits.");
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return salesPeople;
    }
}
