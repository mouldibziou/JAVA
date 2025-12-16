package smarthome.automation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import smarthome.core.CentralController;

/**
 * Manages and executes automation rules.
 */
public class AutomationEngine {
    private Map<String, AutomationRule> rules;
    private CentralController controller;

    public AutomationEngine(CentralController controller) {
        this.rules = new HashMap<>();
        this.controller = controller;
    }

    public void addRule(AutomationRule rule) {
        rules.put(rule.getName(), rule);
        System.out.println("Rule added: " + rule.getName());
    }

    public void removeRule(AutomationRule rule) {
        rules.remove(rule.getName());
        System.out.println("Rule removed: " + rule.getName());
    }

    public void removeRule(String ruleName) {
        if (rules.remove(ruleName) != null) {
            System.out.println("Rule removed: " + ruleName);
        }
    }

    public List<AutomationRule> getRules() {
        return new ArrayList<>(rules.values());
    }

    /**
     * Evaluates all active rules and executes actions for those that match.
     */
    public void evaluateRules() {
        for (AutomationRule rule : rules.values()) {
            try {
                rule.evaluateAndExecute(controller);
            } catch (Exception e) {
                System.err.println("Error executing rule " + rule.getName() + ": " + e.getMessage());
            }
        }
    }
}
