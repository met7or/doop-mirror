package org.clyze.doop.soot;

import org.clyze.doop.common.DoopErrorCodeException;

class MissingClassesException extends DoopErrorCodeException {
    private final String[] classes;

    public MissingClassesException(String[] classes) {
        super(21);
        this.classes = classes;
    }
}
