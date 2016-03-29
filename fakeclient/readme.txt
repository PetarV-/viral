Usage: java com.hackbridge.viral.BotGenerator <filename> <server> <port>
(in future, use a proper jar file or run a batch script)

File structure:
 - Line 1: format version [for now, it's just 1]
 - Line 2: 1 integer, represents number of bots, call it N
 - Following N lines:
    + longitude and latitude: two doubles, starting position
    + maximum change in coordinates (per second)
    + mean time between updates (mean of exponential distribution) in ms
 - Can also have lines that are comments: these need to start with a #
 - If line is malformed, stop

Example file:

------------------------------------------------
1
# sample file, version number HAS to be first
# number of bots follows
3
# bot 1
55.0 0.0 0.00005 10
# bot 2, a bit confused
55.0 0.0 0.00008 10
# bot 3, irregular updates
55.0 0.0 0.00005 30
------------------------------------------------


