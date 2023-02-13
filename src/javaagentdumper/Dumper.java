/*
 * Ну вы же понимаете, что код здесь только мой?
 * Well, you do understand that the code here is only mine?
 */

package javaagentdumper;

import java.io.*;
import java.lang.instrument.*;
import java.security.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author LWJGL2
 */
public class Dumper {

    public static ZipOutputStream out;
    public static Map<String, ZipEntry> addedEntries = new HashMap<>();

    public static int count = 0;

    public static void premain(String agentArgs, Instrumentation inst) throws Exception {
        out = new ZipOutputStream(new FileOutputStream("dumped.jar"));

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("********************************************************************************************************");
                System.out.println("END STREAM");
                System.out.println("********************************************************************************************************");
                
                try {
                    out.close();
                } catch (IOException ex) {
                    Logger.getLogger(Dumper.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }));
        System.out.println("agent loaded");

        // Register our Transformer
        inst.addTransformer(new Transformer());
    }

    public static class Transformer implements ClassFileTransformer {
        // The transform method is called for each non-system class as they are being loaded  

        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            if (className != null) {
                // Skip all system classes
                if (!className.startsWith("java") &&
                        !className.startsWith("sun") &&
                        !className.startsWith("javax") &&
                        !className.startsWith("com") &&
                        !className.startsWith("jdk") &&
                        !className.startsWith("org")) {
                    System.out.println("Dumping class: " + className);
                    // Replace all separator charactors
                    String newName = className.replaceAll("/", ".") + ".class";
                    try {
                        String name = className + ".class";
                        if (addedEntries.containsKey(name)) {
                            name = className + (count++) + ".class";
                        }

                        ZipEntry entry = new ZipEntry(name);
                        addedEntries.put(name, entry);
                        out.putNextEntry(entry);
                        out.write(classfileBuffer, 0, classfileBuffer.length);
                        out.closeEntry();
                    } catch (Throwable ex) {
                        System.out.println("Exception while writing: " + newName);
                        ex.printStackTrace(); 
                    }
                }
            }
            // We are not modifying the bytecode in anyway, so return it as-is
            return classfileBuffer;
        }
    }
}
