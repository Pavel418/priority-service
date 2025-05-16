package org.volunteer.server.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.volunteer.server.model.ProblemInstance;
import org.volunteer.server.util.Chromosome;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public final class GeneticAlgorithmService {

    private final int POP_SIZE;
    private final int MAX_GENERATIONS;
    private final double MUTATION_RATE;

    private final Random RNG;

    public GeneticAlgorithmService(
        @Value("${application.settings.population-size}") int popSize, 
        @Value("${application.settings.max-generations}") int maxGenerations, 
        @Value("${application.settings.mutation-rate}") double mutationRate)
    {
        this.POP_SIZE = popSize; 
        this.MAX_GENERATIONS = maxGenerations; 
        this.MUTATION_RATE = mutationRate;
        this.RNG = new Random();
    }

    public int[] run(ProblemInstance inst) {
        log.info("Genetic algorithm request received");

        List<Chromosome> pop = initPopulation(inst);
        Chromosome best = pop.get(0);

        for (int g = 0; g < MAX_GENERATIONS; g++) {
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

        log.info("Finished genetic algorithm");

        return best.genes;
    }

    /* ---------- helpers ---------- */
    private List<Chromosome> initPopulation(ProblemInstance inst) {
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

    private Chromosome tournament(List<Chromosome> pop) {
        Chromosome a = pop.get(RNG.nextInt(pop.size()));
        Chromosome b = pop.get(RNG.nextInt(pop.size()));
        return a.fitness < b.fitness ? a : b;
    }
} 