//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
package tatc.architecture.variable;

import java.text.MessageFormat;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Variable;

public class RealVariable implements Variable {
    private static final long serialVersionUID = 3141851312155686224L;
    private static final String VALUE_OUT_OF_BOUNDS = "value out of bounds (value: {0}, min: {1}, max: {2})";
    private double value;
    private final double lowerBound;
    private final double upperBound;

    public RealVariable(double lowerBound, double upperBound) {
        this(0.0D / 0.0, lowerBound, upperBound);
    }

    public RealVariable(double value, double lowerBound, double upperBound) {
        this.value = value;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        if (value < lowerBound || value > upperBound) {
            throw new IllegalArgumentException(MessageFormat.format("value out of bounds (value: {0}, min: {1}, max: {2})", value, lowerBound, upperBound));
        }
    }

    public double getValue() {
        return this.value;
    }

    public void setValue(double value) {
        if (value >= this.lowerBound && value <= this.upperBound) {
            this.value = value;
        } else {
            throw new IllegalArgumentException(MessageFormat.format("value out of bounds (value: {0}, min: {1}, max: {2})", value, this.lowerBound, this.upperBound));
        }
    }

    public double getLowerBound() {
        return this.lowerBound;
    }

    public double getUpperBound() {
        return this.upperBound;
    }

    public RealVariable copy() {
        return new RealVariable(this.value, this.lowerBound, this.upperBound);
    }

    public String toString() {
        return Double.toString(this.value);
    }

    public int hashCode() {
        return (new HashCodeBuilder()).append(this.lowerBound).append(this.upperBound).append(this.value).toHashCode();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj != null && obj.getClass() == this.getClass()) {
            RealVariable rhs = (RealVariable)obj;
            return (new EqualsBuilder()).append(this.lowerBound, rhs.lowerBound).append(this.upperBound, rhs.upperBound).append(this.value, rhs.value).isEquals();
        } else {
            return false;
        }
    }

    public void randomize() {
        this.setValue(PRNG.nextDouble(this.lowerBound, this.upperBound));
    }
}
