package edu.tamu.aser.scheduling.strategy;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.tamu.aser.StartExploring;
import edu.tamu.aser.config.Configuration;
import edu.tamu.aser.instrumentation.RVGlobalStateForInstrumentation;
import edu.tamu.aser.trace.AbstractNode;
import edu.tamu.aser.trace.IMemNode;
import edu.tamu.aser.trace.Trace;
import edu.tamu.aser.trace.TraceInfo;
import edu.tamu.aser.runtime.RVRunTime;
import edu.tamu.aser.scheduling.events.EventType;

public class MCRStrategy extends SchedulingStrategy {

	private Queue<List<String>> toExplore;
	public static List<Integer> choicesMade;
	public static List<String> schedulePrefix = new ArrayList<String>();
    private static Trace currentTrace;
	private boolean notYetExecutedFirstSchedule;
	private final static int NUM_THREADS = 10;
	private volatile static ExecutorService executor;
    private ThreadInfo previousThreadInfo;
    public static final Boolean fullTrace = false;  //default

	public static Map<String,Double> threadToProb=new HashMap<>();

	public static HashMap<String , List<Long>> threadTokeyPoints=new HashMap<>();


	public static Map<String,Long> threadSteps=new HashMap<>();




	private int count;
	public MCRStrategy() {
		count = 0;
	}


	/**
	 * before the execution
	 */
	@Override
	public void startingExploration() {
		this.toExplore = new ConcurrentLinkedQueue<List<String>>();
		this.notYetExecutedFirstSchedule = true;
		MCRStrategy.choicesMade = new ArrayList<Integer>();
		MCRStrategy.schedulePrefix = new ArrayList<String>();

		RVRunTime.currentIndex = 0;
		executor = Executors.newFixedThreadPool(NUM_THREADS);	

	}

	/**
	 * called before a new schedule starts
	 */
	@Override
	public void startingScheduleExecution() {
//		List<String> prefix = this.toExplore.poll();
//		if (!MCRStrategy.choicesMade.isEmpty()) {   // when not empty
//			MCRStrategy.choicesMade.clear();
//			MCRStrategy.schedulePrefix = new ArrayList<String>();
//			assert prefix != null;
//			MCRStrategy.schedulePrefix.addAll(prefix);
////			for (String choice : prefix) {
////				MCRStrategy.schedulePrefix.add(choice);
////			}
//		}
		
		RVRunTime.currentIndex = 0;
		RVRunTime.failure_trace.clear();
		initTrace();
		
        previousThreadInfo = null;
	}

	public static String selectKeyByProbability(SortedSet<? extends Object> objectChoices) {
		int sum=0;
		for (Object o :objectChoices){
			ThreadInfo info= (ThreadInfo) o;
			sum+=threadTokeyPoints.get(RVRunTime.threadTidNameMap.get(info.getThread().getId())).size();
		}
		threadToProb.clear();
		for (Object o :objectChoices){
			ThreadInfo info= (ThreadInfo) o;

//			ThreadInfo info= (ThreadInfo) o;
//			sum+=threadTokeyPoints.get(RVRunTime.threadTidNameMap.get(info.getThread().getId())).size();
			threadToProb.put(RVRunTime.threadTidNameMap.get(info.getThread().getId()),(double)threadTokeyPoints.get(RVRunTime.threadTidNameMap.get(info.getThread().getId())).size()/sum);
		}


//		int sum=0;
//		for (Long threadId:currentTrace.threadTokeyPoints.keySet()){
//
//			threadTokeyPoints.put(RVRunTime.threadTidNameMap.get(threadId),currentTrace.threadTokeyPoints.get(threadId));
//			sum+=currentTrace.threadTokeyPoints.get(threadId).size();
//		}
//		//修改后的
//		threadToProb.clear();
//		for (Long threadId:currentTrace.threadTokeyPoints.keySet()){
//
//			threadToProb.put(RVRunTime.threadTidNameMap.get(threadId), (double) ((double)currentTrace.threadTokeyPoints.get(threadId).size()/sum));
//		}


		// 计算所有概率的累积和
		double totalProbability = 0;
		for (double prob : threadToProb.values()) {
			totalProbability += prob;
		}

		// 生成一个随机数，范围在[0, totalProbability]之间
		double randomValue = Math.random() * totalProbability;

		// 遍历map，找到对应的key
		double cumulativeProbability = 0;
		for (Map.Entry<String, Double> entry : threadToProb.entrySet()) {
			cumulativeProbability += entry.getValue();
			if (randomValue <= cumulativeProbability) {
				return entry.getKey();
			}
		}

		return null;  // 仅在发生意外时执行，应该不需要。
	}
	
    public static Trace getTrace() {
        return currentTrace;
    }
    
    /* problem here
    * in the first execution, the initialized trace will be used by the aser-engine project
    * however, in the first initialization, the trace hasn't been complete yet.
    */
	private void initTrace() {
       RVRunTime.init();
       TraceInfo traceInfo = new TraceInfo(
                RVGlobalStateForInstrumentation.variableIdSigMap,
                new HashMap<Integer, String>(), 
                RVGlobalStateForInstrumentation.stmtIdSigMap,
                RVRunTime.threadTidNameMap);
       traceInfo.setVolatileAddresses(RVGlobalStateForInstrumentation.instance.volatilevariables);
       currentTrace = new Trace(traceInfo);
	}

	/**
	 * generate new schedules from the trace by this execution
	 */
	public void completedScheduleExecution() {
		this.notYetExecutedFirstSchedule = false;

		Vector<String> prefix = new Vector<String>();
		for (String choice : MCRStrategy.schedulePrefix) {
			prefix.add(choice);
		}

		if (Configuration.DEBUG) {
		    System.out.print("<< Exploring trace executed along causal schedule " + count + ": ");
	        count++;
	        System.err.println(choicesMade);
	        System.out.print("\n");
        }

		//executeMultiThread(trace, prefix);
		
		/*
		 * after executing the program along the given prefix
		 * then the model checker will analyze the trace generated 
		 * to computer more possible interleavings
		 */
//		System.out.println("执行完了一次");
		executeSingleThread(prefix);
		threadToProb.clear();//清除
		threadSteps.clear();
		threadTokeyPoints.clear();
		int sum=0;
		for (Long threadId:currentTrace.threadTokeyPoints.keySet()){
			threadTokeyPoints.put(RVRunTime.threadTidNameMap.get(threadId),currentTrace.threadTokeyPoints.get(threadId));
			sum+=currentTrace.threadTokeyPoints.get(threadId).size();
		}
		for (Long threadId:currentTrace.threadTokeyPoints.keySet()){
			threadToProb.put(RVRunTime.threadTidNameMap.get(threadId), (double) ((double)currentTrace.threadTokeyPoints.get(threadId).size()/sum));
		}
	}

	public static <T> T getRandomValue(List<T> values, List<Double> probabilities) {
		double rand = Math.random(); // 生成 [0,1) 之间的随机数
		double cumulativeProbability = 0.0;

		for (int i = 0; i < values.size(); i++) {
			cumulativeProbability += probabilities.get(i);
			if (rand < cumulativeProbability) {
				return values.get(i);
			}
		}
		return null; // 正常情况下不会触发
	}
	
	/**
	 * here creates a runnable object and it can then run the method 
	 * @param prefix
	 */

	private void executeSingleThread(Vector<String> prefix) {
	    
	    currentTrace.getTraceInfo().updateIdSigMap( RVGlobalStateForInstrumentation.stmtIdSigMap );   //solving the first trace initialization problem
		currentTrace.finishedLoading(true);
//		currentTrace.getSharedVariables();

//		currentTrace.getRawFullTrace();

//		for (AbstractNode node:currentTrace.getFullTrace()){
//			if (node instanceof IMemNode){
//				IMemNode rwNode= (IMemNode) node;
//				if (rwNode.getAddr()){
//
//				}
//			}
//		}

//		StartExploring causalTrace = new StartExploring(currentTrace, prefix, this.toExplore);
//		Thread causalTraceThread = new Thread(causalTrace);
//		causalTraceThread.start();
//		try {
//			causalTraceThread.join();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
	}

	@SuppressWarnings("unused")
    private void executeMultiThread(Trace trace, Vector<String> prefix) {

		StartExploring causalTrace = new StartExploring(trace, prefix,
				this.toExplore);
		StartExploring.executorsCount.increase();
		MCRStrategy.executor.submit(causalTrace);
	}

	@Override
	public boolean canExecuteMoreSchedules() {
		boolean result = (!this.toExplore.isEmpty())
				|| this.notYetExecutedFirstSchedule;
		if (!result) {
			while (StartExploring.executorsCount.getValue() > 0) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			result = (!this.toExplore.isEmpty())
					|| this.notYetExecutedFirstSchedule;
			return result;
		} else {
			return true;
		}

	}

	/**
	 * choose the next statement to execute
	 * this function needs more inspection
	 */
	@Override
	public Object choose(SortedSet<? extends Object> objectChoices, ChoiceType choiceType)
	{
		/*
		 * Initialize choice
		 */
		int chosenIndex = 0;
		Object chosenObject = null;
		//it might be that the wanted thread is blocked, waiting to be added to the paused threads
		if (threadTokeyPoints.size()==0){
			//第一次执行，没有概率
			chosenIndex = 0;
			while (true) {
				chosenObject = getChosenObject(chosenIndex, objectChoices);

				if(choiceType.equals(ChoiceType.THREAD_TO_FAIR)
						&& chosenObject.equals(previousThreadInfo))
				{
					//change to a different thread
				}
				else
					break;
				chosenIndex++;

			}
		}else {
			chosenIndex=ChosenBoredThread(objectChoices);
			if (chosenIndex==-1){
				String probThreadId= null;//selectKeyByProbability();
				probThreadId=selectKeyByProbability(objectChoices);
				chosenIndex=getChosenThread(objectChoices,probThreadId);
				threadSteps.put(probThreadId,threadSteps.getOrDefault((Object) probThreadId, 0L)+1);
				threadTokeyPoints.get(probThreadId).remove(0);
			}
		}
		MCRStrategy.choicesMade.add(chosenIndex);
		chosenObject = getChosenObject(chosenIndex, objectChoices);
		this.previousThreadInfo = (ThreadInfo) chosenObject;
		
		return chosenObject;
	}

	@Override
	public List<Integer> getChoicesMadeDuringThisSchedule() {
		return MCRStrategy.choicesMade;
	}
	
	
	/**
	 * chose a thread object based on the index
	 * return -1 if not found
	 * @param objectChoices set of object choices
//	 * @param index the given index
	 * @return return the index of chosen thread object
	 */
	private int getChosenThread(SortedSet<? extends Object> objectChoices, String thread) {
//		String name = thread;//TO-DO
		long tid = 0;//Long.parseLong(thread);//-1;
		for (Entry<Long, String> entry : RVRunTime.threadTidNameMap.entrySet()) {
			if (thread.equals(entry.getValue())) {
				tid = entry.getKey();
				break;
			}
		}

		Iterator<? extends Object> iter = objectChoices.iterator();
		int currentIndex = -1;
		while (iter.hasNext()) {
			++currentIndex;
			ThreadInfo ti = (ThreadInfo) iter.next();
			if (ti.getThread().getId() == tid) {
				return currentIndex;
			}
		}

		return -1;
	}
	private int ChosenBoredThread(SortedSet<? extends Object> objectChoices) {
//		String name = thread;//TO-DO
//		long tid = -1;
//		for (Entry<Long, String> entry : RVRunTime.threadTidNameMap.entrySet()) {
//			if (name.equals(entry.getValue())) {
//				tid = entry.getKey();
//				break;
//			}
//		}

		Iterator<? extends Object> iter = objectChoices.iterator();
		int currentIndex = -1;
		while (iter.hasNext()) {
			++currentIndex;
			ThreadInfo ti = (ThreadInfo) iter.next();
			String  nameId=RVRunTime.threadTidNameMap.get(ti.getThread().getId());
//			Long nameId=Long.parseLong(name);
			List<Long> keyPoints=threadTokeyPoints.getOrDefault(nameId,null);
			if (keyPoints==null||keyPoints.size()==0){
//				continue;
				return currentIndex;
			}
			if (threadSteps.getOrDefault(nameId, 0L)<keyPoints.get(0)){
//				if (threadSteps.containsKey(nameId)){
					threadSteps.put(nameId,threadSteps.getOrDefault(nameId, 0L)+1);
//				}else {
//					threadSteps.put(nameId,1);
//				}
				return currentIndex;
			} else if (threadSteps.getOrDefault(nameId, 0L).equals(keyPoints.get(0))) {
				//不执行
				continue;
			}

//
//			if (threadSteps.containsKey(nameId)){
//				threadSteps.put(nameId,threadSteps.get(nameId)+1);
//			}else {
//				threadSteps.put(nameId,1);
//			}
//			return currentIndex;
//
//			if (threadSteps.get(nameId)==currentTrace.threadTokeyPoints.get(nameId))
//			threadSteps.get();
//
//			if (ti.getThread().getId() == tid) {
//				return currentIndex;
//			}
		}

		return -1;
	}
}
