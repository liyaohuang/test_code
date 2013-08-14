package com.lee.btrace;

import java.util.concurrent.atomic.AtomicLong;

import com.sun.btrace.annotations.*;  

import static com.sun.btrace.BTraceUtils.*;

@BTrace
public class YouGoodsServiceSql {
	private static AtomicLong execCount = newAtomicLong(0l);  
	
	@OnMethod(clazz="com.b5m.tao.service.impl.YouGoodsServiceImpl", method="getDataFromCache",
		location=@Location(value=Kind.CALL, clazz="com.b5m.tao.common.util.XMemCachedUtil", method="setCache"))
    public static void traceExecute(){
	    addAndGet(execCount, 1);
	    println(strcat("execCount : ",str(execCount)));
    }
}
