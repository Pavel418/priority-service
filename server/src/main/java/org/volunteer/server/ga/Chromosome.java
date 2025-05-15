// Path: src/main/java/org/volunteer/server/ga/Chromosome.java
package org.volunteer.server.ga;

import java.util.Arrays;
import java.util.Random;

/** Candidate solution: genes[i] = index of service assigned to volunteer i. */
public final class Chromosome implements Comparable<Chromosome> {
    private static final Random RNG = new Random();

    public final int[] genes;        // mutable
    public double fitness;           // cached

    public Chromosome(int volunteerCount, int serviceCount) {
        this.genes = RNG.ints(volunteerCount, 0, serviceCount).toArray();
    }
    public Chromosome(int[] copy) { this.genes = Arrays.copyOf(copy, copy.length); }

    public void computeFitness(ProblemInstance inst) { this.fitness = FitnessCalculator.totalCost(genes, inst); }

    /** Uniform mutation: re‑assign one random volunteer. */
    public void mutate(int serviceCount) {
        int idx = RNG.nextInt(genes.length);
        genes[idx] = RNG.nextInt(serviceCount);
    }

    /** Single‑point crossover. */
    public static Chromosome crossover(Chromosome a, Chromosome b) {
        int len = a.genes.length;
        int cut = 1 + RNG.nextInt(len - 2);
        int[] child = new int[len];
        System.arraycopy(a.genes, 0, child, 0, cut);
        System.arraycopy(b.genes, cut, child, cut, len - cut);
        return new Chromosome(child);
    }

    @Override public int compareTo(Chromosome o) { return Double.compare(this.fitness, o.fitness); }
} 