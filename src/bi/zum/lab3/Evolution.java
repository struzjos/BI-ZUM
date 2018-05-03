package bi.zum.lab3;

import cz.cvut.fit.zum.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.cvut.fit.zum.api.ga.AbstractEvolution;
import cz.cvut.fit.zum.api.ga.AbstractIndividual;
import java.util.Random;
import org.openide.util.lookup.ServiceProvider;

interface ConstantStuff {

    public static final int NUM_OF_ISLANDS = 4;
}

/**
 * @author Your name
 */
@ServiceProvider(service = AbstractEvolution.class)
public class Evolution extends AbstractEvolution<Individual> implements Runnable {

    /**
     * start and final average fitness
     */
    private Pair<Double, Double> avgFitness;
    /**
     * start and final best fitness in whole population
     */
    private Pair<Double, Double> bestFitness;
    /**
     * start and final time
     */
    private Pair<Long, Long> time;
    /**
     * How often to print status of evolution
     */
    private int debugLimit = 1;
    private Random rand = new Random();

    /**
     * The population to be used in the evolution
     */
    public Evolution() {
        isFinished = false;
        avgFitness = new Pair<Double, Double>();
        bestFitness = new Pair<Double, Double>();
        time = new Pair<Long, Long>();
    }

    @Override
    public String getName() {
        return "My evolution";
    }

    @Override
    public void run() {

        List<Population> islands = new ArrayList<>();
        int oneIslePopSize = populationSize / ConstantStuff.NUM_OF_ISLANDS;
        int lastIslePopSize = populationSize / ConstantStuff.NUM_OF_ISLANDS + populationSize % ConstantStuff.NUM_OF_ISLANDS;
        // Initialize the population
        int n = ConstantStuff.NUM_OF_ISLANDS;
        if (lastIslePopSize != 0) {
            n--;
        }
        for (int i = 0; i < n; i++) {
            Population population = new Population(this, oneIslePopSize);
            islands.add(population);
        }
        if (lastIslePopSize != 0) {
            Population population = new Population(this, lastIslePopSize);
            islands.add(population);
        }

        Random random = new Random();

        // Collect initial system time, average fitness, and the best fitness
        time.a = System.currentTimeMillis();

        double bestFit = 0;
        double fitSum = 0.0;
        AbstractIndividual best = null;
        for (Population population : islands) {
            fitSum += population.getAvgFitness() * population.size();
            if (population.getBestIndividual().getFitness() > bestFit) {
                best = population.getBestIndividual();
                bestFit = population.getBestIndividual().getFitness();
            }
        }
        avgFitness.a = fitSum / populationSize;
        bestFitness.a = bestFit;

        // Show on map
        updateMap(best);
        for (Population population : islands) {
            System.out.println(population);
        }

        // Run evolution cycle for the number of generations set in GUI
        int disasterCounter = 0;
        double previousBestFit = bestFit;
        for (int g = 0; g < generations; g++) {

            // the evolution may be terminate from the outside using GUI button
            if (isFinished) {
                break;
            }

            List<Thread> threads = new ArrayList<>();

            for (int i = 0; i < islands.size(); i++) {
                Thread t = new Thread(new SwitchGeneration("thread " + i, islands.get(i), mutationProbability, crossoverProbability));
                t.start();
                threads.add(t);
            }
            for (Thread t : threads) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    System.out.println("Thread interrupted.");
                }
            }

            // for very long evolutions print best individual each 1000 generations
            /*
            if (g % 50 == 0) {
                islands.get(0).sortByFitness();
                for (int i = 1; i < islands.size(); i++) {
                    islands.get(i).sortByFitness();
                    AbstractIndividual tmp2Individual = islands.get(i).getIndividual(0);
                    islands.get(i).setIndividualAt(0, islands.get(i - 1).getIndividual(0));
                    islands.get(i - 1).setIndividualAt(0, tmp2Individual);
                }
            }
             */
            for (int i = 0; i < islands.size(); i++) {
                islands.get(i).sortByFitness();
            }
            if (g % 50 == 0) {
                for (int i = 0; i < islands.size(); i++) {
                    islands.get(i).setIndividualAt(0, islands.get((i + 1) % islands.size()).selectIndividuals(1).get(0));
                }
            }
            bestFit = 0;
            for (Population population : islands) {
                //System.out.println("Population best fitness: " + population.getBestIndividual().getFitness());
                if (population.getBestIndividual().getFitness() > bestFit) {
                    best = population.getBestIndividual();
                    bestFit = population.getBestIndividual().getFitness();
                }
            }
            System.out.println("gen: " + g + "\t bestFit: " + bestFit + "\t disasterCounter:" + disasterCounter);

            if (previousBestFit == bestFit) {
                disasterCounter++;
            } else {
                disasterCounter = 0;
            }
            previousBestFit = bestFit;
            if (disasterCounter == 50) {
                System.out.println("Disaster");
                int isleDestroyed = rand.nextInt(islands.size());
                int nextIsle = (isleDestroyed + 1) % islands.size();
                islands.get(nextIsle).sortByFitness();
                islands.get(isleDestroyed).sortByFitness();
                islands.get(nextIsle).setIndividualAt(0, islands.get(isleDestroyed).getBestIndividual());
                Population population = new Population(this, islands.get(isleDestroyed).size());
                islands.set(isleDestroyed, population);
                disasterCounter = 0;
            }
            if (g % debugLimit == 0) {
                updateMap(best);
            }
            updateGenerationNumber(g);
        }

        // === END ===
        time.b = System.currentTimeMillis();
        System.out.println("Evolution has finished after " + ((time.b - time.a) / 1000.0) + " s...");

        for (Population population : islands) {
            population.sortByFitness();
            avgFitness.b = population.getAvgFitness();
            best = population.getBestIndividual();
            bestFitness.b = best.getFitness();
            //updateMap(best);
            System.out.println("avgFit(G:0)= " + avgFitness.a + " avgFit(G:" + (generations - 1) + ")= " + avgFitness.b + " -> " + ((avgFitness.b / avgFitness.a) * 100) + " %");
            System.out.println("bstFit(G:0)= " + bestFitness.a + " bstFit(G:" + (generations - 1) + ")= " + bestFitness.b + " -> " + ((bestFitness.b / bestFitness.a) * 100) + " %");
            System.out.println("bestIndividual= " + population.getBestIndividual());
            //System.out.println(pop);

        }
        isFinished = true;
        System.out.println("========== Evolution finished =============");
    }
}

class SwitchGeneration extends Thread {

    private Thread t;
    private String threadName;
    private Population population;
    private double mutationProbability;
    private double crossoverProbability;

    SwitchGeneration(String name, Population pop, double mutProb, double crossProb) {
        this.threadName = name;
        this.population = pop;
        this.mutationProbability = mutProb;
        this.crossoverProbability = crossProb;
    }

    @Override
    public void run() {
        Random rndm = new Random();

        // initialize the next generation's population
        ArrayList<AbstractIndividual> newInds = new ArrayList<>();

        // elitism: Preserve the best individual
        // (this is quite exploatory and may lead to premature convergence!)
        newInds.add(population.getBestIndividual().deepCopy());

        // keep filling the new population while not enough individuals in there
        while (newInds.size() < population.size()) {

            // select 2 parents
            List<AbstractIndividual> parents = population.selectIndividuals(2);

            Pair<AbstractIndividual, AbstractIndividual> offspring;

            // with some probability, perform crossover
            if (crossoverProbability < rndm.nextDouble()) {
                offspring = parents.get(0).deepCopy().crossover(
                        parents.get(1).deepCopy());
                offspring.a.computeFitness();
                offspring.b.computeFitness();

            } // otherwise, only copy the parents
            else {
                offspring = new Pair<>();
                offspring.a = parents.get(0).deepCopy();
                offspring.b = parents.get(1).deepCopy();
            }

            // mutate first offspring, add it to the new population
            offspring.a.mutate(mutationProbability);
            offspring.a.computeFitness();

            newInds.add(offspring.a);

            if (newInds.size() < population.size()) {
                offspring.b.mutate(mutationProbability);
                offspring.b.computeFitness();

                // if there is still space left in the new population, add also
                // the second offspring
                newInds.add(offspring.b);
            }
        }

        // replace the current population with the new one
        for (int i = 0; i < newInds.size(); i++) {
            population.setIndividualAt(i, newInds.get(i));
        }

        // print statistic
        //System.out.println("bestFit: " + population.getBestIndividual().getFitness() + "\t avgFit: " + population.getAvgFitness());
        //System.out.println("end(" + threadName + ")");
    }

    public void start() {
        System.out.println("start(" + threadName + ")");
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }
}
