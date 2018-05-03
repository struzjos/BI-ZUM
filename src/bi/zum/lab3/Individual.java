package bi.zum.lab3;

import cz.cvut.fit.zum.api.ga.AbstractEvolution;
import cz.cvut.fit.zum.api.ga.AbstractIndividual;
import cz.cvut.fit.zum.data.StateSpace;
import cz.cvut.fit.zum.util.Pair;
import cz.cvut.fit.zum.data.Edge;
//import cz.cvut.fit.zum.data.Nodes;
import cz.cvut.fit.zum.api.Node;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
//import java.util.Arrays;
import java.util.Random;
//import java.util.List;
//import java.util.Collections;
//import java.lang.Math;

/**
 * @author Josef Struz
 */
public class Individual extends AbstractIndividual {

    private double fitness = Double.NaN;
    private AbstractEvolution evolution = null;

    private boolean[] gen = null;
    private List<Integer> rndmEdges = new ArrayList<>();

    // @TODO Declare your genotype
    /**
     * Creates a new individual
     *
     * @param evolution The evolution object
     * @param init <code>true</code> if the individial should be initialized
     * randomly (we do wish to initialize if we copy the individual)
     */
    public Individual(AbstractEvolution evolution, boolean init) {
        this.evolution = evolution;
        this.gen = new boolean[evolution.getNodesCount()];
        Random rndm = new Random();

        for (int i = 0; i < StateSpace.getNodes().size(); i++) {
            this.rndmEdges.add(i);
        }

        if (init) {
            for (int i = 0; i < evolution.getNodesCount(); i++) {
                if (StateSpace.getNode(i).expand().size() <= 1 || rndm.nextBoolean()) {
                    this.gen[i] = false;
                } else {
                    this.gen[i] = true;
                }
            }
            this.repair();
            computeFitness();
            HillClimb(this.evolution.getGenerations()/100);
            this.minimalize();

        }
    }

    private void HillClimb(int numberOfSteps) {
        Random rndm = new Random();

        //int numberOfGens = (int)(this.evolution.getNodesCount() * this.evolution.getMutationProbability());
        int numberOfGenMutated = this.evolution.getNodesCount() / 50;

        //Individual next = new Individual(this.evolution, false);
        for (int i = 0; i < numberOfSteps; i++) {

            Individual next = this.deepCopy();
            for (int j = 0; j < numberOfGenMutated; j++) {
                int tmpRandInt = rndm.nextInt(this.evolution.getNodesCount());
                next.gen[tmpRandInt] = !next.gen[tmpRandInt];
            }
            next.repair();
            next.computeFitness();

            //double r = rndm.nextDouble();
            //double prop = Math.exp((next.getFitness() - this.getFitness()) / Math.log10(numberOfSteps - i));
            //System.out.println("i: " + i + ", this.fitness: " + this.fitness + ", next.fitness: " + next.getFitness());
            if (next.getFitness() >= this.getFitness()) {
                System.arraycopy(next.gen, 0, this.gen, 0, this.gen.length);
                this.fitness = next.fitness;
            }
            /*else if (r < prop) {
                //System.out.println("Prop: " + prop + ", this.fitness: " + this.fitness + ", next.fitness: " + next.getFitness() + ", rndm.nextDouble(): " + r);
                System.arraycopy(next.gen, 0, this.gen, 0, this.gen.length);
                this.fitness = next.fitness;
            }*/
        }
    }

    @Override
    public boolean isNodeSelected(int j) {
        return this.gen[j];
    }

    /**
     * Evaluate the value of the fitness function for the individual. After the
     * fitness is computed, the <code>getFitness</code> may be called
     * repeatedly, saving computation time.
     */
    @Override
    public void computeFitness() {
        this.fitness = this.gen.length;
        for (int i = 0; i < this.gen.length; i++) {
            if (this.gen[i]) {
                this.fitness--;
            }
        }
    }

    /**
     * Only return the computed fitness value
     *
     * @return value of fitness fucntion
     */
    @Override
    public double getFitness() {
        return this.fitness;
    }

    /**
     * Does random changes in the individual's genotype, taking mutation
     * probability into account.
     *
     * @param mutationRate Probability of a bit being inverted, i.e. a node
     * being added to/removed from the vertex cover.
     */
    @Override
    public void mutate(double mutationRate) {
        Random rndm = new Random();

        for (int i = 0; i < this.gen.length; i++) {
            if (rndm.nextDouble() < mutationRate) {
                this.gen[i] = !this.gen[i];
            }
        }
        this.repair();
        this.computeFitness();
        //HillClimb(evolution.getGenerations() / 100);
        this.minimalize();
    }

    /**
     * Crosses the current individual over with other individual given as a
     * parameter, yielding a pair of offsprings.
     *
     * @param partner The other individual to be crossed over with
     * @return A couple of offspring individuals
     */
    @Override
    public Pair crossover(AbstractIndividual partner) {

        Pair<Individual, Individual> result = new Pair();

        Random rndm = new Random();

        Individual son = new Individual(evolution, false);
        Individual deauther = new Individual(evolution, false);
        boolean change[] = new boolean[this.gen.length];
        int numOfChanges = 0;

        for (int i = 0; i < this.gen.length; i++) {
            son.gen[i] = this.gen[i];
            deauther.gen[i] = partner.isNodeSelected(i);
            change[i] = false;
        }

        int numberOfCuts = rndm.nextInt((this.evolution.getNodesCount() / 100) - 2) + 2;
        int sizeOfOneCut = this.evolution.getNodesCount() / numberOfCuts / 2;
        //int sizeOfLastCut = this.evolution.getNodesCount() % numberOfCuts;
        if (rndm.nextBoolean() && numberOfCuts >= 1) {
            numberOfCuts--;
        }
        if (rndm.nextBoolean() && sizeOfOneCut >= 1) {
            sizeOfOneCut--;
        }

        List<Integer> expand = new ArrayList<>();

        for (int i = 0; i < numberOfCuts; i++) {
            for (int j = 0; j < 10; j++) {
                //System.out.println("bi.zum.lab3.Individual.crossover()");
                int rndmNodeId = rndm.nextInt(this.gen.length);
                if (!change[rndmNodeId]) {
                    for (int k = 0; k < StateSpace.getNode(rndmNodeId).expand().size(); k++) {
                        if (!change[StateSpace.getNode(rndmNodeId).expand().get(k).getId()]) {
                            expand.add(StateSpace.getNode(rndmNodeId).expand().get(k).getId());
                        }
                    }
                    break;
                }
            }
            for (int j = 0; j < sizeOfOneCut && !expand.isEmpty(); j++) {
                if (!change[expand.get(0)]) {
                    son.gen[expand.get(0)] = partner.isNodeSelected(expand.get(0));
                    deauther.gen[expand.get(0)] = this.isNodeSelected(expand.get(0));
                    change[expand.get(0)] = true;
                    numOfChanges++;
                    for (int k = 0; k < StateSpace.getNode(expand.get(0)).expand().size(); k++) {
                        if (!change[StateSpace.getNode(expand.get(0)).expand().get(k).getId()]) {
                            expand.add(StateSpace.getNode(expand.get(0)).expand().get(k).getId());
                        }
                    }
                    expand.remove(0);
                } else {
                    expand.remove(0);
                    j--;
                }
            }
            expand.clear();
        }
        /*
        System.out.println("numOfChanges: " + numOfChanges + " / " + evolution.getNodesCount());

        int cut1 = rndm.nextInt(this.gen.length);
        int cut2 = rndm.nextInt(this.gen.length);

        if (cut1 > cut2) {
            int tmp = cut1;
            cut1 = cut2;
            cut2 = tmp;
        }


        for (int i = 0; i < cut1; i++) {
            son.gen[i] = this.gen[i];
            deauther.gen[i] = other.isNodeSelected(i);
        }

        for (int i = cut1; i < cut2; i++) {
            son.gen[i] = other.isNodeSelected(i);
            deauther.gen[i] = this.gen[i];
        }

        for (int i = cut2; i < this.gen.length; i++) {
            son.gen[i] = this.gen[i];
            deauther.gen[i] = other.isNodeSelected(i);
        }

         */
        son.repair();
        deauther.repair();

        result.a = son;
        result.b = deauther;

        return result;
    }

    /**
     * When you are changing an individual (eg. at crossover) you probably don't
     * want to affect the old one (you don't want to destruct it). So you have
     * to implement "deep copy" of this object.
     *
     * @return identical individual
     */
    @Override
    public Individual deepCopy() {
        Individual newOne = new Individual(evolution, false);

        System.arraycopy(this.gen, 0, newOne.gen, 0, this.gen.length);

        newOne.fitness = this.fitness;
        return newOne;
    }

    /**
     * Repairs the genotype to make it valid, i.e. ensures all the edges are in
     * the vertex cover.
     */
    private void repair() {

        /* We iterate over all the edges */
        Random rndm = new Random();
        /*
        for (Node n : StateSpace.getNodes()) {
            if (this.gen[n.getId()] && n.expand().size() <= 1) {
                this.gen[n.getId()] = false;
            }
        }
         */
        for (Edge e : StateSpace.getEdges()) {
            if (!this.gen[e.getFromId()] && !this.gen[e.getToId()]) {
                if (StateSpace.getNode(e.getFromId()).expand().size() <= 1) {
                    this.gen[e.getToId()] = true;
                } else if (StateSpace.getNode(e.getToId()).expand().size() <= 1) {
                    this.gen[e.getFromId()] = true;
                } else if (rndm.nextBoolean()) {
                    this.gen[e.getFromId()] = true;
                } else {
                    this.gen[e.getToId()] = true;
                }
            }
        }
    }

    private int minimalPotencial() {
        //Random rndm = new Random();
        boolean unnecessary;
        int counter = 0;
        for (Node n : StateSpace.getNodes()) {
            unnecessary = true;
            for (Node ne : n.expand()) {
                if (!this.gen[ne.getId()]) {
                    unnecessary = false;
                    break;
                }
            }
            if (unnecessary) {
                counter++;
            }
        }
        return counter;
    }

    private void minimalize() {
        Collections.shuffle(this.rndmEdges);

        boolean unnecessary;

        for (int num : this.rndmEdges) {
            Node n = StateSpace.getNode(num);
            if (n.expand().size() > 1 && this.gen[n.getId()]) {
                unnecessary = true;
                for (Node ne : n.expand()) {
                    if (!this.gen[ne.getId()] || ne.getId() == n.getId()) {
                        unnecessary = false;
                        break;
                    }
                }
                if (unnecessary) {
                    this.gen[n.getId()] = false;
                }
            }
        }
        //this.repair();
    }

    /**
     * Return a string representation of the individual.
     *
     * @return The string representing this object.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        /* TODO: implement own string representation, such as a comma-separated
         * list of indices of nodes in the vertex cover
         */
        int i = 0;

        for (; i < this.gen.length; i++) {
            if (this.gen[i]) {
                sb.append(i);
                i++;
                break;
            }
        }
        for (; i < this.gen.length; i++) {
            if (this.gen[i]) {
                sb.append(", ");
                sb.append(i);
            }
        }
        return sb.toString();
    }
}
