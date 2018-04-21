package bi.zum.lab3;

import cz.cvut.fit.zum.api.ga.AbstractEvolution;
import cz.cvut.fit.zum.api.ga.AbstractIndividual;
import cz.cvut.fit.zum.data.StateSpace;
import cz.cvut.fit.zum.util.Pair;
import cz.cvut.fit.zum.data.Edge;
import java.util.Arrays;
import java.util.Random;


/**
 * @author Josef Struz
 */
public class Individual extends AbstractIndividual {

    private double fitness = Double.NaN;
    private AbstractEvolution evolution;
    
    private boolean[] gen;
    private int power; 
    
    
    // @TODO Declare your genotype
    

    /**
     * Creates a new individual
     * 
     * @param evolution The evolution object
     * @param randomInit <code>true</code> if the individial should be
     * initialized randomly (we do wish to initialize if we copy the individual)
     */
    public Individual(AbstractEvolution evolution, boolean randomInit) {
        this.evolution = evolution;
        this.gen = new boolean[evolution.getNodesCount()];
        this.power = 0;
        Random rndm = new Random();
        
        if(randomInit) {
            for(int i=0; i<evolution.getNodesCount(); i++) {
                if (rndm.nextBoolean()){
                    this.gen[i] = true;
                    this.power++;
                }
                else
                    this.gen[i] = false;
            }

            this.repair();
            
        }
    }

    @Override
    public boolean isNodeSelected(int j) {
        
        return this.gen[j];
    }

    /**
     * Evaluate the value of the fitness function for the individual. After
     * the fitness is computed, the <code>getFitness</code> may be called
     * repeatedly, saving computation time.
     */
    @Override
    public void computeFitness() {
        this.fitness = (this.gen.length - this.power);
    }

    public void calculatePower() {
        int tmp = 0;
        
        for(int i=0; i<this.gen.length; i++) {
            if(this.gen[i]) tmp++;
        }
        
        this.power = tmp;
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
        
        for (int i = 0 ; i < this.gen.length ; i++){
            if ( rndm.nextDouble() < mutationRate ){
                this.gen[i] = !this.gen[i];
                if (this.gen[i])
                    this.power++;
                else
                    this.power--;
            }
        }
        this.repair();
    }
    
    /**
     * Crosses the current individual over with other individual given as a
     * parameter, yielding a pair of offsprings.
     * 
     * @param other The other individual to be crossed over with
     * @return A couple of offspring individuals
     */
    @Override
    public Pair crossover(AbstractIndividual other) {

        Pair<Individual,Individual> result = new Pair();
        
        Random rndm = new Random();
        
        int cut1 = rndm.nextInt(this.gen.length);
        int cut2 = rndm.nextInt(this.gen.length);
        
        if(cut1 > cut2)
        {
            int tmp = cut1;
            cut1 = cut2;
            cut2 = tmp;
        }
        
        Individual son = new Individual(evolution, false);
        Individual deauther = new Individual(evolution, false);
        
        for(int i=0; i<cut1; i++) {
            son.gen[i] = this.gen[i];
            deauther.gen[i] = other.isNodeSelected(i);
        }
        
        for(int i=cut1; i<cut2; i++) {
            son.gen[i] = other.isNodeSelected(i);
            deauther.gen[i] = this.gen[i];
        }
        
        for(int i=cut2; i<this.gen.length; i++) {
            son.gen[i] = this.gen[i];
            deauther.gen[i] = other.isNodeSelected(i);
        }
        
        son.calculatePower();
        deauther.calculatePower();
        
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

        newOne.power = this.power;
        newOne.fitness = this.fitness;
        return newOne;
    }
    
    /**
    * Repairs the genotype to make it valid, i.e. ensures all the edges
    * are in the vertex cover.
    */
    private void repair() {
 
        /* We iterate over all the edges */
        Random rndm = new Random();
        for(Edge e : StateSpace.getEdges()) { 
            if(!this.gen[e.getFromId()] && !this.gen[e.getToId()]) {
                if(rndm.nextBoolean()) {
                    this.gen[e.getFromId()] = true;
                }
                else {
                    this.gen[e.getToId()] = true;
                }
                this.power++;
            }
        }
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
        
        for(; i < this.gen.length; i++){
            if ( this.gen[i] ){
                sb.append(i);
                i++;
                break;
            }
        }
        for(; i < this.gen.length; i++){
            if ( this.gen[i] ){
                sb.append(", ");
                sb.append(i);
            }
        }
       
        return sb.toString();
    }
}
