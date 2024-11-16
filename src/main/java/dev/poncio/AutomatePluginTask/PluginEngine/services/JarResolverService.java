package dev.poncio.AutomatePluginTask.PluginEngine.services;

import dev.poncio.AutomatePluginTask.PluginEngine.classLoaders.JarClassLoader;
import dev.poncio.AutomatePluginTask.PluginEngine.exceptions.PluginJarLoadException;
import dev.poncio.AutomatePluginTask.PluginSdk.v1.implementation.AbstractPluginTask;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

@Service
public class JarResolverService {

    public JarPluginDetail getPluginDetail(InputStream jarIS) throws PluginJarLoadException {
        return getPluginDetail(byteArraFromIS(jarIS));
    }

    public JarPluginDetail getPluginDetail(byte[] jarByteArray) throws PluginJarLoadException {
        String className = null;
        try {
            className = loadMainClassName(jarByteArray);
            AbstractPluginTask pluginTask = resolveJarPluginTask(jarByteArray);
            return JarPluginDetail.builder()
                    .mainClassName(className)
                    .version(loadVersionClass(jarByteArray))
                    .pluginSdkVersion(pluginTask.getPluginSdkVersion())
                    .plugin(pluginTask)
                    .build();
        } catch (IOException e) {
            throw new PluginJarLoadException("Fail to read JAR file", e);
        } catch (ClassNotFoundException e) {
            throw new PluginJarLoadException(String.format("Classname %s not found", className), e);
        } catch (InvocationTargetException e) {
            throw new PluginJarLoadException("Class constructor threw an error", e);
        } catch (InstantiationException e) {
            throw new PluginJarLoadException("Abstract classes cannot be instantiated", e);
        } catch (IllegalAccessException e) {
            throw new PluginJarLoadException("Wrong constructor access control", e);
        } catch (NoSuchMethodException e) {
            throw new PluginJarLoadException("Constructor not found", e);
        }
    }

    public ByteArrayInputStream transformFileToIS(byte[] jarByteArray) throws PluginJarLoadException {
        return new ByteArrayInputStream(jarByteArray);
    }

    private byte[] byteArraFromIS(InputStream jarIS) throws PluginJarLoadException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int bytesRead;
        byte[] buffer = new byte[1024];

        try {
            while ((bytesRead = jarIS.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new PluginJarLoadException("Fail to read JAR file", e);
        }

        return bos.toByteArray();
    }

    private AbstractPluginTask resolveJarPluginTask(byte[] jarByteArray) throws IOException, ClassNotFoundException, PluginJarLoadException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String className = loadMainClassName(jarByteArray);
        JarClassLoader classLoader = new JarClassLoader(jarByteArray);
        Class<?> pluginClass = classLoader.loadClass(className);
        for (Class<?> innerClass : pluginClass.getDeclaredClasses()) {
            classLoader.loadClass(innerClass.getName());
        }
        if (!AbstractPluginTask.class.isAssignableFrom(pluginClass))
            throw new PluginJarLoadException("Class loaded is not a IPluginTask");
        return (AbstractPluginTask) pluginClass.getDeclaredConstructor().newInstance();
    }

    private String loadMainClassName(byte[] jarByteArray) throws IOException {
        try (JarInputStream jarStream = new JarInputStream(new ByteArrayInputStream(jarByteArray))) {
            Manifest manifest = jarStream.getManifest();
            return manifest.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
        }
    }

    private String loadVersionClass(byte[] jarByteArray) throws IOException, PluginJarLoadException {
        JarClassLoader classLoader = new JarClassLoader(jarByteArray);
        String pomFile = classLoader.readPomFile();
        if (pomFile == null || pomFile.isEmpty()) {
            throw new PluginJarLoadException("No POM file found");
        }
        Properties props = new Properties();
        props.load(new ByteArrayInputStream(pomFile.getBytes()));
        return (String) props.get("version");
    }

    @Data
    @Builder
    public static class JarPluginDetail {
        private String mainClassName;
        private String version;
        private String pluginSdkVersion;
        private AbstractPluginTask plugin;
    }

}
