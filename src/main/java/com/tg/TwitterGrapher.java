package com.tg;

import io.vertx.core.Starter;

/**
 * Hello world!
 *
 */
public class TwitterGrapher 
{
	private static final String VERTICLE_CLASS = "com.tg.GraphServer";
	public static String search_value = null;
    public static void main( String[] args )
    {
    	args = ( args.length > 1 && args[0].equals("run") ) ? args : getCommandLineArguments(args);
    	
    	Starter.main(args);
    }
    
    public static String[] getCommandLineArguments(String[] args){
    	String[] commandLineArguments = new String[args.length+2];
    	commandLineArguments[0] = "run";
    	commandLineArguments[1] = VERTICLE_CLASS;
    	for(int i = 0; i< args.length; i++){
    		commandLineArguments[i+2] = args[i];
    		
    	}
    	return commandLineArguments;
    }
    
	
}
