package bi.zum.lab3;

import cz.cvut.fit.zum.api.ga.AbstractPopulation;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Josef Struz
 */
public class Population extends AbstractPopulation {

    //@Override
    private Individual[] individuals;

    public Population(Evolution evolution, int size, ArrayList<Integer> trueVal, ArrayList<Integer> falseVal) {
        individuals = new Individual[size];
        for (int i = 0; i < individuals.length; i++) {
            individuals[i] = new Individual(evolution, true, trueVal, falseVal);
        }

    }

    @Override
    public int size() {
        return individuals.length;
    }

    @Override
    public double getAvgFitness() {
        double fitSum = 0.0;
        for (Individual individual : individuals) {
            fitSum += individual.getFitness();
        }
        return fitSum / individuals.length;
    }

    @Override
    public Individual getBestIndividual() {
        Individual bestIndividual = individuals[0];
        for (Individual individual : individuals) {
            if (individual.getFitness() > bestIndividual.getFitness()) {
                bestIndividual = individual;
            }
        }
        return bestIndividual;
    }
    
    @Override
    public double getBestFitness() {
        double bestFitness = 0.0;
        for (Individual individual : individuals) {
            if (individual.getFitness() > bestFitness) {
                bestFitness = individual.getFitness();
            }
        }
        return bestFitness;
    }
    
    public void setIndividualAt(int index, Individual individual) {
        individuals[index] = individual;
    }

    public int getLowestDistance() {
        int tmpLowestDistance = Integer.MAX_VALUE;
        for (Individual individual : individuals) {
            if (individual.getDistance() < tmpLowestDistance) {
                tmpLowestDistance = individual.getDistance();
            }
        }
        return tmpLowestDistance;
    }
    
    public void replaceTheWeakestOne(Individual individual){
        int index = 0;
        double lowestFitness = individuals[0].getFitness();
        for (int i = 0; i < individuals.length; i++) {
            if (individuals[i].getFitness() < lowestFitness) {
                index = i;
                lowestFitness = individuals[i].getFitness();
            }
        }
        individuals[index] = individual;
    }

    public List<Individual> selectIndividuals(int count) {
        ArrayList<Individual> selected = new ArrayList<>();

        Random rndm = new Random();
        int membersCount = this.individuals.length / 10 * 4;
        if (membersCount == 0 && this.individuals.length >= 1) {
            membersCount = 1;
        }
        while (selected.size() < count) {

            double bestFitness = Double.NEGATIVE_INFINITY;
            Individual winner = null;

            for (int k = 0; k < membersCount; k++) {

                Individual candidate = this.individuals[rndm.nextInt(this.individuals.length)];
                boolean alreadyThere = false;
                for (Individual s : selected) {
                    if (candidate == s && this.individuals.length > 1) {
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
