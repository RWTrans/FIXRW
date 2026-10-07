package edu.tamu.aser.selective;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Selective scheduling configuration.
 * 
 * Logic:
 * 1. Read specified lines from config file: ClassName:lineNumber
 * 2. Extract variables accessed at those lines (from runtime events)
 * 3. Any field access to those variables ANYWHERE in the program is "interested"
 */
public class SelectiveConfig {
    private static final String CONFIG_FILE = "selective_config.txt";
    
    // Store ClassName:lineNumber from config
    private static Set<String> configuredLocations = new HashSet<>();
    
    // Store variables extracted from configured locations
    // Key: "fieldOwner.fieldName", Value: set of line numbers where first seen
    private static Map<String, Set<Integer>> interestedVariables = new HashMap<>();
    
    private static boolean enabled = false;
    private static boolean extractionPhase = true; // Still extracting variables
    
    static {
        loadConfig();
    }
    
    private static void loadConfig() {
        String configPath = System.getProperty("selective.config", CONFIG_FILE);
        try (BufferedReader reader = new BufferedReader(new FileReader(configPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    configuredLocations.add(line);
                }
            }
            enabled = !configuredLocations.isEmpty();
            if (enabled) {
                System.out.println("[SelectiveConfig] Loaded " + configuredLocations.size() + " configured locations");
                System.out.println("[SelectiveConfig] Will extract variables from these locations and track them globally");
            }
        } catch (IOException e) {
            System.out.println("[SelectiveConfig] Config file not found: " + configPath + ", selective mode disabled");
        }
    }
    
    /**
     * Check if a field access is of interest.
     * 
     * Two-phase logic:
     * Phase 1 (extraction): Check if this event matches a configured location.
     *                     If yes, extract the variable and mark as interested.
     * Phase 2 (tracking): Check if this variable was extracted in phase 1.
     * 
     * @param owner Class name (e.g., "edu/tamu/aser/rff/CounterThread")
     * @param name Field name
     * @param lineNumber Source line number
     * @return true if this access should be scheduled
     */
    public static boolean isInterested(String owner, String name, int lineNumber) {
        if (!enabled) {
            return true; // Default: all events are interesting
        }
        
        // Convert owner format: edu/tamu/aser/rff/CounterThread -> edu.tamu.aser.rff.CounterThread
        String className = owner.replace("/", ".");
        String varKey = className + "." + name;
        
        // Phase 2: Check if this variable was already extracted from a configured location
        if (interestedVariables.containsKey(varKey)) {
            return true;
        }
        
        // Phase 1: Check if this is a configured location, extract variable
        // Only extract if the field name matches the configured line's variable
        String loc1 = className + ":" + lineNumber;
        String loc2 = className + "." + name + ":" + lineNumber;
        
        if (configuredLocations.contains(loc2)) {
            // Exact match: ClassName.fieldName:lineNumber
            interestedVariables.computeIfAbsent(varKey, k -> new HashSet<>()).add(lineNumber);
            return true;
        }
        
        if (configuredLocations.contains(loc1)) {
            // Location match: ClassName:lineNumber
            // Extract all variables accessed at this configured location
            if (!interestedVariables.containsKey(varKey)) {
                interestedVariables.computeIfAbsent(varKey, k -> new HashSet<>()).add(lineNumber);
                return true;
            }
            // Variable already extracted, check if it's from this location
            return interestedVariables.get(varKey).contains(lineNumber);
        }
        
        return false;
    }
    
    /**
     * Check if selective mode is enabled
     */
    public static boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Get status for debugging
     */
    public static String getStatus() {
        if (!enabled) {
            return "Selective mode disabled";
        }
        return "Configured locations: " + configuredLocations.size() + 
               ", Extracted variables: " + interestedVariables.size();
    }
    
    /**
     * Print extracted variables (for debugging)
     */
    public static void printExtractedVariables() {
        if (!enabled) return;
        System.out.println("[SelectiveConfig] Extracted variables:");
        for (Map.Entry<String, Set<Integer>> entry : interestedVariables.entrySet()) {
            System.out.println("  " + entry.getKey() + " (lines: " + entry.getValue() + ")");
        }
    }
}
