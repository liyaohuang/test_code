package com.lee.btrace;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicLong;

import com.sun.btrace.BTraceUtils;
import com.sun.btrace.annotations.*;  

import static com.sun.btrace.BTraceUtils.*;

//统计hibernate下的一些sql查询执行情况
@BTrace
public class QuerySqlTraceAtomic {
	private static AtomicLong execPreparedStatementCount = BTraceUtils.newAtomicLong(0l);
	
	private static AtomicLong execPreparedStatementTotalTime = BTraceUtils.newAtomicLong(0l);
	
	private static AtomicLong execCount50 = BTraceUtils.newAtomicLong(0l);          //1分钟内的执行超过50MS次数
	
	@OnMethod(  
		clazz="com.mysql.jdbc.PreparedStatement",  
		method="executeQuery",  
		location=@Location(Kind.RETURN)
    )   
    public static void tracePreparedStatement(@Self Object self, @Duration long duration, @Return Object result){  
		BTraceUtils.addAndGet(execPreparedStatementCount, 1);
		BTraceUtils.addAndGet(execPreparedStatementTotalTime, duration);
		
		if(duration >= 50000000){
			BTraceUtils.addAndGet(execCount50, 1);
			
	    	Field sqlField = field("com.mysql.jdbc.PreparedStatement", "originalSql");
		    String sql = (String) get(sqlField, self);
		    println("===================================== more than 50MS sql =================================");
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
    	
    	println(strcat("PreparedStatement totalCount: ",str(execPreparedStatementCount)));
    	if(get(execPreparedStatementCount)>0)
    		println(strcat("PreparedStatement avg time(μs): ",str(get(execPreparedStatementTotalTime)/get(execPreparedStatementCount)/1000)));
    	else
    		println("PreparedStatement avg time: 0");
    }
}
