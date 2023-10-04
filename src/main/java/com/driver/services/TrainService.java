package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library
        Train train=new Train();
        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());
        String route="";
        for(Station s:trainEntryDto.getStationRoute()){
            if(route.length()==0) route+=s.toString();
           else route+=","+s.toString();
        }
        train.setRoute(route);
       Train savedTrain=trainRepository.save(train);
        return savedTrain.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.

        Train train=trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();
        String[] route=train.getRoute().split(",");
        HashMap<String,Integer> routeMap=new HashMap<>();
        for(int i=0;i< route.length;i++){
            routeMap.put(route[i],i);
        }
        String startStation=seatAvailabilityEntryDto.getFromStation().toString();
        String endStation=seatAvailabilityEntryDto.getToStation().toString();
        int availableSeats=train.getNoOfSeats();
        for(Ticket ticket: train.getBookedTickets()){
            if(routeMap.get(ticket.getFromStation().toString())<=routeMap.get(startStation)
            && routeMap.get(ticket.getToStation().toString())>=routeMap.get(endStation)){
                availableSeats-=ticket.getPassengersList().size();
            }
        }


       return availableSeats;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.

        Train train=trainRepository.findById(trainId).get();
        String[] route=train.getRoute().split(",");
        boolean validStation=false;
        for(String r:route){
            if(r.equals(station.toString())){
                validStation=true;
            }
            if(validStation) break;
        }
        if(!validStation) throw new Exception("Train is not passing from this station");
        int totalPeople=0;
        for(Ticket ticket:train.getBookedTickets()){
          if(ticket.getFromStation().equals(station))
              totalPeople+=ticket.getPassengersList().size();
        }

        return totalPeople;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0
        Optional<Train> optionalTrain=trainRepository.findById(trainId);
        Train train=optionalTrain.get();
        int age=0;
        for(Ticket ticket:train.getBookedTickets()){
            for(Passenger passenger: ticket.getPassengersList()){
               age=Math.max(age,passenger.getAge());
            }
        }

        return age;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        List<Train> trains=trainRepository.findAll();
        HashMap<Integer,LocalTime> trainIdAndPassingTime=new HashMap<>();
        for(Train train:trains){
            String[] route=train.getRoute().split(",");
            boolean passing=false;
            for(int i=0;i< route.length;i++){
                if(route[i].equals(station.toString())){
                    passing=true;
                    trainIdAndPassingTime.put(train.getTrainId(),train.getDepartureTime().plus(i, ChronoUnit.HOURS));
                }
                if(passing) break;
            }
        }
        List<Integer> trainIds=new ArrayList<>();
        for(Integer id:trainIdAndPassingTime.keySet()){
            int comparisonResultStart= startTime.compareTo(trainIdAndPassingTime.get(id));
            int comparisonResultEnd=trainIdAndPassingTime.get(id).compareTo(endTime);

            if(comparisonResultStart<=0 && comparisonResultEnd<=0){
                trainIds.add(id);
            }
        }

        return trainIds;
    }

}
