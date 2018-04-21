package bi.zum.lab3;

import cz.cvut.fit.zum.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.cvut.fit.zum.api.ga.AbstractEvolution;
import cz.cvut.fit.zum.api.ga.AbstractIndividual;
import java.util.Random;
import org.openide.util.lookup.ServiceProvider;

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
    private int debugLimit = 100;
    private Random rand = new Random();
    
    /**
     * The population to be used in the evolution
     */
    Population population;

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
        
        // Initialize the population
        population = new Population(this, populationSize);
        
        Random random = new Random();
        
        // Collect initial system time, average fitness, and the best fitness
        time.a = System.currentTimeMillis();
        avgFitness.a = population.getAvgFitness();
        AbstractIndividual best = population.getBestIndividual();
        bestFitness.a = best.getFitness();        

        // Show on map
        updateMap(best);
        System.out.println(population);

        
        // Run evolution cycle for the number of generations set in GUI
        for(int g=0; g < generations; g++) {

            // the evolution may be terminate from the outside using GUI button
            if (isFinished) {
                break;
            }
            
            // initialize the next generation's population
            ArrayList<AbstractIndividual> newInds = new ArrayList<AbstractIndividual>();
            
            // elitism: Preserve the best individual
            // (this is quite exploatory and may lead to premature convergence!)
            newInds.add(population.getBestIndividual().deepCopy());

            
            // keep filling the new population while not enough individuals in there
            while(newInds.size() < populationSize) {
                
                // select 2 parents
                List<AbstractIndividual> parents = population.selectIndividuals(2);
                
                Pair<AbstractIndividual,AbstractIndividual> offspring;
                
                // with some probability, perform crossover
                if(crossoverProbability < random.nextDouble()) {
                    offspring = parents.get(0).deepCopy().crossover(
                                    parents.get(1).deepCopy());
                }
                // otherwise, only copy the parents
                else {
                    offspring = new Pair<AbstractIndividual, AbstractIndividual>();
                    offspring.a = parents.get(0).deepCopy();
                    offspring.b = parents.get(1).deepCopy();
                }
                
                // mutate first offspring, add it to the new population
                offspring.a.mutate(mutationProbability);
                offspring.a.computeFitness();
                newInds.add(offspring.a);
                
                // if there is still space left in the new population, add also
                // the second offspring
                if(newInds.size() < populationSize) {
                    offspring.b.mutate(mutationProbability);
                    offspring.b.computeFitness();
                    newInds.add(offspring.b);
                }
            }
            
            // replace the current population with the new one
            for(int i=0; i<newInds.size(); i++) {
                population.setIndividualAt(i, newInds.get(i));
            }

            // print statistic
            System.out.println("gen: " + g + "\t bestFit: " + population.getBestIndividual().getFitness() + "\t avgFit: " + population.getAvgFitness());
            // for very long evolutions print best individual each 1000 generations

            if (g % debugLimit == 0) {
                best = population.getBestIndividual();
                updateMap(best);
            }
            updateGenerationNumber(g);
        }

        // === END ===
        time.b = System.currentTimeMillis();
        population.sortByFitness();
        avgFitness.b = population.getAvgFitness();
        best = population.getBestIndividual();
        bestFitness.b = best.getFitness();
        updateMap(best);
        System.out.println("Evolution has finished after " + ((time.b - time.a) / 1000.0) + " s...");
        System.out.println("avgFit(G:0)= " + avgFitness.a + " avgFit(G:" + (generations - 1) + ")= " + avgFitness.b + " -> " + ((avgFitness.b / avgFitness.a) * 100) + " %");
        System.out.println("bstFit(G:0)= " + bestFitness.a + " bstFit(G:" + (generations - 1) + ")= " + bestFitness.b + " -> " + ((bestFitness.b / bestFitness.a) * 100) + " %");
        System.out.println("bestIndividual= " + population.getBestIndividual());
        //System.out.println(pop);

        isFinished = true;
        System.out.println("========== Evolution finished =============");
    }
}
