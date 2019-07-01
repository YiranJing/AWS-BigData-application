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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.functions.FunctionAnnotation.ForwardedFields;
import org.apache.flink.api.java.functions.FunctionAnnotation.ForwardedFieldsFirst;
import org.apache.flink.api.java.functions.FunctionAnnotation.ForwardedFieldsSecond;
import org.apache.flink.api.java.functions.FunctionAnnotation.ReadFields;


public class AverageDepartureDelay {
	
	public static void main(String[] args) throws Exception {
		  
		BasicConfigurator.configure();
		
		//String year = args[0];  // command argument, in this code, I use 2004 temporarily
		
		/****************************
		*** READ IN DATA NEEDED. ***
		****************************/

		// Don't forget to change file path!
		
		final String PATH = "/Users/yiranjing/Desktop/DATA3404/assignment_data_files/";
		final ParameterTool params = ParameterTool.fromArgs(args);
		String outputFilePath = params.get("output", PATH + "optimization_avg_dep_delay_tiny.txt");
		// Used for time interval calculation
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		 
		// user specific input 
		Scanner scanner = new Scanner(System.in);
        System.out.println("Enter a year:" );  
		String year = scanner.nextLine();
		scanner.close();
	    
	    // obtain handle to execution environment
	    ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
	    
	    
	    DataSet<Tuple5<String, String, String,String,String>> flights =
			      env.readCsvFile(PATH + "ontimeperformance_flights_tiny.csv")
			      .includeFields("0101001101")  // carrier_code, flight_date, tail_number, scheduled_depar, actual_departure
			      .ignoreFirstLine() 
			      .ignoreInvalidLines() 
			      .types(String.class,String.class,String.class, String.class,String.class); 
	    
	    DataSet<Tuple1<String>> aircrafts =
	    		  env.readCsvFile(PATH +"ontimeperformance_aircrafts.csv")
	    		  .includeFields("1") // tail_number
		          .ignoreFirstLine() 
		          .ignoreInvalidLines() 
		          .types(String.class); 
	    
	    DataSet<Tuple3<String, String,String>> airlines =
			      env.readCsvFile(PATH +"ontimeperformance_airlines.csv")
			      .includeFields("111") // carrier_code, name and country
			      .ignoreFirstLine() 
			      .ignoreInvalidLines() 
			      .types(String.class,String.class, String.class); 
	    
	    
	    
		/****************************
		*** ACTUAL IMPLEMENTATION ***
		****************************/						

		/****************************
		*	Implementation
		*
		* 1) Filter Processing
		*    a) Filter US airlines 
		*    b) Filter for specific year
		*    c) Filter out cancelled flights, only keep delayed flights, and then compute delay for each flight
		* 
		* 2) Sorted Airline name in ascending order
		* 3) Join three filtered data sets
		* 4) Group by and Aggregate the result using Airline name, for number, sum, min and max delay time 
		* 5) Compute the average time
		****************************/
	    
		// Step 1 a)
	    DataSet<Tuple2<String, String>> USairlines = 
				airlines.filter(new FilterFunction<Tuple3<String, String, String>>() {
				@Override
				public boolean filter(Tuple3<String, String, String> tuple) {
					// Filter for United States
					return tuple.f2.contains("United States"); }    
				})
				.project(0, 1);
	    
		
	 // Step 1 b)   
	    DataSet<Tuple4<String, String, String,String>>flightsYear = 
	    		flights.filter(new FilterFunction<Tuple5<String, String, String, String, String>>() {
	    		@Override
				public boolean filter(Tuple5<String, String, String,String, String> tuple) {
						// Filter for given year
					return tuple.f1.substring(0,4).equals(year); } 
		        }).project(0,2,3,4); 
	    
	    
    // Step 1 c)
	    DataSet<Tuple3<String,String,Long>>flightsDelay =
	    		     flightsYear.filter(new FilterFunction<Tuple4<String, String, String, String>>() {
	                            public boolean filter(Tuple4<String, String,String,String> entry){
	                            	try {
	                            	return (format.parse(entry.f2).getTime() < format.parse(entry.f3).getTime());}  // filter only delayed flights
	                            	catch(ParseException e) {
	                            		System.out.println("Ignore the cancelled flight");
	                            		return false;
	                            	}
	                            } 
	                     }).flatMap(new TimeDifferenceMapper());
    
	 //Step 2)    
	    DataSet<Tuple2<String, String>> sortedUSairlines = USairlines.sortPartition(1,Order.ASCENDING);  //  1 is airline name
		
	    
	// Step 3	
		DataSet<Tuple2<String, Long>> flightsCraftes = aircrafts
			  .join(flightsDelay,JoinHint.BROADCAST_HASH_FIRST).where(0).equalTo(1).projectSecond(0,2);  // carrier code , number of delay 	
		    
	    DataSet<Tuple2<String,Long>> joinresult = sortedUSairlines
		      .join(flightsCraftes,JoinHint.BROADCAST_HASH_FIRST).where(0).equalTo(0).projectFirst(1).projectSecond(1); // airline name, number of delay
	    
	    
	    
	// Step 4
	    
	    DataSet<Tuple5<String,Integer,Long,Long,Long>> finalresult = 
	    		joinresult
	    		.groupBy(0)
	    		.reduceGroup(new Aggregation())
	    		.setParallelism(1);
	    		
	    		

	    //write out final result
	    finalresult.writeAsText(outputFilePath, WriteMode.OVERWRITE);   
	    
	    long startTime = System.currentTimeMillis();
	    
        // execute the FLink job
	    env.execute("Executing task 2 program tiny");
	    
	    
	    long endTime = System.currentTimeMillis();
	    long timeTaken = endTime-startTime;
	    
	    String timeFilePath = params.get("output", PATH + "results/optimize_Delay_time.txt");
	    BufferedWriter out = new BufferedWriter(new FileWriter(timeFilePath));
	    out.write("Time taken for execution was: " + timeTaken+"\n");
	    out.close();
	   
	  
	
	}
	/**
	* Calculate delay time for each delay flight
	* After get the delay time, filter out the scheduled and actual departure time columns
	* View step 1 c)
	*/
	 @ForwardedFields("0;1") // Specify that the first and second element is copied without any changes
	 @ReadFields("2;3") // specifies what fields were used to compute a result value
	 private static class TimeDifferenceMapper implements FlatMapFunction<Tuple4<String, String, String, String>, Tuple3<String,String,Long>>{
	     SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
	          @Override
	          public void flatMap(Tuple4<String, String, String, String>input_tuple, Collector<Tuple3<String,String,Long>> out) throws ParseException { 
	        	  Long diff_min =(long) ((format.parse(input_tuple.f3).getTime()-format.parse(input_tuple.f2).getTime())/(60.0 * 1000.0) % 60.0);
	        	 if (diff_min>0) {
	        		  out.collect(new Tuple3<String,String,Long>(input_tuple.f0, input_tuple.f1, diff_min)); 
	        	 }
		    }
		  }
	 
	 
	  /**
		* Calculate the number of delay, average, min, max of each airline
		* View step 3
		*/  
	public static class Aggregation implements GroupReduceFunction<Tuple2<String, Long>, Tuple5<String, Integer, Long, Long, Long>> {
		    @Override
			public void reduce(Iterable<Tuple2<String, Long>> combinedData, Collector<Tuple5<String, Integer, Long, Long, Long>> result) {
				int count = 0;
				long sum = 0;
				long max= Long.MIN_VALUE;
				long min = Long.MAX_VALUE;
				String airline_name = null;
				for (Tuple2<String, Long> entry: combinedData) {
					if (airline_name == null) {
						airline_name = entry.f0;
					}
					count ++;
					sum = sum + entry.f1;
					if (entry.f1 > max) {
						max = entry.f1;
					}
					if (entry.f1 < min) {
						min = entry.f1;
					}			
				}
				result.collect(new Tuple5<String, Integer, Long, Long, Long>(airline_name, count, sum/count, min, max));		
			}
		}

		  
	 

}
