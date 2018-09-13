/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.exceptions;

/**
 * This exceptions should be used when the cost and risk module fails
 * @author nhitomi
 */
public class CostRiskException extends Exception{
    
    private static final long serialVersionUID = -582204603813522326L;

    public CostRiskException() {
    }

    public CostRiskException(String message) {
        super(message);
    }

    public CostRiskException(String message, Throwable cause) {
        super(message, cause);
    }

    public CostRiskException(Throwable cause) {
        super(cause);
    }

    public CostRiskException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }   
}
