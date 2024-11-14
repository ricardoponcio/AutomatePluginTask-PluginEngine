package dev.poncio.AutomatePluginTask.PluginEngine.classLoaders;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class JarClassLoader extends ClassLoader {
    private final byte[] jarFileByteArray;

    public JarClassLoader(byte[] jarFileByteArray) {
        this.jarFileByteArray = jarFileByteArray;
    }

    @Override
    protected Class<?> findClass(String name) {
        byte[] classBytes = new byte[0];
        try {
            classBytes = readClassFile(name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return defineClass(name, classBytes, 0, classBytes.length);
    }

    private JarInputStream get() throws IOException {
        return new JarInputStream(new ByteArrayInputStream(this.jarFileByteArray));
    }

    private byte[] readClassFile(String name) throws IOException {
        return readJarResource((resourceName) -> resourceName.equals(name + ".class"));
    }

    public String readPomFile() throws IOException {
        byte[] pomByteArray = readJarResource((resourceName) -> resourceName.startsWith("META-INF/maven/") && resourceName.endsWith("/pom.properties"));
        return new String(pomByteArray, StandardCharsets.UTF_8);
    }

    private byte[] readJarResource(Function<String, Boolean> nameValidation) throws IOException {
        JarInputStream jarInputStream = get();

        JarEntry entry = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((entry = jarInputStream.getNextJarEntry()) != null) {
            if (nameValidation.apply(entry.getName())) {
                int bytesRead;
                byte[] buffer = new byte[1024];

                while ((bytesRead = jarInputStream.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }

                return bos.toByteArray();
            }
        }

        return new byte[0];
    }
}