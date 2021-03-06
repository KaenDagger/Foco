package com.pervysage.thelimitbreaker.foco.database

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.pervysage.thelimitbreaker.foco.database.entities.ContactInfo


@Dao
interface ContactsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg contactInfo:ContactInfo)

    @Delete
    fun delete(vararg contactInfo:ContactInfo)

    @Query("SELECT * FROM contact_info ORDER BY name")
    fun getAllContacts(): List<ContactInfo>

    @Query("SELECT * FROM contact_info ORDER BY name")
    fun getAllContactsLive(): LiveData<List<ContactInfo>>

    @Update
    fun update(vararg contactInfo: ContactInfo)

    @Query("SELECT * FROM contact_info WHERE number LIKE :number")
    fun getInfoFromNumber(number:String):ContactInfo?

}