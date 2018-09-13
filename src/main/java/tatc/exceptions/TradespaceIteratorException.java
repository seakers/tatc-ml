/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.exceptions;

/**
 * This exceptions should be used when the tradespace iterator fails
 * @author nhitomi
 */
public class TradespaceIteratorException extends Exception{
    
    private static final long serialVersionUID = -582204603813522326L;

    public TradespaceIteratorException() {
    }

    public TradespaceIteratorException(String message) {
        super(message);
    }

    public TradespaceIteratorException(String message, Throwable cause) {
        super(message, cause);
    }

    public TradespaceIteratorException(Throwable cause) {
        super(cause);
    }

    public TradespaceIteratorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }   
}
