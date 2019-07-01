package data_group_assignment_version2;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.common.operators.Order;
import org.apache.flink.util.Collector;
import org.apache.flink.core.fs.FileSystem.WriteMode;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.log4j.BasicConfigurator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;

public class MostPopularAircraftTypes {
	
	public static void main(String[] args) throws Exception {

		BasicConfigurator.configure();
		ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

		/****************************
		*** READ IN DATA NEEDED. ***
		****************************/
		Scanner scanner = new Scanner(System.in);
        System.out.println("Enter a country:" );  
		String country = scanner.nextLine();
		scanner.close();
		
		// Don't forget to change file path!
		final String PATH = "/Users/jazlynj.m.lin/assignment_data_files/";
		//final String PATH = "hdfs://soit-hdp-pro-1.ucc.usyd.edu.au/";
		final ParameterTool params = ParameterTool.fromArgs(args);
		String outputFilePath = params.get("output", PATH + "results/most_popular_result_tiny.txt");
		//String outputFilePath = params.get("output", PATH + "user/jlin0701/assignment_data_files/results/most_popular_result_tiny.txt");
		
		// (carrier code, tail number)
		DataSet<Tuple2<String, String>> flights =
		env.readCsvFile(PATH + "ontimeperformance_flights_tiny.csv")
		//env.readCsvFile(PATH + "share/data3404/assignment/ontimeperformance_flights_tiny.csv")
						.includeFields("010000100000")
						.ignoreFirstLine()
						.ignoreInvalidLines()
						.types(String.class, String.class);
		
		// (carrier code, name, country)
		DataSet<Tuple3<String, String, String>> airlines =
		env.readCsvFile(PATH + "ontimeperformance_airlines.csv")
		//env.readCsvFile(PATH + "share/data3404/assignment/ontimeperformance_airlines.csv")
						.includeFields("111")
						.ignoreFirstLine()
						.ignoreInvalidLines()
						.types(String.class, String.class, String.class);
		
		// (tail_number, manufacturer, model)
		DataSet<Tuple3<String, String, String>> aircrafts =
		env.readCsvFile(PATH + "ontimeperformance_aircrafts.csv")
		//env.readCsvFile(PATH + "share/data3404/assignment/ontimeperformance_aircrafts.csv")
						.includeFields("101010000")
						.ignoreFirstLine()
						.ignoreInvalidLines()
						.types(String.class, String.class, String.class);

		/****************************
		*** ACTUAL IMPLEMENTATION ***
		****************************/						

		/****************************
		*	Implementation
		*	1) flights join aircrafts join airlines
		*   2) Apply filter for United States
		*   3) Rank grouped by aircraft types
		****************************/
			
        // Step 1: Filter and retrieve and return carrier code  + name based off Country.
        // Input: (carrier code, name, country)
        // Output: (carrier code, name)
        airlines.filter(new FilterFunction<Tuple3<String, String, String>>() {
            @Override
            public boolean filter(Tuple3<String, String, String> tuple) {
                // Filter for user specified country.
                return tuple.f2.contains("United States"); }   // will change to country
            })
            .project(0, 1);
            
        // Step 2: Join datasets.

        // Join on carrier code.
        // Input: (carrier code, name) X (carrier code, tail number)
        // Output: (name) X (tail number)
        DataSet<Tuple2<String, String>> flightsOnAirlines = 
            airlines.join(flights, JoinHint.BROADCAST_HASH_FIRST)
            .where(0)
            .equalTo(0)
            .projectFirst(1)
            .projectSecond(1);


        // Join on tail number.
        // Input: [(name, tail number)] X (tail_number, manufacturer, model)
        // Output: [(name) X (tail number)] X (manufacturer, model)
        DataSet<Tuple4<String, String, String, String>> finalData =
            flightsOnAirlines.join(aircrafts, JoinHint.BROADCAST_HASH_FIRST)
            .where(1)
            .equalTo(0)
            .projectFirst(0,1)
            .projectSecond(1,2);
            
        	// Step 3: Ranking top 5 aircraft types for each airline.
            finalData = finalData
            .sortPartition(0, Order.DESCENDING);

            // Output: (name, manufacturer, model, count)
			DataSet<Tuple4<String, String, String, Integer>> finalResult1 = finalData.reduceGroup(new Rank());		
            


            // Step 4
			DataSet<Tuple2<String, String>> finalResult2 = finalResult1
				.groupBy(0)
				.sortGroup(3, Order.DESCENDING)
				.first(5)
				.groupBy(0)
				.sortGroup(3, Order.DESCENDING)
				.reduceGroup(new Concat())
                .sortPartition(0, Order.ASCENDING);
                
				finalResult2.writeAsText(outputFilePath, WriteMode.OVERWRITE);
				
				long startTime = System.currentTimeMillis();
				    
		        // execute the FLink job
				env.execute("Executing task 3 program");
			    
			    
			    long endTime = System.currentTimeMillis();
			    long timeTaken = endTime-startTime;
			    
			    String timeFilePath = params.get("output", PATH + "results/Most_popular_time.txt");
			    BufferedWriter out = new BufferedWriter(new FileWriter(timeFilePath));
			    out.write("Time taken for execution was: " + timeTaken+"\n");
			    out.close();
		}

	
	 /**
		* Equi-join flights and aircrafts csv.
		* View step 1
	  */
	private static class EquiJoinAirlinesCountry implements JoinFunction <Tuple2<String, String>, Tuple3<String, String, String>, Tuple2<String, String>> {
		@Override
		public Tuple2<String, String> join(Tuple2<String, String> flightsData, Tuple3<String, String, String> aircraftsData){
			return new Tuple2<>(flightsData.f0, aircraftsData.f1 + " " + aircraftsData.f2);
		}
	}

 /**
	* Equi-join flights/aircrafts with airlines csv.
	* View step 1
	*/
	private static class EquiJoinFlightAircraftWithAirlines implements JoinFunction<Tuple2<String, String>, Tuple3<String, String, String>, Tuple3<String, String, String>> {
		@Override
		public Tuple3<String, String, String> join(Tuple2<String, String> flightsOnAircraftsData, Tuple3<String, String, String> airlinesData){
			return new Tuple3<>(airlinesData.f1, flightsOnAircraftsData.f1, airlinesData.f2);
		}
	}

	/**
	* Rank the groupings
	* View step 3
    */
    // Input: [(name) X (tail number)] X (manufacturer, model)
    // Output: (name, manufacturer, model, count)
	public static class Rank implements GroupReduceFunction<Tuple4<String, String, String, String>, Tuple4<String, String, String, Integer>> {
		
		@Override
		public void reduce(Iterable<Tuple4<String, String, String, String>> combinedData, Collector<Tuple4<String, String, String, Integer>> result) {
            
            // For each airline, keep track of top 5 flights.


			HashMap<String, Integer> counter = new HashMap<String, Integer>(); // To help us construct data at the end.
            HashMap<String, Integer> tempResult = new HashMap<String, Integer>();   
            
            String currentAirline = "";
            
			// Count how often entry appears.
			for(Tuple4<String, String, String, String> entry: combinedData) {
                if (currentAirline == "")
                {
                    // First iteration.
                    currentAirline = entry.f0;
                    String aircraftType = entry.f2 + "%" + entry.f3;
                    counter.put(aircraftType, 0);
                    continue;
                }
                
                // Still on same airline.
                if (entry.f0 == currentAirline)
                {    
                    String aircraftType = entry.f2 + "%" + entry.f3;
                    // Now check to see if same aircraft type, if not, then check temp hashmap.

                    if (counter.containsKey(aircraftType))
                    {
                        // Update count.
                        counter.put(aircraftType, counter.get(aircraftType)+1);
                        continue;
                    }
                    else
                    {
                        // New model. Check if hashmap can fit or need to evict.
                        if (counter.size() == 5)
                        {
                            // No space so need to evict.
                            String smallestKey = getSmallestKey(counter);
                            counter.remove(smallestKey);
                            counter.put(aircraftType, 1);
                        }
                        else
                        {
                            // Enough space so insert.
                            counter.put(aircraftType, 1);
                            continue;
                        }
                    }
                }
                else
                {
                    // New airline, so store results of previous airline.
                    // Transfer counter hashmap result into final result.
                    // Clear counter.
                    // Update airline name.
                    // Now add stuff into counter.
                    // Counter.
                    for (String key: counter.keySet())
                    {
                        String newKeyName = currentAirline + "%" + key;
                        tempResult.put(newKeyName, counter.get(key));
                    }
                    counter.clear();
                    currentAirline = entry.f0;
                    String aircraftType = entry.f2 + "%" + entry.f3;
                    counter.put(aircraftType, 1);
                    continue;
                }				
            }
            

            // Grab the airline name, manufacturer, model, count.
			for(String key : tempResult.keySet()){
				int count = counter.get(key);
				String [] tuple = key.split("%"); // To help us store information.
				result.collect(new Tuple4 <> (tuple[0], tuple[1], tuple[2], count));
			}
        }

        public String getSmallestKey(HashMap<String, Integer> hashMap)
        {
            String smallestKey = "";
            int smallestCount = Integer.MAX_VALUE;
            for (String key: hashMap.keySet())
            {
                if (hashMap.get(key) < smallestCount)
                {
                    smallestCount = hashMap.get(key);
                    smallestKey = key;
                }
            }
            return smallestKey;
        }
        

	}

 /**
	* Constructs and concatenates filtered result for final output.
	* View step 3
    */
    // Output: (name, manufacturer, model, count)
	private static class Concat implements GroupReduceFunction<Tuple4<String, String, String, Integer>, Tuple2 <String, String>> {
		@Override
		public void reduce(Iterable<Tuple4<String, String, String, Integer>> object, Collector<Tuple2<String, String>> output) throws Exception {
			
			String head = null; 
			String line = null;
			for(Tuple4<String, String, String, Integer> entry : object){
				if(head == null){
					head = entry.f0;
					line = "[";
			}
				if(entry.f0.equals(head)){
                    if(line.length() != 1)
                    {
						line += ", ";
                    }
                    String aircraftType = entry.f1 + " " + entry.f2;
					line += aircraftType;
				}
			}
			line += "]";
			output.collect(new Tuple2<>(head, line));
		}
	}
}