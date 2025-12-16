package smarthome.automation;

import smarthome.core.CentralController;

/**
 * Represents an automation rule consisting of a condition and an action.
 * Follows the IF-THEN pattern.
 */
public class AutomationRule {
    private String name;
    private Condition condition;
    private Action action;
    private boolean isActive;

    /**
     * Creates a new AutomationRule.
     * 
     * @param name      The name of the rule.
     * @param condition The condition to evaluate.
     * @param action    The action to execute if the condition is met.
     */
    public AutomationRule(String name, Condition condition, Action action) {
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException("Rule name cannot be empty");
        if (condition == null)
            throw new IllegalArgumentException("Condition cannot be null");
        if (action == null)
            throw new IllegalArgumentException("Action cannot be null");

        this.name = name;
        this.condition = condition;
        this.action = action;
        this.isActive = true;
    }

    /**
     * Checks the condition and executes the action if the condition is true and the
     * rule is active.
     * 
     * @param controller The central controller.
     */
    public void evaluateAndExecute(CentralController controller) {
        if (!isActive)
            return;

        if (condition.evaluate(controller)) {
            System.out.println("Rule Triggered: " + name);
            action.execute(controller);
        }
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
