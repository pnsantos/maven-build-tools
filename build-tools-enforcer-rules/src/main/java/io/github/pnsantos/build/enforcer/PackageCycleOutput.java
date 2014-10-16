package pt.ptinovacao.iam.build.enforcer;

import pt.ptinovacao.iam.build.enforcer.util.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jdepend.framework.JavaClass;
import jdepend.framework.JavaPackage;
import pt.ptinovacao.iam.build.enforcer.comparator.JavaClassComparator;

public final class PackageCycleOutput {

    private final PackageCycleGraph graph;
    private final StringBuilder output;

    public PackageCycleOutput(PackageCycleGraph graph) {
        this.graph = graph;
        this.output = new StringBuilder();
    }

    public static PackageCycleOutput from(List<JavaPackage> packages) {
        return new PackageCycleOutput(new PackageCycleGraph(packages));
    }

    @Override
    public String toString() {
        for (List<JavaPackage> packageCycles : graph.collectCycles()) {
            for (JavaPackage currentPackage : packageCycles) {
                output.append("\n    ").append(currentPackage.getName()).append(" depends on:");

                for (JavaPackage packageInCycle : packageCycles) {
                    appendCyclicPackageDependency(currentPackage, packageInCycle);
                }
            }
        }

        return output.append("\n").toString();
    }

    private void appendCyclicPackageDependency(JavaPackage currentPackage, JavaPackage packageInCycle) {
        if (currentPackage.equals(packageInCycle)) {
            return;
        }

        List<JavaClass> affectedClasses = getAffectedClasses(currentPackage, packageInCycle);
        if (!affectedClasses.isEmpty()) {
            output.append("\n        ").append(packageInCycle.getName()).append(" (");

            for (JavaClass affectedClass : affectedClasses) {
                output.append(getClassName(affectedClass)).append(", ");
            }

            output.replace(output.length() - ", ".length(), output.length(), ")");
        }
    }

    private String getClassName(JavaClass affectedClass) {
        return affectedClass.getName().substring(affectedClass.getPackageName().length() + 1);
    }

    private List<JavaClass> getAffectedClasses(JavaPackage currentPackage, JavaPackage packageInCycle) {
        List<JavaClass> affectedClasses = new ArrayList<>();

        List<JavaClass> currentPackageClasses = Lists.uncheckedCast(currentPackage.getClasses(), JavaClass.class);
        for (JavaClass javaClass : currentPackageClasses) {
            if (javaClass.getImportedPackages().contains(packageInCycle)) {
                affectedClasses.add(javaClass);
            }
        }

        Collections.sort(affectedClasses, JavaClassComparator.INSTANCE);

        return affectedClasses;
    }
}
