/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networklayer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import static networklayer.NetworkLayerServer.routers;

/**
 *
 * @author samsung
 */
public class Client {

    public static void printRoutingTable(ArrayList<RoutingTableEntry> currentRoutingTable) {
        if (currentRoutingTable.size() == 0) {
            System.out.println("Router Down!!");
        } else {
            for (RoutingTableEntry rte : currentRoutingTable) {
                System.out.println(rte.getRouterId() + "   " + rte.getDistance() + "   " + rte.getGatewayRouterId());
            }
        }
        System.out.println("###########################################################################");

    }

    public static void main(String[] args) {

        Socket socket = null;
        ObjectInputStream input = null;
        ObjectOutputStream output;
        try {
            socket = new Socket("localhost", 1234);
            input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("Connected to server");
            EndDevice endDevice = (EndDevice) input.readObject();

            String isReady = "notReady";
            while (!isReady.equals("ready")) {
                isReady = (String) input.readObject();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String command = reader.readLine();
            output.writeObject(command);
            if (command.equals("s")) {
                int drops=0;
                for (int i = 0; i < 30; i++) {
                    String randomMessage = "message no.: " + i;//+'\n';
                    String spcRequest = "SHOW_ROUTE";
                    output.writeObject(randomMessage);

                    if (i == 20) {
                        output.writeObject(spcRequest);
                        String routingPath = (String) input.readObject();
                        System.out.println("Routing path: " + routingPath);
                        int hopCount = (int) input.readObject();
                        System.out.println("# of hops: " + hopCount);
                        if(hopCount==-1)drops++;
                        int numOfrouters = (int) input.readObject();
                        System.out.println("# of Routers: " + numOfrouters);
                        for (int j = 0; j < numOfrouters; j++) {
                            ArrayList<RoutingTableEntry> routingTable = (ArrayList<RoutingTableEntry>) input.readObject();
                            System.out.println("Router: " + (j+1));
                            printRoutingTable(routingTable);
                        }
                        
                    } else {
                        output.writeObject("notSpecial");
                        int hopCount = (int) input.readObject();
                        
                        if(hopCount==-1)drops++;
                        
                    }
                    //TimeUnit.SECONDS.sleep(1);
                    
                }
                System.out.println("# of drops: " + drops);
                
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }catch (ClassNotFoundException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        /**
         * Tasks
         */
        /*
        1. Receive EndDevice configuration from server
        2. [Adjustment in NetworkLayerServer.java: Server internally
        handles a list of active clients.]
        3. for(int i=0;i<100;i++)
        4. {
        5.      Generate a random message
        6.      [Adjustment in ServerThread.java] Assign a random receiver from active client list
        7.      if(i==20)
        8.      {
        9.            Send the messageto server and a special request "SHOW_ROUTE"
        10.           Display routing path, hop count and routing table of each router [You need to receive
        all the required info from the server in response to "SHOW_ROUTE" request]
        11.     }
        12.     else
        13.     {
        14.           Simply send the message and recipient IP address to server.
        15.     }
        16.     If server can successfully send the message, client will get an acknowledgement along with hop count
        Otherwise, client will get a failure message [dropped packet]
        17. }
        18. Report average number of hops and drop rate
         */
        /**
         * Tasks
         */
        /*
        1. Receive EndDevice configuration from server
        2. [Adjustment in NetworkLayerServer.java: Server internally
        handles a list of active clients.]
        3. for(int i=0;i<100;i++)
        4. {
        5.      Generate a random message
        6.      [Adjustment in ServerThread.java] Assign a random receiver from active client list
        7.      if(i==20)
        8.      {
        9.            Send the messageto server and a special request "SHOW_ROUTE"
        10.           Display routing path, hop count and routing table of each router [You need to receive
        all the required info from the server in response to "SHOW_ROUTE" request]
        11.     }
        12.     else
        13.     {
        14.           Simply send the message and recipient IP address to server.
        15.     }
        16.     If server can successfully send the message, client will get an acknowledgement along with hop count
        Otherwise, client will get a failure message [dropped packet]
        17. }
        18. Report average number of hops and drop rate
         */
        /**
         * Tasks
         */
        /*
        1. Receive EndDevice configuration from server
        2. [Adjustment in NetworkLayerServer.java: Server internally
        handles a list of active clients.]
        3. for(int i=0;i<100;i++)
        4. {
        5.      Generate a random message
        6.      [Adjustment in ServerThread.java] Assign a random receiver from active client list
        7.      if(i==20)
        8.      {
        9.            Send the messageto server and a special request "SHOW_ROUTE"
        10.           Display routing path, hop count and routing table of each router [You need to receive
        all the required info from the server in response to "SHOW_ROUTE" request]
        11.     }
        12.     else
        13.     {
        14.           Simply send the message and recipient IP address to server.
        15.     }
        16.     If server can successfully send the message, client will get an acknowledgement along with hop count
        Otherwise, client will get a failure message [dropped packet]
        17. }
        18. Report average number of hops and drop rate
         */
        /**
         * Tasks
         */
        /*
        1. Receive EndDevice configuration from server
        2. [Adjustment in NetworkLayerServer.java: Server internally
        handles a list of active clients.]
        3. for(int i=0;i<100;i++)
        4. {
        5.      Generate a random message
        6.      [Adjustment in ServerThread.java] Assign a random receiver from active client list
        7.      if(i==20)
        8.      {
        9.            Send the messageto server and a special request "SHOW_ROUTE"
        10.           Display routing path, hop count and routing table of each router [You need to receive
        all the required info from the server in response to "SHOW_ROUTE" request]
        11.     }
        12.     else
        13.     {
        14.           Simply send the message and recipient IP address to server.
        15.     }
        16.     If server can successfully send the message, client will get an acknowledgement along with hop count
        Otherwise, client will get a failure message [dropped packet]
        17. }
        18. Report average number of hops and drop rate
         */ 
        /**
         * Tasks
         */
        /*
        1. Receive EndDevice configuration from server
        2. [Adjustment in NetworkLayerServer.java: Server internally
        handles a list of active clients.]
        3. for(int i=0;i<100;i++)
        4. {
        5.      Generate a random message
        6.      [Adjustment in ServerThread.java] Assign a random receiver from active client list
        7.      if(i==20)
        8.      {
        9.            Send the messageto server and a special request "SHOW_ROUTE"
        10.           Display routing path, hop count and routing table of each router [You need to receive
        all the required info from the server in response to "SHOW_ROUTE" request]
        11.     }
        12.     else
        13.     {
        14.           Simply send the message and recipient IP address to server.
        15.     }
        16.     If server can successfully send the message, client will get an acknowledgement along with hop count
        Otherwise, client will get a failure message [dropped packet]
        17. }
        18. Report average number of hops and drop rate
         */
        /**
         * Tasks
         */
        /*
        1. Receive EndDevice configuration from server
        2. [Adjustment in NetworkLayerServer.java: Server internally
        handles a list of active clients.]
        3. for(int i=0;i<100;i++)
        4. {
        5.      Generate a random message
        6.      [Adjustment in ServerThread.java] Assign a random receiver from active client list
        7.      if(i==20)
        8.      {
        9.            Send the messageto server and a special request "SHOW_ROUTE"
        10.           Display routing path, hop count and routing table of each router [You need to receive
        all the required info from the server in response to "SHOW_ROUTE" request]
        11.     }
        12.     else
        13.     {
        14.           Simply send the message and recipient IP address to server.
        15.     }
        16.     If server can successfully send the message, client will get an acknowledgement along with hop count
        Otherwise, client will get a failure message [dropped packet]
        17. }
        18. Report average number of hops and drop rate
         */

        /**
         * Tasks
         */
        /*
        1. Receive EndDevice configuration from server
        2. [Adjustment in NetworkLayerServer.java: Server internally
        handles a list of active clients.]
        3. for(int i=0;i<100;i++)
        4. {
        5.      Generate a random message
        6.      [Adjustment in ServerThread.java] Assign a random receiver from active client list
        7.      if(i==20)
        8.      {
        9.            Send the messageto server and a special request "SHOW_ROUTE"
        10.           Display routing path, hop count and routing table of each router [You need to receive
        all the required info from the server in response to "SHOW_ROUTE" request]
        11.     }
        12.     else
        13.     {
        14.           Simply send the message and recipient IP address to server.
        15.     }
        16.     If server can successfully send the message, client will get an acknowledgement along with hop count
        Otherwise, client will get a failure message [dropped packet]
        17. }
        18. Report average number of hops and drop rate
         */
        /**
         * Tasks
         */
        /*
            1. Receive EndDevice configuration from server
            2. [Adjustment in NetworkLayerServer.java: Server internally
            handles a list of active clients.]
            3. for(int i=0;i<100;i++)
            4. {
            5.      Generate a random message
            6.      [Adjustment in ServerThread.java] Assign a random receiver from active client list
            7.      if(i==20)
            8.      {
            9.            Send the messageto server and a special request "SHOW_ROUTE"
            10.           Display routing path, hop count and routing table of each router [You need to receive
            all the required info from the server in response to "SHOW_ROUTE" request]
            11.     }
            12.     else
            13.     {
            14.           Simply send the message and recipient IP address to server.
            15.     }
            16.     If server can successfully send the message, client will get an acknowledgement along with hop count
            Otherwise, client will get a failure message [dropped packet]
            17. }
            18. Report average number of hops and drop rate
         */
    }
}
