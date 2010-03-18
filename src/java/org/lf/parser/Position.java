package org.lf.parser;

public interface Position {
    // Note: the correspondence Position <=> Record (by Log.readRecord) is M:1,
    // that is, there may exist non-equal positions A,B such that readRecord(A)==readRecord(B).
}