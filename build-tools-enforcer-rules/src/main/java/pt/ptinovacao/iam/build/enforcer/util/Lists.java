package pt.ptinovacao.iam.build.enforcer.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class Lists {

    private Lists() {
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> uncheckedCast(Collection collection, Class<T> clazz) {
        return new ArrayList<>((Collection<T>) collection);
    }
}
