package andrushkva.com.primenumbers.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by Vyacheslav on 23.07.2018.
 */

@Database(entities = {PrimeNumbers.class}, version = 1)
public abstract class PrimeNumbersDatabase extends RoomDatabase{
    public abstract PrimeNumbersDao primeNumbersDao();
}
