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

Precompiled `.apk` and `.jar` files of the *Viral* server, Android client and "fake" client are readily available in the latest release tag of this repository.

The source may be downloaded as an archive from GitHub, or the
repository may be directly cloned by running the following command
within a terminal:

    $ git clone https://github.com/PetarV-/viral.git`

### Server compilation and execution

Once the source code has been cloned, it may be compiled by invoking
`javac` on the relevant files. Once in the root folder of the
repository, execute the following:
    
    $ cd server/src/main/java
    $ javac com/hackbridge/viral/*.java
    $ java com.hackbridge.viral.Main <port> <round_duration_ms> <delay_between_rounds_ms> <network_params_file> <run_tikzer?> (<tikzer_port>)

The final command launches the server; the parameters that need to be
provided are as follows:

-   the port the server will listen on for clients;

-   the duration of a single experiment in milliseconds;

-   the delay between experiments in milliseconds;

-   a path to a file containing the multiplex network’s parameters
    (described in more detail in the next paragraph);

-   a boolean string (`true` or `false`)
    specifying whether or not the visualisation utility should
    be launched. If `true`, an additional
    `tikzer_port` parameter should be provided,
    specifying the port at which the visualisation utility will be
    serving the latest visualisation.

### Network parameters

The `network_params_file` contains lines in the format
`<parameter> <value>`. The following parameters
are used to configure the properties of the multiplex network:

Parameter | Description
--- | ---
`initialInfectedProbability` | The probability a new node is initially diseased (with physical state `infected` or `carrier`)
`initialAwareProbability` | The probability a new node has initial awareness state `aware`, as opposed to `unaware`
`initialSymptomaticProbability` | The probability that a newly infected node has physical state `infected`, as opposed to `carrier`
`infectedIfVaccinatedProbability` | The probability a vaccinated node becomes infected with the disease agent when one of its edges with another infected node is activated
`spontaneousRecoveryProbability` | The probability that a diseased node spontaneously recovers when one of its edges is activated
`activateEdgeProbability` | The probability that an edge is activated
`infectorProbability` | The probability that a new node has the role `infector`, as opposed to `human`
`developSymptomsProbability` | The probability that a `carrier` node becomes `infected` when one of its edges is activated
`lambdaFactor` | Used in the distance expression. A larger value increases the rate at which an edge’s activation probability decreases with distance
`exponentialMultiplier` | Used in the distance expression, for scaling the edge weights
`loggingFrequency` | The frequency at which the network state is logged to a file (expressed in the number of steps between logs)
`edgeSelectionAlgorithm` | The algorithm used for edge selection (`ExactRandom` or `GibbsSampling`)
`numStepsForGibbsSampling` | If the `GibbsSampling` algorithm is used for edge selection, specifies the number of sampling steps it should make before reporting the edge to be activated.

### Android setup

Compiling the Android application from source is heavily dependent on the architecture of the host system as well as the IDE you are using, and therefore we do not recommend setting it up in this fashion. Should you still choose to do this, [Android Studio](https://developer.android.com/studio/index.html) will be required in the least, in order to gain access to all of the required packages for Android development. In all other cases, please proceed by downloading the precompiled `Viral.apk` file from the [latest release tag](https://github.com/PetarV-/viral/releases) of this repository.

The Android device chosen to install the application should be newer
than Android level 16. After installing the `Viral.apk` file, simply
press on the *Viral* icon, and then input the hostname and port of the
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
Poisson process i.e. the time *T* between updates is a random variable
with an exponential distribution *E(λ)*, with the
probability density function *f(t) = λ exp(-λt)*.
No other behaviour is given to the bots, other than them vaccinating
themselves if they have access to the valid vaccine code and have the
*human* role.

### Fake client configuration

If you are compiling the fake client from source, execute the following (starting from the root
folder of the repository):

    $ cd fakeclient
    $ javac com/hackbridge/viral/*.java
    $ java com.hackbridge.viral.BotGenerator <bot_parameter_file> <server> <port>

The command line arguments that need to be provided are:

-   a path to a file that contains the bot parameters (the precise
    format is described in detail in the next paragraph);

-   IP address of a *Viral* server;

-   the port on which a *Viral* server is accepting requests.

### Bot parameters

A file containing information about bots needs to adhere to the
following conventions:

-   The first line of the input contains the format version, which is a
    positive integer. Currently, 1 is assumed to be the version number.

-   After the version number, any incidence of the pound sign (\#) means
    that the rest of a line is a comment and will not be taken into
    account by the parser.

-   The next number is *n*, the number of bots that will be generated.
    This should be a non-negative integer not greater than 500.

-   The data for the $n$ bots should be placed in *n* distinct lines.
    Each bot is described as a 4-tuple of whitespace-separated double
    precision floating-point numbers:

    -   **initial longitude**;

    -   **initial latitude**;

    -   **maximum change:** represents the maximum change
        in longitude and latitude between per one second;

    -   **mean time between updates:** expected number of
        milliseconds between two updates (i.e. mean of
        the distribution).


License
-------
MIT

References
----------

## References

If you make advantage of Viral or derive it within your research, please cite the following article:

Veličković, P., Ivašković A., Lau, S. and Stanojević, M. (2016) Viral: Real-world competing process simulations on multiplex networks. *The 1st Belgrade Bioinformatics Conference (BelBi 2016)*
