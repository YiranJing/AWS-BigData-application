package assignment;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.common.operators.Order;
import org.apache.flink.util.Collector;
import org.apache.flink.core.fs.FileSystem.WriteMode;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.log4j.BasicConfigurator;

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
import org.apache.flink.api.java.functions.FunctionAnnotation.ForwardedFields;

import java.util.concurrent.TimeUnit;
import java.util.Scanner;


public class AverageDepartureDelay {
	
	public static void main(String[] args) throws Exception {
		  
		BasicConfigurator.configure();
		
		/****************************
		*** READ IN DATA NEEDED. ***
		****************************/

		// Don't forget to change file path!
		
		//final String PATH = "hdfs://soit-hdp-pro-1.ucc.usyd.edu.au/";
		final String PATH = "/Users/yiranjing/Desktop/DATA3404/assignment_data_files/";
		//final String PATH = "hdfs://soit-hdp-pro-1.ucc.usyd.edu.au/";
		final ParameterTool params = ParameterTool.fromArgs(args);
		//final String Out_PATH = "hdfs://soit-hdp-pro-1.ucc.usyd.edu.au/user/yjin5856/result/";
		//String outputFilePath = params.get("output", PATH + "user/jlin0701/assignment_data_files/results/avg_dep_delay_tiny.txt");
		String outputFilePath = params.get("output", PATH + "results/bad_avg_dep_delay_tiny.txt");
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
		* 1) Join three data sets
		* 2) Filter Processing
		*    a) Filter US airlines 
		*    b) Filter for specific year
		*    c) Filter out cancelled flights, only keep delayed flights, and then compute delay for each flight
		* 
		* 3) Group by and Aggregate the result using Airline name, for number, sum, min and max delay time 
		* 4) Compute the average time
		* 5) Sorted Airline name in ascending order
		****************************/
	    
	    
	    
	// Step 1
	    // output: carrier_code, flight_date, scheduled_depar, actual_departure
		DataSet<Tuple4<String,String,String,String>> flightsCraftes = aircrafts
			  .join(flights).where(0).equalTo(2).projectSecond(0,1,3,4);  
		  
		// output: flight_date, scheduled_depar, actual_departure, name, country
	    DataSet<Tuple5<String,String,String,String,String>> join = flightsCraftes
		      .join(airlines).where(0).equalTo(0).projectFirst(1,2,3).projectSecond(1,2);   
	    
	    
	    
	    
	    
	 // Step 2 a)    
	    DataSet<Tuple5<String,String,String,String,String>> USjoinresult = 
	    		join.filter(new FilterFunction<Tuple5<String,String,String,String,String>>() {
				@Override
				public boolean filter(Tuple5<String,String,String,String,String> tuple) {
					// Filter for United States
					return tuple.f4.contains("United States"); }    
				})
				.project(0,1,2,3,4);
	    
		
	 // Step 2 b)    
	    // output: scheduled_depar, actual_departure, name
	    DataSet<Tuple3<String,String,String>> USYearjoinresult = 
	    		USjoinresult.filter(new FilterFunction<Tuple5<String, String, String, String, String>>() {
	    		@Override
				public boolean filter(Tuple5<String, String, String,String, String> tuple) {
						// Filter for given year
					return tuple.f0.substring(0,4).equals(year); } 
		        }).project(1,2,3); 
	    
	    
    // Step 2 c)
	    //output:  name, delay_time
	    DataSet<Tuple2<String,Long>> joinresult =
	    		USYearjoinresult.filter(new FilterFunction<Tuple3<String,String,String>>() {
	                            public boolean filter(Tuple3<String,String,String> entry){
	                            	try {
	                            	return (format.parse(entry.f0).getTime() < format.parse(entry.f1).getTime());}  // filter only delayed flights
	                            	catch(ParseException e) {
	                            		System.out.println("Ignore the cancelled flight");
	                            		return false;
	                            	}
	                            } 
	                     }).flatMap(new TimeDifferenceMapper());
    
    

	    
	    
	    
	// Step 3
	    DataSet<Tuple2<String,Integer>> joinresult_num = joinresult.flatMap(new NumMapper())
	    	      .groupBy(0) 
	    	      .sum(1);  
	    
	    DataSet<Tuple3<String,Integer,Long>> joinresult_num_sum = joinresult.groupBy(0).sum(1)
	    		.join(joinresult_num).where(0).equalTo(0).projectSecond(0,1).projectFirst(1);
	    		
	    DataSet<Tuple4<String,Integer,Long,Long>> joinresult_num_sum_min = joinresult.groupBy(0).min(1)
	    		.join(joinresult_num_sum).where(0).equalTo(0).projectSecond(0,1,2).projectFirst(1);
	    
	    DataSet<Tuple5<String,Integer,Long,Long,Long>> joinresult_num_sum_min_max = joinresult.groupBy(0).max(1)
	    		.join(joinresult_num_sum_min).where(0).equalTo(0).projectSecond(0,1,2,3).projectFirst(1);
	    
	    
    // Step 4,5 
	    DataSet<Tuple5<String,Integer,Long,Long,Long>> finalresult = 
	    		joinresult_num_sum_min_max.flatMap(new AvgMapper())
	    		.sortPartition(0,Order.ASCENDING).setParallelism(1);
	 
	    //write out final result
	    finalresult.writeAsText(outputFilePath, WriteMode.OVERWRITE);   
	    
	    long startTime = System.currentTimeMillis();
	    
        // execute the FLink job
	    env.execute("Executing task 2 program");
	    
	    
	    long endTime = System.currentTimeMillis();
	    long timeTaken = endTime-startTime;
	    
	    String timeFilePath = params.get("output", PATH + "results/bad_Delay_time.txt");
	    BufferedWriter out = new BufferedWriter(new FileWriter(timeFilePath));
	    out.write("Time taken for execution was: " + timeTaken+"\n");
	    out.close();


	    
	}
	/**
	* Calculate delay time for each delay flight
	* After get the delay time, filter out the scheduled and actual departure time columns
	* View step 1 c)
	*/
	 private static class TimeDifferenceMapper implements FlatMapFunction<Tuple3<String,String,String>, Tuple2<String,Long>>{
	     SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
	          @Override
	          public void flatMap(Tuple3<String,String,String> input_tuple, Collector<Tuple2<String,Long>> out) throws ParseException { 
	        	  Long diff_min =(long) ((format.parse(input_tuple.f1).getTime()-format.parse(input_tuple.f0).getTime())/(60.0 * 1000.0) % 60.0);
	        	  if (diff_min>0) {
	        		  out.collect(new Tuple2<String,Long>(input_tuple.f2, diff_min)); 
	        	  }
		    }
		  }
	/**
	* Count the number of delay flights for each airline
	* View step 3
	*/	   
	  private static class NumMapper implements FlatMapFunction<Tuple2<String,Long>, Tuple2<String,Integer>> {
		    @Override
		    public void flatMap( Tuple2<String,Long> input_tuple, Collector<Tuple2<String,Integer>> out) {
		      out.collect(new Tuple2<String,Integer>(input_tuple.f0,1));
		    }
		  }
	/**
	* Calculate average delay time based on the count
	* View step 3
	*/
	  private static class AvgMapper implements FlatMapFunction<Tuple5<String,Integer,Long,Long,Long>, Tuple5<String,Integer,Long,Long,Long>> {
		    @Override
		    public void flatMap(Tuple5<String,Integer,Long,Long,Long> input_tuple, Collector<Tuple5<String,Integer,Long,Long,Long>> out) {
		    	Long avg = input_tuple.f2/input_tuple.f1;
		      out.collect(new Tuple5<String,Integer,Long,Long,Long>(input_tuple.f0,input_tuple.f1,avg,input_tuple.f3,input_tuple.f4));
		    }
		  }
}
