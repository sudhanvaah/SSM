/* *****************************************************************************
 * Simulation of a simple M/M/1 queue (Grocery).
 * Class: Simulation - Modeling and Performance Analysis 
 *        with Discrete-Event Simulation
 *        RWTH Aachen, Germany, 2007
 *        
 * Author: Dr. Mesut Günes, guenes@cs.rwth-aachen.de        
 * 
 * Notice: 
 * This code is based on the example presented in the very nice book of 
 * Jerry Banks, John S. Carson II, Barry L. Nelson, David M. Nicol: 
 * Discrete-Event System Simulation, Fourth Edition, Prentice Hall, 2005.
 * 
 * However, the code is not exactly the same ;-)
 *  
 ******************************************************************************/


import java.util.*;

public class Sim {
    
    // Class Sim variables
    public static double clock; 
    public static double meanInterArrivalTime; 
    public static double meanServiceTime; 
    public static double sigma;
    public static double lastEventTime; 
    public static double totalBusy; 
    public static double maxQueueLength; 
    public static double sumResponseTime;

    public static long numberOfCustomers; 
    public static long queueLength; 
    public static long numberInService; // either 0 or 1
    public static long totalCustomers; 
    public static long numberOfDepartures; 
    public static long longService;

    public final static int arrival = 1;
    public final static int departure = 2;

    public static EventList futureEventList;

    public static Queue customers;

    public static Random stream;
    
    
    // seed the event list with TotalCustomers arrivals
    public static void initialization() {
        clock = 0.0;
        queueLength = 0;
        numberInService = 0;
        lastEventTime = 0.0;
        totalBusy = 0;
        maxQueueLength = 0;
        sumResponseTime = 0;
        numberOfDepartures = 0;
        longService = 0;

        // create first arrival event
        Event evt = new Event(arrival, exponential(stream, meanInterArrivalTime));
        futureEventList.enqueue(evt);
    }
    
    public static void processArrival(Event evt) {
        customers.enqueue(evt);                
        queueLength++;
        
        // if the server is idle, fetch the event, do statistics
        // and put into service
        if (numberInService == 0)
            scheduleDeparture();
        else
            totalBusy += (clock - lastEventTime); // server is busy

        // adjust max queue length statistics
        if (maxQueueLength < queueLength)
            maxQueueLength = queueLength;

        // schedule the next arrival
        Event nextArrival = new Event(arrival, clock + exponential(stream, meanInterArrivalTime));
        futureEventList.enqueue(nextArrival);
        lastEventTime = clock;
    }

    public static void scheduleDeparture() {
        double ServiceTime;        
        ServiceTime = exponential(stream, meanServiceTime);

        Event depart = new Event(departure, clock + ServiceTime);
        futureEventList.enqueue(depart);
        numberInService = 1;
        queueLength--;
    }

    public static void processDeparture(Event e) {
        // get the customer description
        Event finished = (Event) customers.dequeue();
        
        //System.out.println("d " + e.time);
        
        // measure the response time and add to the sum
        double response = (clock - finished.getTime());

        sumResponseTime += response;
        if (response > 4.0)
            longService++; // record long service
        totalBusy += (clock - lastEventTime);
        numberOfDepartures++;
        lastEventTime = clock;
        
        // if there are customers in the queue then schedule
        // the departure of the next one
        if (queueLength > 0)
            scheduleDeparture();
        else
            numberInService = 0;        
    }   
    
    public static void reportGeneration() {
        double rho = totalBusy / clock;
        double avgr = sumResponseTime / totalCustomers;
        double pc4 = ((double) longService) / totalCustomers;

        System.out.println("SINGLE SERVER QUEUE SIMULATION - GROCERY STORE CHECKOUT COUNTER ");
        System.out.println("\tMEAN INTERARRIVAL TIME                         " + meanInterArrivalTime);
        System.out.println("\tMEAN SERVICE TIME                              " + meanServiceTime);
        System.out.println("\tSTANDARD DEVIATION OF SERVICE TIMES            " + sigma);
        System.out.println("\tNUMBER OF CUSTOMERS SERVED                     " + totalCustomers);
        System.out.println();
        System.out.println("\tSERVER UTILIZATION                             " + rho);
        System.out.println("\tMAXIMUM LINE LENGTH                            " + maxQueueLength);
        System.out.println("\tAVERAGE RESPONSE TIME                          " + avgr + "  MINUTES");
        System.out.println("\tPROPORTION WHO SPEND FOUR ");
        System.out.println("\t MINUTES OR MORE IN SYSTEM                     " + pc4);
        System.out.println("\tSIMULATION RUNLENGTH                           " + clock + " MINUTES");
        System.out.println("\tNUMBER OF DEPARTURES                           " + totalCustomers);
    }
    
    public static double exponential(Random rng, double mean) {
        return -mean * Math.log(rng.nextDouble());
    }    
           
    public static void main(String argv[]) {
        meanInterArrivalTime = 4.3;
        meanServiceTime = 1.9;
        sigma = 0.5;
        totalCustomers = 500;
        long seed = 123567;
        //long seed = Long.parseLong(argv[0]);

        stream = new Random(seed); // initialize rng stream
        futureEventList = new EventList();
        customers = new Queue();
        
        initialization();

        // Loop until first "TotalCustomers" have departed
        while (numberOfDepartures < totalCustomers) {
            Event evt = (Event) futureEventList.getMin(); // get imminent event
            futureEventList.dequeue(); // be rid of it
            clock = evt.getTime(); // advance simulation time
            if (evt.getType() == arrival)
                processArrival(evt);
            else
                processDeparture(evt);
        }
        
        reportGeneration();        
    }
    
}// of Sim


/* *****************************************************************************
 * 
 * HELPER CLASSES 
 * 
 ******************************************************************************/


/** 
 * A very simple event class consisting of only two attributed
 */
class Event {
    private double time;
    private int type;

    public Event(int _type, double _time) {
        type = _type;
        time = _time;
    }

    public int getType() {
        return type;
    }

    public double getTime() {
        return time;
    }
}// Event



/**
 * EventList implements the Future Event List (FEL) in a very simple way by 
 * using the LinkedList class of Java. 
 */
class EventList extends LinkedList {
    
    public EventList() {
        super();
    }
    
    public Object getMin() {        
        return getFirst();        
    }
        
    public void enqueue(Object _o) {
        add(_o);
    }
    
    public void dequeue() {
        removeFirst();
    }
}// EventList



/**
 * An extreme simple Queue implementation by using of LinkedList class of Java. 
 */
class Queue extends LinkedList {
    
    public void enqueue(Object _o) {
        add(_o);        
    }
    
    public Object dequeue() {
        return removeFirst();
    }
}// Queue




