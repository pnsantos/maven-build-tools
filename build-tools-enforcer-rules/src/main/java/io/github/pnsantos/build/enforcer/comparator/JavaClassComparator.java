package io.github.pnsantos.build.enforcer.comparator;

import java.util.Comparator;
import jdepend.framework.JavaClass;

public enum JavaClassComparator implements Comparator<JavaClass> {

    INSTANCE;

    @Override
    public int compare(JavaClass class1, JavaClass class2) {
        return class1.getName().compareTo(class2.getName());
    }
}
