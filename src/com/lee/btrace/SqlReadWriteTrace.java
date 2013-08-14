package com.lee.btrace;

import java.lang.reflect.Field;

import com.sun.btrace.annotations.*;  

import static com.sun.btrace.BTraceUtils.*;

//统计hibernate下的一些sql执行情况
@BTrace
public class SqlReadWriteTrace {
	/**
	 * 查询统计
	 * */
	private static long execStatementCount = 0l;  
	
	private static long execStatementTotalTime = 0l;
	
	private static long execPreparedStatementCount = 0l;
	
	private static long execPreparedStatementTotalTime = 0l;
	
	private static long execCount= 0l;                 //2分钟内的执行次数
	private static long execCount50To100= 0l;          //2分钟内的执行50MS-100MS次数
	private static long execCount100= 0l;              //2分钟内的执行100MS以上次数
	
	private static long execTimeCount= 0l;             //2分钟内的执行总时间
	
	@OnMethod(
		clazz="com.mysql.jdbc.Statement",  
		method="executeQuery",  
		location=@Location(Kind.RETURN)
    )
    public static void traceStatementQuery(String sql, @Duration long duration, @Return Object result){  
		execStatementCount++;
		execStatementTotalTime += duration;
		
		println("===================================== Statement executeQuery =================================");
		println(strcat("time(μs): ", str(duration/1000)));
		
	    //打印返回值
	    if(sql!=null)
	    	println(strcat("sql: ", str(sql)));
    }
	
	@OnMethod(  
		clazz="com.mysql.jdbc.PreparedStatement",  
		method="executeQuery",  
		location=@Location(Kind.RETURN)
    )   
    public static void tracePreparedStatementQuery(@Self Object self, @Duration long duration, @Return Object result){  
		execPreparedStatementCount++;
		execPreparedStatementTotalTime += duration;
		
		execCount += 1;
		execTimeCount += duration;
		if(duration >= 50000000 && duration < 100000000){  //超过[50-100)MS则打印
			execCount50To100++;
			
	    	Field sqlField = field("com.mysql.jdbc.PreparedStatement", "originalSql");
		    String sql = (String) get(sqlField, self);
		    println("===================================== between 50MS and 100MS sql =================================");
		    println(strcat("    date: ", str(timeMillis())));
		    println(strcat("time(μs): ", str(duration/1000)));
		    println(strcat("     sql: ", sql));
		    println("   stack: ");
		    jstack();
	    }else if(duration >= 100000000){  //大于等于100MS则打印
	    	execCount100++;
	    	
	    	Field sqlField = field("com.mysql.jdbc.PreparedStatement", "originalSql");
		    String sql = (String) get(sqlField, self);
		    println("==================================      more than 100MS sql      =================================");
		    println(strcat("    date: ", str(timeMillis())));
		    println(strcat("time(μs): ", str(duration/1000)));
		    println(strcat("     sql: ", sql));
		    println("   stack: ");
		    jstack();
	    }
    }
	
	/*@OnMethod(  
		clazz="com.mysql.jdbc.Connection",
		method="prepareStatement",  
		location=@Location(Kind.RETURN)
    )   
	public static void traceConnCreateStatement(String sql, @Duration long duration, @Return Object result){  
		execPreparedStatementCount++;
		execPreparedStatementTotalTime += duration;
		
		println("===================================== Connection create Preparestatement =================================");
	    println(strcat("time(μs): ", str(duration/1000)));
	    println(strcat("sql: ", sql));
    }*/
	
	private static long updateCount = 0l;
	
	private static long updateTimeCount = 0l;
	
	private static long updateTotalCount = 0l;
	
	private static long updateTimeTotalCount = 0l;
	
	/**
	 * 更新统计
	 * */
	@OnMethod(  
		clazz="com.mysql.jdbc.PreparedStatement",  
		method="executeUpdate",  
		location=@Location(Kind.RETURN)
    )   
    public static void traceUpdate(@Self Object self, @Duration long duration, @Return Object result){  
		updateCount++;
		updateTimeCount += duration;
		
		updateTotalCount += 1;
		updateTimeTotalCount += duration;
    }
    
    @OnTimer(60000)
    public static void print(){
    	println(strcat(strcat("==============================>  ", str(timeMillis()))," <============================"));
    	println("         ---------    query one minute    ----------- ");
    	long execCount50To100New = execCount50To100;
    	long execCount100New = execCount100;
    	long execCountNew = execCount;
    	long execTimeCountNew = execTimeCount;
    	
    	execCount50To100 =0l;
    	execCount100 = 0l;
    	execCount = 0l;
    	execTimeCount = 0l;
    	
    	if(execCountNew>0){
    		println((strcat("sql execCount                : ",str(execCountNew))));
    		println((strcat("sql qps                      : ",str(execCountNew/60))));
        	println((strcat("sql 50MS-100MS execCount     : ",str(execCount50To100New))));
        	println((strcat("sql more than 100MS execCount: ",str(execCount100New))));
        	println((strcat("sql avg time(μs)             : ",str(execTimeCountNew/execCountNew/1000))));
    	}else{
    		println("sql execCount                : 0");
    		println("sql qps                      : 0");
        	println("sql 50MS-100MS execCount     : 0");
        	println("sql more than 100MS execCount: 0");
        	println("sql avg time(μs)             : 0");
    	}
    	
    	println("         ---------   query total    ----------- ");
    	println(strcat("Statement totalCount: ",str(execStatementCount)));
    	if(execStatementCount>0)
    		println(strcat("Statement avg time(μs): ",str(execStatementTotalTime/execStatementCount/1000)));
    	else
    		println("Statement avg time: 0");
    	
    	println(strcat("PreparedStatement totalCount: ",str(execPreparedStatementCount)));
    	if(execPreparedStatementCount>0)
    		println(strcat("PreparedStatement avg time(μs): ",str(execPreparedStatementTotalTime/execPreparedStatementCount/1000)));
    	else
    		println("PreparedStatement avg time: 0");
    	
    	println(strcat("totalCount: ",str((execStatementCount+execPreparedStatementCount))));
    	if(execPreparedStatementCount>0 || execPreparedStatementCount>0)
    		println(strcat("avg time(μs): ",str((execStatementTotalTime+execPreparedStatementTotalTime)/(execStatementCount+execPreparedStatementCount)/1000)));
    	else
    		println("total avg time: 0");
    	
    	println("         ---------    write one minute    ----------- ");
    	long updateCountNew = updateCount;
    	long updateTimeCountNew = updateTimeCount;
    	
    	updateCount =0l;
    	updateTimeCount = 0l;
    	
    	if(updateCount>0){
    		println((strcat("sql execCount                : ",str(updateCountNew))));
    		println((strcat("sql tps                      : ",str(updateCountNew/60))));
        	println((strcat("sql avg time(μs)             : ",str(updateTimeCountNew/updateCountNew/1000))));
    	}else{
    		println("sql execCount                : 0");
        	println("sql tps                      : 0");
        	println("sql avg time(μs)             : 0");
    	}
    	
    	println("         ---------    write total    ----------- ");
    	if(updateTotalCount>0){
    		println((strcat("sql execCount                : ",str(updateTotalCount))));
        	println((strcat("sql avg time(μs)             : ",str(updateTimeTotalCount/updateTotalCount/1000))));
    	}else{
    		println("sql execCount                : 0");
        	println("sql avg time(μs)             : 0");
    	}
    }
}
