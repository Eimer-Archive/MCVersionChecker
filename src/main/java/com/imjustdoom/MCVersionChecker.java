package com.imjustdoom;

import com.sun.tools.javac.Main;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MCVersionChecker {
    public static ClassLoader classLoader;
    public static Sheet sheet;
    public static int NUMBER = 0;

    public static void main(String[] args) throws SecurityException, IllegalArgumentException, IOException {

        String dir;

        while(true) {

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(System.in));

            System.out.println("Enter the path to the server software files: ");
            dir = reader.readLine();

            if(!Paths.get(dir).toFile().exists()) {
                System.out.println("Directory not found!");
                continue;
            }

            Workbook wb = new HSSFWorkbook();

            String sheetPath = Paths.get("").toAbsolutePath() + "/sheet.xlsx";
            sheet = wb.createSheet("MCVersionChecker");

            for (File file : new File(dir).listFiles()) {
                if (file.isDirectory() && file.listFiles().length > 0 && FilenameUtils.getExtension(file.listFiles()[0].getName()).equalsIgnoreCase("jar"))
                    getBukkitVersion(file.listFiles()[0]);
                else if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("jar")) getBukkitVersion(file);
            }

            System.out.println(System.lineSeparator() + "Finished scanning, saving data...");

            // Write the output to a file
            FileOutputStream fileOut = new FileOutputStream(sheetPath);
            wb.write(fileOut);
            fileOut.close();

            System.out.println("Data saved to " + sheetPath + System.lineSeparator());
        }
    }

    public static void addRow(String build, String CBV, String MVC, String BV) {
        Row row = sheet.createRow((short) NUMBER);
        //sheet.shiftRows(1, sheet.getLastRowNum()+1, 1, true,true);
        row.createCell(0).setCellValue(build);
        row.createCell(1).setCellValue(CBV);
        row.createCell(2).setCellValue(MVC);
        row.createCell(3).setCellValue(BV);

        NUMBER++;
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

    public static void getBukkitVersion(File file) throws IOException {
        System.out.println("\nFile: " + file.getName());
        classLoader = URLClassLoader.newInstance(new URL[]{new URL("file:" + file.getAbsolutePath())}, MCVersionChecker.class.getClassLoader());
        String cbv, msv = null;
        var csz = getClass("org.bukkit.craftbukkit.Main");
        int jnksb = -1;
        var msz = getClass("net.minecraft.server.MinecraftServer");
        if (msz == null) {
            ClassReader csmcr = new ClassReader(csz.getResourceAsStream(csz.getSimpleName() + ".class"));
            ClassNode csmcn = new ClassNode();
            csmcr.accept(csmcn, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            MethodNode csmmn = null;
            for (MethodNode mn : csmcn.methods)
                if (mn.name.equals("main")) {
                    for (int i = 0; i < mn.instructions.size(); i++) {
                        AbstractInsnNode instr = mn.instructions.get(i);
                        if (instr.getOpcode() == Opcodes.INVOKESTATIC) {
                            MethodInsnNode iscn = (MethodInsnNode) instr;
                            if (iscn.owner.contains("MinecraftServer")) {
                                msz = getClass(iscn.owner.replace('/', '.'));
                                break;
                            }
                        }
                    }
                }
        }

        cbv = csz.getPackage().getImplementationVersion();
        if (cbv == null) {
            cbv = "";
        }

        ClassReader mscr = new ClassReader(msz.getResourceAsStream(msz.getSimpleName() + ".class"));
        ClassNode mscn = new ClassNode();
        mscr.accept(mscn, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        MethodNode msvmn = null;
        for (MethodNode mn : mscn.methods)
            if (mn.name.equals("getVersion")) {
                msvmn = mn;
                break;
            }
        if (msvmn != null) {
            for (int i = 0; i < msvmn.instructions.size(); i++) {
                AbstractInsnNode instr = msvmn.instructions.get(i);
                if (instr.getOpcode() == Opcodes.LDC) {
                    var ldcn = (LdcInsnNode) instr;
                    if (ldcn.cst instanceof String) {
                        msv = ldcn.cst.toString();
                        break;
                    }
                }
            }
        } else {
            boolean isMsvFound = false;
            for (MethodNode msmn : mscn.methods) {
                if (isMsvFound) {
                    break;
                }
                for (int i = 0; i < msmn.instructions.size(); i++) {
                    AbstractInsnNode instr = msmn.instructions.get(i);
                    if (instr.getOpcode() == Opcodes.LDC) {
                        var ldcn = (LdcInsnNode) instr;
                        if (ldcn.cst instanceof String) {
                            String s = ldcn.cst.toString();
                            if (s.contains("version")) {
                                msv = s.split("version ")[1];
                                break;
                            }
                        }
                    }
                }
            }
        }
        Pattern p = Pattern.compile("git-Bukkit-.*-b(\\d*)(?:jnks)?");
        Matcher m = p.matcher(cbv);
        if (m.matches()) {
            jnksb = Integer.parseInt(m.group(1));
        }

        String CBV = "R0.0";
        Pattern p2 = Pattern.compile("(R\\d(\\.\\d)?)");
        Matcher m2 = p2.matcher(cbv);
        if (m2.find()) {
            CBV = m2.group(1);
        }

        if(msv == null) return;
        msv = msv.replace("Beta ", "b");
        if(true) {
            System.out.println("MC Version: " + msv);
            System.out.println("Build: " + jnksb);
            System.out.println("CB: " + CBV);
            System.out.println(cbv);
        }

        addRow(String.valueOf(jnksb), CBV, msv, cbv);
    }
}