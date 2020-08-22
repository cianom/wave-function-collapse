package cianom.wfc.core.pipe.solver;


import cianom.wfc.core.api.Pattern;

public class PatternAtPosition {

    private final int position;
    private final Pattern pattern;

    public PatternAtPosition(final int position, final Pattern pattern) {
        this.position = position;
        this.pattern = pattern;
    }

    public int getPosition() {
        return this.position;
    }

    public Pattern getPattern() {
        return this.pattern;
    }

}
