package de.sofd.viskit.ui.imagelist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;

public class ILVBaseMixinPreproc {

    public void runOnDirectory(File baseDir, File srcFile) throws Exception {
        runOnDirectorySrcAbs(baseDir, new File(baseDir, srcFile.getPath()));
    }
    
    private void runOnDirectorySrcAbs(File baseDir, File srcFileAbs) throws Exception {
        if (baseDir.isFile() && baseDir.getName().endsWith(".pp.properties")) {
            runOnSingleFile(baseDir, srcFileAbs);
        } else if (baseDir.isDirectory()){
            for (File f : baseDir.listFiles()) {
                runOnDirectorySrcAbs(f, srcFileAbs);
            }
        }
    }
    
    public void runOnSingleFile(File ppPropsFile, File srcFile) throws Exception {
        Properties ppProps = new Properties();
        ppProps.load(new FileInputStream(ppPropsFile));
        
        String outFileName = ppPropsFile.getPath().replace(".pp.properties", ".java");
        File outFile = new File(outFileName);
        if (outFile.exists()) {
            if (outFile.lastModified() > srcFile.lastModified() && outFile.lastModified() > ppPropsFile.lastModified()) {
                System.err.println(outFileName + " is up-to-date, not regenerating");
                return;
            }
        }
        System.err.println("generating " + outFileName + " from " + srcFile + " and " + ppPropsFile);
        String srcClassName = srcFile.getName().replaceAll(".java", "");
        String outClassName = ppPropsFile.getName().replace(".pp.properties", "");
        
        //PASS 1: replace ImageListViewBaseImpl with outClassName, remove /*< etc. to get the velocity input template
        StringWriter p1outWriter = new StringWriter(3000);
        p1outWriter.write(
                "/*\n" +
                " * DO NOT EDIT THIS FILE!\n" +
                " * \n" +
                " * It was automatically generated from\n" +
                " * " + srcFile + " and\n" +
                " * " + ppPropsFile + ".\n" +
                " * Edit those files instead.\n" +
                " */\n\n");
        try {
            BufferedReader inReader = new BufferedReader(new FileReader(srcFile));
            try {
                int n = 0;
                for (String l = inReader.readLine(); l != null; l = inReader.readLine()) {
                    l = l.replace("/*<", "");
                    l = l.replace(">*/", "");
                    l = l.replace("//</", "");
                    l = l.replace(srcClassName, outClassName);
                    p1outWriter.write(l);
                    p1outWriter.write("\n");
                    if (n++ == 10) {
                        p1outWriter.write("//DO NOT EDIT THIS FILE!\n");
                        n = 0;
                    }
                }
            } finally {
                inReader.close();
            }
        } finally {
            p1outWriter.close();
        }

        ////output p1 result for debugging
        //{
        //    FileWriter os = new FileWriter("/tmp/dbg.txt", false);
        //    os.write(p1outWriter.toString());
        //    os.close();
        //}
        
        //PASS 2: run velocity over output of pass 1
        VelocityContext velCtx = new VelocityContext(ppProps);
        Writer p2outWriter = new FileWriter(outFileName, false);  //TODO: truncates?
        try {
            Velocity.evaluate(velCtx, p2outWriter, "ilvBaseMixin", new StringReader(p1outWriter.toString()));
        } catch (ParseErrorException pex) {
            dumpParseErrorException(p1outWriter.toString(), pex);
            new File(outFileName).delete();
            throw pex;
        } catch (Exception ex) {
            new File(outFileName).delete();
            throw ex;
        } finally {
            p2outWriter.close();
        }
    }
    
    public static void dumpParseErrorException(String template, ParseErrorException pex) {
        // pex doesn't contain the line/column numbers correctly, so we can't output
        // the line where the error happened :-( Bugreport issued at https://issues.apache.org/jira/browse/VELOCITY-777
        int line = pex.getLineNumber();
        int col = pex.getColumnNumber();
        if (line != -1 && col != -1) {
            System.err.println("err on line " + line + ", col " + col);
        }
    }

    
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("usage: java " + ILVBaseMixinPreproc.class.getSimpleName() + " <base directory> <source .java file (relative to base)>");
            System.exit(-1);
        }
        File baseDir = new File(args[0]);
        File srcFile = new File(args[1]);
        ILVBaseMixinPreproc pp = new ILVBaseMixinPreproc();
        pp.runOnDirectorySrcAbs(baseDir, srcFile);
    }

}
