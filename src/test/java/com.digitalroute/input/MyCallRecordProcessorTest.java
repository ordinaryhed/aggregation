package com.digitalroute.input;

import com.digitalroute.output.BillingGateway;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MyCallRecordProcessorTest {
    @Test
    public void addToCacheOrFluch() throws Exception {
        MyCallRecordProcessor processor = getMyCallRecordProcessor();
        InputStream is = new ByteArrayInputStream("1K:2,555555,666666,1,15\n1B:1,111111,222222,2,5\n1K:3,555555,666666,1,15\n1K:4,555555,666666,2,2\n".getBytes(Charset.defaultCharset()));
        processor.processBatch(is);

    }

    @Test
    public void processBatch() throws Exception {
    }

    @Test
    public void processLine() throws Exception {
    }

    @Test
    public void createCDR() throws Exception {
        MyCallRecordProcessor a = getMyCallRecordProcessor();

        String in = "1B:1,111111,222222,2,5\n";
        CDR cdr = a.createCDR(in.getBytes());
        assertEquals("1B", cdr.callId);
        assertEquals(1, cdr.seqNum);
        assertEquals("111111", cdr.aNum);
        assertEquals("222222", cdr.bNum);
        assertEquals('2', cdr.causeForOutput);
        assertEquals(5, cdr.duration);

        in = "1K:3,555555,666666,1,15\n";
        cdr = a.createCDR(in.getBytes());
        assertEquals("1K", cdr.callId);
        assertEquals(3, cdr.seqNum);
        assertEquals("555555", cdr.aNum);
        assertEquals("666666", cdr.bNum);
        assertEquals('1', cdr.causeForOutput);
        assertEquals(15, cdr.duration);

    }

    private MyCallRecordProcessor getMyCallRecordProcessor() {
        return new MyCallRecordProcessor(new BillingGateway() {
                public List<CDR> list = new ArrayList<>();

                @Override
                public void beginBatch() {

                }

                @Override
                public void consume(String callId, int seqNum, String aNum, String bNum, byte causeForOutput, int duration) {
                    System.out.println(callId + " " + seqNum + " " + aNum + " " + bNum + " " + causeForOutput + " " + duration);
                }

                @Override
                public void endBatch(long totalDuration) {

                }

                @Override
                public void logError(ErrorCause errorCause, String callId, int seqNum, String aNum, String bNum) {

                }
            });
    }

}
