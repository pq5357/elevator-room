package com.willowtreeapps.android.elevatorroom.persistence;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by willowtree on 4/21/17.
 */
@Dao
@TypeConverters(Person.StateConverter.class)
public interface PersonDao {

    @Query("SELECT * FROM " + Person.TABLE)
    List<Person> loadAllPeople();

    String SELECT_ACTIVE_PEOPLE = "SELECT * FROM " + Person.TABLE + " WHERE gone == 0";

    @Query(SELECT_ACTIVE_PEOPLE)
    LiveData<List<Person>> activePeople();

    @Query(SELECT_ACTIVE_PEOPLE)
    List<Person> loadActivePeople();

    @Insert
    void newPerson(Person person);

    @Update
    void updatePerson(Person person);

    @Query("DELETE FROM " + Person.TABLE)
    void deleteAllPeople();

}
