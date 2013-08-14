package com.lee.btrace;

import com.sun.btrace.annotations.*;  

import static com.sun.btrace.BTraceUtils.*;

@BTrace
public class MemCacheUtilGetTrace2 {
	private static double execCount = 0l;
	private static double execTime = 0l;
	private static double bingoCount = 0l;
	private static double bingoTime = 0l;
	
	private static double twoMinExecCount = 0l;
	private static double twoMinExecTime = 0l;
	private static double twoMinBingoCount = 0l;
	private static double twoMinBingoTime = 0l;
	
	@OnMethod(  
		clazz="com.b5m.tao.common.util.XMemCachedUtil",
		method="getCache",  
		location=@Location(Kind.RETURN)
    )   
    public static void traceExecute(String key, @Duration long duration, @Return Object result){
		execCount = execCount+1;
		execTime = execTime + duration;
		
		twoMinExecCount += 1;
		twoMinExecTime += duration;
		
		if(result!=null){
			bingoCount = bingoCount+1;
			bingoTime = bingoTime + duration;
			
			twoMinBingoCount += 1;
			twoMinBingoTime += 1;
			
		}
		
		//if(duration>10000000){
			String date = strcat("Date: ", str(timeMillis()));
			String dateKey = strcat(strcat(date,"      key:["), str(key));
			println(strcat(strcat(dateKey,"]           useTime(μs):"),str(duration/1000)));
		//}
    }
    
    @OnTimer(60000)
    public static void print(){
    	println(strcat(strcat("==============================>  ", str(timeMillis()))," <============================"));
    	double execCountNew = twoMinExecCount;
    	double execTimeNew = twoMinExecTime;
    	double bingoCountNew = twoMinBingoCount;
    	double bingoTimeNew = twoMinBingoTime;
    	
    	twoMinExecCount =0d;
    	twoMinExecTime = 0l;
    	twoMinBingoCount = 0l;
    	twoMinBingoTime = 0l;
    	
    	println(strcat("execCount: ", str(execCountNew)));
    	println(strcat("hasDataCount: ", str(bingoCountNew)));
    	if(execCountNew>0){
    		println(strcat("hasDataRate: ", str((bingoCountNew/execCountNew))));
        	println(strcat("avgTime(μs): ", str(execTimeNew/execCountNew/1000)));
    	}else{
    		println("hasDataRate: nodata");
        	println("avgTime(μs): nodata");
    	}
    	if(bingoCountNew>0){
    		println(strcat("avgHasDataTime(μs): ", str(bingoTimeNew/bingoCountNew/1000)));
    	}else{
    		println("avgHasDataTime(μs): nodata");
    	}
    	println(strcat("qps: ", str(execCountNew/60)));
    	
    	println("         ---------    total    ----------- ");
    	println(strcat("execCount: ", str(execCount)));
    	println(strcat("hasDataCount: ", str(bingoCount)));
    	
    	if(execCount>0){
    		println(strcat("hasDataRate: ", str((bingoCount/execCount))));
        	println(strcat("avgTime(μs): ", str(execTime/execCount/1000)));
    	}else{
    		println("hasDataRate: nodata");
        	println("avgTime(μs): nodata");
    	}
    	if(bingoCount>0){
    		println(strcat("avgHasDataTime(μs): ", str(bingoTime/bingoCount/1000)));
    	}else{
    		println("avgHasDataTime(μs): nodata");
    	}
    }
}
