package andrushkva.com.primenumbers.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

/**
 * Created by Vyacheslav on 23.07.2018.
 */

@Dao
public interface PrimeNumbersDao {

    @Query("SELECT listPrimeNumbers FROM primenumbers WHERE interval = :interval")
    String getByInterval(int interval);

    @Insert
    void insert(PrimeNumbers primeNumbers);
}
