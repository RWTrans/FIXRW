package edu.tamu.aser.instrumentation;

import edu.tamu.aser.icb.*;
import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

public class Instrumentor {
    private static final String MCR_STRATEGY = "edu.tamu.aser.scheduling.strategy.MCRStrategy";
    private static final String DOT = ".";
    private static final String SLASH = "/";
    private static final String SEMICOLON = ";";

    private static final Set<String> pckgPrefixesToIgnore = new HashSet<String>();
    private static final Set<String> pckgsToIgnore = new HashSet<String>();
    private static final Set<String> classPrefixesToIgnore = new HashSet<String>();
    private static final Set<String> classesToIgnore = new HashSet<String>();
    private static final Set<String> pckgPrefixesToAllow = new HashSet<String>();
    private static final Set<String> pckgsToAllow = new HashSet<String>();
    private static final Set<String> classPrefixesToAllow = new HashSet<String>();
    private static final Set<String> classesToAllow = new HashSet<String>();

    public static String memModel;
    public static final String logClass = "edu/tamu/aser/icb/Log";
    public static final String INSTR_EVENTS_RECEIVER = "edu/tamu/aser/icb/InstrumentationEventsReceiver";
    private static Boolean debug;

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("[DEBUG] Instrumentor.premain started");
        
        MCRProperties mcrProps = MCRProperties.getInstance();
        storePropertyValues(mcrProps.getProperty(MCRProperties.INSTRUMENTATION_PACKAGES_IGNORE_PREFIXES_KEY), pckgPrefixesToIgnore);
        storePropertyValues(mcrProps.getProperty(MCRProperties.INSTRUMENTATION_PACKAGES_IGNORE_KEY), pckgsToIgnore);
        storePropertyValues(mcrProps.getProperty(MCRProperties.INSTRUMENTATION_CLASSES_IGNORE_PREFIXES_KEY), classPrefixesToIgnore);
        storePropertyValues(mcrProps.getProperty(MCRProperties.INSTRUMENTATION_CLASSES_IGNORE_KEY), classesToIgnore);
        storePropertyValues(mcrProps.getProperty(MCRProperties.INSTRUMENTATION_PACKAGES_ALLOW_PREFIXES_KEY), pckgPrefixesToAllow);
        storePropertyValues(mcrProps.getProperty(MCRProperties.INSTRUMENTATION_PACKAGES_ALLOW_KEY), pckgsToAllow);
        storePropertyValues(mcrProps.getProperty(MCRProperties.INSTRUMENTATION_CLASSES_ALLOW_PREFIXES_KEY), classPrefixesToAllow);
        storePropertyValues(mcrProps.getProperty(MCRProperties.INSTRUMENTATION_CLASSES_ALLOW_KEY), classesToAllow);
        System.out.println("[DEBUG] classes.allow value: " + mcrProps.getProperty(MCRProperties.INSTRUMENTATION_CLASSES_ALLOW_KEY));
        System.out.println("[DEBUG] classesToAllow size: " + classesToAllow.size());

        memModel = mcrProps.getProperty(MCRProperties.memModel_KEY);
        debug = Boolean.parseBoolean(mcrProps.getProperty("debug"));

        inst.addTransformer(new ClassFileTransformer() {
            public byte[] transform(ClassLoader loader,
                                    String className,
                                    Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) throws IllegalClassFormatException {
                try {
                    if (className == null) {
                        System.err.println("[Instrumentor] Null className for classBeingRedefined: " + classBeingRedefined);
                        return classfileBuffer;
                    }
                    if (shouldInstrumentClass(className)) {
                        if (debug) {
                            System.out.println("Instrumenting " + className);
                        }
                        return transformClass(loader, className, classfileBuffer);
                    }
                } catch (Throwable th) {
                    th.printStackTrace();
                    System.err.println("Instrument error: " + th.getMessage());
                    System.err.println("Class: " + className);
                }
                return classfileBuffer;
            }
        }, true);
        
        // Force retransform of pre-loaded classes
        try {
            Class<?> writerClass = Class.forName("edu.tamu.aser.rff.WriterThread");
            Class<?> readerClass = Class.forName("edu.tamu.aser.rff.ReaderThread");
            System.out.println("[DEBUG] WriterThread classloader: " + writerClass.getClassLoader());
            System.out.println("[DEBUG] Instrumentation: " + inst);
            inst.retransformClasses(writerClass, readerClass);
            System.out.println("[DEBUG] Retransformed WriterThread and ReaderThread");
        } catch (Exception e) {
            System.out.println("[DEBUG] Could not retransform: " + e);
            e.printStackTrace();
        }
    }
    
    private static byte[] transformClass(ClassLoader loader, String className, byte[] classfileBuffer) {
        final String strategy = MCRProperties.getInstance().getProperty(MCRProperties.SCHEDULING_STRATEGY_KEY);
        ClassReader classReader = new ClassReader(classfileBuffer);
        ClassWriter classWriter = new ExtendedClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);

        if (MCR_STRATEGY.equals(strategy)) {
            ClassVisitor classVisitor = new ClassVisitor(Opcodes.ASM5, classWriter) {};
            classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
            classfileBuffer = classWriter.toByteArray();
        } else {
            ThreadEventsClassTransformer threadEventsTransformer = new ThreadEventsClassTransformer(classWriter);
            classReader.accept(threadEventsTransformer, ClassReader.EXPAND_FRAMES);

            classReader = new ClassReader(classWriter.toByteArray());
            classWriter = new ExtendedClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
            SharedAccessEventsClassTransformer sharedAccessEventsTransformer = new SharedAccessEventsClassTransformer(classWriter);

            classReader.accept(sharedAccessEventsTransformer, ClassReader.EXPAND_FRAMES);

            classReader = new ClassReader(classWriter.toByteArray());
            classWriter = new ExtendedClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
            JUCEventsClassTransformer jucEventsTransformer = new JUCEventsClassTransformer(classWriter);
            classReader.accept(jucEventsTransformer, ClassReader.EXPAND_FRAMES);

            classReader = new ClassReader(classWriter.toByteArray());
            classWriter = new ExtendedClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
            FireEventsClassTransformer fireEventsTransformer = new FireEventsClassTransformer(classWriter);

            classReader.accept(fireEventsTransformer, ClassReader.EXPAND_FRAMES);

            classfileBuffer = classWriter.toByteArray();
        }
        return classfileBuffer;
    }

    private static boolean shouldInstrumentClass(String name) {
        // name is in slash format: edu/tamu/aser/rff/WriterThread
        int packageEnd = name.lastIndexOf(SLASH);
        String pckgName = packageEnd == -1 ? name : name.substring(0, packageEnd);

        for (String prefix : pckgPrefixesToIgnore) {
            if (pckgName.startsWith(prefix)) {
                return false;
            }
        }
        if (pckgsToIgnore.contains(pckgName)) {
            return false;
        }
        for (String prefix : classPrefixesToIgnore) {
            if (name.startsWith(prefix)) {
                return false;
            }
        }
        if (classesToIgnore.contains(name)) {
            return false;
        }

        for (String prefix : pckgPrefixesToAllow) {
            if (pckgName.startsWith(prefix)) {
                return true;
            }
        }
        if (pckgsToAllow.contains(pckgName)) {
            return true;
        }
        for (String prefix : classPrefixesToAllow) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        if (classesToAllow.contains(name)) {
            return true;
        }

        return false;
    }

    private static void storePropertyValues(String values, Set<String> toSet) {
        if (values != null) {
            String[] split = values.split(SEMICOLON);
            for (String val : split) {
                val = val.trim();
                if (!val.isEmpty()) {
                    toSet.add(val.replace(DOT, SLASH));
                    System.out.println("[DEBUG] Added to set: " + val.replace(DOT, SLASH));
                }
            }
        }
    }
}
