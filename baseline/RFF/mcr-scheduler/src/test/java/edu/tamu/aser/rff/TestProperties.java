package edu.tamu.aser.rff;

import edu.tamu.aser.instrumentation.MCRProperties;

public class TestProperties {
    public static void main(String[] args) {
        MCRProperties props = MCRProperties.getInstance();
        System.out.println("strategy: " + props.getProperty("mcr.exploration.scheduling.strategy"));
        System.out.println("ignore.prefixes: " + props.getProperty("mcr.instrumentation.packages.ignore.prefixes"));
        System.out.println("allow.prefixes: " + props.getProperty("mcr.instrumentation.packages.allow.prefixes"));
    }
}
