package netty.samples;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class PercentileTest {

    @Test
    public void testThatMultisetCountsMultipleOccurencesOfSameValue() throws Exception {
        final Multiset<Long> elapsedSet = TreeMultiset.create();
        elapsedSet.add(1L);
        elapsedSet.add(1L);
        assertThat(elapsedSet.size(), is(equalTo(2)));
    }

    @Test
    public void testMaxMultiset() throws Exception {
        final Multiset<Long> elapsedSet = TreeMultiset.create();
        elapsedSet.add(1L);
        elapsedSet.add(2L);
        assertThat(Collections.max(elapsedSet), is(2L));
    }

    @Test
    public void testPercentileSmallSample() throws Exception {
        final Multiset<Long> elapsedSet = TreeMultiset.create();
        elapsedSet.add(1L);
        elapsedSet.add(1L);
        elapsedSet.add(2L);
        elapsedSet.add(3L);


        final Long actVal =2L;
        Collection<Long> filtered = Collections2.filter(elapsedSet, new Predicate<Long>() {
            @Override
            public boolean apply(Long input) {
                return input < actVal;
            }
        });

        assertThat(filtered.size(),is(2));


    }
}
