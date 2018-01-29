/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networklayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author CSE_BUET
 */
public class NetworkLayerServer {

    static int clientCount = 1;
    static ArrayList<Router> routers = new ArrayList<>();
    static ArrayList<IPAddress> activeClients = new ArrayList<>();
    static RouterStateChanger stateChanger = null;
    static boolean convergence;
    /**
     * Each map entry represents number of client end devices connected to the
     * interface
     */
    static Map<IPAddress, Integer> clientInterfaces = new HashMap<>();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        /**
         * Task: Maintain an active client list
         */
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(1234);
        } catch (IOException ex) {
            Logger.getLogger(NetworkLayerServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Server Ready: " + serverSocket.getInetAddress().getHostAddress());

        System.out.println("Creating router topology");

        readTopology();
        // printRouters();

        /**
         * Initialize routing tables for all routers
         */
        initRoutingTables();
        //printRoutingTables();
        /**
         * Update routing table using distance vector routing until convergence
         */
        DVR(5);
        printRoutingTables();

        /**
         * Starts a new thread which turns on/off routers randomly depending on
         * parameter Constants.LAMBDA
         */
        stateChanger = new RouterStateChanger();

        while (true) {
            try {
                Socket clientSock = serverSocket.accept();
                System.out.println("Client attempted to connect");
                ServerThread.endDevice = getClientDeviceSetup();
                new ServerThread(clientSock);
            } catch (IOException ex) {
                Logger.getLogger(NetworkLayerServer.class.getName()).log(Level.SEVERE, null, ex);

            }
        }

    }

    public static void initRoutingTables() {
        for (int i = 0; i < routers.size(); i++) {
            routers.get(i).initiateRoutingTable();
        }
    }

    public static Router getRouter(int id) {
        for (Router router : routers) {
            if (router.getRouterId() == id) {
                return router;
            }
        }
        return null;
    }
    
    public static Router getRouter(IPAddress ip) {
        Short[] thisIP=ip.getBytes();
        for (Router router : routers) {
            for(IPAddress currIp:router.getInterfaceAddrs()){
                Short[] currIP=currIp.getBytes();
                if(thisIP[0].equals(currIP[0])&&thisIP[1].equals(currIP[1])&&thisIP[2].equals(currIP[2]))return router;
            }
        }
        return null;
    }

    /**
     * Task: Implement Distance Vector Routing with Split Horizon and Forced
     * Update
     */
    public static void DVR(int startingRouterId) {
        /**
         * pseudocode
         */
        /*
        while(convergence)
        {
            //convergence means no change in any routingTable before and after executing the following for loop
            for each router r <starting from the router with routerId = startingRouterId, in any order>
            {
                1. T <- getRoutingTable of the router r
                2. N <- find routers which are the active neighbors of the current router r
                3. Update routingTable of each router t in N using the 
                   routing table of r [Hint: Use t.updateRoutingTable(r)]
            }
        }
         */

        convergence = true;

        while (convergence) {
            convergence = false;
            int count = 0;
            int id = 0;
            while (count < routers.size()) {
                id = (startingRouterId + count - 1) % (routers.size()) + 1;
                Router router = getRouter(id);
                System.out.println("DVR on router: " + router.getRouterId());

                for (Integer neighbourId : router.getNeighborRouterIds()) {
                    Router neighbour = getRouter(neighbourId.intValue());
                    if (neighbour.getState()) {
                        neighbour.updateRoutingTable(router);
                    }

                }
                count++;
            }

        }

    }

    /**
     * Task: Implement Distance Vector Routing without Split Horizon and Forced
     * Update
     */
    public static void simpleDVR(int startingRouterId) {
        convergence = true;

        while (convergence) {
            convergence = false;
            int count = 0;
            int id = 0;
            while (count < routers.size()) {
                id = (startingRouterId + count - 1) % (routers.size()) + 1;
                Router router = getRouter(id);
                System.out.println("DVR on router: " + router.getRouterId());

                for (Integer neighbourId : router.getNeighborRouterIds()) {
                    Router neighbour = getRouter(neighbourId.intValue());
                    if (neighbour.getState()) {
                        neighbour.updateRoutingTable(router);
                    }

                }
                count++;
            }

        }

    }
    
    public static void removeClient(IPAddress ip){
        Short[] add=ip.getBytes();
        for(int i=0;i<activeClients.size();i++){
            Short[] curr=activeClients.get(i).getBytes();
            if(curr[0].equals(add[0])&&curr[1].equals(add[1])&&curr[2].equals(add[2])){
                activeClients.remove(i);
                break;
            }
        }
        
    }
    
    public static int getClientId(IPAddress ip){
        Short[] add=ip.getBytes();
        for(int i=0;i<activeClients.size();i++){
            Short[] curr=activeClients.get(i).getBytes();
            if(curr[0].equals(add[0])&&curr[1].equals(add[1])&&curr[2].equals(add[2])){
                return i;
            }
        }
        return -1;
    }

    public static EndDevice getClientDeviceSetup() {
        Random random = new Random();
        int r = Math.abs(random.nextInt(clientInterfaces.size()));

        System.out.println("Size: " + clientInterfaces.size() + "\n" + r);

        IPAddress ip = null;
        IPAddress gateway = null;

        int i = 0;
        for (Map.Entry<IPAddress, Integer> entry : clientInterfaces.entrySet()) {
            IPAddress key = entry.getKey();
            Integer value = entry.getValue();
            if (i == r) {
                gateway = key;
                ip = new IPAddress(gateway.getBytes()[0] + "." + gateway.getBytes()[1] + "." + gateway.getBytes()[2] + "." + (value + 2));
                activeClients.add(ip);
                value++;
                clientInterfaces.put(key, value);
                break;
            }
            i++;
        }

        EndDevice device = new EndDevice(ip, gateway);
        System.out.println("Device : " + ip + "::::" + gateway);
        return device;
    }

    public static void printRouters() {
        for (int i = 0; i < routers.size(); i++) {
            System.out.println("------------------\n" + routers.get(i));
        }
    }

    public static void printRoutingTables() {
        ArrayList<RoutingTableEntry> currentRoutingTable;
        for (Router router : routers) {
            System.out.println("Router no: " + router.getRouterId());
            currentRoutingTable = router.getRoutingTable();
            for (RoutingTableEntry rte : currentRoutingTable) {
                System.out.println(rte.getRouterId() + "   " + rte.getDistance() + "   " + rte.getGatewayRouterId());
            }
            System.out.println("###########################################################################");
        }
    }

    public static void readTopology() {
        Scanner inputFile = null;
        try {
            inputFile = new Scanner(new File("topology.txt"));
            //skip first 27 lines
            int skipLines = 27;
            for (int i = 0; i < skipLines; i++) {
                inputFile.nextLine();
            }

            //start reading contents
            while (inputFile.hasNext()) {
                inputFile.nextLine();
                int routerId;
                ArrayList<Integer> neighborRouters = new ArrayList<>();
                ArrayList<IPAddress> interfaceAddrs = new ArrayList<>();

                routerId = inputFile.nextInt();

                int count = inputFile.nextInt();
                for (int i = 0; i < count; i++) {
                    neighborRouters.add(inputFile.nextInt());
                }
                count = inputFile.nextInt();
                inputFile.nextLine();

                for (int i = 0; i < count; i++) {
                    String s = inputFile.nextLine();
                    System.out.println(s);
                    IPAddress ip = new IPAddress(s);
                    interfaceAddrs.add(ip);

                    /**
                     * First interface is always client interface
                     */
                    if (i == 0) {
                        //client interface is not connected to any end device yet
                        clientInterfaces.put(ip, 0);
                    }
                }
                Router router = new Router(routerId, neighborRouters, interfaceAddrs);
                routers.add(router);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(NetworkLayerServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
