package sbtjooq.codegen.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;


public class GenerationTool {

    private static final Logger logger = LoggerFactory.getLogger(GenerationTool.class);

    private final MethodHandle delegate;
    private final List<Path> configurations;

    public GenerationTool(MethodHandle delegate, List<Path> configurations) {
        this.delegate = delegate;
        this.configurations = configurations;
    }

    public void generate() throws Throwable {
        invokeDelegate();
    }

    private void invokeDelegate() throws Throwable {
        final String[] args = configurations.stream().map(Path::toString).toArray(String[]::new);
        //noinspection ConfusingArgumentToVarargsMethod
        delegate.invokeExact(args);
    }


    public static Class<?> detectGenerationToolClass() throws ClassNotFoundException {
        try {
            return Class.forName("org.jooq.codegen.GenerationTool");
        } catch (ClassNotFoundException e1) {
            try {
                return Class.forName("org.jooq.util.GenerationTool");
            } catch (ClassNotFoundException e2) {
                e2.addSuppressed(e1);
                throw e2;
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

    public static void main(String[] args) throws Throwable {
        final Class<?> genToolClass = detectGenerationToolClass();
        logger.debug("Delegate to detected {}", genToolClass.getName());

        final MethodHandle delegate = getMainMethod(genToolClass);
        final List<Path> configurations = Arrays.stream(args).map(Paths::get).collect(toList());

        showJooqLogo();

        new GenerationTool(delegate, configurations).generate();
    }

}
