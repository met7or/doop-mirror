package org.clyze.doop.common;

import java.util.*;
import java.util.function.Function;
import org.clyze.doop.common.Database;
import org.clyze.doop.common.PredicateFile;
import org.clyze.scanner.NativeDatabaseConsumer;

public class DatabaseConnector implements NativeDatabaseConsumer {
    private final Database db;
    private final Map<String, PredicateFile> predicates = new HashMap<>();

    public DatabaseConnector(Database db) {
        this.db = db;
    }

    public void add(String pfName, String arg, String... args) {
        if (pfName != null) {
            // Function<String, PredicateFile> mf = n -> PredicateFile.valueOf(n);
            db.add(predicates.computeIfAbsent(pfName, PredicateFile::valueOf), arg, args);
        } else
            System.err.println("Error: no output predicate '" + pfName + "' is available.");
    }

}