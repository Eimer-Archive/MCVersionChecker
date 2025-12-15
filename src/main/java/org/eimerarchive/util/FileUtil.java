package org.eimerarchive.util;

import com.sun.tools.javac.Main;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.Scanner;

public class FileUtil {
    private FileUtil() {}

    public static void getBukkitVersion(File file) throws IOException, NoSuchAlgorithmException {
        System.out.println("Processing File: " + file.getName());

        // Loads the .jar file into a ClassLoader so it can be checked for classes
        ClassLoader classLoader = URLClassLoader.newInstance(new URL[]{URI.create("file:" + file.getAbsolutePath()).toURL()}, Main.class.getClassLoader());
        String fullCraftBukkitVersion;
        String minecraftServerVersion = null;
        String craftbukkitVersion;
        String gitHash = null;
        int jenkinsBuild = -1;

        Properties props = new Properties();
        props.load(classLoader.getResourceAsStream("META-INF/maven/org.bukkit/craftbukkit/pom.properties"));
        craftbukkitVersion = props.getProperty("version");
        Class<?> craftbukkitMainClass = getClass("org.bukkit.craftbukkit.Main", classLoader);
        // Class<?> os = getClass("joptsimple.OptionSet");
        Class<?> minecraftServerClass = getClass("net.minecraft.server.MinecraftServer", classLoader);
        if (minecraftServerClass == null) {
            ClassReader craftbukkitMainClassReader = new ClassReader(craftbukkitMainClass.getResourceAsStream(craftbukkitMainClass.getSimpleName() + ".class"));
            ClassNode craftbukkitMainClassNode = new ClassNode();
            craftbukkitMainClassReader.accept(craftbukkitMainClassNode, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            for (MethodNode mn : craftbukkitMainClassNode.methods) {
                if (!mn.name.equals("main")) {
                    continue;
                }

                for (int i = 0; i < mn.instructions.size(); i++) {
                    if (!(mn.instructions.get(i) instanceof MethodInsnNode insn) || !insn.owner.contains("MinecraftServer")) {
                        continue;
                    }

                    minecraftServerClass = getClass(insn.owner.replace('/', '.'), classLoader);
                }
            }
        }

        fullCraftBukkitVersion = craftbukkitMainClass == null ? "" : craftbukkitMainClass.getPackage().getImplementationVersion();
        if (fullCraftBukkitVersion == null) {
            fullCraftBukkitVersion = "";
        }

        ClassReader minecraftServerClassReader = new ClassReader(minecraftServerClass.getResourceAsStream(minecraftServerClass.getSimpleName() + ".class"));
        ClassNode minecraftServerClassNode = new ClassNode();
        minecraftServerClassReader.accept(minecraftServerClassNode, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        MethodNode minecraftServerClassMethodNode = null;
        for (MethodNode node : minecraftServerClassNode.methods) {
            if (node.name.equals("getVersion")) {
                minecraftServerClassMethodNode = node;
                break;
            }
        }

        if (minecraftServerClassMethodNode != null) {
            //System.out.println("Got MinecraftServer.getVersion()!");
            for (int i = 0; i < minecraftServerClassMethodNode.instructions.size(); i++) {
                AbstractInsnNode instr = minecraftServerClassMethodNode.instructions.get(i);
                if (!(instr instanceof  LdcInsnNode ldcn) || !(ldcn.cst instanceof String string)) {
                    continue;
                }

                minecraftServerVersion = string;
                //System.out.println("MinecraftServer.getVersion() => " + msv);
            }
        } else {
            //System.out.println("MinecraftServer.getVersion() not found, trying workaround...");
            boolean isMinecraftServerVersionFound = false;
            for (MethodNode minecraftServerNode : minecraftServerClassNode.methods) {
                if (isMinecraftServerVersionFound) {
                    break;
                }
                //System.out.println("Got MinecraftServer.init()!");
                for (int i = 0; i < minecraftServerNode.instructions.size(); i++) {
                    AbstractInsnNode instr = minecraftServerNode.instructions.get(i);
                    if (!(instr instanceof LdcInsnNode ldcn) || !(ldcn.cst instanceof String string) || !string.contains("version")) {
                        continue;
                    }

                    //System.out.println("MinecraftServer." + msmn.name + "() => " + s);
                    minecraftServerVersion = string.split("version ")[1];
                }
            }
        }

        String[] parts = fullCraftBukkitVersion.split("-");
        String githubCommitHash = null;
        String buildNumber;
        for (String part : parts) {
            if (part.equals("git")) {
                continue;
            }
            if (part.startsWith("g") && part.length() == 8) {
                githubCommitHash = part.substring(1);
            }
            if (part.startsWith("b")) {
                buildNumber = part;
                if (part.endsWith("jnks")) {
                    jenkinsBuild = Integer.parseInt(part.substring(1, part.length() - 4));
                }
            }
        }
        if (githubCommitHash != null) {
            try (InputStream inputStream = Runtime.getRuntime().exec("git log -1 --pretty=format:%H " + githubCommitHash, null, new File("./CraftBukkit")).getInputStream();
                 Scanner s = new Scanner(inputStream).useDelimiter("\\A")) {
                gitHash = s.next();
            } catch (Exception e) {
                gitHash = "";
            }
        }

        System.out.println("Build:" + (jenkinsBuild > 0 ? Integer.toString(jenkinsBuild) : "???"));
        System.out.println("CraftBukkit: " + craftbukkitVersion);
        System.out.println("MinecraftServer: " + minecraftServerVersion);
        System.out.println("CraftBukkit: " + fullCraftBukkitVersion);
        System.out.println("Git hash: " + gitHash);
        System.out.println("MD5: " + getFileChecksum(MessageDigest.getInstance("MD5"), file));
        System.out.println("SHA1: " + getFileChecksum(MessageDigest.getInstance("SHA1"), file));
    }

    private static String getFileChecksum(MessageDigest digest, File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            // Create byte array to read data in chunks
            byte[] byteArray = new byte[1024];

            // Read file data and update in message digest
            int bytesCount;
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
            byte[] bytes = digest.digest();

            // The bytes[] is in decimal format, so convert it to hexadecimal format
            StringBuilder builder = new StringBuilder();
            for (byte aByte : bytes) {
                builder.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }

            return builder.toString().toUpperCase();
        } catch (IOException e) {
            System.err.println("Unable to calculate file hash: " + e.getMessage());
            return null;
        }
    }

    public static Class<?> getClass(String name, ClassLoader classLoader) {
        try {
            return Class.forName(name, true, classLoader);
        } catch (ClassNotFoundException e) {
            System.err.println("Unable to load class: " + e.getMessage());
            return null;
        }
    }

    public static Method getMethod(Class<?> c, String name) {
        try {
            return c.getMethod(name);
        } catch (NoSuchMethodException | SecurityException e) {
            System.err.println("Unable to get method: " + e.getMessage());
            return null;
        }
    }
}
