# Reliable-UDP

**How Acknowledgement in this implementation's working:**

We are creating a boolean array for acknowledgement where each index signifies the packet number and value at that index represents whether the packet is received or not. (FALSE-not received. TRUE- received).

The packet number is stored at Zeroth index of each datagram packet sent by the server. EG:

| true | true | false | true | true | false | true | false |


0 1 2 3 4 5 6 7

## GoBackN scenario:

In this case server will encounter &quot;false&quot; at index 2 and thus will resend all the packets followed by packet number 2(including 2).

## Selective Repeat scenario:

In this case server will encounter &quot;false&quot; at index 2, 5 , 7 and thus selectively resend these packets back to the client.



## SERVER

1. Creates a socket connection at port
2. Creates new datagram packets with buffer size of 4KB.The server will wait for client request

    
3. Once the server receives the resource request, it will start checking for the requested
4. Converts the file to byte stream and divide it into packets of size 4kb each and send all
5. Now the server will ask for Ack from theclient
6. Then it handles the acknowledgement by resending the packets (GoBack N or SelectiveRepeat).
7. 5,6 will be executed until server receives a positive acknowledgement, i.e., all
8. Finally, it will notify the client that all packets are received by sending &quot;successfully sent the resource&quot;.

## CLIENT

1. Creates new socket connection at port number 9998

2. Sends a request for resource&quot;photo.jpg&quot;
3. It will update acknowledgement array every time it receives
4. Sends the same ack array to Server every time server requests for
5. Execute Step 3 until it receives data packet with success
6. On successful transmission, it will prompt for the file name by which it has to be stored in the client-side directory.
6. Accumulates all the packets together and store in the specified

### Comparison:

  1. **TCP with Go-Back-N overUDP**

- Our UDP acknowledgement is max of 32 bytes, which can send file up to 1MB with 4KB buffer reliably.

Taking the scenario under consideration where the Server is sending 30 packets and packet number 10 gets lost during transmission at first iteration, Using TCP implementation, it would have used minimum of 30\*5 + 20\*5 = **250 words** (5words/acknowledgment).

But through reliable UDP implementation which we have designed, it will take at most **8words** for transmission of whole file.

Thus, we can say this implementation is more cost effective.



  2. **TCP with Selective Repeat overUDP**

- Taking the scenario under consideration where the Server is sending 30 packets and packet number 10 gets lost during transmission at first iteration, Using TCP implementation, it would have used minimum of 30\*5 + 1\*5 = **155 words** (5words/acknowledgment).

But through reliable UDP implementation which we have designed, it will take at most **8words** for transmission of whole file.



  3. **Go-Back-N over UDP with Selective Repeat overUDP**

-Considering the scenario, we have generated here where the Server is sending about 30 packets of data and packet no. 10 gets lost in between for the first time and Server needs to resend it to client.

Implementing Go-Back-N over UDP, the server had to send all the packets succeeding 10th again. i.e., 21 packets again.

But implementing Selective Repeat over UDP, the server had to resend only the 10th packet which was lost.

Thus, Selective Repeat proved to be more efficient.
