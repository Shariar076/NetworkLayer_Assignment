/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networklayer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author samsung
 */
public class Router implements Serializable{

    private int routerId;
    private int numberOfInterfaces;
    private ArrayList<IPAddress> interfaceAddrs;//list of IP address of all interfaces of the router
    private ArrayList<RoutingTableEntry> routingTable;//used to implement DVR
    private ArrayList<Integer> neighborRouterIds;//Contains both "UP" and "DOWN" state routers
    private Boolean state;//true represents "UP" state and false is for "DOWN" state

    public Router() {
        interfaceAddrs = new ArrayList<>();
        routingTable = new ArrayList<>();
        neighborRouterIds = new ArrayList<>();

        /**
         * 80% Probability that the router is up
         */
        Random random = new Random();
        double p = random.nextDouble();
        if (p <= 0.80) {
            state = true;
        } else {
            state = false;
        }

        numberOfInterfaces = 0;
    }

    public Router(int routerId, ArrayList<Integer> neighborRouters, ArrayList<IPAddress> interfaceAddrs) {
        this.routerId = routerId;
        this.interfaceAddrs = interfaceAddrs;
        this.neighborRouterIds = neighborRouters;
        routingTable = new ArrayList<>();

        /**
         * 80% Probability that the router is up
         */
        Random random = new Random();
        double p = random.nextDouble();
        if (p <= 0.80) {
            state = true;
        } else {
            state = false;
        }

        numberOfInterfaces = this.interfaceAddrs.size();
    }

    @Override
    public String toString() {
        String temp = "";
        temp += "Router ID: " + routerId + "\n";
        temp += "Intefaces: \n";
        for (int i = 0; i < numberOfInterfaces; i++) {
            temp += interfaceAddrs.get(i).getString() + "\t";
        }
        temp += "\n";
        temp += "Neighbors: \n";
        for (int i = 0; i < neighborRouterIds.size(); i++) {
            temp += neighborRouterIds.get(i) + "\t";
        }
        return temp;
    }

    /**
     * Initialize the distance(hop count) for each router. for itself,
     * distance=0; for any connected router with state=true, distance=1;
     * otherwise distance=Constants.INFTY;
     */
    public void initiateRoutingTable() {
        routingTable.clear();
        for (Router router : NetworkLayerServer.routers) {
            boolean neighbour = false;
            RoutingTableEntry rte = null;
            if (router.routerId == this.routerId) {
                rte = new RoutingTableEntry(router.routerId, 0, this.routerId);
            } else {
                for (Integer id : neighborRouterIds) {               //check the neighbours
                    if(id.intValue()==router.routerId){
                        neighbour=true;
                        rte= new RoutingTableEntry(router.routerId, 1, router.routerId);
                        break;
                    }
                }
                if(!neighbour){
                    rte= new RoutingTableEntry(router.routerId, Constants.INFTY, -1);
                }
            }
            routingTable.add(rte);
        }
    }

    /**
     * Delete all the routingTableEntry
     */
    public void clearRoutingTable() {
       routingTable.clear();
    }

    /**
     * Update the routing table for this router using the entries of Router
     * neighbor
     *
     * @param neighbor
     */
    public void updateRoutingTable(Router neighborRouter) {
        if(!neighborRouter.getState()){
            for(int i=0;i<routingTable.size();i++){
                if(routingTable.get(i).getRouterId()==neighborRouter.routerId){
                    routingTable.get(i).setDistance(Constants.INFTY);
                }
            }
        }
        else{

            for (int i = 0; i < routingTable.size(); i++) {
                RoutingTableEntry current= this.routingTable.get(i);
                RoutingTableEntry neighbour= neighborRouter.routingTable.get(i);
                double newDistance= (1+ neighbour.getDistance());
                
               // System.out.println("Entry: "+i+" in router: "+this.routerId+"NewDist: "+newDistance+" currentDIstance: "+current.getDistance());
                if(newDistance>Constants.INFTY)newDistance=Constants.INFTY;
                if(current.getGatewayRouterId()== neighborRouter.getRouterId()|| 
                        (newDistance<current.getDistance()&& neighbour.getGatewayRouterId()!=this.getRouterId())){
                    if(current.getDistance()!=newDistance){
                        
                        this.routingTable.get(i).setDistance(newDistance);
                        this.routingTable.get(i).setGatewayRouterId(neighborRouter.routerId);
                        NetworkLayerServer.convergence=true;
                    //    System.out.println("Entry: "+i+" changed in router: "+this.routerId);
                    //    System.out.println("Gateway: "+current.getGatewayRouterId());
                    //    System.out.println("Neighbour: "+neighbour.getRouterId());
                    }
                }
            }
            
        }
        
       
    }
    
 public void simpleUpdate(Router neighborRouter) {
        if(!neighborRouter.getState()){
            for(int i=0;i<routingTable.size();i++){
                if(routingTable.get(i).getRouterId()==neighborRouter.routerId){
                    routingTable.get(i).setDistance(Constants.INFTY);
                }
            }
        }
        else{

            for (int i = 0; i < routingTable.size(); i++) {
                RoutingTableEntry current= this.routingTable.get(i);
                RoutingTableEntry neighbour= neighborRouter.routingTable.get(i);
                double newDistance= (1+ neighbour.getDistance());
                
                if(newDistance>Constants.INFTY)newDistance=Constants.INFTY;
                if(newDistance<current.getDistance()){
                    if(current.getDistance()!=newDistance){
                        
                        this.routingTable.get(i).setDistance(newDistance);
                        this.routingTable.get(i).setGatewayRouterId(neighborRouter.routerId);
                        NetworkLayerServer.convergence=true;
                    }
                }
            }
            
        }
        
       
    }

    /**
     * If the state was up, down it; if state was down, up it
     */
    public void revertState() {
        state = !state;
        if (state == true) {
            this.initiateRoutingTable();
        } else {
            this.clearRoutingTable();
        }
    }

    public int getRouterId() {
        return routerId;
    }
    
    public void setRouterId(int routerId) {
        this.routerId = routerId;
    }

    public int getNumberOfInterfaces() {
        return numberOfInterfaces;
    }

    public void setNumberOfInterfaces(int numberOfInterfaces) {
        this.numberOfInterfaces = numberOfInterfaces;
    }

    public ArrayList<IPAddress> getInterfaceAddrs() {
        return interfaceAddrs;
    }

    public void setInterfaceAddrs(ArrayList<IPAddress> interfaceAddrs) {
        this.interfaceAddrs = interfaceAddrs;
        numberOfInterfaces = this.interfaceAddrs.size();
    }

    public ArrayList<RoutingTableEntry> getRoutingTable() {
        return routingTable;
    }

    public void addRoutingTableEntry(RoutingTableEntry entry) {
        this.routingTable.add(entry);
    }

    public ArrayList<Integer> getNeighborRouterIds() {
        return neighborRouterIds;
    }

    public void setNeighborRouterIds(ArrayList<Integer> neighborRouterIds) {
        this.neighborRouterIds = neighborRouterIds;
    }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

}
