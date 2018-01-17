package com.digitalroute.input;

import com.digitalroute.output.BillingGateway;


import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class MyCallRecordProcessor implements CallRecordsProcessor {
    BillingGateway gateWay;
    Map<String, List<Session>> sessions = new HashMap<>();

    public MyCallRecordProcessor(BillingGateway gateWay) {
        this.gateWay = gateWay;
    }

    @java.lang.Override
    public void processBatch(InputStream in) {
        long start = System.currentTimeMillis();
        gateWay.beginBatch();

        try {
            byte[] buffer = new byte[1000];
            while (in.read(buffer) > 0) {
                processLine(buffer);
            }
        }
        catch (IOException e) {
            System.out.print(e.getMessage());
        }
        gateWay.endBatch(System.currentTimeMillis() - start);
    }

    public void processLine(byte[] buffer) {
        CDR cdr = createCDR(buffer);
        addToSessionOrFluch(cdr);
    }

    public void addToSessionOrFluch(CDR cdr) {

        Session session = addToSessions(cdr);

        if (session == null) {
            System.out.println("handle no matching session for callId = '_'");
            return;
        }

        Iterator<CDR> iter = session.cdrs.iterator();

        if (cdr.causeForOutput == 2) {
            int dur = 0;
            while (iter.hasNext()) {
                CDR r = iter.next();
                dur += r.duration;
            }

            gateWay.consume(cdr.callId, cdr.seqNum, cdr.aNum, cdr.bNum, cdr.causeForOutput, dur);
//            removeSession(cdr, session);
        }
    }

    private void removeSession(CDR cdr, Session session) {
        sessions.get(cdr.sessionKey());
    }

    /**
     *
     * @param cdr
     * @return null if not corresponds to valid session
     */
    private Session addToSessions(CDR cdr) {
        if (sessions.containsKey(cdr.sessionKey()) && cdr.isCompleateSessionKey()) {
            for (Session existingSession: sessions.get(cdr.sessionKey())) {
                if (existingSession.callId.equals(cdr.callId)) {
                    return existingSession;
                }
            };
            Session newSession = new Session(cdr.callId, cdr);
            sessions.get(cdr.sessionKey()).add(newSession);
            return newSession;
        } else if (sessions.containsKey(cdr.sessionKey())) {
            Session existingSession = sessions.get(cdr.sessionKey()).iterator().next();
            existingSession.cdrs.add(cdr);
            return existingSession;
        }

        // skipp cdrs with callId = '_' if no matching aNum & bNum
        return null;
    }


    public CDR createCDR(byte[] buffer) {
        String in = new String(buffer);

        int posCallID = in.indexOf(':');
        int posSeqNum = in.indexOf(',');
        int posANum = in.indexOf(',', posSeqNum+1);
        int posBNum = in.indexOf(',', posANum+1);
        int posCauseForOutput = in.indexOf(',', posBNum+1);
        int posDuration = in.indexOf('\n', posCauseForOutput+1);

        //        System.out.println(new String (buffer));
//        System.out.println(posCallID + " " + posSeqNum + " " + posANum + " " + posBNum + " " + posCauseForOutput);

        String callID = in.substring(0,posCallID);
        int seqNum = Integer.parseInt(in.substring(posCallID+1, posSeqNum));
        String aNum = in.substring(posSeqNum+1, posANum);
        String bNum = in.substring(posANum+1, posBNum);
        byte causeForOutput = buffer[posBNum+1];
        System.out.println(posCauseForOutput+1);
        System.out.println(posDuration);
        System.out.println(in.substring(posCauseForOutput+1, posDuration) + "!");
        int duration = Integer.parseInt(in.substring(posCauseForOutput+1, posDuration));

        return new CDR(callID, seqNum, aNum, bNum, causeForOutput, duration);
    }



}
