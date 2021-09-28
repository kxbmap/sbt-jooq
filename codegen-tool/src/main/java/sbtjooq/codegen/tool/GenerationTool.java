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


    public static Class<?> detectGenerationToolClass() throws ClassNotFoundException {
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

    public static MethodHandle getMainMethod(Class<?> mainClass) throws ReflectiveOperationException {
        final MethodType mainType = MethodType.methodType(void.class, String[].class);
        return MethodHandles.publicLookup().findStatic(mainClass, "main", mainType);
    }

    public static void showJooqLogo() {
        try {
            Class.forName("org.jooq.impl.DefaultRenderContext");
        } catch (ClassNotFoundException ignored) {
        }
    }

    public static void showJavaVersion() {
        logger.info(String.format(
            "Running on %s Java %s", System.getProperty("java.vendor"), System.getProperty("java.version")));
    }

    public static void main(String[] args) throws Throwable {
        final Class<?> genToolClass = detectGenerationToolClass();
        final MethodHandle delegate = getMainMethod(genToolClass);
        logger.debug("Delegate to detected {}", genToolClass);
        showJooqLogo();
        showJavaVersion();
        new GenerationTool(delegate, args).generate();
    }

}
