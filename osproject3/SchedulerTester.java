import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class SchedulerTester {

    public int clock = 0;
    public static void main(String[] args) {

        String jobs = args[0];
        switch(args[1]){
            case "RR":
                RoundRobinScheduler rr = new RoundRobinScheduler(jobs);
                break;
            case "SRT":
                ShortestRemainingTimeScheduler srt = new ShortestRemainingTimeScheduler(jobs);
                break;
            case "FB":
                FeedbackScheduler fb = new FeedbackScheduler(jobs);
                break;
            case "ALL":
                RoundRobinScheduler rra = new RoundRobinScheduler(jobs);
                ShortestRemainingTimeScheduler srta = new ShortestRemainingTimeScheduler(jobs);
                FeedbackScheduler fba = new FeedbackScheduler(jobs);
                break;
            default:
        };



    }
}

class Job implements Comparable<Job>{
    String name;
    int arrival;
    int duration;

    Job next;

    Job(String name, int arrival, int duration){
        this.name = name;
        this.arrival = arrival;
        this.duration = duration;
    }

    @Override
    public int compareTo(Job anotherJob) {
        if(this.duration > anotherJob.duration) return 1;
        else if(this.duration < anotherJob.duration) return -1;
        else{
            if((int)this.name.charAt(0) < (int)anotherJob.name.charAt(0)) return -1;
            else return 1;
        }
    }

    public String toString(){
        return name + " " + arrival + " " + duration;
    }
};

abstract class Scheduler {

    public int clock = 0;
    File jobFile;
    Scanner reader;
    Job head;

    Scheduler(String filename){

        try {
            this.jobFile = new File(filename);
            this.reader = new Scanner(this.jobFile);
            String job = reader.nextLine();
            String[] components = job.split("\t", 3);
            this.head = new Job(components[0], Integer.parseInt(components[1]), Integer.parseInt(components[2]));
            System.out.print(components[0] + " ");
            Job traversal = head;
            while(reader.hasNextLine()){
                job = reader.nextLine();
                components = job.split("\t", 3);
                System.out.print(components[0] + " ");
                traversal.next = new Job(components[0], Integer.parseInt(components[1]), Integer.parseInt(components[2]));
                traversal = traversal.next;
            }
            System.out.println();
            this.schedule();
        } catch(FileNotFoundException e){
            e.printStackTrace();
        }

    }

    void printX(int which){
        for (int i = 0; i < which; i++) System.out.print("  ");
        System.out.println("X");
    }

    int convertToInt(char character){
        return (int) character - 65;
    }

    abstract void schedule();
}

class RoundRobinScheduler extends Scheduler {

    RoundRobinScheduler(String filename){
        super(filename);
    }
    void schedule(){
        Queue<Job> jobs = new LinkedList<Job>();
        Job traversal = this.head;
        Job previous = null;

        while(!jobs.isEmpty() || traversal != null || previous != null) {
            if (traversal != null && traversal.arrival <= this.clock) {
                jobs.add(traversal);
                traversal = traversal.next;
            }

            if (previous != null) jobs.add(previous);
            if (jobs.isEmpty()){
                System.out.println();
                this.clock += 1;
                continue;
            }

            previous = jobs.poll();
            printX(convertToInt(previous.name.charAt(0)));
            previous.duration -= 1;
            if (0 >= previous.duration) previous = null;
            this.clock += 1;
        }

        System.out.println();

    }
}

class ShortestRemainingTimeScheduler extends Scheduler {

    ShortestRemainingTimeScheduler(String filename){
        super(filename);
    }
    void schedule(){
        Job traversal = this.head;
        PriorityQueue<Job> jobs = new PriorityQueue<>();

        while(!jobs.isEmpty() || traversal != null) {
            if (traversal != null && traversal.arrival <= this.clock) {
                jobs.add(traversal);
                traversal = traversal.next;
            }

            if (jobs.isEmpty()){
                System.out.println();
                this.clock += 1;
                continue;
            }

            Job current = jobs.poll();
            printX(convertToInt(current.name.charAt(0)));
            current.duration -= 1;
            if (0 < current.duration) jobs.add(current);
            this.clock += 1;
        }

        System.out.println();
    }

    String findMin(ArrayList<String> arr){
        String minimum = "";
        int min = Integer.MAX_VALUE;
        for(int i = 0; i < arr.size(); i++){
            String[] components = arr.get(i).split(" ", 3);
            if(Integer.parseInt(components[2]) < min){
                min = Integer.parseInt(components[2]);
                minimum = arr.get(i);
            }
        }
        return minimum;
    }
}

class FeedbackScheduler extends Scheduler {

    FeedbackScheduler(String filename){
        super(filename);
    }
    void schedule(){
        ArrayList<Queue<Job>> queues = new ArrayList<>();
        for(int i = 0; i < 3; i++){
            queues.add(new LinkedList<Job>());
        }
        Job traversal = this.head;

        while(!queues.get(0).isEmpty() || !queues.get(1).isEmpty() || !queues.get(2).isEmpty() || traversal != null){
            if (traversal != null && traversal.arrival <= this.clock) {
                queues.get(0).add(traversal);
                traversal = traversal.next;
            }

            if (queues.get(0).isEmpty() && queues.get(1).isEmpty() && queues.get(2).isEmpty()){
                System.out.println();
                this.clock += 1;
                continue;
            }

            for(int i = 0; i < 3; i++){
                if(queues.get(i).isEmpty()) continue;
                Job current = queues.get(i).poll();
                printX(convertToInt(current.name.charAt(0)));
                current.duration -= 1;
                if (0 < current.duration) {
                    if(queues.get(0).isEmpty() && queues.get(1).isEmpty() && queues.get(2).isEmpty()
                            && traversal != null && traversal.arrival == this.clock + 1)
                        queues.get(i).add(current);
                    else if(i < 2) queues.get(i+1).add(current);
                    else queues.get(i).add(current);
                }
                this.clock += 1;
                break;
            }
        }

        System.out.println();
    }
}