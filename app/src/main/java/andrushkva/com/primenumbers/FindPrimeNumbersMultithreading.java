package andrushkva.com.primenumbers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FindPrimeNumbersMultithreading extends Thread {

    private int start, finish;

    private List<Integer> primeList;

    public FindPrimeNumbersMultithreading(int start, int finish)
    {
        this.start = start;
        this.finish = finish;
        primeList = new ArrayList<>();
    }

    public List<Integer> getPrimeList()
    {
        return primeList;
    }

    public void run()
    {
        for (int i = Math.max(2, start); i < finish; i++){
            boolean isPrime = true;
            for (int j = 2; j <= i; j++){
                if(i % j == 0 && i != j){
                    isPrime = false;
                    break;
                }
            }
            if(isPrime) {
                primeList.add(i);
            }
        }
    }

    public static ArrayList<Integer> parallelFind(int interval, int threads)
    {
        FindPrimeNumbersMultithreading[] primeThreads = new FindPrimeNumbersMultithreading[threads];

        for (int i = 0; i < threads; i++) {
            int step = interval / threads;
            int start = i * step;
            int finish;
            if(i != threads - 1) {
                finish = (i + 1) * step;
            } else {
                finish = interval + 1;
            }
            primeThreads[i] = new FindPrimeNumbersMultithreading(start, finish);
            primeThreads[i].start();
        }

        try {
            for (FindPrimeNumbersMultithreading prime : primeThreads) {
                prime.join();
            }
        } catch (InterruptedException e) { }

        ArrayList<Integer> allPrimes = new ArrayList<>();
        for (FindPrimeNumbersMultithreading prime : primeThreads) {
            allPrimes.addAll(prime.getPrimeList());
        }
        Collections.sort(allPrimes);

        return allPrimes;
    }

}