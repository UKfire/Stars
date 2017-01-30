package com.ytying;

import com.ytying.classloader.MyClassLoader;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.tools.*;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by kefan.wkf on 17/1/13.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class TestMySQL {

    @Test
    public void testHello() throws Exception {
        //TODO 试试循环会发生什么!!! 答:循环的话,如果class是同一个全名,则出发缓存机制,如果class全名不同,则顺利完成,由此推测是ClassLoader的问题;
        //TODO 试试自定义ClassLoader怎么样!!!这这里,自定义ClassLoader可以成功完成,接下来试试在Tomcat容器中会发生什么,经过测试发现自定义ClassLoader可以很好解决问题;

        MyClassLoader myClassLoader = new MyClassLoader("/Users/UKfire/Desktop/WkfCode/Stars/target/classes");

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        String sOutputPath = "./target/classes";

        int i = 10;
        while (i-- > 0) {
            String className = "heihei" + new Random().nextInt(100);
            StringWriter writer = new StringWriter();
            PrintWriter out = new PrintWriter(writer);
            out.println("package com.ytying.source;");
            out.println("public class " + className + " {");
            out.println("  public static void main(String args[]) {");
            out.println("    System.out.println(\"heihei = " + i + "\");");
            out.println("  }");
            out.println("}");
            out.close();
            JavaFileObject file = new JavaSourceFromString(className, writer.toString());

            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

            StandardJavaFileManager oStandardJavaFileManager = compiler.
                    getStandardFileManager(diagnostics, null, null);

            JavaFileManager.Location oLocation = StandardLocation.CLASS_OUTPUT;
            oStandardJavaFileManager.setLocation(oLocation, Arrays
                    .asList(new File[]{new File(sOutputPath)}));

            Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(file);
            JavaCompiler.CompilationTask task = compiler.getTask(null, oStandardJavaFileManager, diagnostics, null, null, compilationUnits);


            boolean success = task.call();
            for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
                System.out.println(diagnostic.getCode());
                System.out.println(diagnostic.getKind());
                System.out.println(diagnostic.getPosition());
                System.out.println(diagnostic.getStartPosition());
                System.out.println(diagnostic.getEndPosition());
                System.out.println(diagnostic.getSource());
                System.out.println(diagnostic.getMessage(null));
            }
            System.out.println("Success: " + success);

            if (success) {
                try {
                    Class.forName("com.ytying.source." + className,true,myClassLoader).getDeclaredMethod("main", new Class[]{String[].class})
                            .invoke(null, new Object[]{null});
                } catch (ClassNotFoundException e) {
                    System.err.println("Class not found: " + e);
                } catch (NoSuchMethodException e) {
                    System.err.println("No such method: " + e);
                } catch (IllegalAccessException e) {
                    System.err.println("Illegal access: " + e);
                } catch (InvocationTargetException e) {
                    System.err.println("Invocation target: " + e);
                }
            }
        }


    }

//    @Autowired
//    public ApplicationContext context;

//    @Autowired
//    private HibernateUtils hibernateUtils;

//    @Autowired
//    private UserService userService;

    @Test
    public void testDataSource() throws SQLException, Exception {

        int i = 10;
        while (i-- > 0) {

            StringWriter writer = new StringWriter();// 内存字符串输出流
            PrintWriter out = new PrintWriter(writer);
            out.println("package com.ytying.source;");
            out.println("public class Hello{");
            out.println("public static void main(String[] args){");
            out.println("System.out.println(\"helloworld= " + i + "\");");
            out.println("}");
            out.println("}");
            out.flush();
            out.close();
            // 开始编译
            JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
            JavaFileObject fileObject = new JavaSourceFromString("Hello", writer.toString());
            JavaCompiler.CompilationTask task = javaCompiler.getTask(null, null, null, Arrays.asList("-d", "./target/classes/"), null, Arrays.asList(fileObject));
            boolean success = task.call();
            if (!success) {
                System.out.println("编译失败!");
            } else {
                System.out.println(System.getProperty("user.dir"));
                System.out.println("编译成功！");
                URL[] urls = new URL[]{new URL("file:/" + System.getProperty("user.dir") + "/classes/")};
                URLClassLoader classLoader = new URLClassLoader(urls);
                Class class1 = classLoader.loadClass("com.ytying.source.Hello");
                Method method = class1.getDeclaredMethod("main", String[].class);
                String[] args1 = {null};
                method.invoke(class1.newInstance(), args1);
            }
        }
    }

    @Test
    public void testClassLoader() {
        ClassLoader classLoader = TestMySQL.class.getClassLoader();
        while (classLoader != null) {
            System.out.println(classLoader.toString());
            classLoader = classLoader.getParent();
        }
    }

}

class JavaSourceFromString extends SimpleJavaFileObject {
    final String code;

    JavaSourceFromString(String name, String code) {
        super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.code = code;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return code;
    }
}
