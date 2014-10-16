package io.github.pnsantos.build.enforcer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;

import jdepend.framework.JDepend;
import jdepend.framework.JavaPackage;
import io.github.pnsantos.build.enforcer.util.Lists;

public final class NoPackageCyclesRule implements EnforcerRule {

    private static final String MAVEN_ARTIFACT_ID = "${project.artifactId}";
    private static final String MAVEN_SKIP_ANALYSIS = "${enforcer.cyclic.packages.skip}";
    private static final String MAVEN_BUILD_OUTPUT_DIR = "${project.build.outputDirectory}";
    private static final String MAVEN_BUILD_OUTPUT_TST_DIR = "${project.build.testOutputDirectory}";
    private final List<File> directories = new LinkedList<>();
    private boolean failOnViolation = true;
    private EnforcerRuleHelper helper;
    private Log logger;

    @Override
    public void execute(EnforcerRuleHelper erh) throws EnforcerRuleException {
        init(erh);

        if (doSkip()) {
            logger.info("Skipping package cyclic dependency check (skip flag is enabled).");
            return;
        }

        addDirectory(MAVEN_BUILD_OUTPUT_DIR);
        addDirectory(MAVEN_BUILD_OUTPUT_TST_DIR);

        if (directories.isEmpty()) {
            logger.info("Skipping package cyclic dependency check (no classes found).");
            return;
        }

        final String artifactId = evaluate(MAVEN_ARTIFACT_ID);

        logger.info(String.format("Searching %s packages for cyclic dependencies.", artifactId));

        JDepend jDepend = new JDepend();
        for (File directory : directories) {
            addDirectoryToJDepend(jDepend, directory);
        }

        jDepend.analyze();

        if (jDepend.containsCycles()) {
            List<JavaPackage> packages = Lists.uncheckedCast(jDepend.getPackages(), JavaPackage.class);

            PackageCycleOutput output = PackageCycleOutput.from(packages);

            String message = String.format("Package cycles detected @ %s%n%s", artifactId, output.toString());

            if (failOnViolation) {
                throw new EnforcerRuleException(message);
            } else {
                logger.warn(message);
            }
        } else {
            logger.info(String.format("No cyclic dependencies were found in %s.", artifactId));
        }
    }

    @Override
    public boolean isCacheable() {
        return false;
    }

    @Override
    public boolean isResultValid(EnforcerRule er) {
        return false;
    }

    @Override
    public String getCacheId() {
        return "";
    }

    public boolean isFailOnViolation() {
        return failOnViolation;
    }

    public void setFailOnViolation(boolean failOnViolation) {
        this.failOnViolation = failOnViolation;
    }

    private void init(EnforcerRuleHelper erh) {
        helper = erh;
        logger = erh.getLog();
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> castToList(Collection collection, Class<T> clazz) {
        return new ArrayList<>((Collection<T>) collection);
    }

    private boolean doSkip() throws EnforcerRuleException {
        return Boolean.parseBoolean(evaluate(MAVEN_SKIP_ANALYSIS));
    }

    private void addDirectoryToJDepend(JDepend jDepend, File folder) throws EnforcerRuleException {
        try {
            jDepend.addDirectory(folder.getAbsolutePath());
        } catch (IOException ex) {
            throw new EnforcerRuleException("Unable to access target folder '" + ex.getLocalizedMessage() + "'", ex);
        }
    }

    private void addDirectory(String var) throws EnforcerRuleException {
        File directory = new File(evaluate(var));
        if (directory.canRead()) {
            helper.getLog().info("Adding folder " + directory.getAbsolutePath() + " for package cycles search.");
            directories.add(directory);
        } else {
            helper.getLog().info("Folder " + directory.getAbsolutePath() + " could not be found.");
        }
    }

    private String evaluate(String var) throws EnforcerRuleException {
        try {
            return (String) helper.evaluate(var);
        } catch (ExpressionEvaluationException ex) {
            throw new EnforcerRuleException("Unable to lookup an expression '" + ex.getLocalizedMessage() + "'", ex);
        }
    }
}
