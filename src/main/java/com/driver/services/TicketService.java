package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db
        Optional<Train> optionalTrain=trainRepository.findById(bookTicketEntryDto.getTrainId());
        if(!optionalTrain.isPresent()) throw  new Exception("Invalid Train Id!!");
        String[] route=optionalTrain.get().getRoute().split(",");
        int start=-1,end=-1;
        boolean startStation=false,endStation=false;
        for(int i=0;i<route.length;i++){
            if(route[i].equals(bookTicketEntryDto.getFromStation().toString())){
                start=i;
                startStation=true;
            }
            if(route[i].equals(bookTicketEntryDto.getToStation().toString())){
                end=i;
                endStation=true;
            }
        }
        if(start==-1||end==-1 || !startStation || !endStation || start>end){
            throw new Exception("Invalid stations");
        }
        if(optionalTrain.get().getNoOfSeats()<bookTicketEntryDto.getNoOfSeats()){
            throw new Exception("Less tickets are available");
        }
        int noOfStations=Math.abs(end-start);
        int totalFare=noOfStations*300*bookTicketEntryDto.getNoOfSeats();

        Train train=optionalTrain.get();
//        train.setNoOfSeats(train.getNoOfSeats()-bookTicketEntryDto.getNoOfSeats());


        Ticket ticket=new Ticket();
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());
        ticket.setTotalFare(totalFare);
        ticket.setTrain(optionalTrain.get());
        for(int id:bookTicketEntryDto.getPassengerIds()){
            Optional<Passenger> optionalPassenger=passengerRepository.findById(id);
            ticket.getPassengersList().add(optionalPassenger.get());
        }
       Ticket savedTicket=ticketRepository.save(ticket);
        train.getBookedTickets().add(savedTicket);
        Optional<Passenger> optionalPassenger=passengerRepository.findById(bookTicketEntryDto.getBookingPersonId());
        Passenger passenger=optionalPassenger.get();
        passenger.getBookedTickets().add(savedTicket);
        trainRepository.save(train);
        passengerRepository.save(passenger);

       return savedTicket.getTicketId();

    }
}
