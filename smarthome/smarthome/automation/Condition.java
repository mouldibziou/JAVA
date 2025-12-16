package smarthome.automation;

import smarthome.core.CentralController;

/**
 * Functional interface for defining conditions in automation rules.
 */
public interface Condition {
    /**
     * Evaluates the condition based on the current system state.
     * 
     * @param controller The central controller providing access to the system.
     * @return true if the condition is met, false otherwise.
     */
    boolean evaluate(CentralController controller);
}
