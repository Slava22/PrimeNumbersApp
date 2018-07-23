package andrushkva.com.primenumbers.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by Vyacheslav on 23.07.2018.
 */

@Entity
public class PrimeNumbers {

    @PrimaryKey
    public int interval;

    public String listPrimeNumbers;

    public PrimeNumbers(int interval, String listPrimeNumbers) {
        this.interval = interval;
        this.listPrimeNumbers = listPrimeNumbers;
    }
}
