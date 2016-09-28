/*
Ryan Demo
rdemo1
600.226.01
P2
 */

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Deque;
import java.util.HashSet;
import java.io.FileReader;
import java.io.IOException;

/**
 * Main driver to run JHUgle search engine for project 2.
 * Prompts user for commands and search terms. Terminates on QUIT.
 * @author Ryan Demo
 */
public final class JHUgle {

    /**
     * Max load for LPHashMap that stores input.
     */
    private static final float MAX_LOAD = (float) 0.7;

    /**
     * Dummy constructor for utility class.
     */
    private JHUgle() {
    }

    /** Main loop for JHUgle application.
     * Loads an LPHashMap with the (word, URL list) pairs from
     * the input file. Sends this map to a loop that handles
     * user interaction.
     * @param args takes one command line arg for filename as input file
     * @throws IOException errors if file cannot be read
     */
    public static void main(String[] args) throws IOException {
        
        //using example code posted on piazza
        Scanner infile = null;
        boolean inerror = false;

        try {
            System.out.println("0 " + args[0]
                    + " should be input filename.");
            infile = new Scanner(new FileReader(args[0]));
        } catch (ArrayIndexOutOfBoundsException a) {
            System.err.println("Must give input filename at command line.");
            inerror = true;
        } catch (IOException f) {
            System.err.println("Can't open that file, try again.");
            inerror = true;
        }
        if (inerror) {
            System.err.println("Exiting...");
            System.exit(1);
        }

        //create an LPHashMap for search terms
        LPHashMap<String, List<String>> searchMap =
                new LPHashMap(JHUgle.MAX_LOAD);

        //load file into LPHashMap
        Scanner inline;
        String url, terms, searchTerm;
        List<String> existingURLList;
        while (infile.hasNextLine()) {
            url = infile.nextLine();
            terms = infile.nextLine();
            inline = new Scanner(terms);
            while (inline.hasNext()) { //iterate through words on each line
                searchTerm = inline.next();
                existingURLList = searchMap.get(searchTerm);
                if (existingURLList == null) {
                    //initialize URL list for key if it doesn't exist yet
                    existingURLList = new ArrayList<String>();
                }
                existingURLList.add(url);
                searchMap.put(searchTerm, existingURLList);
            }
        }
        
        userInteraction(searchMap);
        
    }
    
    /** Main interaction loop. Puts results of queries as HashSets onto
     * a Deque and looks for (print, quit, and, or) commands.
     * @param searchMap LPHashMap loaded from input file
     */
    public static void userInteraction(LPHashMap<String, List<String>>
        searchMap) {
        
        //start user interaction
        System.out.println("Welcomed to JHUgle!");
        
        //initialize query and scanner
        String query = "";
        Scanner input = new Scanner(System.in);
        
        //create a deque
        Deque<HashSet<String>> searchStack = new ArrayDeque<HashSet<String>>();
        
        //main interaction loop
        while (!query.equals("QUIT") || !query.equals("quit")) {
            System.out.print("Please enter a query: ");
            query = input.next();
            query = query.toLowerCase(); //make queries case insensitive
            System.out.println();

            switch (query) {
                case "print":
                    printStack(searchStack);
                    break;
                case "quit":
                    input.close();
                    System.exit(1);
                    break;
                case "and":
                    searchStack = intersection(searchStack);
                    break;
                case "or":
                    searchStack = union(searchStack);
                    break;
                default:
                    List<String> result = searchMap.get(query);
                    if (result != null) {
                        searchStack.add(new HashSet<String>(result));
                    } else {
                        searchStack.add(new HashSet<String>());
                    }
                    break;
            }
        }
        
        input.close(); //close scanner
        
    }

    /** Prints the element on the top of the stack passed to it.
     * @param searchStack the stack from which to print the top element
     */
    public static void printStack(Deque<HashSet<String>> searchStack) {
        try {
            System.out.println(searchStack.getLast().toString().substring(1,
                    searchStack.getLast().toString().length() - 1));
            System.out.println("Stack size: " + searchStack.size());
        } catch (NoSuchElementException e) {
            //occurs when stack size is 0
            System.err.println("Stack is empty!");
        }
    }

    /** Pops the last two sets of URLs off the stack and
     * performs an intersection set operation. Called when
     * given the AND command.
     * @param searchStack
     * stack from which the top two elements are popped
     * @return searchStack after the set intersection operation
     */
    public static Deque<HashSet<String>>
        intersection(Deque<HashSet<String>> searchStack) {
        //create new deque to edit, so original can be returned if
        //intersection results in error
        Deque<HashSet<String>> editedSearchStack =
                new ArrayDeque<HashSet<String>>(searchStack);
        try {
            HashSet<String> firstSet = editedSearchStack.pollLast();
            HashSet<String> secondSet = editedSearchStack.pollLast();
            HashSet<String> intersection = new HashSet<String>(firstSet);
            intersection.retainAll(secondSet); //HashSet intersection method
            editedSearchStack.add(intersection); //add intersection to stack
            return editedSearchStack; //return new master stack
        } catch (NullPointerException e) {
          //occurs after attempted pop of empty stack
            System.err.println("Insufficient elements on stack for set "
                    + "intersection");
        }
        return searchStack;
    }

    /** Pops the last two sets of URLs off the stack and
     * performs a union operation. Called when given the
     * OR command.
     * @param searchStack
     * stack from which the top two elements are popped
     * @return searchStack after the set union operation
     */
    public static Deque<HashSet<String>>
        union(Deque<HashSet<String>> searchStack) {
        //create new deque to edit, so original can be returned if
        //union results in error
        Deque<HashSet<String>> editedSearchStack =
                new ArrayDeque<HashSet<String>>(searchStack);
        try {
            HashSet<String> firstSet = editedSearchStack.pollLast();
            HashSet<String> secondSet = editedSearchStack.pollLast();
            HashSet<String> union = new HashSet<String>(firstSet);
            union.addAll(secondSet); //HashSet union method
            editedSearchStack.add(union); //add union to edited stack
            return editedSearchStack; //return new master stack
        } catch (NullPointerException e) {
            //occurs after attempted pop of empty stack
            System.err.println("Insufficient elements on stack for"
                    + " set union");
        }
        return searchStack;
    }
}
