// Path: src/main/java/org/volunteer/server/ga/Chromosome.java
package org.volunteer.server.util;

import java.util.Arrays;
import java.util.Random;

import org.volunteer.server.model.ProblemInstance;

/**
 * Represents a potential solution in the genetic algorithm's population.
 * <p>
 * Encodes volunteer-to-service assignments where genes[i] contains the service index
 * assigned to the i-th volunteer. Fitness is calculated relative to a ProblemInstance.
 * Not thread-safe due to mutable genes and cached fitness state.
 */
public final class Chromosome implements Comparable<Chromosome> {
    private static final Random RNG = new Random();

    /**
     * Mutable gene sequence where index represents volunteer ID, and value represents
     * assigned service index. Direct modifications require subsequent fitness recalculation.
     */
    public final int[] genes;

    /**
     * Cached fitness score calculated against a ProblemInstance. Must be explicitly
     * updated after any gene modifications for accurate comparisons.
     */
    public double fitness;

    /**
     * Creates a chromosome with random service assignments.
     * @param volunteerCount number of volunteers to assign
     * @param serviceCount range for service indices [0, serviceCount)
     */
    public Chromosome(int volunteerCount, int serviceCount) {
        this.genes = RNG.ints(volunteerCount, 0, serviceCount).toArray();
    }

    /**
     * Creates a chromosome through deep copying of an existing gene sequence.
     * @param copy gene sequence to duplicate
     */
    public Chromosome(int[] copy) {
        this.genes = Arrays.copyOf(copy, copy.length);
    }

    /**
     * Computes and caches fitness score based on problem constraints.
     * @param inst problem context containing cost calculation rules
     */
    public void computeFitness(ProblemInstance inst) {
        this.fitness = FitnessCalculator.totalCost(genes, inst);
    }

    /**
     * Randomly alters one volunteer's service assignment.
     * @param serviceCount range for new service index [0, serviceCount)
     */
    public void mutate(int serviceCount) {
        int idx = RNG.nextInt(genes.length);
        genes[idx] = RNG.nextInt(serviceCount);
    }

    /**
     * Creates offspring through single-point crossover of two parents.
     * @param a first parent chromosome
     * @param b second parent chromosome
     * @return new chromosome combining genetic material from both parents
     */
    public static Chromosome crossover(Chromosome a, Chromosome b) {
        int len = a.genes.length;
        int cut = 1 + RNG.nextInt(len - 2);
        int[] child = new int[len];
        System.arraycopy(a.genes, 0, child, 0, cut);
        System.arraycopy(b.genes, cut, child, cut, len - cut);
        return new Chromosome(child);
    }

    /**
     * Compares chromosomes by fitness for selection purposes.
     * @return negative, zero, or positive if this fitness is less than, equal to,
     *         or greater than the other's fitness
     */
    @Override
    public int compareTo(Chromosome o) {
        return Double.compare(this.fitness, o.fitness);
    }
}