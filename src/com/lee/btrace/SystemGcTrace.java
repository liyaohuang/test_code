package com.lee.btrace;

import com.sun.btrace.annotations.*;  

import static com.sun.btrace.BTraceUtils.*;

@BTrace
public class SystemGcTrace {
	
	@OnMethod(
		clazz="java.lang.System",  
		method="gc",  
		location=@Location(Kind.ENTRY)
    )
    public static void traceGc(){  
		println("   stack: ");
	    jstack();
    }
	
}
