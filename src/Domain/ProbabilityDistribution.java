/*
 * Data structure for NeedlemanWunsch with string similarity.
 */
package Domain;

public class ProbabilityDistribution {

    private String code;
    private Double probability;

    public ProbabilityDistribution() {

    }

    public ProbabilityDistribution(String code, double probability) {
        this.code = code;
        this.probability = probability;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Double getProbability() {
        return probability;
    }

    public void setProbability(Double probability) {
        this.probability = probability;
    }
}
