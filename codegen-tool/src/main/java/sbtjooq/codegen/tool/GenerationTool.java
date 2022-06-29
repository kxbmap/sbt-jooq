/*
 * Copyright 2015 Tsukasa Kitachi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sbtjooq.codegen.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Wrapper of jOOQ GenerationTool
 */
public class GenerationTool {

    private static final Logger logger = LoggerFactory.getLogger(GenerationTool.class);

    private static final Class<?> delegateClass;
    private static final MethodHandle delegateMain;

    static {
        final Class<?> clazz = detectGenerationToolClass();
        delegateClass = clazz;
        delegateMain = getMainMethod(clazz);
    }

    private static Class<?> detectGenerationToolClass() {
        try {
            return Class.forName("org.jooq.codegen.GenerationTool");
        } catch (ClassNotFoundException e) {
            try {
                return Class.forName("org.jooq.util.GenerationTool");
            } catch (ClassNotFoundException suppressed) {
                e.addSuppressed(suppressed);
                throw new RuntimeException(e);
            }
        }
    }

    private static MethodHandle getMainMethod(Class<?> mainClass) {
        final MethodType mainType = MethodType.methodType(void.class, String[].class);
        try {
            return MethodHandles.publicLookup().findStatic(mainClass, "main", mainType);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    private static void showRunningInfo() {
        final String vendor = System.getProperty("java.vendor");
        final String version = System.getProperty("java.version");
        logger.info("Running {} ({} Java {})", delegateClass.getName(), vendor, version);
    }

    private static void showJooqLogo() {
        try {
            Class.forName("org.jooq.impl.DefaultRenderContext");
        } catch (ClassNotFoundException ignored) {
        }
    }

    /**
     * Invoke jOOQ GenerationTool
     * @param configurations configuration files
     * @throws Throwable thrown by jOOQ GenerationTool
     */
    public void generate(String[] configurations) throws Throwable {
        //noinspection ConfusingArgumentToVarargsMethod
        delegateMain.invokeExact(configurations);
    }

    /**
     * Entry point
     * @param args configuration files
     * @throws Throwable thrown by jOOQ GenerationTool
     */
    public static void main(String[] args) throws Throwable {
        showRunningInfo();
        showJooqLogo();
        new GenerationTool().generate(args);
    }

}
