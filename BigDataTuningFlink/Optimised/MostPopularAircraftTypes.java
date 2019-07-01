package optimization;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.common.operators.Order;
import org.apache.flink.api.common.operators.base.JoinOperatorBase.JoinHint;
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
import org.apache.flink.api.java.functions.FunctionAnnotation.ForwardedFieldsFirst;
import org.apache.flink.api.java.functions.FunctionAnnotation.ReadFieldsSecond;

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
		final String PATH = "/Users/yiranjing/Desktop/DATA3404/assignment_data_files/";
		//final String PATH = "hdfs://soit-hdp-pro-1.ucc.usyd.edu.au/";
		final ParameterTool params = ParameterTool.fromArgs(args);
		String outputFilePath = params.get("output", PATH + "results/optimize_most_popular_result_tiny.txt");
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
                return tuple.f2.contains(country); } 
            })
            .project(0, 1);


		// Step 2: Join both datasets 
		// Input: (carrier code, tail number) X (tail_number, manufacturer, model)
        // Output: (carrier_code ,aircraft_type)
		DataSet<Tuple2<String, String>> flightsOnAircrafts =
			flights.join(aircrafts)
			.where(1)
			.equalTo(0)
			.with(new EquiJoinAirlinesCountry());


		// Input: (carrier code, aircraft_type) X (carrier code, airline name)
		// Output: (airline name, aircraft_type)
		DataSet<Tuple2<String, String>> finalResult =
			flightsOnAircrafts.join(airlines)
			.where(0)
			.equalTo(0)
			.projectSecond(1).projectFirst(1);
			
           
		// Step 3: Ranking
			DataSet<Tuple3<String, String, Integer>> finalResult1 = finalResult.reduceGroup(new Rank());		
			
			DataSet<Tuple2<String, String>> finalResult2 = finalResult1
				.groupBy(0)
				.sortGroup(2, Order.DESCENDING)
				.first(5)
				.groupBy(0)
				.sortGroup(2, Order.DESCENDING)
				.reduceGroup(new Concat())
				.sortPartition(0, Order.ASCENDING)
				.setParallelism(1);

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
	* Rank the groupings
	* View step 3
	*/
	public static class Rank implements GroupReduceFunction<Tuple2<String, String>, Tuple3<String, String, Integer>> {
		@Override
		public void reduce(Iterable<Tuple2<String, String>> combinedData, Collector<Tuple3<String, String, Integer>> result) {
			
			HashMap<String, Integer> counter = new HashMap<String, Integer>(16_000_000, 1); // To help us construct data at the end.
				

			// Count how often entry appears.
			for(Tuple2<String, String> entry: combinedData) {
				String line = entry.f0 + "%" + entry.f1;
				int count = counter.containsKey(line) ? counter.get(line) : 0;
				counter.put(line, count + 1);
			}
			// Collect result of count.
			for(String key : counter.keySet()){
				int count = counter.get(key);
				String [] tuple = key.split("%"); // To help us store information.
				result.collect(new Tuple3 <> (tuple[0], tuple[1], count));
			}
		}
	}

 /**
	* Constructs and concatenates filtered result for final output.
	* View step 3
	*/
	private static class Concat implements GroupReduceFunction<Tuple3<String, String, Integer>, Tuple2 <String, String>> {
		@Override
		public void reduce(Iterable<Tuple3<String, String, Integer>> object, Collector<Tuple2<String, String>> output) throws Exception {
			
			String head = null; 
			String line = null;
			for(Tuple3<String, String, Integer> count : object){
				if(head == null){
					head = count.f0;
					line = "[";
			}
				if(count.f0.equals(head)){
					if(line.length() != 1){
						line += ", ";
					}
					line += count.f1;
				}
			}
			line += "]";
			output.collect(new Tuple2<>(head, line));
		}
	}
}
