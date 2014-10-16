package pt.ptinovacao.iam.build.enforcer.comparator;

import java.util.Comparator;
import jdepend.framework.JavaPackage;

public enum JavaPackageComparator implements Comparator<JavaPackage> {

    INSTANCE;

    @Override
    public int compare(JavaPackage pkg1, JavaPackage pkg2) {
        return pkg1.getName().compareTo(pkg2.getName());
    }
}
