package com.lee.btrace;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicLong;

import com.sun.btrace.BTraceUtils;
import com.sun.btrace.annotations.*;  

import static com.sun.btrace.BTraceUtils.*;

//统计hibernate下的一些sql查询执行情况   分支下修改  再次修改  主干修改
@BTrace
public class QuerySqlTrace {
	private static long execStatementCount = 0l;  
	
	private static long execStatementTotalTime = 0l;
	
	private static long execPreparedStatementCount = 0l;
	
	private static long execPreparedStatementTotalTime = 0l;
	
	private static long execCount30To50= 0l;           //1分钟内的执行30MS-50MS次数
	private static long execCount10To30= 0l;           //1分钟内的执行30MS-50MS次数
	private static long execCount50To100= 0l;          //1分钟内的执行50MS-100MS次数
	private static long execCount100= 0l;              //1分钟内的执行100MS以上次数
	
	private static AtomicLong execCount= BTraceUtils.newAtomicLong(0l);                 //1分钟内的执行次数
	private static long execTimeCount= 0l;             //1分钟内的执行总时间
	
	@OnMethod(
		clazz="com.mysql.jdbc.Statement",  
		method="executeQuery",  
		location=@Location(Kind.RETURN)
    )
    public static void traceStatement(String sql, @Duration long duration, @Return Object result){  
		//Hibernate基本没有调用这个方法
		execStatementCount++;
		execStatementTotalTime += duration;
		
		BTraceUtils.addAndGet(execCount, 1);
		execTimeCount += duration;
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
    public static void tracePreparedStatement(@Self Object self, @Duration long duration, @Return Object result){  
		execPreparedStatementCount++;
		execPreparedStatementTotalTime += duration;
		
		BTraceUtils.addAndGet(execCount, 1);
		execTimeCount += duration;
		if(duration >= 10000000 && duration < 30000000){
			execCount10To30++;
			
			Field sqlField = field("com.mysql.jdbc.PreparedStatement", "originalSql");
		    String sql = (String) get(sqlField, self);
		    println("===================================== between 10MS and 30MS sql =================================");
		    println(strcat("    date: ", str(timeMillis())));
		    println(strcat("time(μs): ", str(duration/1000)));
		    println(strcat("     sql: ", sql));
		    println("   stack: ");
		    jstack();
		}else if(duration >= 30000000 && duration < 50000000){
			execCount30To50++;
			
			Field sqlField = field("com.mysql.jdbc.PreparedStatement", "originalSql");
		    String sql = (String) get(sqlField, self);
		    println("===================================== between 30MS and 50MS sql =================================");
		    println(strcat("    date: ", str(timeMillis())));
		    println(strcat("time(μs): ", str(duration/1000)));
		    println(strcat("     sql: ", sql));
		    println("   stack: ");
		    jstack();
		}else if(duration >= 50000000 && duration < 100000000){  //超过[50-100)MS则打印
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
	
    @OnTimer(60000)
    public static void print(){
    	println(strcat(strcat("==============================>  ", str(timeMillis()))," <============================"));
    	long execCount10To30New = execCount10To30;
    	long execCount30To50New = execCount30To50;
    	long execCount50To100New = execCount50To100;
    	long execCount100New = execCount100;
    	AtomicLong execCountNew = execCount;
    	long execTimeCountNew = execTimeCount;
    	
    	execCount50To100 =0l;
    	execCount100 = 0l;
    	execCount = BTraceUtils.newAtomicLong(0l);
    	execTimeCount = 0l;
    	
    	if(BTraceUtils.get(execCountNew)>0){
    		println((strcat("sql execCount                : ",str(BTraceUtils.get(execCountNew)))));
    		println((strcat("sql 10MS-30MS execCount      : ",str(execCount10To30New))));
    		println((strcat("sql 30MS-50MS execCount      : ",str(execCount30To50New))));
        	println((strcat("sql 50MS-100MS execCount     : ",str(execCount50To100New))));
        	println((strcat("sql more than 100MS execCount: ",str(execCount100New))));
        	println((strcat("sql qps                      : ",str(BTraceUtils.get(execCountNew)/60))));
        	println((strcat("sql avg time(μs)             : ",str(execTimeCountNew/BTraceUtils.get(execCountNew)/1000))));
    	}else{
    		println("sql execCount                : 0");
    		println("sql 10MS-30MS execCount      : 0");
    		println("sql 30MS-50MS execCount      : 0");
        	println("sql 50MS-100MS execCount     : 0");
        	println("sql more than 100MS execCount: 0");
        	println("sql avg time(μs)             : 0");
    	}
    	
    	println("         ---------    total    ----------- ");
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
    }
}
