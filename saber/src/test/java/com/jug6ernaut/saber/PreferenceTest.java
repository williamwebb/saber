package com.jug6ernaut.saber;

import android.content.Context;
import com.jug6ernaut.saber.Saber.Finder;
import com.jug6ernaut.saber.preferences.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by williamwebb on 7/9/15.
 */
@RunWith(RobolectricTestRunner.class) @Config(manifest = Config.NONE)
public class PreferenceTest {

    @Test
    public void stringPreferenceTest() {
        Context c =  Robolectric.application;

        StringPreference pref = Finder.getPreference(c, "", "", "what", StringPreference.class);
        assertNotNull(pref);
        assertEquals("what", pref.get());
        pref.set("whatwhat");
        assertEquals("whatwhat",pref.get());

        pref = Finder.getPreference(c, "", "1", "", StringPreference.class);
        assertNotNull(pref);
        assertEquals("", pref.get());
    }

    @Test
    public void stringSetPreferenceTest() {
        Context c =  Robolectric.application;

        StringSetPreference pref = Finder.getPreference(c, "", "", "[\"what\",\"whatwhat\"]", StringSetPreference.class);
        assertNotNull(pref);
        assertEquals(new HashSet<>(Arrays.asList("what", "whatwhat")), pref.get());
        pref.set(new HashSet<>(Collections.singletonList("what")));
        assertEquals(new HashSet<>(Collections.singletonList("what")),pref.get());

        pref = Finder.getPreference(c, "", "1", "", StringSetPreference.class);
        assertNotNull(pref);
        assertEquals(new HashSet<>(), pref.get());
    }

    @Test
    public void intPreferenceTest() {
        Context c =  Robolectric.application;

        IntPreference pref = Finder.getPreference(c, "", "", "32", IntPreference.class);
        assertNotNull(pref);
        assertEquals(Integer.valueOf(32), pref.get());
        pref.set(10);
        assertEquals(Integer.valueOf(10),pref.get());

        pref = Finder.getPreference(c, "", "1", "", IntPreference.class);
        assertNotNull(pref);
        assertEquals(Integer.valueOf(0), pref.get());
    }

    @Test
    public void longPreferenceTest() {
        Context c =  Robolectric.application;

        LongPreference pref = Finder.getPreference(c, "", "", "32", LongPreference.class);
        assertNotNull(pref);
        assertEquals(Long.valueOf(32), pref.get());
        pref.set(10L);
        assertEquals(Long.valueOf(10),pref.get());

        pref = Finder.getPreference(c, "", "1", "", LongPreference.class);
        assertNotNull(pref);
        assertEquals(Long.valueOf(0), pref.get());
    }

    @Test
    public void floatPreferenceTest() {
        Context c =  Robolectric.application;

        FloatPreference pref = Finder.getPreference(c, "", "", "32.5", FloatPreference.class);
        assertNotNull(pref);
        assertEquals(Float.valueOf(32.5F), pref.get());
        pref.set(10.65F);
        assertEquals(Float.valueOf(10.65F),pref.get());

        pref = Finder.getPreference(c, "", "1", "", FloatPreference.class);
        assertNotNull(pref);
        assertEquals(Float.valueOf(0), pref.get());
    }
}
