package org.eimerarchive;

import com.sun.tools.javac.Main;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.Scanner;

public class FileUtil {

    public static ClassLoader classLoader;

    public static void getBukkitVersion(File file) throws IOException, NoSuchAlgorithmException {
        System.out.println("\nFile: " + file.getName());
        classLoader = URLClassLoader.newInstance(new URL[]{new URL("file:" + file.getAbsolutePath())}, Main.class.getClassLoader());
        String cbv = null, msv = null, cbmv = null, ghc = null;
        Properties props = new Properties();
        props.load(classLoader.getResourceAsStream("META-INF/maven/org.bukkit/craftbukkit/pom.properties"));
        cbmv = props.getProperty("version");
        var csz = getClass("org.bukkit.craftbukkit.Main");
        int jnksb = -1;
        // var os = getClass("joptsimple.OptionSet");
        var msz = getClass("net.minecraft.server.MinecraftServer");
        if (msz == null) {
            ClassReader csmcr = new ClassReader(csz.getResourceAsStream(csz.getSimpleName() + ".class"));
            ClassNode csmcn = new ClassNode();
            csmcr.accept(csmcn, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            MethodNode csmmn = null;
            for (MethodNode mn: csmcn.methods)
                if (mn.name.equals("main")) {
                    for (int i = 0; i < mn.instructions.size(); i++) {
                        AbstractInsnNode instr = mn.instructions.get(i);
                        if (instr.getOpcode() == Opcodes.INVOKESTATIC) {
                            MethodInsnNode iscn = (MethodInsnNode)instr;
                            if (iscn.owner.contains("MinecraftServer")) {
                                msz = getClass(iscn.owner.replace('/', '.'));
                                break;
                            }
                        }
                    }
                }
        }
        // var msi_173 = msz.getDeclaredConstructor(os).newInstance(new Object[]{null});
        // var csi_173 = csz.getDeclaredConstructor(msz, getClass("net.minecraft.server.ServerConfigurationManager")).newInstance(msi_173, null);
        cbv = csz.getPackage().getImplementationVersion();
        if (cbv == null) {
            cbv = "";
        }
        //var msvm = getMethod(msz, "getVersion");
        // System.out.println(csvm.getName());
        // System.out.println((String)csvm.invoke(csz));
        //System.out.println(cbv);
        ClassReader mscr = new ClassReader(msz.getResourceAsStream(msz.getSimpleName() + ".class"));
        ClassNode mscn = new ClassNode();
        mscr.accept(mscn, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        MethodNode msvmn = null;
        for (MethodNode mn: mscn.methods)
            if (mn.name.equals("getVersion")) {
                msvmn = mn;
                break;
            }
        if (msvmn != null) {
            //System.out.println("Got MinecraftServer.getVersion()!");
            for (int i = 0; i < msvmn.instructions.size(); i++) {
                AbstractInsnNode instr = msvmn.instructions.get(i);
                if (instr.getOpcode() == Opcodes.LDC) {
                    var ldcn = (LdcInsnNode)instr;
                    if (ldcn.cst instanceof String) {
                        msv = ldcn.cst.toString();
                        //System.out.println("MinecraftServer.getVersion() => " + msv);
                        break;
                    }
                }
            }
        } else {
            //System.out.println("MinecraftServer.getVersion() not found, trying workaround...");
            boolean isMsvFound = false;
            for (MethodNode msmn: mscn.methods) {
                if (isMsvFound) {
                    break;
                }
                //System.out.println("Got MinecraftServer.init()!");
                for (int i = 0; i < msmn.instructions.size(); i++) {
                    AbstractInsnNode instr = msmn.instructions.get(i);
                    if (instr.getOpcode() == Opcodes.LDC) {
                        var ldcn = (LdcInsnNode)instr;
                        if (ldcn.cst instanceof String) {
                            String s = ldcn.cst.toString();
                            if (s.contains("version")) {
                                //System.out.println("MinecraftServer." + msmn.name + "() => " + s);
                                msv = s.split("version ")[1];
                                break;
                            }
                        }
                    }
                }
            }
        }

        String[] parts = cbv.split("-");
        String ghcs = null;
        String cbb = null;
        for (String part : parts) {
            if (part.equals("git")) {
                continue;
            }
            if (part.startsWith("g") && part.length() == 8) {
                ghcs = part.substring(1);
            }
            if (part.startsWith("b")) {
                cbb = part;
                if (part.endsWith("jnks")) {
                    jnksb = Integer.parseInt(part.substring(1, part.length() - 4));
                }
            }
        }
        if (ghcs != null) {
            try (InputStream inputStream = Runtime.getRuntime()
                    .exec("git log -1 --pretty=format:%H " + ghcs, null, new File("./CraftBukkit")).getInputStream();
                 Scanner s = new Scanner(inputStream).useDelimiter("\\A")) {
                ghc = s.next();
            } catch (Exception e) {
                ghc = "";
            }
        }

        msv = msv.replace("Beta ", "b");
        var ver = String.format("%s,%s,%s,%s,%s,%s,%s%n",
                jnksb > 0 ? Integer.toString(jnksb) : "???", cbmv, msv, cbv, ghc,
                getFileChecksum(MessageDigest.getInstance("MD5"), file.getAbsolutePath()),
                getFileChecksum(MessageDigest.getInstance("SHA1"), file.getAbsolutePath()));
        System.out.printf(ver);

        System.out.println("Build:" + (jnksb > 0 ? Integer.toString(jnksb) : "???"));
        System.out.println("CraftBukkit: " + cbmv);
        System.out.println("MinecraftServer: " + msv);
        System.out.println("CraftBukkit: " + cbv);
        System.out.println("Git hash: " + ghc);
        System.out.println("MD5: " + getFileChecksum(MessageDigest.getInstance("MD5"), file.getAbsolutePath()));
        System.out.println("SHA1: " + getFileChecksum(MessageDigest.getInstance("SHA1"), file.getAbsolutePath()));

//        Pattern p = Pattern.compile("git-Bukkit-.*-b(\\d*)(?:jnks)?");
//        Matcher m = p.matcher(cbv);
//        if (m.matches()) {
//            jnksb = Integer.parseInt(m.group(1));
//        }
//
//        String CBV = "R0.0";
//        Pattern p2 = Pattern.compile("(R\\d(\\.\\d)?)");
//        Matcher m2 = p2.matcher(cbv);
//        if (m2.find()) {
//            CBV = m2.group(1);
//        }
//
//        if(msv == null) return;
//        msv = msv.replace("Beta ", "b");
//        if(true) {
//            System.out.println("MC Version: " + msv);
//            System.out.println("Build: " + jnksb);
//            System.out.println("CB: " + CBV);
//            System.out.println(cbv);
//        }

        //addRow(String.valueOf(jnksb), CBV, msv, cbv);
    }

    private static String getFileChecksum(MessageDigest digest, String file) throws IOException
    {
        //Get file input stream for reading the file content
        FileInputStream fis = new FileInputStream(file);

        //Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        //Read file data and update in message digest
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        };

        //close the stream; We don't need it now.
        fis.close();

        //Get the hash's bytes
        byte[] bytes = digest.digest();

        //This bytes[] has bytes in decimal format;
        //Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< bytes.length ;i++)
        {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        //return complete hash
        return sb.toString().toUpperCase();
    }

    public static Class<?> getClass(String name) {
        try {
            return Class.forName(name, true, classLoader);
        } catch (ClassNotFoundException e) {
            //e.printStackTrace();
            return null;
        }
    }

    public static Method getMethod(Class<?> c, String name) {
        try {
            return c.getMethod(name);
        } catch (NoSuchMethodException | SecurityException e) {
            //e.printStackTrace();
            return null;
        }
    }
}
