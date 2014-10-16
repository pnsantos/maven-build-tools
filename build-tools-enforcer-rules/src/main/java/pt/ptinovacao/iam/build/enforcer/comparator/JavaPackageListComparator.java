package pt.ptinovacao.iam.build.enforcer.comparator;

import java.util.Comparator;
import java.util.List;
import jdepend.framework.JavaPackage;

public enum JavaPackageListComparator implements Comparator<List<JavaPackage>> {

    INSTANCE;

    @Override
    public int compare(List<JavaPackage> pkg1, List<JavaPackage> pkg2) {
        return pkg1.get(0).getName().compareTo(pkg2.get(0).getName());
    }
}
