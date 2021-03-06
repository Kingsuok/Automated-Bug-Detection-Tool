

//finally
/**
 *this code is for project C. this deals with the inter prodedural problem in the map first, 
 *then jude the bug as the rule. 
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class TestBug {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		int level = 1; // set the inter procedural level. initial value is 1; we can chang the value to implement different levels
		int support;
		int confidence;
		boolean interProcedural;
		if (args.length == 1 && args[0].equals("-intp")){
			support = 3;// default value of support
			confidence = 65; // default value of confidence
			interProcedural  = true;
		}else if (args.length == 2 && (support = Integer.parseInt(args[0]))>=0 && (confidence = Integer.parseInt(args[1]))>=0 ){
			interProcedural  = false;
		}else if (args.length == 3 && (support = Integer.parseInt(args[0]))>=0 && (confidence = Integer.parseInt(args[1]))>=0 && (interProcedural = args[2].equals("-intp")) ){
		}else{
			support = 3;// default value of support
			confidence = 65; // default value of confidence
			interProcedural  = false;
		}
		String callGraph = readStdin();// read call graph from the stdin 
		
		Map<String,Set<String>> map = buildMap(callGraph); 
		if (interProcedural == false){
			calculation ( map, support,confidence);	
		}else{
			bugProcess( map,support,confidence,level);
		}
		
	}
	
	
public static Map<String,Set<String>> buildMap(String callGraph){
		boolean storeFlag = false;// a flag to store the map
		Map<String,Set<String>> map =  new HashMap<String, Set<String>>(); // build a callgraph's map
		List<String> graphArray;
		String caller = new String();// caller's name 
		Set<String> callee =  new HashSet<String>(); //callee's name
		Pattern p = Pattern.compile("(?<=\').+(?=\')"); //regular to find function's name between ' and '
		graphArray = new ArrayList<String>(Arrays.asList(callGraph.split("\n")));
		graphArray.add("");
		for(String line : graphArray){
		
			String newLine = line.trim();// delete starting and ending space
			
			if (newLine.length()!=0){
				String[] lineToArray = newLine.split(" ");// line to array based on " "
			
				if (lineToArray[2].equals("node")  && lineToArray[3].equals("for")){// judge the caller's location
					Matcher m=p.matcher(lineToArray[5]); //match caller's name
					if(m.find()){    
						caller = m.group(); // find caller's name
						storeFlag = true;
						continue;
					}
				}
				if (storeFlag == true ){
				
					Matcher m=p.matcher(line);// match callee's name
					if(m.find()){    
						String calleeName = m.group(); // find callee's name
						callee.add(calleeName);
					}
				}
			
			}else{
			//store map
				if (storeFlag == true ){
					Set calleeFinal = new HashSet();
					calleeFinal.addAll(callee);	
					map.put(caller, calleeFinal);	
					callee.clear(); // empty callee for next time
					storeFlag = false;
				}
			}
		}
			return map;
		
	}
	/***************************The following is for no inter procedural process****************************/
	/**
	 * function: calculate the number of pair of functions and the number of single function in the pair
	 * @param map
	 * @param support
	 * @param confidence
	 */
	public static void calculation (Map<String,Set<String>> map, int support,int confidence){
		Map<Set<String>,Integer> pairFunction = new HashMap<Set<String>,Integer>();// key:pair of functions, value: the number that the pair functions are called together
		Map<String,Integer> singleFunction = new HashMap<String,Integer>();// key: single function ,value : the number that the single function is called 
		int firstNum = 1; // the number that pair functions and single function are called first separately 
		List<Set<String>> calleeList = new ArrayList(map.values()); //calleeList : a list of sets which contains callees 
		/*compute the number that each function is called */
		for (Set<String> calleeItem :calleeList){
			int size = calleeItem.size();
				String[] itemArray = calleeItem.toArray(new String[size]);// calleeItem to array
				
				for (int i = 0; i< size;i++){
					if (singleFunction.containsKey(itemArray[i])){ // judge whether single function had been in map  or not
						singleFunction.put(itemArray[i], singleFunction.get(itemArray[i])+1);
					}else{
						singleFunction.put(itemArray[i], firstNum); 
					}
				}
			}
		/*compute the number that every pair of  functions is called together */
		for (Set<String> calleeItem :calleeList){
			int size = calleeItem.size();
			String[] itemArray = calleeItem.toArray(new String[size]);
			if(size>1){ 
			for (int i=0; i<size;i++){
				for (int j =i+1; j<size;j++){
						Set<String> pairName = new HashSet<String>();// pairName : a set of a pair of functions
						pairName.add(itemArray[i]);
						pairName.add(itemArray[j]);
						if (pairFunction.containsKey(pairName)){
							pairFunction.put(pairName, pairFunction.get(pairName)+1);
						}else{
							pairFunction.put(pairName, firstNum);
						}
				}
			  }
			}
		}
		
		Set<String[]> bug = judge(pairFunction,singleFunction,support,confidence); 
		output( map, bug, support, confidence );
		
	}
	
	/**
	 * function:jude bug
	 * @param pair
	 * @param single
	 * @param support
	 * @param confidence
	 * @return
	 */
	
	public static Set<String[]> judge(Map<Set<String>,Integer> pair,Map<String,Integer> single,int support,int confidence){
		int supportCaculated, func1Num, func2Num;
		float func1Confidence,func2Confidence;
		Set<String[]> bug = new HashSet<String[]>();// bug: a set to store the bug 
		
		Set<Map.Entry<Set<String>,Integer>> entrySet = pair.entrySet();
		
		//for (Set<String> pairName : pair.keySet()){
		for(Map.Entry<Set<String>,Integer> entry : entrySet){
			//String[] pairNameArray = pairName.toArray(new String[2]);
			//supportCaculated = pair.get(pairName);
			String[] pairNameArray = entry.getKey().toArray(new String[2]);
			supportCaculated = entry.getValue();
			func1Num = single.get(pairNameArray[0]);
			func2Num = single.get(pairNameArray[1]);
			
			if (func1Num != 0){
				func1Confidence = (float)supportCaculated/func1Num;
				}else {
					func1Confidence = 0;
				}
				
			if (func2Num != 0){
				func2Confidence = (float)supportCaculated/func2Num;	
				}else{
					func2Confidence = 0;
				}
			/* 4 decimal places*/
			float Confidence1Caculated = (float)(Math.round(func1Confidence*10000))/10000;
			float Confidence2Caculated = (float)(Math.round(func2Confidence*10000))/10000;
			
			if (supportCaculated>=support && Confidence1Caculated>=(float)confidence/100){
				String[] nature = new String[4];
				nature[0] = pairNameArray[0];//nature[0] is one in function 
				nature[1] = pairNameArray[1];//nature[1] is one not in function
				nature[2] = String.valueOf(supportCaculated);// int to string
				nature[3] = String.valueOf(Confidence1Caculated);//float to string
				bug.add(nature);
			}
			
			if (supportCaculated>=support && Confidence2Caculated>=(float)confidence/100){
				String[] reverse = new String[4];
				reverse[0] = pairNameArray[1];//reverse[0] is one in function
				reverse[1] = pairNameArray[0];//reverse[0] is one in function
				reverse[2] = String.valueOf(supportCaculated);
				reverse[3] = String.valueOf(Confidence2Caculated);
				bug.add(reverse);
			}	
		}
		return bug;
	}
	
	/**
	 * function:output the result
	 * @param map
	 * @param bug
	 * @param support
	 * @param confidence
	 */
	public static void output(Map<String,Set<String>> map,Set<String[]> bug, int support, int confidence ){
		NumberFormat nt = NumberFormat.getPercentInstance(); // float to percent
		nt.setMinimumFractionDigits(2);// float to percent
		String[] sort = new String[2];
		Set<Map.Entry<String,Set<String>>> entrySet = map.entrySet();
		for (String[] bugItem :bug){
			//for (String caller : map.keySet()){//#################
			for (Map.Entry<String,Set<String>> entry : entrySet){
				if (!entry.getValue().contains(bugItem[1])&&entry.getValue().contains(bugItem[0])){
					sort[0] = bugItem[0];
					sort[1] = bugItem[1];
					Arrays.sort(sort);// nature sequence of pair function
					
					System.out.println("bug: "+bugItem[0]+" in "+entry.getKey()+", pair: ("+sort[0]+", "+sort[1]+"), "+"support: "+bugItem[2]+", confidence: "+nt.format(Float.valueOf(bugItem[3])));
				}
				
			}
		}
	}
		
	
	/***************************The following is for inter procedural process****************************/
	/*The main point : not like the no inter procedural process (it calculates all pairs's parameters and bugs together). 
	 *Inter procedural process calculates each pair's parameter and bug separately.
	 *Each pair will calculate a new map which map has no inter procedural for the pair. 
	 * 
	 */
	
	public static void bugProcess(Map<String,Set<String>> map,int support,int confidence,int level){
		Set<List<String>> pairs = new HashSet<List<String>>();// store the pairs of methods
		List<String> callerList = new ArrayList<String>(map.keySet());
		String[] sort = new String[2];
		List<Set<String>> calleeList = new ArrayList(map.values()); //calleeList : a list of sets which contains callees 
		// get all pairs of the test code
		for (Set<String> calleeItem :calleeList){
			int size = calleeItem.size();
			String[] itemArray = calleeItem.toArray(new String[size]);
			if(size>1){ 
			for (int i=0; i<size;i++){
				for (int j =i+1; j<size;j++){
					sort[0] = itemArray[i];
					sort[1] = itemArray[j];
					Arrays.sort(sort);// nature sequence of pair function
					List<String> aPair = new ArrayList<String>();
					aPair.add(sort[0]);
					aPair.add(sort[1]);
					pairs.add(aPair);
				}
			  }
			}
		}
	// implement judge of each pair
		Map<String,Set<String>> interMap;
		int[] parameter;
		List<String[]> bug;
		for (List<String> pairName : pairs){
			interMap = map;
			for (int i = 0 ;i < level; i++){			
			interMap = interProcedural(interMap,pairName);
			}
			parameter = calculationInter (interMap, pairName);
			bug = judgeInter( pairName, parameter,support,confidence);
			outputInter(interMap,pairName, bug );
		}
	}
	
/**
 * function: judge and implement inter procedural process
 * the most important part is whether a callee method is inter procedural method.
 * way: not considering all pairs together. just separated pairs, for one pair, we can know that if the callee is one of the pair,
 * the callee will not be replaced. if the callee is not , the callee will be repalced  
 * @param map
 * @return
 */
	public static Map<String,Set<String>> interProcedural(Map<String,Set<String>> map,List<String> pair){
	
		Set<String> calleeFunction;// = new HashSet<String>();
		calleeFunction = map.keySet();
		Map<String,Set<String>> intpMap = new HashMap<String,Set<String>>();
		Set<String> intersection = new HashSet<String>();
		Set<String> pairSet = new HashSet<String>(pair);
		Set<Map.Entry<String,Set<String>>> entrySet = map.entrySet();
		//for (String callerName : map.keySet()) {
		for (Map.Entry<String,Set<String>> entry : entrySet) {
			Set<String> newCallee = new HashSet<String>();
			newCallee.addAll(entry.getValue());
			for (String calleeName : entry.getValue()) {
				if (map.containsKey(calleeName)) {
					if (!calleeName.equals(pair.get(0)) && !calleeName.equals(pair.get(1))) {
						newCallee.remove(calleeName);
						newCallee.addAll(map.get(calleeName));
					}
				}
			}
			intpMap.put(entry.getKey(), newCallee);

		}
		return intpMap;
	}
	
	
	/**
	 * function: calculate the number of pair of functions and the number of single function in the pair
	 * @param map
	 * @param support
	 * @param confidence
	 */
	public static int[] calculationInter (Map<String,Set<String>> map, List<String> pairName){
		List<Set<String>> calleeList = new ArrayList(map.values()); //calleeList : a list of sets which contains callees 		
		int[] parameter = new int[3];// store the computed support and confidende
		parameter[0] = 0;
		parameter[1] = 0;
		parameter[2] = 0;
		for (Set<String> calleeItem: calleeList){
			if (calleeItem.contains(pairName.get(0)) && calleeItem.contains(pairName.get(1))){
				parameter[0] += 1;
				parameter[1] += 1;
				parameter[2] += 1;
			}else if(calleeItem.contains(pairName.get(0))){
				parameter[1] += 1;
			}else if (calleeItem.contains(pairName.get(1))){
				parameter[2] += 1;
			}
		}
		return parameter;
		
	}
	
	/**
	 * function:jude bug
	 * @param pair
	 * @param single
	 * @param support
	 * @param confidence
	 * @return
	 */
	
	public static List<String[]> judgeInter(List<String> pair, int[] parameter,int support,int confidence){
		int supportCaculated, func1Num, func2Num;
		float func1Confidence,func2Confidence;
		List<String[]> bug = new ArrayList<String[]>();// bug: a set to store the bug 
			supportCaculated = parameter[0];
			func1Num = parameter[1];
			func2Num = parameter[2];
			
			if (func1Num != 0){
				func1Confidence = (float)supportCaculated/func1Num;
				}else {
					func1Confidence = 0;
				}
				
			if (func2Num != 0){
				func2Confidence = (float)supportCaculated/func2Num;	
				}else{
					func2Confidence = 0;
				}
			/* 4 decimal places*/
			float Confidence1Caculated = (float)(Math.round(func1Confidence*10000))/10000;
			float Confidence2Caculated = (float)(Math.round(func2Confidence*10000))/10000;
			
			if (supportCaculated>=support && Confidence1Caculated>=(float)confidence/100){
				String[] nature = new String[4];
				nature[0] = pair.get(0);
				nature[1] = pair.get(1);
				nature[2] = String.valueOf(supportCaculated);// int to string
				nature[3] = String.valueOf(Confidence1Caculated);//float to string
				bug.add(nature);
			}
			
			if (supportCaculated>=support && Confidence2Caculated>=(float)confidence/100){
				String[] reverse = new String[4];
				reverse[0] = pair.get(1);
				reverse[1] = pair.get(0);
				reverse[2] = String.valueOf(supportCaculated);
				reverse[3] = String.valueOf(Confidence2Caculated);
				bug.add(reverse);
			}	
		
		return bug;
	}
	
	/**
	 * function:output the result
	 * @param map
	 * @param bug
	 * @param support
	 * @param confidence
	 */
	public static void outputInter(Map<String,Set<String>> map,List<String> pair,List<String[]> bug ){
		NumberFormat nt = NumberFormat.getPercentInstance(); // float to percent
		nt.setMinimumFractionDigits(2);// float to percent
		for (String[] bugItem :bug){
			for (String caller : map.keySet()){
				if (!map.get(caller).contains(bugItem[1])&&map.get(caller).contains(bugItem[0])){
					System.out.println("bug: "+bugItem[0]+" in "+caller+", pair: ("+pair.get(0)+", "+pair.get(1)+"), "+"support: "+bugItem[2]+", confidence: "+nt.format(Float.valueOf(bugItem[3])));
				}
				
			}
		}
	}
	/**********************************************************************************************************/
	/**
	 * function:read call graph from Stdin 
	 * @return
	 */
	public static String readStdin (){
		StringBuffer result = new StringBuffer();
		Scanner scanner = new Scanner(System.in);
	    while(scanner.hasNext()){
			result.append(scanner.nextLine());
			result.append("\n");
				}	       
	    return result.toString();
		}
}
