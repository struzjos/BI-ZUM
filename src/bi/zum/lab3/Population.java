package bi.zum.lab3;

import cz.cvut.fit.zum.api.ga.AbstractEvolution;
import cz.cvut.fit.zum.api.ga.AbstractIndividual;
import cz.cvut.fit.zum.api.ga.AbstractPopulation;
import cz.cvut.fit.zum.data.StateSpace;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Josef Struz
 */
public class Population extends AbstractPopulation {

    public Population(AbstractEvolution evolution, int size) {
        individuals = new Individual[size];
        for (int i = 0; i < individuals.length; i++) {
            individuals[i] = new Individual(evolution, true);
            individuals[i].computeFitness();
        }

    }

    /**
     * Method to select individuals from population
     *
     * @param count The number of individuals to be selected
     * @return List of selected individuals
     */
    public List<AbstractIndividual> selectIndividuals(int count) {
        ArrayList<AbstractIndividual> selected = new ArrayList<AbstractIndividual>();

        // example of random selection of N individuals
        /*
        AbstractIndividual individual = individuals[rndm.nextInt(individuals.length)];
        while (selected.size() < count) {
            selected.add(individual);
            individual = individuals[rndm.nextInt(individuals.length)];
        }
         */
        // TODO: implement your own (and better) method of selection
        Random rndm = new Random();
        int membersCount = this.individuals.length / 10;
        if (membersCount < 10 && this.individuals.length >= 10) {
            membersCount = 10;
        } else if (membersCount == 0 && this.individuals.length >= 1) {
            membersCount = 1;
        }
        while (selected.size() < count) {
            //System.out.println("bi.zum.lab3.Population.selectIndividuals() " + membersCount);

            double bestFitness = Double.NEGATIVE_INFINITY;
            AbstractIndividual winner = null;

            for (int k = 0; k < membersCount; k++) {

                AbstractIndividual candidate = this.individuals[rndm.nextInt(this.individuals.length)];
                boolean alreadyThere = false;
                for( AbstractIndividual s : selected ){
                    if( candidate == s && this.individuals.length > 1 ){
                        alreadyThere = true;
                    }
                }
                if (candidate.getFitness() > bestFitness && !alreadyThere) {
                    winner = candidate;
                    bestFitness = candidate.getFitness();
                }
            }

            selected.add(winner);
        }

        return selected;
    }
}
