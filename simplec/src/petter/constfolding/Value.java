package petter.constfolding;

class Value {

    private Integer value;

    private Value(Integer value) {
        this.value = value;
    }

    static Value top = new Value(null);

    static Value makeValue(int value) {
        return new Value(value);
    }

    static Value join(Value lhs, Value rhs) {
        if (lhs.isTop()) {
            return lhs;
        } else if (rhs.isTop()) {
            return rhs;
        } else if (lhs.getValue() == rhs.getValue()) {
            return lhs;
        } else {
            return top;
        }
    }

    static boolean isLessEqual(Value lhs, Value rhs) {
        if (rhs.isTop()) {
            return true;
        } else if (lhs.isTop()) {
            return false;
        } else {
            return lhs.getValue() == rhs.getValue();
        }
    }

    boolean isTop() {
        return value == null;
    }

    int getValue() {
        assert(!isTop());
        return value;
    }

    public String toString() {
        return !isTop() ? value + "" : "T";
    }
}
