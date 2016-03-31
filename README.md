# viral
you're infected

Usage
=====

*Viral* is primarily pitched as a tool for performing *controlled
experiments*; after setting up a central server (preferably on a <span
style="font-variant:small-caps;">Unix</span>-based system, to make
advantage of the visualisation utility), participating subjects should
have Android phones with the *Viral* application running in their
possession, and understood the rules of the game. This section is
primarily concerned with the necessary details for performing the
initial setup and configuration of the server and the Android
application. We also provide details of an auxiliary tool that can
simulate additional participants performing a random walk and not doing
any awareness-related interactions (other than vaccinating themselves if
possible and desirable), and present a few results we have obtained in
such synthetic experiments.

Installation
------------

The source may be downloaded as an archive from GitHub, or the
repository may be directly cloned by running the following command
within a terminal: <span>\
`$ git clone https://github.com/PetarV-/viral.git`</span>

### Server compilation and execution.

Once the source code has been cloned, it may be compiled by invoking
<span>javac</span> on the relevant files. Once in the root folder of the
repository, execute the following: <span>\
`$ cd server/src/main/java`</span> <span>\
`$ javac com/hackbridge/viral/*.java`</span> <span>\
`$ java com.hackbridge.viral.Main <port> <round_duration_ms> <delay_between_rounds_ms> <network_params_file> <run_tikzer?> (<tikzer_port>)`</span>
The final command launches the server; the parameters that need to be
provided are as follows:

-   the port the server will listen on for clients;

-   the duration of a single experiment in milliseconds;

-   the delay between experiments in milliseconds;

-   a path to a file containing the multiplex network’s parameters
    (described in more detail in the next paragraph);

-   a boolean string (<span>true</span> or <span>false</span>)
    specifying whether or not the visualisation utility should
    be launched. If <span>true</span>, an additional
    <span>&lt;tikzer\_port&gt;</span> parameter should be provided,
    specifying the port at which the visualisation utility will be
    serving the latest visualisation.

### Network parameters.

The <span>network\_params\_file</span> contains lines in the format
<span>&lt;parameter&gt; &lt;value&gt;</span>. The following parameters
are used to configure the properties of the multiplex network:

  Parameter                                         Description
  ------------------------------------------------- -----------------------------------------------------------------------------------------------------------------------------------------
  <span>initialInfected</span> `Probability`        The probability a new node is initially diseased (with physical state <span>infected</span> or <span>carrier</span>)
  <span>initialAware</span> `Probability`           The probability a new node has initial awareness state <span>aware</span> as opposed to <span>unaware</span>
  <span>initialSymptomatic</span> ` Probability`    The probability that a newly infected node has physical state <span>infected</span> as opposed to <span>carrier</span>
  <span>infectedIfVaccinated</span> `Probability`   The probability a vaccinated node becomes infected with the disease agent when one of its edges with another infected node is activated
  <span>spontaneousRecovery</span> `Probability`    The probability that a diseased node spontaneously recovers when one of its edges is activated
  <span>activateEdge</span> `Probability`           The probability that an edge is activated
  <span>infectorProbability</span>                  The probability that a new node has the role <span>infector</span> as opposed to <span>human</span>
  <span>developSymptoms</span> ` Probability`       The probability that a <span>carrier</span> node becomes <span>infected</span> when one of its edges is activated
  <span>lambdaFactor</span>                         Used in equation \[eq:matrix\]. A larger value increases the rate at which an edge’s activation probability decreases with distance
  <span>exponentialMultiplier</span>                Used in equation \[eq:matrix\] for scaling the edge weights.

### Android setup.

The Android device chosen to install the application should be newer
than Android level 16. After installing the *Viral.apk* file, simply
press on the *Viral* icon. Then input the hostname and port of the
server set-up as outlined above. After a successful connection has been
established, the *Viral* client will be up and running—no further
configuration is needed.

Synthetic experiments
---------------------

While the primary purpose of *Viral* is creating data from a controlled
and real environment, it also supports the addition of bots (virtual
participants), whom the server does not distinguish from users. In the
current model, the bots perform random walks and periodically send
position updates to the server. Sending the updates is modelled as a
Poisson process i.e. the time $T$ between updates is a random variable
with an exponential distribution $\mathcal{E}(\lambda)$, with the
probability density function $f_T\left(t\right)=\lambda e^{-\lambda t}$.
No other behaviour is given to the bots, other than them vaccinating
themselves if they have access to the valid vaccine code and have the
*human* role.

### Fake client configuration.

To use the fake clients, execute the following (starting from the root
folder of the repository): <span>\
`$ cd fakeclient`</span> <span>\
`$ javac com/hackbridge/viral/*.java`</span> <span>\
`$ java com.hackbridge.viral.BotGenerator <bot_parameter_file> <server> <port>`</span>

The command line arguments that need to be provided are:

-   a path to a file that contains the bot parameters (the precise
    format is described in detail in the next paragraph);

-   IP address of a *Viral* server;

-   the port on which a *Viral* server is accepting requests.

### Bot parameters.

A file containing information about bots needs to adhere to the
following conventions:

-   The first line of the input contains the format version, which is a
    positive integer. Currently, 1 is assumed to be the version number.

-   After the version number, any incidence of the pound sign (\#) means
    that the rest of a line is a comment and will not be taken into
    account by the parser.

-   The next number is $n$, the number of bots that will be generated.
    This should be a non-negative integer not greater than $500$.

-   The data for the $n$ bots should be placed in $n$ distinct lines.
    Each bot is described as a $4$-tuple of whitespace-separated double
    precision floating-point numbers:

    -   <span>**initial longitude**</span>;

    -   <span>**initial latitude**</span>;

    -   <span>**maximum change:**</span> represents the maximum change
        in longitude and latitude between per one second;

    -   <span>**mean time between updates:**</span> expected number of
        milliseconds between two updates (i.e. mean of
        the distribution).


