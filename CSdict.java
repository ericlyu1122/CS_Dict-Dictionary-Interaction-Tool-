
// You can use this file as a starting point for your dictionary client
// The file contains the code for command line parsing and it also
// illustrates how to read and partially parse the input typed by the user. 
// Although your main class has to be in this file, there is no requirement that you
// use this template or hav all or your classes in this file.

import java.awt.desktop.SystemSleepEvent;
import java.lang.System;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.*;
import java.util.List;

//
// This is an implementation of a simplified version of a command
// line dictionary client. The only argument the program takes is
// -d which turns on debugging output. 
//


public class CSdict {
    static final int MAX_LEN = 255;
    static Boolean debugOn = false;
    
    private static final int PERMITTED_ARGUMENT_COUNT = 1;
    private static String command;
    private static String[] arguments;

    // global variable in terms of socket
    private static Socket echoSocket = null;
    private static PrintWriter out;
    private static BufferedReader in;
    // reference list one means 1 argument input || none means no argument input
    final static List<String> refCommandOne = Arrays.asList("set", "define",
            "match", "prefixmatch");
    final static List<String> refCommandNone = Arrays.asList("close", "quit",
            "dict");
    private static String dict;
    public static void main(String [] args) {

        int len;
        // Verify command line arguments

        if (args.length == PERMITTED_ARGUMENT_COUNT) {
            debugOn = args[0].equals("-d");
            if (debugOn) {
                System.out.println("Debugging output enabled");
            } else {
                System.out.println("902 Invalid command line option - Only -d is allowed");
                return;
            }
        } else if (args.length > PERMITTED_ARGUMENT_COUNT) {
            System.out.println("901 Too many command line options - Only -d is allowed");
            return;
        }


        // Example code to read command line input and extract arguments.
        boolean quit = false;
        while (true) {
            byte cmdString[] = new byte[MAX_LEN];
            try {
                System.out.print("317dict> ");
                System.in.read(cmdString);

                // Convert the command string to ASII
                String inputString = new String(cmdString, "ASCII");

                // Split the string into words
                String[] inputs = inputString.trim().split("( |\t)+");

                // Set the command
                command = inputs[0].toLowerCase().trim();

                // Remainder of the inputs is the arguments.
                arguments = Arrays.copyOfRange(inputs, 1, inputs.length);

            } catch (IOException exception) {
                System.err.println("998 Input error while reading commands, terminating.");
                System.exit(-1);
            }

            // Silently ignore the empty lines and lines started with "#"
            if(command.isEmpty() || command.startsWith("#") || command.startsWith(" #")) continue;

            switch (command){
                case "open":
                    if(checkValid(command,arguments)) openHelper();
                    break;
                case "close":
                    if(checkValid(command,arguments)) closeHelper();
                    break;
                case "quit":
                    if(checkValid(command,arguments)) quitHelper();
                    break;
                case "dict":
                    if(checkValid(command,arguments)) dictHelper();
                    break;
                case "set":
                    if(checkValid(command,arguments)) setHelper();
                    break;
                case "define":
                    if(checkValid(command,arguments)) defineHelper();
                    break;
                case "match":
                    if(checkValid(command,arguments)) matchHelper("exact");
                    break;
                case "prefixmatch":
                    if(checkValid(command,arguments)) matchHelper("prefix");
                    break;
                default:
                    System.err.println("900 Invalid command.");
                    break;
            }


        }
    }
    // helper function for checking the validation of the command and argument
    // e.g. different numbers of arguments|| invalid command e.t.c
    private static boolean checkValid(String commandIn,String[] argument){
        // handle open command
        if(commandIn.equals("open")){
            if(echoSocket!=null && !echoSocket.isClosed()){
                System.err.println("910 Supplied command not expected at this time.");
                return false;
            }
            if(argument.length != 2){
                System.err.println("903 Incorrect number of arguments.");
                return false;
            }
            return true;
        }
        // handle other command
        // invalid command handle error
        if(!refCommandOne.contains(commandIn) && !refCommandNone.contains(commandIn)){
            System.err.println("900 Invalid command.");
            return false;
        }
        // the socket does not open.
        if(!commandIn.equals("quit")&&(echoSocket == null || echoSocket.isClosed())){
            System.err.println("910 Supplied command not expected at this time.");
            return false;
        }
        // socket opened.
        // None argument dict, close, quit,
        if(refCommandNone.contains(commandIn)&&argument.length!=0){
            System.err.println("903 Incorrect number of arguments.");
            return false;
        }
        // 1 argument set, define, match, prefixmatch
        if(refCommandOne.contains(commandIn)&&argument.length!=1){
            System.err.println("903 Incorrect number of arguments.");
            return false;
        }
        // success
        return true;
    }

    public static void openHelper(){
        try {
            // initialize the dict
            dict = "*";

            if(debugOn) System.out.println("--> OPEN "+arguments[0]+ " "+ arguments[1]);
            echoSocket = new Socket();
            // handle the socket connected timeout. Set 30s. www.google.com 81
            echoSocket.connect(new InetSocketAddress(InetAddress.getByName(arguments[0]),
                    Integer.parseInt(arguments[1])),30000);

            // get in & out
            out = new PrintWriter(echoSocket.getOutputStream(), true);
            in = new BufferedReader(
                    new InputStreamReader(echoSocket.getInputStream()));

            try {
                String getFromServe = in.readLine();
                // print response status
                if(debugOn) System.out.println("--> "+getFromServe);
                // handle wrong status and response code != 220
                if (getFromServe == null) {
                    throw new SocketTimeoutException();
                } else if (!getFromServe.startsWith("220")) {
                    System.err.println("999 Processing error. " + getFromServe.split(" ", 2)[1]);
                }
            } catch (SocketTimeoutException e) {
                in.close();
                out.close();
                echoSocket.close();
                echoSocket = null;
                System.err.println("920 Control connection to "+arguments[0]+" on port "+arguments[1]+" failed to open.");
            }

        } catch (IOException e) {
            echoSocket = null;
            System.err.println("920 Control connection to "+arguments[0]+" on port "+arguments[1]+" failed to open.");
        } catch (IllegalArgumentException e) {
            // non-numerical & exceed the valid range for port
            echoSocket = null;
            System.err.println("904 Invalid argument.");
        }
    }

    // quit the csDict helper
    public static void quitHelper() {
        if(echoSocket!=null&&!echoSocket.isClosed()){
            // TODO: ? handle --> QUIT ?
            debugOn = false;
            handleCloseSocket();

        }
        System.exit(0);
    }
    // close the socket
    public static void closeHelper() {
        if(echoSocket!=null && !echoSocket.isClosed()){
            handleCloseSocket();
            debugOn = false;
        }else{
            System.err.println("910 Supplied command not expected at this time.");
        }

    }
    // helper when closing and quit
    private static void handleCloseSocket(){
        if (debugOn) System.out.println("--> QUIT");
        dict = "*";
        try{
            out.println("QUIT");
            String getFromServe = in.readLine();
            if (getFromServe.split(" ")[0].matches("[0-9]]{3}")
                    && getFromServe.startsWith("221")) {
                // print response status
                if (debugOn) System.out.println("<-- " + getFromServe);
                in.close();
                out.close();
                echoSocket.close();
            }
            echoSocket = null;
        }catch (IOException error){
            System.err.println("999 Processing error. Fail to close the socket.");
        }
    }
    // retrieve all dictionary
    public static void dictHelper() {
        if(debugOn) System.out.println("--> DICT");
        out.println("SHOW DB");
        try {
            String getFrom;
            boolean status;
            while((getFrom = in.readLine())!=null){
                status = getFrom.split(" ")[0].matches("[0-9]]{3}");
                if(status) if(debugOn) System.out.println("<--"+ getFrom);

                // 250 ok || no database present
                if(getFrom.contains("250")||getFrom.contains("554")) break;
                if(!status) System.out.println(getFrom);

            }
        } catch (IOException e) {
            System.err.println("925 Control connection I/O error, closing control connection.");
            handleCloseSocket();
        }

    }

    // set the use of the dict
    public static void setHelper() {
        // retrieve from argument
        dict = arguments[0];
    }
    // define the word prompt from user
    public static void defineHelper() {
        String word = arguments[0];
        if(debugOn) System.out.println("--> DEFINE "+ dict + " " + word);
        out.println("DEFINE "+ dict + " " + word);
        try {
            String getFrom;
            boolean status;
            boolean blockText = false;
            while((getFrom = in.readLine())!=null){
                // empty line -> skip
                if(getFrom.split(" ").length==0) continue;
                // get any response code given by the server response
                status = getFrom.split(" ")[0].matches("[0-9]{3}");
                if(status) {
                    if (debugOn) System.out.println("<-- " + getFrom);
                    // number of definition retrieved
                    if (getFrom.contains("150")) continue;
                    // no definition found && invalid dict
                    if (getFrom.contains("552") || getFrom.contains("550")) {

                        if(debugOn) System.out.println("<-- " + getFrom);

                        // invalid dict
                        if(getFrom.contains("550"))
                            System.err.println("999 Processing error. Invalid dict which does not list under dict.");
                        // reset

                        System.out.println("**No definition found**");
                        //TODO:  default match
                        matchHelper(".");
                        dict = "*";
                        return;
                    }
                    // 250 ok
                    if(getFrom.contains("250")) return;

                    // new definition from a dict
                    if (getFrom.contains("151")){
                        // 151 "pig" wn xxxxx
                        // [2] dict [3 description]
                        System.out.println("@ "+ getFrom.split(" ")[2]
                                +" "+getFrom.split(" ",4)[3]);
                        // the following text is full description
                        blockText = true;
                        continue;
                    }
                    // reset flag when reading the new block of text.
                    if(debugOn && blockText) blockText = false;
                }
                System.out.println(getFrom);

            }
        } catch (IOException e) {
            System.err.println("925 Control connection I/O error, closing control connection.");
            handleCloseSocket();
        }

    }
    public static void matchHelper(String strategy) {
        try{
            String word = arguments[0];
            if(debugOn) System.out.println("--> MATCH " + dict + " " + strategy + " " + word);
            out.println("MATCH " + dict + " " + strategy + " " + word);
            String getFrom;
            boolean status;
            // handle match/ prefix match needed for extra printed line
//            System.out.println(dict + " " + word);

            while((getFrom = in.readLine()) != null){
                status = getFrom.split(" ")[0].matches("[0-9]{3}");
                if(status){
                    if(debugOn) System.out.println("--> " + getFrom);
                    // check for the valid of dict first
                    if(getFrom.contains("152")) continue;
                    if(getFrom.contains("550")){
                        dict = "*"; // reset dict
                        System.err.println(
                                "999 Processing error. Invalid dict which does not list under dict.");
                        return;
                    }
                    // not found any match
                    if(getFrom.contains("552")){
                        if(strategy.equals(".")){
                            // return by define -> no match after no definition found
                            System.out.println("***No matches found***");
                        }else {
                            System.out.println("****No matching word(s) found****");
                        }
                        return;
                    }
                    // 250 OK
                    if(getFrom.contains("250")) break;

                }
                System.out.println(getFrom);
            }
        }catch (IOException e){
            System.err.println("925 Control connection I/O error, closing control connection.");
            handleCloseSocket();
        }

    }


}
    
    
