package io.github.pnsantos.build.enforcer;

import io.github.pnsantos.build.enforcer.util.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import jdepend.framework.JavaPackage;
import io.github.pnsantos.build.enforcer.comparator.JavaPackageComparator;
import io.github.pnsantos.build.enforcer.comparator.JavaPackageListComparator;

public final class PackageCycleGraph {

    private final DirectedGraph<JavaPackage, DefaultEdge> graph;

    public PackageCycleGraph(List<JavaPackage> packages) {
        this.graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        buildGraphFromPackageList(packages);
    }

    public List<List<JavaPackage>> collectCycles() {
        List<Set<JavaPackage>> cycles = new StrongConnectivityInspector<>(graph).stronglyConnectedSets();

        Iterator<Set<JavaPackage>> iter = cycles.iterator();
        while (iter.hasNext()) {
            Set<JavaPackage> packages = iter.next();
            if (packages.size() == 1) {
                iter.remove();
            }
        }

        return sort(cycles);
    }

    private void buildGraphFromPackageList(List<JavaPackage> packages) {
        for (JavaPackage pkg : packages) {
            graph.addVertex(pkg);
        }

        for (JavaPackage pkg : packages) {
            List<JavaPackage> edges = Lists.uncheckedCast(pkg.getEfferents(), JavaPackage.class);

            for (JavaPackage edge : edges) {
                graph.addEdge(pkg, edge);
            }
        }
    }

    private List<List<JavaPackage>> sort(List<Set<JavaPackage>> cycles) {
        List<List<JavaPackage>> orderedCycles = new ArrayList<>();

        for (Set<JavaPackage> cycle : cycles) {
            List<JavaPackage> cycleList = new ArrayList<>(cycle);

            Collections.sort(cycleList, JavaPackageComparator.INSTANCE);

            orderedCycles.add(cycleList);
        }

        Collections.sort(orderedCycles, JavaPackageListComparator.INSTANCE);

        return orderedCycles;
    }
}
