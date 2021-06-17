package Task;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {

        JSONParser jsonP = new JSONParser();
        List<Person> salesPeople = addSalesPeopleToList(jsonP);


        long topPerformerThreshold = 0;
        long periodLimit = 0;
        boolean useExprienceMultiplier = false;

        try (FileReader reader = new FileReader("reportDefinition.json")) {
            Object obj = jsonP.parse(reader);

            JSONObject jsonObject = (JSONObject) obj;

            topPerformerThreshold = (long) jsonObject.get("topPerformersThreshold");
            useExprienceMultiplier = (boolean) jsonObject.get("useExprienceMultiplier");
            periodLimit = (long) jsonObject.get("periodLimit");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Map<String, Double> bestPerformers = firstFilter(salesPeople, periodLimit, useExprienceMultiplier);


        bestPerformers = secondFilter(topPerformerThreshold, bestPerformers);

        //printResult


    }

    private static Map<String, Double> firstFilter(List<Person> salesPeople,
                                                   long periodLimit, boolean useExprienceMultiplier) {
        Map<String, Double> bestSalesPeopleOneFilter = new HashMap<>();

        for (int i = 0; i < salesPeople.size(); i++) {
            addBestSalesPplToTheMap(salesPeople, periodLimit,
                    useExprienceMultiplier, bestSalesPeopleOneFilter, i);
        }

        return bestSalesPeopleOneFilter;
    }

    private static void addBestSalesPplToTheMap(List<Person> salesPeople, long periodLimit,
                                                boolean useExprienceMultiplier,
                                                Map<String, Double> bestSalesPeopleOneFilter, int i) {
        Person person = salesPeople.get(i);

        if (person.getSalesPeriod() <= periodLimit) {
            double score = 0;
            if (useExprienceMultiplier) {
                score = (person.totalSales * 1.0 / person.salesPeriod) * person.experienceMultiplier;
            } else {
                score = (person.totalSales * 1.0 / person.salesPeriod);
            }

            // Edge case -> what happens if you enter 2 ppl with the same name?
            if (bestSalesPeopleOneFilter.containsKey(person.getName())) {
                String format = String.format
                        ("%s is found more than once. Please enter each person only 1 time!", person.getName());
                JOptionPane.showMessageDialog(null, format);
            } else {
                bestSalesPeopleOneFilter.put(person.getName(), score);
            }
        }
    }

    private static Map<String, Double> secondFilter(long topPerformerThreshold,
                                                    Map<String, Double> bestSalesPeopleOneFilter) {

        bestSalesPeopleOneFilter = sortByScore(bestSalesPeopleOneFilter);

        //Round to the smaller number
        double theLengthOfEndResult = Math.floor(bestSalesPeopleOneFilter.size() * (topPerformerThreshold * 1.0 / 100));

        if (theLengthOfEndResult == 0) {
            printTooHighStandards();
        } else {
            addSalesPeopleToTheEndList(bestSalesPeopleOneFilter, theLengthOfEndResult);
        }

        return bestSalesPeopleOneFilter;
    }

    private static void printTooHighStandards() {
        String filePath = "TopPerformers.csv";

        try {
            FileWriter fw = new FileWriter(filePath);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);

            pw.println("You have set too high standards. Nobody meets them.");

            pw.flush();
            pw.close();

            JOptionPane.showMessageDialog
                    (null, "You have set too high standards. Nobody meets them.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Record not saved");
            e.printStackTrace();
        }
    }

    private static List<Person> addSalesPeopleToList(JSONParser jsonP) {
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
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return salesPeople;
    }

    private static void addSalesPeopleToTheEndList
            (Map<String, Double> bestSalesPeopleOneFilter, double lengthOfMapWithBothFilters) {

        String filePath = "TopPerformers.csv";

        try {
            FileWriter fw = new FileWriter(filePath);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);

            pw.println("Result");
            pw.println("Name, Score");

            bestSalesPeopleOneFilter.entrySet().
                    stream()
                    .limit((long) lengthOfMapWithBothFilters)
                    .forEach((entry) -> pw.println(entry.getKey() + ", " + entry.getValue()));

            pw.flush();
            pw.close();

            JOptionPane.showMessageDialog(null, "Record saved.");

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Record not saved.");
            e.printStackTrace();
        }
    }


    public static <K, V extends Comparable<? super V>> Map<K, V> sortByScore(Map<K, V> unsortedMap) {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(unsortedMap.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}

