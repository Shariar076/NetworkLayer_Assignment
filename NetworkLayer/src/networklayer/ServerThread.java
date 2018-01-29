/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networklayer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author samsung
 */
public class ServerThread implements Runnable {

    private Thread t;
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    public static EndDevice endDevice;
    public String route = "";
    public int droppedPckt=0;
    public ServerThread(Socket socket) {

        this.socket = socket;
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            // output.flush();
            input = new ObjectInputStream(socket.getInputStream());

        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Server Ready for client " + NetworkLayerServer.clientCount);
        NetworkLayerServer.clientCount++;

        t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {

        try {

            output.writeObject(endDevice);

            while (true) {
                if (NetworkLayerServer.activeClients.size() > 1) {
                    output.writeObject("ready");
                    break;
                } else {
                    output.writeObject("notReady");
                }
            }

            IPAddress sourceIp = endDevice.getIp();

            String command = (String) input.readObject();
            if (command.equals("s")) {
                while (true) {
                    IPAddress destIp = sourceIp;
                    Router sourceRouter= NetworkLayerServer.getRouter(sourceIp);
                    Router destRouter= NetworkLayerServer.getRouter(destIp);
                    while (sourceIp == destIp/*||sourceRouter.getRouterId()==destRouter.getRouterId()*/) {
                        Random random = new Random();
                        int randIndx = random.nextInt(NetworkLayerServer.activeClients.size());
                        destIp = NetworkLayerServer.activeClients.get(randIndx); //new IPAddress("192.168.100.2");
                    }
                    String message = (String) input.readObject();
                    String request = (String) input.readObject();
                    System.out.println("Client: " + NetworkLayerServer.getClientId(sourceIp) + " msg: " + message);
                    Packet testPacket = new Packet(message, request, sourceIp, destIp);
                    int hop = (int) deliverPacket(testPacket);
                    System.out.println("Route: " + route);
                    if (request.equals("SHOW_ROUTE")) {
                        output.writeObject(route);
                        output.writeObject(hop);
                        output.writeObject(NetworkLayerServer.routers.size());
                        output.flush();
                        for (int i = 0; i < NetworkLayerServer.routers.size(); i++) {
                            output.writeObject(NetworkLayerServer.routers.get(i).getRoutingTable());
                            output.flush();
                        }
                        //System.out.println("special done!!");
                    }else{
                        output.writeObject(hop);
                    }
                }
            }
        } catch (IOException ex) {
            NetworkLayerServer.removeClient(endDevice.getIp());
            System.out.println("Client: " + NetworkLayerServer.getClientId(endDevice.getIp()) + " is done current active: " + NetworkLayerServer.activeClients.size());

            //Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        /**
         * Synchronize actions with client.
         */
        /*
        Tasks:
        1. Upon receiving a packet server will assign a recipient.
        [Also modify packet to add destination]
        2. call deliverPacket(packet)
        3. If the packet contains "SHOW_ROUTE" request, then fetch the required information
        and send back to client
        4. Either send acknowledgement with number of hops or send failure message back to client
         */

        /**
         * Synchronize actions with client.
         */
        /*
            Tasks:
            1. Upon receiving a packet server will assign a recipient.
            [Also modify packet to add destination]
            2. call deliverPacket(packet)
            3. If the packet contains "SHOW_ROUTE" request, then fetch the required information
            and send back to client
            4. Either send acknowledgement with number of hops or send failure message back to client
         */
    }

    /**
     * Returns true if successfully delivered Returns false if packet is dropped
     *
     * @param p
     * @return
     */
    public double deliverPacket(Packet p) {
        /*
        1. Find the router s which has an interface
                such that the interface and source end device have same network address.
        2. Find the router d which has an interface
                such that the interface and destination end device have same network address.
        3. Implement forwarding, i.e., s forwards to its gateway router x considering d as the destination.
                similarly, x forwards to the next gateway router y considering d as the destination, 
                and eventually the packet reaches to destination router d.
                
            3(a) If, while forwarding, any gateway x, found from routingTable of router x is in down state[x.state==FALSE]
                    (i) Drop packet
                    (ii) Update the entry with distance Constants.INFTY
                    (iii) Block NetworkLayerServer.stateChanger.t
                    (iv) Apply DVR starting from router r.
                    (v) Resume NetworkLayerServer.stateChanger.t
                            
            3(b) If, while forwarding, a router x receives the packet from router y, 
                    but routingTableEntry shows Constants.INFTY distance from x to y,
                    (i) Update the entry with distance 1
                    (ii) Block NetworkLayerServer.stateChanger.t
                    (iii) Apply DVR starting from router x.
                    (iv) Resume NetworkLayerServer.stateChanger.t
                            
        4. If 3(a) occurs at any stage, packet will be dropped, 
            otherwise successfully sent to the destination router
         */
        Boolean res = null;
        Router sourceRouter = null, destRouter = null;
        boolean srcFound = false, destFound = false;
        for (Router router : NetworkLayerServer.routers) {
            if (srcFound && destFound) {
                break;
            }
            for (IPAddress ip : router.getInterfaceAddrs()) {
                Short[] ipCurr = ip.getBytes();
                Short[] ipSource = p.getSourceIP().getBytes();
                Short[] ipDest = p.getDestinationIP().getBytes();
                if (ipCurr[0].equals(ipSource[0]) && ipCurr[1].equals(ipSource[1]) && ipCurr[2].equals(ipSource[2])) {
                    sourceRouter = router;
                    System.out.println("Source router: " + sourceRouter.getRouterId());
                    srcFound = true;

                }
                if (ipCurr[0].equals(ipDest[0]) && ipCurr[1].equals(ipDest[1]) && ipCurr[2].equals(ipDest[2])) {
                    destRouter = router;
                    System.out.println("Destination router: " + destRouter.getRouterId());
                    srcFound = true;

                }
            }
        }
        route = String.valueOf(sourceRouter.getRouterId());
        double hopCount = 0;
        
        while (true) {
            if (sourceRouter.getRouterId() == destRouter.getRouterId()) {
                break;
            }
            if (!sourceRouter.getState() /*&& !destRouter.getState()*/) {
                System.out.println("Source: "+sourceRouter.getRouterId()+" is down");
                droppedPckt++;
                return -1;
            }

            for (RoutingTableEntry currentEntry : sourceRouter.getRoutingTable()) {
                if (sourceRouter.getNeighborRouterIds().contains(currentEntry)) {
                    Router neighbourRouter = NetworkLayerServer.getRouter(currentEntry.getRouterId());
                    if (neighbourRouter.getState() && currentEntry.getDistance() == Constants.INFTY) {
                        currentEntry.setDistance(1);
                        currentEntry.setGatewayRouterId(currentEntry.getRouterId());
                        NetworkLayerServer.stateChanger.t.stop();
                        NetworkLayerServer.DVR(sourceRouter.getRouterId());
                        //  NetworkLayerServer.stateChanger.t.start();
                        NetworkLayerServer.stateChanger = new RouterStateChanger();
                    }
                    if (!neighbourRouter.getState() && currentEntry.getDistance() != Constants.INFTY) {
                        currentEntry.setDistance(Constants.INFTY);
                        currentEntry.setGatewayRouterId(-1);
                        NetworkLayerServer.stateChanger.t.stop();
                        NetworkLayerServer.DVR(sourceRouter.getRouterId());
                        //  NetworkLayerServer.stateChanger.t.start();
                        NetworkLayerServer.stateChanger = new RouterStateChanger();
                    }
                }
            }

            for (RoutingTableEntry currentEntry : sourceRouter.getRoutingTable()) {
                if (currentEntry.getRouterId() == destRouter.getRouterId()) {
                    if (currentEntry.getDistance() == Constants.INFTY) {
                        System.out.println("Router: " + currentEntry.getRouterId() + " is down");
                        droppedPckt++;
                        return -1;
                    }
                    sourceRouter = NetworkLayerServer.getRouter(currentEntry.getGatewayRouterId());
                    route += " >> " + currentEntry.getGatewayRouterId();
                    hopCount++;
                    break;
                }
            }
        }

        return hopCount;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj); //To change body of generated methods, choose Tools | Templates.
    }

}
