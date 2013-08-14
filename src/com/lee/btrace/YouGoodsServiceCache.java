package com.lee.btrace;

import com.sun.btrace.annotations.*;  
import static com.sun.btrace.BTraceUtils.*;

@BTrace
public class YouGoodsServiceCache {
	private static long execCount = 0l;  
	
	private static long execTotalTime = 0l;
	
	@TLS
	private static long startTime = 0;
    
    @OnMethod(  
    	clazz="com.b5m.tao.service.impl.YouGoodsServiceImpl",  
    	method="getDataFromCache"  
	)
  	public static void startExecute(){  
    	println("====== enter");
	    startTime = timeMillis();
	    
	    //println("who call com.b5m.tao.service.impl.YouGoodsServiceImpl.getDataFromCache :");
	    //jstack();
   	}
	   
	@OnMethod(  
		clazz="com.b5m.tao.service.impl.YouGoodsServiceImpl",  
		method="getDataFromCache",  
		location=@Location(Kind.RETURN)  
    )   
    public static void traceExecute(@Duration long duration, @Return Object result){  
		execCount++;
		
		long time = timeMillis() - startTime;  //time统计方法调用时间，毫秒级别，结果1000表示1000毫秒
		execTotalTime += time;
		
		println("====== return");
	    println(strcat("execute time: ", str(time)));
	    println(strcat("duration: ", str(duration)));   //duration  方法耗费时间，是us级别
	    
	    //打印返回值
	    println(str(result));
    }   
    
    @OnTimer(60000)
    public static void print(){
    	println("====== time");  
    	println(strcat("totalCount: ",str(execCount)));
    	println(strcat(strcat("totalTime: ",str(execTotalTime)), "ms"));
    	if(execCount>0)
    		println(strcat("avg time: ",str(execTotalTime/execCount)));
    }
}
