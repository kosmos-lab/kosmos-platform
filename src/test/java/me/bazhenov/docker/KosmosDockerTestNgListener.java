package me.bazhenov.docker;


import common.Utils;
import de.kosmos_lab.platform.utils.KosmoSHelper;
import de.kosmos_lab.utils.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.TestListenerAdapter;
import org.testng.annotations.Listeners;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.lang.Thread.currentThread;

/**
 * Orchestrates container initialisation and tear down for TestNG.
 * <p>
 * Typical usage consist of registering this class as a TestNG listener (see. {@link Listeners}) and then using
 * annotations {@link Container}, {@link AfterContainerStart} and {@link ContainerPort}.
 * <p>
 * Customized to check if a special environment is set to not start the docker while already in a docker container. The
 * CI Server is not usable with docker-in-docker(dind) images
 */
public class KosmosDockerTestNgListener extends TestListenerAdapter {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("KosmosDockerTestNgListener");

    private final Docker docker = new Docker();

    @Override
    public void onFinish(ITestContext testContext) {
        super.onFinish(testContext);

        if (!Utils.skip_docker_creation()) {
            try {
                docker.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    @Override
    public void onStart(ITestContext testContext) {
        super.onStart(testContext);

        if (!Utils.skip_docker_creation()) {
            logger.info("preparing to start container");
            KosmoSHelper.setEnv("SETUPKNX", "1");
            KosmoSHelper.setEnv("SETUPHA", "0");

            KosmoSHelper.setEnv("HA_HOST", "hatest");
            DockerAnnotationsInspector inspector = new DockerAnnotationsInspector();
            ExecutorService starter = Executors.newCachedThreadPool();

            try {
                Map<Object, Boolean> testObjects = new IdentityHashMap<>();
                for (ITestNGMethod m : testContext.getAllTestMethods()) {
                    testObjects.putIfAbsent(m.getInstance(), true);
                }

                // Retrieving container preferences and creating namespaces
                testObjects.keySet().stream()
                        .map(Object::getClass)
                        .forEach(inspector::createNamespace);

                List<Future<?>> futures = new ArrayList<>();
                File dir = new File("docker/ha/testconfig/");
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (!f.getName().equals("configuration.yaml") && !f.getName().equals("run")) {
                            if (f.isFile()) {
                                if (!f.delete()) {
                                    Assert.fail("could not delete " + f.getName());
                                }
                            } else if (f.isDirectory()) {
                                if (!FileUtils.deleteDirectory(f)) {
                                    Assert.fail("could not delete " + f.getName());
                                }
                            }
                        }
                    }
                }
                logger.info("starting container");

                // Starting containers and waiting for ports in parallel threads
                for (ContainerNamespace namespace : inspector.getAllNamespaces()) {
                    for (ContainerDefinition definition : namespace.getAllDefinitions()) {
                        if (SystemUtils.IS_OS_LINUX) {
                            definition.addVolume(new VolumeDef("/etc/services.d/home-assistant/run", new File("docker/ha/testconfig/run")));
                            definition.addEnvironment("PUID", String.valueOf(new com.sun.security.auth.module.UnixSystem().getUid()));
                            definition.addEnvironment("PGID", String.valueOf(new com.sun.security.auth.module.UnixSystem().getGid()));
                        }

                        try {
                            Process p = Runtime.getRuntime().exec("docker rm -f ha_integration_test");
                            p.waitFor();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }


                        definition.addCustomOption("--name=ha_integration_test");
                        futures.add(starter.submit(() -> {
                            try {
                                String containerId = docker.start(definition);
                                Map<Integer, Integer> publishedTcpPorts = docker.getPublishedTcpPorts(containerId);
                                namespace.registerPublishedTcpPorts(definition, publishedTcpPorts);
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            } catch (InterruptedException e) {
                                currentThread().interrupt();
                            }
                        }));
                    }
                }

                // Waiting for all containers ready
                for (Future<?> future : futures) {
                    try {
                        future.get();
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }

                // Performing port identification
                for (Object test : testObjects.keySet()) {
                    inspector.resolveNotificationMethod(test.getClass()).ifPresent(m -> m.call(test));
                }
            } catch (InterruptedException e) {
                currentThread().interrupt();
            } finally {
                starter.shutdown();
            }
            logger.info("docker startup complete");
        } else {
            logger.info("skipping docker");
        }
    }
}