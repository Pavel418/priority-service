// Path: src/main/java/org/volunteer/server/ga/GeneticAlgorithm.java
package org.volunteer.server.ga;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Simple steady‑state GA good enough for ≤ 30 volunteers, 10 services. */
public final class GeneticAlgorithm {

    private static final int POP_SIZE = 50;
    private static final int GENERATIONS = 400;
    private static final double MUTATION_RATE = 0.12;
    private static final Random RNG = new Random();

    private GeneticAlgorithm() {}

    public static int[] run(ProblemInstance inst) {
        List<Chromosome> pop = initPopulation(inst);
        Chromosome best = pop.get(0);

        for (int g = 0; g < GENERATIONS; g++) {
            Chromosome p1 = tournament(pop);
            Chromosome p2 = tournament(pop);
            Chromosome child = Chromosome.crossover(p1, p2);

            if (RNG.nextDouble() < MUTATION_RATE) {
                child.mutate(inst.services().size());
            }
            child.computeFitness(inst);

            // Replace worst
            pop.sort(Collections.reverseOrder());
            pop.set(0, child);

            if (child.fitness < best.fitness) best = child;
        }
        return best.genes;
    }

    /* ---------- helpers ---------- */
    private static List<Chromosome> initPopulation(ProblemInstance inst) {
        List<Chromosome> pop = new ArrayList<>(POP_SIZE);
        int v = inst.volunteers().size();
        int s = inst.services().size();

        for (int i = 0; i < POP_SIZE; i++) {
            Chromosome ch = new Chromosome(v, s);
            ch.computeFitness(inst);
            pop.add(ch);
        }
        pop.sort(null);
        return pop;
    }

    private static Chromosome tournament(List<Chromosome> pop) {
        Chromosome a = pop.get(RNG.nextInt(pop.size()));
        Chromosome b = pop.get(RNG.nextInt(pop.size()));
        return a.fitness < b.fitness ? a : b;
    }
} 