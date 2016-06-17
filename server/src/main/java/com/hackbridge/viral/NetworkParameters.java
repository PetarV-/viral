package com.hackbridge.viral;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * This represents the internal properties of the multiplex network used in StateManager, such as
 * the different transition probabilities.
 */
public class NetworkParameters {

    /* Default parameters */

    // The probability a new node has the disease (INFECTED or CARRIER)
    private double initialInfectedProbability = 0.20;

    // The probability a new node has AwarenessState Aware.
    private double initialAwareProbability = 0.20;

    // The probability that a new diseased node is INFECTED (instead of CARRIER).
    private double initialSymptomaticProbability = 0.20;

    // The probability a vaccinated node becomes diseased when its edge with another diseased
    // node is activated.
    private double infectedIfVaccinatedProbability = 0.05;

    // The probability that a diseased node spontaneously recovers when one of its edges is
    // activated.
    private double spontaneousRecoveryProbability = 0.03;

    // The probability that an edge is activated.
    private double activateEdgeProbability = 0.10;

    // The probability that a new node has the INFECTOR role (instead of HUMAN).
    private double infectorProbability =  0.30;

    // The probability that a CARRIER node becomes INFECTED.
    private double developSymptomsProbability = 0.10;

    // Parameters used in exponentiating to invert the distance.
    // Inverted distance = exponentialMultiplier*e^(-lambdaFactor*distance).
    private double lambdaFactor = 0.002;
    private double exponentialMultiplier = 1000.0;

    // Frequency of logging to file. The current state will be logged every loggingFrequency
    // updates.
    private int loggingFrequency = 1;


    // Number of steps when selecting edges using Gibbs Sampling.
    private int numStepsForGibbsSampling = 1000;

    public enum EdgeSelectionAlgorithm {
        // An edge with distance/weight d is selected exactly proportional to d.
        // This requires storing the O(n^2) distances between every node.
        ExactRandom,

        // An edge (x,y) with distance d is maintained. x or y are changed uniformly at random.
        // Assume wlog x is changed to x' st (x', y) has distance d'. If d' <= d, then accept
        // this step. Otherwise accept with probability d/d'.
        GibbsSampling,
    }

    private EdgeSelectionAlgorithm edgeSelectionAlgorithm = EdgeSelectionAlgorithm.ExactRandom;

    public NetworkParameters() {}

    /**
     * Parameters can be read from the configuration file formatted as
     * parameterName parameterValue
     *
     * Example:
     * initialInfectedProbability 0.2
     * initialAwareProbability 0.2
     * edgeSelectionAlgorithm GibbsSampling
     * @param configurationFile
     */
    public NetworkParameters(String configurationFile) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(configurationFile));
            String line;

            Class c = this.getClass();

            while ((line = br.readLine()) != null) {
                try {
                    String[] str = line.split("\\s+");
                    if (str.length < 2) {
                        continue;
                    }

                    Field field = c.getDeclaredField(str[0]);
                    Class<?> fieldClass = field.getType();
                    if (fieldClass.isAssignableFrom(int.class)) {
                        int value = Integer.parseInt(str[1]);
                        field.setInt(this, value);
                    } else if (fieldClass.isAssignableFrom(double.class)) {
                        double value = Double.parseDouble(str[1]);
                        field.setDouble(this, value);
                    } else if (fieldClass.isAssignableFrom(EdgeSelectionAlgorithm.class)) {
                        field.set(this, Enum.valueOf((Class<Enum>) field.getType(), str[1]));
                    }
                } catch (Exception e) {
                    System.err.println("Failed to parse " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double getInitialInfectedProbability() {
        return initialInfectedProbability;
    }

    public void setInitialInfectedProbability(double initialInfectedProbability) {
        this.initialInfectedProbability = initialInfectedProbability;
    }

    public double getInitialAwareProbability() {
        return initialAwareProbability;
    }

    public void setInitialAwareProbability(double initialAwareProbability) {
        this.initialAwareProbability = initialAwareProbability;
    }

    public double getInitialSymptomaticProbability() {
        return initialSymptomaticProbability;
    }

    public void setInitialSymptomaticProbability(double initialSymptomaticProbability) {
        this.initialSymptomaticProbability = initialSymptomaticProbability;
    }

    public double getInfectedIfVaccinatedProbability() {
        return infectedIfVaccinatedProbability;
    }

    public void setInfectedIfVaccinatedProbability(double infectedIfVaccinatedProbability) {
        this.infectedIfVaccinatedProbability = infectedIfVaccinatedProbability;
    }

    public double getSpontaneousRecoveryProbability() {
        return spontaneousRecoveryProbability;
    }

    public void setSpontaneousRecoveryProbability(double spontaneousRecoveryProbability) {
        this.spontaneousRecoveryProbability = spontaneousRecoveryProbability;
    }

    public double getActivateEdgeProbability() {
        return activateEdgeProbability;
    }

    public void setActivateEdgeProbability(double activateEdgeProbability) {
        this.activateEdgeProbability = activateEdgeProbability;
    }

    public double getInfectorProbability() {
        return infectorProbability;
    }

    public void setInfectorProbability(double infectorProbability) {
        this.infectorProbability = infectorProbability;
    }

    public double getDevelopSymptomsProbability() {
        return developSymptomsProbability;
    }

    public void setDevelopSymptomsProbability(double developSymptomsProbability) {
        this.developSymptomsProbability = developSymptomsProbability;
    }

    public double getLambdaFactor() {
        return lambdaFactor;
    }

    public void setLambdaFactor(double lambdaFactor) {
        this.lambdaFactor = lambdaFactor;
    }

    public double getExponentialMultiplier() {
        return exponentialMultiplier;
    }

    public void setExponentialMultiplier(double exponentialMultiplier) {
        this.exponentialMultiplier = exponentialMultiplier;
    }

    public EdgeSelectionAlgorithm getEdgeSelectionAlgorithm() {
        return edgeSelectionAlgorithm;
    }

    public int getNumStepsForGibbsSampling() {
        return numStepsForGibbsSampling;
    }



}
