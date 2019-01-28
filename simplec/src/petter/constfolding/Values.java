package petter.constfolding;

import petter.cfg.expression.Variable;

import java.util.HashMap;
import java.util.Map;

public class Values {

    private HashMap<Variable, Value> mapping;

    private Values(HashMap<Variable, Value> mapping) {
        this.mapping = mapping;
    }

    static final Values bottom = new Values(null);
    static final Values top = new Values(new HashMap<>());

    static Values makeValues(HashMap<Variable, Value> values) {
        assert (values != null);
        return new Values(values);
    }

    boolean isBottom() {
        return mapping == null;
    }

    Value getValue(Variable variable) {
        assert(!isBottom());
        return mapping.getOrDefault(variable, Value.top);
    }

    Values with(Variable var, Value value) {
        if (!value.isTop()) {
            final HashMap<Variable, Value> newMapping =
                    mapping != null
                    ? (HashMap<Variable, Value>) mapping.clone()
                    : new HashMap<>();
            newMapping.put(var, value);
            return makeValues(newMapping);
        } else {
            return this;
        }
    }

    public String toString() {
        if (isBottom()) {
            return "B";
        } else {
            final StringBuilder b = new StringBuilder();
            for (Map.Entry<Variable , Value> e: mapping.entrySet()) {
                b.append(e.getKey()).append("=").append(e.getValue()).append("; ");
            }
            b.append("_=").append(Value.top).append(";");
            return b.toString();
        }
    }

    static Values join(Values lhs, Values rhs) {
        if (lhs.isBottom()) {
            return rhs;
        } else if (rhs.isBottom()) {
            return lhs;
        } else {
            final HashMap<Variable , Value> newMapping = new HashMap<>();

            for (Variable key : lhs.mapping.keySet()) {
                final Value lhsValue = lhs.getValue(key);
                final Value rhsValue = rhs.getValue(key);

                final Value newValue = Value.join(lhsValue, rhsValue);
                if (!newValue.isTop()) {
                    newMapping.put(key, newValue);
                }
            }

            return new Values(newMapping);
        }
    }

    static boolean isLessEqual(Values lhs, Values rhs) {
        if (lhs.isBottom()) {
            return true;
        } else if (rhs.isBottom()) {
            return false;
        } else {
            for (Variable key : rhs.mapping.keySet()) {
                final Value lhsValue = lhs.getValue(key);
                final Value rhsValue = rhs.getValue(key);

                if (!Value.isLessEqual(lhsValue, rhsValue)) {
                    return false;
                }
            }

            return true;
        }
    }
}
