package com.lee.btrace;

import com.sun.btrace.annotations.*;  

import static com.sun.btrace.BTraceUtils.*;

@BTrace
public class YouGoodsDaoTrace {
	@OnMethod(clazz="com.b5m.tao.dao.impl.YouGoodsDaoImpl", method="/.*/",
		location=@Location(value=Kind.ENTRY))
    public static void traceExecute(@ProbeClassName String probeClass, @ProbeMethodName String probeMethod){
	    println(strcat(strcat(probeClass, "."),probeMethod));  //只监控类里面方法，不包含父类方法
    }
}
