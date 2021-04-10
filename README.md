# CS_Dict-Dictionary-Interaction-Tool-


## Overview

This Project is based on the protocol RFC 2229 by establishing the connection via TCP through PORT 2628. 

## Configuring your environment

To start using this project, you need to get your computer configured so you can build and execute the code.
To do this, follow these steps; the specifics of each step (especially the first two) will vary based on which operating system your computer has:

1. [Install git](https://git-scm.com/downloads) (v2.X). After installing you should be able to execute `git --version` on the command line.

1. Clone your repository by running `git clone REPO_URL` from the command line. Run `java -jar CSdict.jar [-d]`.

## Project commands

Once your environment is configured you need to further prepare the project's tooling and dependencies.
In the project folder:

1. `open SERVER PORT` to Opens a new TCP/IP connection to a dictionary server.

1. `dict` to Retrieve and print the list of all the dictionaries the server supports.

1. `set DICTIONARY` to Set the dictionary to retrieve subsequent definitions and/or matches from. 

1. `define WORD` to Retrieve and print all the definitions for WORD. If the word can't be found and no definitions are returned then do a match using the server's default matching strategy. 

1. `match WORD` to Retrieve and print all the #exact# matches for WORD.

1. `prefixmatch WORD` to Retrieve and print all the #prefix# matches. for WORD.

1. `close` to send the appropriate command to the server and receiving a response, closes the established connection and enters a state where the next command expected is an open or quit.

1. `quit` to Closes any established connection and exits the program. .

## Running and Error Response Code Spec
```bash
1. 900 Invalid command. This is printed when the command entered by the user is not one of the accepted commands.
1. 901 Too many command line options - Only -d is allowed.  This is printed if there are too many command line options.
1. 902 Invalid command line option - Only -d is allowed.  This is printed if a command line option is not -d. The printing of error 901 takes priority over this message.
1. 903 Incorrect number of arguments. This is printed when the command is valid but the wrong number of arguments is provided. Note this could be the case if there are either too many or too few arguments.
1. 904 Invalid argument. This is printed when the command is valid, and has the proper number of arguments, but one or more the arguments are invalid. For example the second argument of open is a port number so a non-numeric value for the second argument would produce this error.
1. 910 Supplied command not expected at this time. This is printed when the command is valid, but not allowed at this time. For example, when the client first starts the only commands it can accept are open and quit. If it gets any other known command it would print this message. Note that the printing of this message takes priority over error messages 903 and 904. (i.e. even if errors 903 or 904 occur this message is the only one to print.)
1. 920 Control connection to xxx on port yyy failed to open. When an attempt to establish the connection can't be completed within a reasonable time (say 30 seconds), or the socket cannot be created, then print this message, replacing xxx and yyy with the hostname and port number of the dictionary server you are trying to establish the control connection to,.
1. 925 Control connection I/O error, closing control connection. If at any point an error while attempting to read from, or write to, the open control connection occurs, this message is printed, and the socket closed/destroyed. The client is then to go back to the state were it is expecting an open command.
1. 998 Input error while reading commands, terminating. This error message is printed if an exception is thrown while the client is reading its commands (i.e standard input). After printing this message the client will terminate.
1. 999 Processing error. yyyy. 
