
/**
 * this code is for project1 d, 
 * when two functions' support and confidence both all bigger than the criterion, the pair is a goal pair. 
 * This way reduce the false positive bugs in (a)'s output.
 * The new bug is the type of bug : A , B should be pair. That means in the frequency of A equals to the frequency of B in a method,
 * if there are A and B and (A,B) is a pair that A,B must appear together 
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
		
		
		int support;
		int confidence;			
		if (args.length == 2 && (support = Integer.parseInt(args[0]))>=0 && (confidence = Integer.parseInt(args[1]))>=0 ){			
		}else{
			support = 3;// default value of support
			confidence = 65; // default value of confidence
		}
		
		String callGraph = readStdin();// read call graph from the stdin 
		
		
		
		Object[] maps = buildMap(callGraph);
		HashMap<String,Set<String>> map = (HashMap<String,Set<String>>)maps[0];
		HashMap<String,List<String>> mapDup = (HashMap<String,List<String>>)maps[1];
		calculation (mapDup, map, support,confidence);				
		
	}
	
		/**
	 * function: build map: key is the caller and value is a set which contains callees
	 * @param callGraph
	 * @return
	 */
	
	public static Object[] buildMap(String callGraph){
		boolean storeFlag = false;// a flag to store the map
		Map<String,Set<String>> map =  new HashMap<String, Set<String>>(); // build a callgraph's map whose value is a set, so callees should not be duplicated
		Map<String,List<String>> mapDup = new HashMap<String,List<String>>();//build a callgraph's map whose value is a list, so callees can be duplicated
		List<String> calleeList = new ArrayList<String>();
		List<String> graphArray;
		String caller = new String();// caller's name 
		Set<String> callee =  new HashSet<String>(); //callee's name
		Pattern p = Pattern.compile("(?<=\').+(?=\')"); //regular to find function's name between ' and '
		graphArray = new ArrayList<String>(Arrays.asList(callGraph.split("\n")));//string to ArrayList based on the "\n"
		graphArray.add("");//add a blank line to the last , adding this is for the following code to store, or the last node information will not be stored
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
						calleeList.add(calleeName);
					}
				}
			
			}else{
			//store map
				if (storeFlag == true ){
					Set calleeFinal = new HashSet();
					List<String> calleeListFinal = new ArrayList<String>();
					calleeFinal.addAll(callee);
					calleeListFinal.addAll(calleeList);
					map.put(caller, calleeFinal);	
					mapDup.put(caller, calleeListFinal);
					calleeList.clear();
					callee.clear(); // empty callee for next time
					storeFlag = false;
				}
			}
		}
		
		Object[] maps = new Object[2];
		maps[0] = map;
		maps[1] = mapDup;
		//return map;
		return maps;
	}
	
	/**
	 * function: calculate the number of pair of functions and the number of single function in the pair
	 * @param map
	 * @param support
	 * @param confidence
	 */
	public static void calculation (Map<String,List<String>> mapDup,Map<String,Set<String>> map, int support,int confidence){
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
		output( mapDup,map, bug, support, confidence );
		
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
		
		for(Map.Entry<Set<String>,Integer> entry : entrySet){
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
			
			if (supportCaculated>=support && Confidence1Caculated>=(float)confidence/100 && Confidence2Caculated>=(float)confidence/100){// when two functions' support and confidence both all bigger than the criterion, the pair is a goal pair
				String[] nature = new String[4];
				nature[0] = pairNameArray[0];//nature[0] is one in function 
				nature[1] = pairNameArray[1];//nature[1] is one not in function
				nature[2] = String.valueOf(supportCaculated);// int to string
				nature[3] = String.valueOf(Confidence1Caculated);//float to string
				bug.add(nature);
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
	public static void output(Map<String,List<String>> mapDup,Map<String,Set<String>> map,Set<String[]> bug, int support, int confidence ){
		NumberFormat nt = NumberFormat.getPercentInstance(); // float to percent
		nt.setMinimumFractionDigits(2);// float to percent
		String[] sort = new String[2];
		Set<Map.Entry<String,Set<String>>> entrySet = map.entrySet();
		for (String[] bugItem :bug){
			for (Map.Entry<String,Set<String>> entry : entrySet){
				if (!entry.getValue().contains(bugItem[1])&&entry.getValue().contains(bugItem[0])){
					sort[0] = bugItem[0];
					sort[1] = bugItem[1];
					Arrays.sort(sort);// nature sequence of pair function
					
					System.out.println("bug: "+bugItem[0]+" in "+entry.getKey()+", pair: ("+sort[0]+", "+sort[1]+"), "+"support: "+bugItem[2]+", confidence: "+nt.format(Float.valueOf(bugItem[3])));
				} else if (!entry.getValue().contains(bugItem[0])&&entry.getValue().contains(bugItem[1])){
					sort[0] = bugItem[0];
					sort[1] = bugItem[1];
					Arrays.sort(sort);// nature sequence of pair function
					
					System.out.println("bug: "+bugItem[1]+" in "+entry.getKey()+", pair: ("+sort[0]+", "+sort[1]+"), "+"support: "+bugItem[2]+", confidence: "+nt.format(Float.valueOf(bugItem[3])));
				} else if ((entry.getValue().contains(bugItem[0]) && entry.getValue().contains(bugItem[1]))){
					sort[0] = bugItem[0];
					sort[1] = bugItem[1];
					Arrays.sort(sort);// nature sequence of pair function					
					int pairName1Num = Collections.frequency(mapDup.get(entry.getKey()), sort[0]);
					int pairName2Num = Collections.frequency(mapDup.get(entry.getKey()), sort[1]);
					if(pairName1Num>pairName2Num){
						System.out.println("bug: "+sort[0]+" in "+entry.getKey()+", pair: ("+sort[0]+", "+sort[1]+"), "+sort[0] + "frequency: "+pairName1Num +", " + sort[1] +"frequency: " + pairName2Num);	
					}else if(pairName1Num<pairName2Num){
						System.out.println("bug: "+sort[1]+" in "+entry.getKey()+", pair: ("+sort[0]+", "+sort[1]+"), "+sort[1] + "frequency: "+pairName2Num +", " + sort[0] +"frequency: " + pairName1Num);	
					}
				}
				
			}
		}
	}
	
		
	
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
