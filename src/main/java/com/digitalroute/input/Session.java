package com.digitalroute.input;

import java.util.LinkedList;
import java.util.List;

public class Session {
    public List<CDR> cdrs = new LinkedList<>();
    public String callId;

    Session(String callId, CDR cdr) {
        this.callId = callId;
        cdrs.add(cdr);
    };
}
