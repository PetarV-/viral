package com.hackbridge.viral;

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

    public NetworkParameters() {}

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
}
