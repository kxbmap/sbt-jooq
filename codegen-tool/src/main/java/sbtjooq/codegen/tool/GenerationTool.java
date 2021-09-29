package sbtjooq.codegen.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;


public class GenerationTool {

    private static final Logger logger = LoggerFactory.getLogger(GenerationTool.class);

    private final MethodHandle delegate;
    private final String[] args;

    public GenerationTool(MethodHandle delegate, String[] args) {
        this.delegate = delegate;
        this.args = args;
    }

    public void generate() throws Throwable {
        invokeDelegate();
    }

    private void invokeDelegate() throws Throwable {
        //noinspection ConfusingArgumentToVarargsMethod
        delegate.invokeExact(args);
    }


    private static Class<?> detectGenerationToolClass() throws ClassNotFoundException {
        try {
            return Class.forName("org.jooq.codegen.GenerationTool");
        } catch (ClassNotFoundException e) {
            try {
                return Class.forName("org.jooq.util.GenerationTool");
            } catch (ClassNotFoundException suppressed) {
                e.addSuppressed(suppressed);
                throw e;
            }
        }
    }

    private static MethodHandle getMainMethod(Class<?> mainClass) throws ReflectiveOperationException {
        final MethodType mainType = MethodType.methodType(void.class, String[].class);
        return MethodHandles.publicLookup().findStatic(mainClass, "main", mainType);
    }

    private static void showRunningInfo(Class<?> mainClass) {
        final String vendor = System.getProperty("java.vendor");
        final String version = System.getProperty("java.version");
        logger.info("Running {} ({} Java {})", mainClass.getName(), vendor, version);
    }

    private static void showJooqLogo() {
        try {
            Class.forName("org.jooq.impl.DefaultRenderContext");
        } catch (ClassNotFoundException ignored) {
        }
    }

    public static void main(String[] args) throws Throwable {
        final Class<?> mainClass = detectGenerationToolClass();
        final MethodHandle delegate = getMainMethod(mainClass);
        showRunningInfo(mainClass);
        showJooqLogo();
        new GenerationTool(delegate, args).generate();
    }

}
