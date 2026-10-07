package edu.tamu.aser.rff;

/**
 * Configuration for RFF.
 */
public class RFFConfig {
    // Timeout in milliseconds
    private long timeoutMillis = 300000; // 5 minutes
    
    // Maximum number of iterations
    private int maxIterations = 10000;
    
    // Maximum energy per schedule
    private int maxEnergy = 16;
    
    // Power schedule hyperparameters
    private double energyBeta = 2.0;
    private double energyGamma = 1.0;
    
    // Random seed for reproducibility
    private long randomSeed = -1; // -1 means random
    
    // Debug mode
    private boolean debug = false;
    
    // Getters and setters
    public long getTimeoutMillis() { return timeoutMillis; }
    public RFFConfig setTimeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
        return this;
    }
    
    public int getMaxIterations() { return maxIterations; }
    public RFFConfig setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
        return this;
    }
    
    public int getMaxEnergy() { return maxEnergy; }
    public RFFConfig setMaxEnergy(int maxEnergy) {
        this.maxEnergy = maxEnergy;
        return this;
    }
    
    public double getEnergyBeta() { return energyBeta; }
    public RFFConfig setEnergyBeta(double energyBeta) {
        this.energyBeta = energyBeta;
        return this;
    }
    
    public double getEnergyGamma() { return energyGamma; }
    public RFFConfig setEnergyGamma(double energyGamma) {
        this.energyGamma = energyGamma;
        return this;
    }
    
    public long getRandomSeed() { return randomSeed; }
    public RFFConfig setRandomSeed(long randomSeed) {
        this.randomSeed = randomSeed;
        return this;
    }
    
    public boolean isDebug() { return debug; }
    public RFFConfig setDebug(boolean debug) {
        this.debug = debug;
        return this;
    }
    
    /**
     * Get random seed (generates one if not set)
     */
    public long getEffectiveRandomSeed() {
        if (randomSeed == -1) {
            randomSeed = System.currentTimeMillis();
        }
        return randomSeed;
    }
}
