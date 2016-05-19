package common;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by mqueudot on 04/03/16.
 */
public class CLPsychLabelsTest {

    @Test
    public void testGetLabelFromId() throws Exception {
        assertEquals("CLPsychLabels.getLabelFromId should return Green when given 0 as parameter (first in enum)",
                CLPsychLabels.Green,CLPsychLabels.getLabelFromId(0));
    }
}