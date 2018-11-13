package tatc.tradespaceiterator;
import tatc.architecture.variable.MonolithVariable;
import java.util.Properties;
import java.util.Set;


public class StandardFormProblem {
    private StandardFormProblemImplementation problem;

    private StandardFormProblemProperties properties;

    public StandardFormProblem(TradespaceSearchRequest tsr, Properties properties, ProblemType type) {
        this.properties=new StandardFormProblemProperties(tsr,properties);
        switch (type){
            case FF:
                this.problem=new StandardFormProblemFullFactorial(this.properties);
                break;
            case EPS:
                this.problem=new StandardFormMOEA(this.properties);
                break;
            case AOS:
                this.problem=new StandardFormAOS(this.properties);
                break;
            case KDO:
                this.problem=new StandardFormKDO(this.properties);
                break;
        }

    }

    public Set<MonolithVariable> getExistingSatellites(){
        return properties.existingSatellites;
    }

    /**
     * Shuts down the search and releases any resources. Shuts down any threads
     * dedicated to search.
     */
    public void shutdown() {
        properties.rm.shutdown();
    }

    public enum ProblemType{
        FF,
        EPS,
        AOS,
        KDO
    }

    public void run(){
        problem.start();
    }

}
