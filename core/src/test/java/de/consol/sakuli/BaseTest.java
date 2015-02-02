/*
 * Sakuli - Testing and Monitoring-Tool for Websites and common UIs.
 *
 * Copyright 2013 - 2014 the original author or authors.
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

package de.consol.sakuli;

import de.consol.sakuli.loader.BaseActionLoader;
import de.consol.sakuli.loader.BeanLoader;
import de.consol.sakuli.utils.SakuliPropertyPlaceholderConfigurer;
import net.sf.sahi.report.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.Scanner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

/**
 * @author tschneck Date: 25.07.13
 */
public abstract class BaseTest {

    public static final String INCLUDE_FOLDER_PATH = "." + File.separator + "src" + File.separator + "main" + File.separator + "_include";
    public static final String SAHI_FOLDER_PATH = ".." + File.separator + "sahi";
    public static final String TEST_FOLDER_PATH = getResource("/_testsuite4JUnit");
    public static final String TEST_CONTEXT_PATH = "JUnit-beanRefFactory.xml";
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseTest.class);
    protected BaseActionLoader loaderMock;

    public static String getResource(String resourceName) {
        try {
            return Paths.get(BaseTest.class.getResource(resourceName).toURI()).toString();
        } catch (URISyntaxException e) {
            LOGGER.error("could not resolve Testsuite from classpath resource '{}'", resourceName, e);
            return null;
        }
    }

    public static void deleteFile(Path logFile) {
        FileSystemProvider provider = logFile.getFileSystem().provider();
        try {
            provider.deleteIfExists(logFile);
        } catch (IOException e) {
            //do nothing
        }
    }


    public static void assertRegExMatch(String string, String regex) {
        assertTrue(string.matches(regex),
                String.format("string '%s' won't match to regex '%s'", string, regex));
    }

    public static void assertContains(String string, String contains) {
        assertTrue(string.contains(contains),
                String.format("string '%s' won't contain '%s'", string, contains));
    }

    public static String getLastLineWithContent(Path file, String s) throws IOException {

        Scanner in;
        String lastLine = "";

        in = new Scanner(Files.newInputStream(file));
        while (in.hasNextLine()) {
            String line = in.nextLine();
            if (line.contains(s)) {
                lastLine = line;
            }
        }
        return lastLine;

    }

    public static String getLastLineOfLogFile(Path file) throws IOException {
        return getLastLineWithContent(file, "");
    }

    public static String getLastLineOfLogFile(Path file, int lastLines) throws IOException {
        Scanner in;
        StringBuilder result = new StringBuilder();

        in = new Scanner(Files.newInputStream(file));
        int countOfLines = 0;
        while (in.hasNextLine()) {
            countOfLines++;
            in.nextLine();
        }

        in = new Scanner(Files.newInputStream(file));
        int countOfReadInLines = 0;
        while (in.hasNextLine()) {
            countOfReadInLines++;
            String line = in.nextLine();
            if (countOfLines - countOfReadInLines <= lastLines) {
                result.append(line).append("\n");
            }
        }


        return result.toString();
    }

    public static void setSystemProperty(String value, String key) {
        if (value != null) {
            System.setProperty(key, value);
        } else {
            System.clearProperty(key);
        }
    }

    /**
     * Set the property 'log-level-sakuli' for the file 'sakuli-log-config.xml'.
     *
     * @param logLevel as String e.g. 'DEBUG'
     */
    public static void setSakuliLogLevel(String logLevel) {
        setSystemProperty(logLevel, "log-level-sakuli");
    }

    /**
     * Set the property 'log-level-sikuli' for the file 'sakuli-log-config.xml'.
     *
     * @param logLevel as String e.g. 'DEBUG'
     */
    public static void setSikuliLogLevel(String logLevel) {
        setSystemProperty(logLevel, "log-level-sikuli");
    }

    @BeforeClass(alwaysRun = true)
    public void setContextProperties() {
        setSakuliLogLevel("DEBUG");
        SakuliPropertyPlaceholderConfigurer.TEST_SUITE_FOLDER_VALUE = TEST_FOLDER_PATH;
        SakuliPropertyPlaceholderConfigurer.INCLUDE_FOLDER_VALUE = INCLUDE_FOLDER_PATH;
        SakuliPropertyPlaceholderConfigurer.SAHI_PROXY_HOME_VALUE = SAHI_FOLDER_PATH;
        BeanLoader.CONTEXT_PATH = TEST_CONTEXT_PATH;
        BeanLoader.refreshContext();
        loaderMock = BeanLoader.loadBean(BaseActionLoader.class);
        when(loaderMock.getSahiReport()).thenReturn(mock(Report.class));
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        setSakuliLogLevel(null);
    }

}
