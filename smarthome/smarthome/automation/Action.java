package smarthome.automation;

import smarthome.core.CentralController;

/**
 * Functional interface for defining actions in automation rules.
 */
public interface Action {
    /**
     * Executes the action on the system.
     * 
     * @param controller The central controller providing access to the system.
     */
    void execute(CentralController controller);
}
