package wincom.generator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public final class Parser {

    private static class ParseMethod {

        final String name;
        final String retType;
        final String[] types;
        final String[] names;

        ParseMethod(String name, String retType, String[] types, String[] names) {
            this.name = name;
            this.retType = retType;
            this.types = types;
            this.names = names;
        }
    }

    private static class ParseJava {

        final String pack;
        final String className;
        final ParseMethod[] methods;
        final String[] imports;
        final String superClass;

        ParseJava(String pack, String className, ParseMethod[] methods, String[] imports, String superClass) {
            this.pack = pack;
            this.className = className;
            this.methods = methods;
            this.imports = imports;
            this.superClass = superClass;
        }
    }

    public static ParseJava parseJava(File dir, String className,
                                      String pack, String startPack) throws IOException {
        BufferedReader rdr = new BufferedReader(new FileReader(new File(dir, className + ".java")));
        List<ParseMethod> methods = new ArrayList<ParseMethod>();
        List<String> imports = new ArrayList<String>();
        boolean no = false;
        String superClass = null;
        while (true) {
            String line = rdr.readLine();
            if (line == null)
                break;
            line = line.trim();
            String imp = "import";
            if (line.startsWith(imp)) {
                line = line.substring(imp.length()).trim();
                if (line.startsWith(startPack)) {
                    line = line.substring(startPack.length());
                    int p = line.lastIndexOf(';');
                    line = line.substring(0, p).trim();
                    imports.add(line);
                }
                continue;
            }
            String prefix = "public";
            if (!line.startsWith(prefix))
                continue;
            line = line.substring(prefix.length()).trim();
            if (line.startsWith("interface")) {
                no = true;
                break;
            }
            if (line.startsWith("class")) {
                String str = "extends";
                int p = line.indexOf(str);
                if (p >= 0) {
                    line = line.substring(p + str.length()).trim();
                    if (!line.startsWith("Dispatch")) {
                        p = line.indexOf(' ');
                        superClass = line.substring(0, p);
                    }
                }
                continue;
            }
            if (line.startsWith("static") || line.startsWith("class"))
                continue;
            if (line.startsWith(className)) {
                if (line.substring(className.length()).trim().startsWith("("))
                    continue;
            }
            //System.out.println(line);
            int p = line.indexOf(' ');
            String retType = line.substring(0, p);
            line = line.substring(p + 1).trim();
            p = line.indexOf('(');
            String method = line.substring(0, p).trim();
            line = line.substring(p + 1).trim();
            p = line.indexOf(')');
            line = line.substring(0, p);
            List<String> args = new ArrayList<String>();
            while (line.length() > 0) {
                p = line.indexOf(',');
                if (p < 0) {
                    args.add(line);
                    break;
                } else {
                    String arg = line.substring(0, p).trim();
                    args.add(arg);
                    line = line.substring(p + 1).trim();
                }
            }
            String[] types = new String[args.size()];
            String[] names = new String[args.size()];
            for (int i = 0; i < args.size(); i++) {
                String arg = args.get(i);
                p = arg.indexOf(' ');
                types[i] = arg.substring(0, p);
                names[i] = arg.substring(p + 1).trim();
            }
            methods.add(new ParseMethod(method, retType, types, names));
        }
        rdr.close();
        if (no)
            return null;
        ParseMethod[] parseMethods = methods.toArray(new ParseMethod[methods.size()]);
        return new ParseJava(pack, className, parseMethods, imports.toArray(new String[imports.size()]), superClass);
    }

    public static void parseTree(File dir, String pack, List<ParseJava> map, String startPack) throws IOException {
        final String ext = ".java";
        File[] java = dir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().endsWith(ext);
            }
        });
        for (File file : java) {
            String name = file.getName();
            String className = name.substring(0, name.length() - ext.length());
            ParseJava cls = parseJava(dir, className, pack, startPack);
            if (cls != null) {
                map.add(cls);
            }
        }
        File[] dirs = dir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        for (File diri : dirs) {
            parseTree(diri, pack + "." + diri.getName(), map, startPack);
        }
    }

    public static List<ParseJava> parseFiles(File srcRoot, String startPack) throws IOException {
        StringTokenizer tok = new StringTokenizer(startPack, ".");
        File dir = srcRoot;
        while (tok.hasMoreTokens()) {
            String t = tok.nextToken();
            dir = new File(dir, t);
        }
        List<ParseJava> map = new ArrayList<ParseJava>();
        parseTree(dir, startPack, map, startPack);
        return map;
    }

    static final String[] elemTypes = {
        "byte", "short", "int", "long",
        "char", "float", "double", "boolean",
        "void"
    };
    static final String[] elemClass = {
        "Byte", "Short", "Integer", "Long",
        "Character", "Float", "Double", "Boolean",
        "Void"
    };

    private static boolean simpleObject(String type) {
        return "Object".equals(type) || "String".equals(type) || "java.util.Date".equals(type) || "String[]".equals(type);
    }

    private static String mapType(String type) {
        for (String elemType : elemTypes) {
            if (elemType.equals(type)) {
                return type;
            }
        }
        if (simpleObject(type))
            return type;
        return type + "Wrapper";
    }

    private static String mapClass(String type) {
        for (int i = 0; i < elemTypes.length; i++) {
            if (elemTypes[i].equals(type)) {
                return elemClass[i] + ".TYPE";
            }
        }
        return type + ".class";
    }

    private static String mapRet(String type) {
        for (int i = 0; i < elemTypes.length; i++) {
            if (elemTypes[i].equals(type)) {
                return elemClass[i];
            }
        }
        return type;
    }

    private static String mapObject(String type, String obj) {
        for (int i = 0; i < elemTypes.length; i++) {
            if (elemTypes[i].equals(type)) {
                return elemClass[i] + ".valueOf(" + obj + ")";
            }
        }
        if (simpleObject(type))
            return obj;
        return obj + ".instance";
    }

    private static String mapReturn(String type) {
        for (String elemType : elemTypes) {
            if (elemType.equals(type)) {
                return "_ret." + type + "Value()";
            }
        }
        if (simpleObject(type))
            return "_ret";
        return "new " + type + "Wrapper(this, _ret)";
    }

    public static void printMethod(PrintWriter w, String className, ParseMethod method) {
        w.print("    public " + mapType(method.retType) + " " + method.name + "(");
        for (int i = 0; i < method.names.length; i++) {
            if (i > 0) {
                w.print(", ");
            }
            w.print(mapType(method.types[i]) + " " + method.names[i]);
        }
        w.println(") throws ComException {");
        w.print("        Method _method = getMethod(" + className + ".class, \"" + method.name + "\", new Class[] {");
        for (int i = 0; i < method.names.length; i++) {
            if (i > 0) {
                w.print(", ");
            }
            w.print(mapClass(method.types[i]));
        }
        w.println("});");
        w.print("        ");
        if (!"void".equals(method.retType)) {
            w.print(mapRet(method.retType) + " _ret = (" + mapRet(method.retType) + ") ");
        }
        w.print("runMethodInThread(_method, instance, new Object[] {");
        for (int i = 0; i < method.names.length; i++) {
            if (i > 0) {
                w.print(", ");
            }
            w.print(mapObject(method.types[i], method.names[i]));
        }
        w.println("});");
        if (!"void".equals(method.retType)) {
            w.println("        return " + mapReturn(method.retType) + ";");
        }
        w.println("    }");
    }

    public static void printClass(ParseJava cls, String startPack, String destPack, File srcRoot) throws IOException {
        String newPack = destPack + cls.pack.substring(startPack.length());
        StringTokenizer tok = new StringTokenizer(newPack, ".");
        File dir = srcRoot;
        while (tok.hasMoreTokens()) {
            String t = tok.nextToken();
            dir = new File(dir, t);
        }
        dir.mkdirs();
        PrintWriter w = new PrintWriter(new FileWriter(new File(dir, cls.className + "Wrapper.java")));
        w.println("package " + newPack + ";");
        w.println();
        w.println("import " + cls.pack + ".*;");
        for (String imp : cls.imports) {
            w.println("import " + startPack + imp + ";");
            if (!imp.endsWith("*")) {
                w.println("import " + destPack + imp + "Wrapper;");
            } else {
                w.println("import " + destPack + imp + ";");
            }
        }
        w.println("import com.jacob.com.Variant;");
        w.println("import wincom.ComException;");
        w.println("import wincom.VariantWrapper;");
        w.println("import wincom.Wrapper;");
        w.println("import java.lang.reflect.Method;");
        w.println();
        w.println("@SuppressWarnings({\"cast\"})");
        if (cls.superClass != null) {
            w.println("public class " + cls.className + "Wrapper extends " + cls.superClass + "Wrapper {");
        } else {
            w.println("public class " + cls.className + "Wrapper extends Wrapper {");
        }
        w.println();
        w.println("    public final " + cls.className + " instance;");
        w.println();
        w.println("    public " + cls.className + "Wrapper(Wrapper wrapper, " + cls.className + " instance) {");
        w.println("        super(wrapper, instance);");
        w.println("        this.instance = instance;");
        w.println("    }");
        for (ParseMethod method : cls.methods) {
            w.println();
            printMethod(w, cls.className, method);
        }
        w.println("}");
        w.close();
    }

    public static void main(String[] args) throws IOException {
        File srcRoot;
        String basePack;
        if (args.length < 2) {
            System.out.println("Usage: wincom.generator.Parser <root directory> <base package name>");
            return;
        } else {
            srcRoot = new File(args[0]);
            basePack = args[1] + ".";
        }
        String startPack = basePack + "com";
        String destPack = basePack + "wrapper";
        List<ParseJava> map = parseFiles(srcRoot, startPack);
        for (ParseJava java : map) {
            printClass(java, startPack, destPack, srcRoot);
        }
    }
}
