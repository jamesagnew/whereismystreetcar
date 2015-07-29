package ca.wimsc.client.common.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

public class NumbersAndTimestampsTest {

    @Test
    public void testAddNumber() {
        
        long nextTimestamp = System.currentTimeMillis();
        NumbersAndTimestamps nat = new NumbersAndTimestamps();
        
        List<NumberAndTimestamp> expected = new ArrayList<NumberAndTimestamp>();
        for (int i = 0; i < 24; i++) {
            
            nextTimestamp = nextTimestamp + 12345;
            NumberAndTimestamp next = new NumberAndTimestamp(i, new Date(nextTimestamp));
            expected.add(next);
            nat.addNumber(next, 24);
        }
        
        String encoded = nat.marshall();
        System.out.println("Encoded is " + encoded.length() + " - " + encoded);
        
        NumbersAndTimestamps actual = new NumbersAndTimestamps(nat.marshall());
        for (int i = 0; i < 24; i++) {
            Assert.assertEquals(expected.get(i), actual.getValues().get(i));
        }

        nextTimestamp = nextTimestamp + 12345;
        NumberAndTimestamp next = new NumberAndTimestamp(25, new Date(nextTimestamp));
        expected.add(next);
        nat.addNumber(next, 24);

        actual = new NumbersAndTimestamps(nat.marshall());
        for (int i = 0; i < 24; i++) {
            Assert.assertEquals(expected.get(i + 1), actual.getValues().get(i));
        }
        
    }
    
}
