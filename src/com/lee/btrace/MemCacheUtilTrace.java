package com.lee.btrace;

import com.sun.btrace.annotations.*;  
import static com.sun.btrace.BTraceUtils.*;

@BTrace
public class MemCacheUtilTrace {
	private static long execCount = 0l;  
	
	private static long execTotalTime = 0l;
	   
	@OnMethod(  
		clazz="com.b5m.tao.common.util.XMemCachedUtil",  
		method="getCache",  
		location=@Location(Kind.RETURN)
    )   
    public static void traceExecute(String key, @Duration long duration, @Return Object result){  
		execCount++;
		execTotalTime += duration;
		
		println("====== return");
	    println(strcat("execute time: ", str(duration)));
	    
	    //打印返回值
	    println(strcat("result: ", str(result)));
	    println(strcat("key: ", str(key)));
    }
    
    @OnTimer(60000)
    public static void print(){
    	println("====== time");
    	println(strcat("totalCount: ",str(execCount)));
    	if(execCount>0)
    		println(strcat("avg time: ",str(execTotalTime/execCount)));
    }
}
