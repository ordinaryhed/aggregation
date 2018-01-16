package com.digitalroute.input;

import com.digitalroute.output.BillingGateway;


import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class MyCallRecordProcessor implements CallRecordsProcessor {
    BillingGateway gateWay;
    Map<String, List<CDR>> cache = new HashMap<>();

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
        addToCacheOrFluch(cdr);
    }

    public void addToCacheOrFluch(CDR cdr) {
        List<CDR> list = null;
        if (cache.containsKey(cdr.cacheKey())) {
            list = cache.get(cdr.cacheKey());
        } else {
            list = new ArrayList<CDR>();
        }

        Iterator<CDR> iter = list.iterator();

        list.add(cdr);

        if (cdr.causeForOutput == 2) {
            int dur = 0;
            while (iter.hasNext()) {
                CDR r = iter.next();
                dur += r.duration;
            }

            gateWay.consume(cdr.callId, cdr.seqNum, cdr.aNum, cdr.bNum, cdr.causeForOutput, dur);
            cache.remove(cdr.cacheKey());
        }
        else {
            cache.put(cdr.cacheKey(), list);
        }
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
