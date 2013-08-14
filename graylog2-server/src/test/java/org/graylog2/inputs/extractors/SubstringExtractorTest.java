/**
 * Copyright 2013 Lennart Koopmann <lennart@torch.sh>
 *
 * This file is part of Graylog2.
 *
 * Graylog2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog2.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.graylog2.inputs.extractors;

import org.graylog2.ConfigurationException;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.Tools;
import org.graylog2.plugin.inputs.Extractor;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Lennart Koopmann <lennart@torch.sh>
 */
public class SubstringExtractorTest {

    @Test
    public void testBasicExtraction() throws Exception {
        Message msg = new Message("The short message", "TestUnit", Tools.getUTCTimestampWithMilliseconds());

        msg.addField("somefield", "<10> 07 Aug 2013 somesubsystem: this is my message for username9001 id:9001");

        SubstringExtractor x = new SubstringExtractor("foo", "foo", Extractor.CursorStrategy.COPY, "somefield", "our_result", config(17, 30), "foo");
        x.run(msg);

        assertNotNull(msg.getField("our_result"));
        assertEquals("<10> 07 Aug 2013 somesubsystem: this is my message for username9001 id:9001", msg.getField("somefield"));
        assertEquals("somesubsystem", msg.getField("our_result"));
    }

    @Test
    public void testBasicExtractionWithCutStrategy() throws Exception {
        Message msg = new Message("The short message", "TestUnit", Tools.getUTCTimestampWithMilliseconds());

        msg.addField("somefield", "<10> 07 Aug 2013 somesubsystem: this is my message for username9001 id:9001");

        SubstringExtractor x = new SubstringExtractor("foo", "foo", Extractor.CursorStrategy.CUT, "somefield", "our_result", config(17, 30), "foo");
        x.run(msg);

        assertNotNull(msg.getField("our_result"));
        assertEquals("<10> 07 Aug 2013 : this is my message for username9001 id:9001", msg.getField("somefield"));
        assertEquals("somesubsystem", msg.getField("our_result"));
    }

    @Test
    public void testBasicExtractionWithCutStrategyDoesNotCutStandardFields() throws Exception {
        Message msg = new Message("The short message", "TestUnit", Tools.getUTCTimestampWithMilliseconds());

        SubstringExtractor x = new SubstringExtractor("foo", "foo", Extractor.CursorStrategy.CUT, "source", "our_result", config(4,8), "foo");
        x.run(msg);

        assertNotNull(msg.getField("our_result"));
        assertEquals("TestUnit", msg.getField("source")); // Not cut!
        assertEquals("Unit", msg.getField("our_result"));
    }

    @Test
    public void testBasicExtractionDoesNotFailOnNonMatch() throws Exception {
        Message msg = new Message("The short message", "TestUnit", Tools.getUTCTimestampWithMilliseconds());

        msg.addField("somefield", "<10> 07 Aug 2013 somesubsystem: this is my message for username9001 id:9001");

        SubstringExtractor x = new SubstringExtractor("foo", "foo", Extractor.CursorStrategy.COPY, "somefield", "our_result", config(100, 200), "foo");
        x.run(msg);

        assertNull(msg.getField("our_result"));
        assertEquals("<10> 07 Aug 2013 somesubsystem: this is my message for username9001 id:9001", msg.getField("somefield"));
    }

    @Test
    public void testBasicExtractionDoesNotFailOnNonMatchWithCutStrategy() throws Exception {
        Message msg = new Message("The short message", "TestUnit", Tools.getUTCTimestampWithMilliseconds());

        msg.addField("somefield", "<10> 07 Aug 2013 somesubsystem: this is my message for username9001 id:9001");

        SubstringExtractor x = new SubstringExtractor("foo", "foo", Extractor.CursorStrategy.CUT, "somefield", "our_result", config(100, 200), "foo");
        x.run(msg);

        assertNull(msg.getField("our_result"));
        assertEquals("<10> 07 Aug 2013 somesubsystem: this is my message for username9001 id:9001", msg.getField("somefield"));
    }

    @Test
    public void testDoesNotFailOnNonExistentSourceField() throws Exception {
        Message msg = new Message("The short message", "TestUnit", Tools.getUTCTimestampWithMilliseconds());

        SubstringExtractor x = new SubstringExtractor("foo", "foo", Extractor.CursorStrategy.CUT, "LOLIDONTEXIST", "our_result", config(0,1), "foo");
        x.run(msg);
    }

    @Test
    public void testDoesNotFailOnSourceFieldThatIsNotOfTypeString() throws Exception {
        Message msg = new Message("The short message", "TestUnit", Tools.getUTCTimestampWithMilliseconds());

        msg.addField("somefield", 9001);

        SubstringExtractor x = new SubstringExtractor("foo", "foo", Extractor.CursorStrategy.CUT, "somefield", "our_result", config(0,1), "foo");
        x.run(msg);
    }

    @Test
    public void testBasicExtractionWithCutStrategyDoesNotLeaveEmptyFields() throws Exception {
        Message msg = new Message("The short message", "TestUnit", Tools.getUTCTimestampWithMilliseconds());

        msg.addField("somefield", "<10> 07 Aug 2013 somesubsystem: this is my message for username9001 id:9001");

        SubstringExtractor x = new SubstringExtractor("foo", "foo", Extractor.CursorStrategy.CUT, "somefield", "our_result", config(0,75), "foo");
        x.run(msg);

        assertNotNull(msg.getField("our_result"));
        assertEquals("fullyCutByExtractor", msg.getField("somefield"));
    }


    @Test(expected = Extractor.ReservedFieldException.class)
    public void testDoesNotRunAgainstReservedFields() throws Exception {
        new SubstringExtractor("foo", "foo", Extractor.CursorStrategy.CUT, "somefield", "source", config(0,1), "foo");
    }

    @Test(expected = ConfigurationException.class)
    public void testDoesNotInitializeOnNullConfigMap() throws Exception {
        new SubstringExtractor("foo", "foo", Extractor.CursorStrategy.CUT, "somefield", "somefield", null, "foo");
    }

    @Test(expected = ConfigurationException.class)
    public void testDoesNotInitializeOnNullStartValue() throws Exception {
        new SubstringExtractor("foo", "foo", Extractor.CursorStrategy.CUT, "somefield", "somefield", config(null, 2), "foo");
    }

    @Test(expected = ConfigurationException.class)
    public void testDoesNotInitializeOnNullEndValue() throws Exception {
        new SubstringExtractor("foo", "foo", Extractor.CursorStrategy.CUT, "somefield", "somefield", config(1, null), "foo");
    }

    @Test(expected = ConfigurationException.class)
    public void testDoesNotInitializeOnStringStartValue() throws Exception {
        new SubstringExtractor("foo", "foo", Extractor.CursorStrategy.CUT, "somefield", "somefield", config("1", 2), "foo");
    }

    @Test(expected = ConfigurationException.class)
    public void testDoesNotInitializeOnStringEndValue() throws Exception {
        new SubstringExtractor("foo", "foo", Extractor.CursorStrategy.CUT, "somefield", "somefield", config(1, "2"), "foo");
    }

    public static Map<String, Object> config(final Object start, final Object end) {
        return new HashMap<String, Object>() {{
            put("begin_index", start);
            put("end_index", end);
        }};
    }

}
