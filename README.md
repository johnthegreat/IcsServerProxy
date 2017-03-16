IcsServerProxy

Standalone application that enables pass-through of traffic (timeseal and non-timeseal) to Internet Chess Servers (ICS) such as FICS (freechess.org).

A number of potential uses:
* Intercept / modify outgoing traffic to provide input or output to third party integrations.
* Allowing users to connect to your proxy, you can choose to terminate all connections simultaneously (e.g. enforce curfews).
* Allowing the proxy to "listen" on multiple ports can work around certain limitations of potential servers to "listen" only on one port.
* This may useful as a sort of "load balancer" or to determine which backing server(s) can be connected to.

Requires Apache Commons Codec library.
Should be able to be built and usable out-of-the-box.