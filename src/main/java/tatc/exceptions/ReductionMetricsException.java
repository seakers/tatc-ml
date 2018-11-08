/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.exceptions;

/**
 * This exceptions should be used when the reduction and metrics module fails 
 * @author nhitomi
 */
public class ReductionMetricsException extends Exception{
    
    private static final long serialVersionUID = -589434436651402241L;

    public ReductionMetricsException() {
    }

    public ReductionMetricsException(String message) {
        super(message);
    }

    public ReductionMetricsException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReductionMetricsException(Throwable cause) {
        super(cause);
    }

    public ReductionMetricsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
