package vroom.optimization.online.jmsa.events;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import vroom.common.utilities.events.EventComparator;

public class MSAEventQueueTest {

    private MSAEventQueue     mManager;

    private ArrayList<MSAEvent> mTestEvents;

    @Before
    public void setUp() throws Exception {

        mManager = new MSAEventQueue();
        mTestEvents = new ArrayList<MSAEvent>(70);

        Object o = new Object();

        for (int i = 0; i < 5; i++) {
            mTestEvents.add(new DecisionEvent(0, null));
            synchronized (o) {
                o.wait(100);
            }
            mTestEvents.add(new OptimizeEvent(null));
            synchronized (o) {
                o.wait(100);
            }
            mTestEvents.add(new GenerateEvent(null));
            synchronized (o) {
                o.wait(100);
            }
            mTestEvents.add(ResourceEvent.newStartServiceEvent(0, null, i, null));
            synchronized (o) {
                o.wait(100);
            }
            mTestEvents.add(ResourceEvent.newEndOfServiceEvent(0, null, i, null, null));
            synchronized (o) {
                o.wait(100);
            }
            mTestEvents.add(ResourceEvent.newStopServiceEvent(0, null, i, null));
            synchronized (o) {
                o.wait(100);
            }
            mTestEvents.add(ResourceEvent.newRequestAssignedEvent(0, null, i, null, null));

            synchronized (o) {
                o.wait(100);
            }
        }

        Collections.shuffle(mTestEvents);
    }

    @Test
    public void testPushEventMSAEvent() {
        System.out.println("--------------------------------------");
        System.out.println("testPushEventMSAEvent");
        System.out.println("--------------------------------------");
        System.out.println(" Test Events:");
        System.out.println(" " + mTestEvents);
        for (MSAEvent event : mTestEvents) {
            try {
                mManager.pushEvent(event);
            } catch (Exception e) {
                fail("Exception caught when pushing event " + event + " - " + e.toString());
            }
        }
        System.out.println(" Pending Events:");
        System.out.println(" " + Arrays.toString(mManager.getPendingEvents()));
    }

    @Test
    public void testTakeNextEvent() {
        System.out.println("--------------------------------------");
        System.out.println("testTakeNextEvent");
        System.out.println("--------------------------------------");
        System.out.println(Integer.MAX_VALUE);
        System.out.println(Integer.MIN_VALUE);
        System.out.println(" Test Events:");
        System.out.println(" " + mTestEvents);

        for (MSAEvent event : mTestEvents) {
            try {
                mManager.pushEvent(event);
            } catch (Exception e) {
                fail("Exception caught when pushing event " + event + " - " + e.toString());
            }
        }
        System.out.println(" Pending Events:");
        System.out.println(" " + Arrays.toString(mManager.getPendingEvents()));

        LinkedList<MSAEvent> events = new LinkedList<MSAEvent>();

        System.out.println(" Taking Events:");
        MSAEvent prev = null;
        EventComparator comparator = new EventComparator();
        while (!mManager.isEmpty()) {
            try {
                MSAEvent next = mManager.takeNextEvent();
                System.out.println("  " + next);
                events.add(next);

                if (prev != null) {
                    int comp = comparator.compare(prev, next);
                    System.out.println("  >Comparing " + prev + " and " + next + " : " + comp);
                    assertTrue("compare(prev,next) returned a positive result", comp <= 0);
                    assertTrue("the comparison result is equal to Integer.MIN_INT",
                            comp > Integer.MIN_VALUE);
                    comp = comparator.compare(next, prev);
                    // System.out.println("  >Comparing "+next+" and "+prev+" : "+comp);
                    assertTrue("compare(next,prev) returned a negative result", comp >= 0);
                    assertTrue("the comparison result is equal to Integer.MAX_INT",
                            comp < Integer.MAX_VALUE);
                }
                prev = next;

            } catch (InterruptedException e) {
                fail("Exception caught when taking the next event - " + e.toString());
            }
        }

        System.out.println(" Taken Events:");
        System.out.println(" " + events);
    }
}
