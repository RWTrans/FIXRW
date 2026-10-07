package edu.tamu.aser.selective;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Selective scheduling based on execution trace analysis.
 * 
 * Two-phase approach:
 * Phase 1 (Trace Collection): Run once, collect all field access events
 * Phase 2 (Analysis): From configured lines, extract variables, find all matching events
 * Phase 3 (Scheduling): Only schedule events that match extracted variables
 */
public class TraceBasedSelectiveConfig {
    private static final String CONFIG_FILE = "selective_config.txt";
    private static final String TRACE_FILE = "trace_events.txt";
    
    // Phase 1: Trace collection
    private static boolean traceCollectionMode = false;
    private static List<TraceEvent> collectedTrace = new ArrayList<>();
    
    // Phase 2: Extracted variables from analysis
    // Key: "fieldOwner.fieldName"
    private static Set<String> interestedVariables = ConcurrentHashMap.newKeySet();
    
    // Phase 3: Scheduling mode
    private static boolean schedulingMode = false;
    
    private static boolean enabled = false;
    
    static {
        loadConfig();
    }
    
    static class TraceEvent {
        String owner;      // Class name: edu/tamu/aser/rff/CounterThread
        String fieldName;  // Field name: counter
        int lineNumber;    // Source line: 25
        boolean isRead;    // true=read, false=write
        long timestamp;    // Event order
        String threadName; // Which thread
        
        TraceEvent(String owner, String fieldName, int lineNumber, boolean isRead, String threadName) {
            this.owner = owner;
            this.fieldName = fieldName;
            this.lineNumber = lineNumber;
            this.isRead = isRead;
            this.threadName = threadName;
            this.timestamp = System.nanoTime();
        }
        
        String getVarKey() {
            return owner.replace("/", ".") + "." + fieldName;
        }
        
        @Override
        public String toString() {
            return String.format("%s:%d %s.%s %s by %s", 
                owner.replace("/", "."), lineNumber, 
                owner.substring(owner.lastIndexOf("/") + 1), fieldName,
                isRead ? "READ" : "WRITE", threadName);
        }
    }
    
    private static void loadConfig() {
        String configPath = System.getProperty("selective.config", CONFIG_FILE);
        File configFile = new File(configPath);
        
        if (!configFile.exists()) {
            System.out.println("[TraceBasedSelective] No config file, selective mode disabled");
            return;
        }
        
        // Check if analyzed variables file exists (from previous run)
        File analyzedFile = new File("interested_variables.txt");
        if (analyzedFile.exists()) {
            // Phase 3: Scheduling mode - load analyzed variables
            loadAnalyzedVariables();
            schedulingMode = true;
            enabled = true;
            System.out.println("[TraceBasedSelective] Scheduling mode: " + interestedVariables.size() + " variables");
        } else {
            // Phase 1: Trace collection mode
            traceCollectionMode = true;
            enabled = true;
            System.out.println("[TraceBasedSelective] Trace collection mode (first run)");
        }
    }
    
    /**
     * Called for every field access event during execution
     */
    public static boolean isInterested(String owner, String name, int lineNumber, boolean isRead) {
        if (!enabled) {
            return true; // Default: all events scheduled
        }
        
        if (traceCollectionMode) {
            // Phase 1: Collect all events
            collectedTrace.add(new TraceEvent(owner, name, lineNumber, isRead, 
                Thread.currentThread().getName()));
            return true; // Schedule everything in trace collection mode
        }
        
        if (schedulingMode) {
            // Phase 3: Only schedule if variable matches
            String varKey = owner.replace("/", ".") + "." + name;
            return interestedVariables.contains(varKey);
        }
        
        return true;
    }
    
    /**
     * End of execution - analyze trace if in collection mode
     */
    public static void onExecutionEnd() {
        if (!traceCollectionMode) {
            return;
        }
        
        System.out.println("\n[TraceBasedSelective] Execution ended, collected " + collectedTrace.size() + " events");
        
        // Save trace to file
        saveTrace();
        
        // Analyze trace: extract variables from configured locations
        analyzeTrace();
        
        // Save analyzed variables for next run
        saveAnalyzedVariables();
        
        System.out.println("[TraceBasedSelective] Analysis complete. Run again for selective scheduling.");
    }
    
    private static void saveTrace() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(TRACE_FILE))) {
            for (TraceEvent event : collectedTrace) {
                writer.println(event.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void analyzeTrace() {
        String configPath = System.getProperty("selective.config", CONFIG_FILE);
        Set<String> configuredLocations = new HashSet<>();
        
        // Read configured locations
        try (BufferedReader reader = new BufferedReader(new FileReader(configPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    configuredLocations.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        
        System.out.println("[TraceBasedSelective] Analyzing trace against " + configuredLocations.size() + " configured locations");
        
        // Find variables accessed at configured locations
        for (TraceEvent event : collectedTrace) {
            String className = event.owner.replace("/", ".");
            String loc1 = className + ":" + event.lineNumber;
            String loc2 = className + "." + event.fieldName + ":" + event.lineNumber;
            
            if (configuredLocations.contains(loc1) || configuredLocations.contains(loc2)) {
                String varKey = event.getVarKey();
                interestedVariables.add(varKey);
                System.out.println("  Extracted variable: " + varKey + " (from " + loc1 + ")");
            }
        }
        
        System.out.println("[TraceBasedSelective] Total extracted variables: " + interestedVariables.size());
    }
    
    private static void saveAnalyzedVariables() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("interested_variables.txt"))) {
            for (String var : interestedVariables) {
                writer.println(var);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void loadAnalyzedVariables() {
        try (BufferedReader reader = new BufferedReader(new FileReader("interested_variables.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                interestedVariables.add(line.trim());
            }
            System.out.println("[TraceBasedSelective] Loaded " + interestedVariables.size() + " interested variables:");
            for (String var : interestedVariables) {
                System.out.println("  " + var);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static boolean isEnabled() {
        return enabled;
    }
    
    public static boolean isTraceCollectionMode() {
        return traceCollectionMode;
    }
    
    public static boolean isSchedulingMode() {
        return schedulingMode;
    }
}
