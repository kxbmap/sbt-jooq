package sbtjooq.codegen.tool;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;


public class GenerationTool {

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


    public static MethodHandle getMainMethod(String className) throws ReflectiveOperationException {
        final Class<?> mainClass = Class.forName(className);
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
        final int xc = 1;
        if (xc > args.length) throw new IndexOutOfBoundsException("Index out of range: " + (xc - 1));

        final MethodHandle delegate = getMainMethod(args[0]);
        final List<Path> configurations = Arrays.stream(args, xc, args.length).map(Paths::get).collect(toList());

        showJooqLogo();

        new GenerationTool(delegate, configurations).generate();
    }

}
