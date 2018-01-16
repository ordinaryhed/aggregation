package com.digitalroute.input;

public class CDR {
    /*
    Call identity string terminated by `":"`.
    `_` (underscore) represents an incomplete record.
    */
    String callId;

    /*
    A sequence number terminated by `","`
     */
    int seqNum;

    /*
    A-number string (the one making the call) terminated by `","`
     */
    String aNum;

    /*
    B-number string (the one receiving the call) terminated by `","`
     */
    String bNum;

    /*
    The reason record was created, terminated by `","`.
            * `1` represents ongoing call
            * `2` represents end of call
            * `0` is used for `incomplete records` (can appear durin g network problems)
     */
    byte causeForOutput;

    int duration;

    CDR(String callId, int seqNum, String aNum, String bNum, byte causeForOutput, int duration) {
        this.callId = callId;
        this.seqNum = seqNum;
        this.aNum = aNum;
        this.bNum = bNum;
        this.causeForOutput = causeForOutput;
        this.duration = duration;
    }

    public String cacheKey() {
        return callId + " " + aNum + " " + bNum;
    }
}