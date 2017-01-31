# Java HTTP Web Proxy Cache Server

## Functionality:

Takes incoming HTTP requests on a specificed port, and tries to serve the object from a local cache. If the cache does not contain the desired object, the server then acts as a client and contacts the origin server to obtain the object. Once received, the object is served to the original client, and the object is cached locally.

The server is iterative (i.e. only accepts one connection at a time).

## Run:

In <code>src/</code> folder run:

<code>./jmake</code>

to compile and run on port <code>8080</code>, or run:

<code>java WebProxy \<port number\></code>

to configure manually.


