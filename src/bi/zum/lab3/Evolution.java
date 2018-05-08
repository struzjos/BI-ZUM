package bi.zum.lab3;

import cz.cvut.fit.zum.util.Pair;
import cz.cvut.fit.zum.api.Node;
import java.util.ArrayList;
import java.util.List;
import cz.cvut.fit.zum.data.StateSpace;
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
    private ArrayList<List<Integer>> paths = new ArrayList<>();
    private ArrayList<Integer> trueValues = new ArrayList<>();
    private ArrayList<Integer> falseValues = new ArrayList<>();

    public int lowestDistance = Integer.MAX_VALUE;
    public double bestFit = 0.0;
    public double fitSum = 0.0;
    public int disasterCounter = 0;

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

    private class MyNode {

        List<Integer> expand;
        int id;

        public MyNode() {
            expand = new ArrayList<>();
        }
    }

    private void optimalize() {
        boolean genValues[] = new boolean[StateSpace.nodesCount()];
        boolean genAlreadyPut[] = new boolean[StateSpace.nodesCount()];
        MyNode myNodes[] = new MyNode[StateSpace.nodesCount()];
        for (int i = 0; i < StateSpace.nodesCount(); i++) {
            MyNode tmpMyNode = new MyNode();
            tmpMyNode.id = StateSpace.getNode(i).getId();
            for (Node n : StateSpace.getNode(i).expand()) {
                tmpMyNode.expand.add(n.getId());
            }
            myNodes[i] = tmpMyNode;
            genValues[i] = false;
            genAlreadyPut[i] = false;
        }
        boolean change = true;

        for (int i = 0; i < StateSpace.nodesCount(); i++) {
            for (int exp : myNodes[i].expand) {
                if (exp == i) {
                    genValues[i] = true;
                }
            }
        }

        while (change) {
            change = false;
            for (int i = 0; i < StateSpace.nodesCount(); i++) {
                if (myNodes[i].expand.size() == 1 && !genValues[myNodes[i].id]) {
                    change = true;
                    for (int j = 0; j < myNodes[myNodes[i].expand.get(0)].expand.size(); j++) {
                        if (myNodes[myNodes[i].expand.get(0)].expand.get(j) == myNodes[i].id) {
                            myNodes[myNodes[i].expand.get(0)].expand.remove(j);
                            break;
                        }
                    }
                    genValues[myNodes[i].expand.get(0)] = true;

                    myNodes[i].expand.clear();

                    falseValues.add(myNodes[i].id);
                }
            }
            for (int i = 0; i < StateSpace.nodesCount(); i++) {
                if (genValues[myNodes[i].id] && !genAlreadyPut[myNodes[i].id]) {
                    for (int exp : myNodes[i].expand) {
                        for (int j = 0; j < myNodes[exp].expand.size(); j++) {
                            if (myNodes[exp].expand.get(j) == myNodes[i].id && exp != myNodes[i].id) {
                                myNodes[exp].expand.remove(j);
                                break;
                            }
                        }
                    }
                    myNodes[i].expand.clear();
                    genAlreadyPut[i] = true;

                    trueValues.add(myNodes[i].id);
                }
            }
        }
    }

    @Override
    public void run() {

        this.optimalize();
        /*
        System.out.println("TrueValues size: " + trueValues.size() + ", FalseValue size: " + falseValues.size());
        for( int i : trueValues )
            System.out.println("TrueValue: " + i);
        for( int i : falseValues )
            System.out.println("FalseValue: " + i);
         */
        List<Population> islands = new ArrayList<>();
        int oneIslePopSize = populationSize / ConstantStuff.NUM_OF_ISLANDS;
        int lastIslePopSize = populationSize / ConstantStuff.NUM_OF_ISLANDS + populationSize % ConstantStuff.NUM_OF_ISLANDS;
        int n = ConstantStuff.NUM_OF_ISLANDS;
        if (lastIslePopSize != 0) {
            n--;
        }
        for (int i = 0; i < n; i++) {
            Population population = new Population(this, oneIslePopSize, trueValues, falseValues);
            islands.add(population);
        }
        if (lastIslePopSize != 0) {
            Population population = new Population(this, lastIslePopSize, trueValues, falseValues);
            islands.add(population);
        }

        time.a = System.currentTimeMillis();

        AbstractIndividual best = null;
        for (Population population : islands) {
            fitSum += population.getAvgFitness() * population.size();
            if (population.getBestIndividual().getFitness() > bestFit) {
                best = population.getBestIndividual();
                bestFit = population.getBestIndividual().getFitness();
            }
            if (population.getLowestDistance() < lowestDistance) {
                lowestDistance = population.getLowestDistance();
            }
        }
        avgFitness.a = fitSum / populationSize;
        bestFitness.a = bestFit;

        // Show on map
        updateMap(best);
        /*
        for (Population population : islands) {
            System.out.println(population);
        }
         */

        
        double previousBestFit = bestFit;
        for (int g = 0; g < generations; g++) {

            if (isFinished) {
                updateMap(best);
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
                    System.out.println("Thread interrupted. Err: " + e);
                }
            }

            if (g % 50 == 0) {
                for (int i = 0; i < islands.size(); i++) {
                    islands.get(i).replaceTheWeakestOne(islands.get((i + 1) % islands.size()).selectIndividuals(1).get(0));
                }
            }
            bestFit = 0;
            for (Population population : islands) {
                //System.out.println("Population best fitness: " + population.getBestIndividual().getFitness());
                if (population.getBestIndividual().getFitness() > bestFit) {
                    best = population.getBestIndividual();
                    bestFit = population.getBestIndividual().getFitness();
                }
                if (population.getLowestDistance() < lowestDistance) {
                    lowestDistance = population.getLowestDistance();
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
                islands.get(nextIsle).replaceTheWeakestOne(islands.get(isleDestroyed).getBestIndividual());
                Population population = new Population(this, islands.get(isleDestroyed).size(), trueValues, falseValues);
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
            avgFitness.b = population.getAvgFitness();
            best = population.getBestIndividual();
            bestFitness.b = best.getFitness();
            System.out.println("avgFit(G:0)= " + avgFitness.a + " avgFit(G:" + (generations - 1) + ")= " + avgFitness.b + " -> " + ((avgFitness.b / avgFitness.a) * 100) + " %");
            System.out.println("bstFit(G:0)= " + bestFitness.a + " bstFit(G:" + (generations - 1) + ")= " + bestFitness.b + " -> " + ((bestFitness.b / bestFitness.a) * 100) + " %");
            System.out.println("bestIndividual= " + population.getBestIndividual());

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

        ArrayList<Individual> newInds = new ArrayList<>();

        newInds.add(population.getBestIndividual().deepCopy());

        while (newInds.size() < population.size()) {

            List<Individual> parents = population.selectIndividuals(2);

            Pair<Individual, Individual> offspring;

            if (crossoverProbability < rndm.nextDouble()) {
                offspring = parents.get(0).deepCopy().crossover(
                        parents.get(1).deepCopy());

            } 
            else {
                offspring = new Pair<>();
                offspring.a = parents.get(0).deepCopy();
                offspring.b = parents.get(1).deepCopy();
            }

            offspring.a.mutate(mutationProbability);

            newInds.add(offspring.a);

            if (newInds.size() < population.size()) {
                offspring.b.mutate(mutationProbability);

                newInds.add(offspring.b);
            }
        }

        for (int i = 0; i < newInds.size(); i++) {
            population.setIndividualAt(i, newInds.get(i));
        }
    }

    public void start() {
        System.out.println("start(" + threadName + ")");
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }
}
