package com.example.demo.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entities.User;

public interface Userdao extends JpaRepository<User, Integer> {
//	@Query("select u from User u where u.email = :email")
//	public User getUserByUserName(@Param("email") String email);

	@Query("select u from User u where u.email = :email")
	public Optional<User> findByUserName(@Param("email") String email);

	@Query("select u from User u where u.email = :email")
	public User getUserByUserName(@Param("email") String email);

//	@Query("select u from UserDetail u where u.id = :id")
//	public User getUserById(@Param("id") String id);

	@Query("update User u set u.failedAttempt=?1 where u.email=?2 ")
	@Modifying
	public void updateFailedAttempt(int failedAttempt, String email);

	@Query("update User u set u.alert_message_sent=?1 where u.email=?2 ")
	@Modifying
	public void updateAlert(int failedAttempt, String email);

	@Query("select u from User u where u.email = :email or u.phone = :phone")
	public Optional<User> findByUserNameAndPhone(@Param("email") String email, @Param("phone") String phone);
	
	@Query("select u from User u where u.email = :email or u.phone = :phone")
	public Optional<User> findByUserNameAndPhone();
	
//	@Query("select u from User u where u.account_number = :account_number or u.phone = :phone")
//	public Optional<User> findByPhoneAndAccountNumber(long account_number,String phone);

	@Query(value = "update employee u set u.lock_date_and_time=null,expire_lock_date_and_time=null,u.account_non_locked='1',u.failed_attempts=0 where u.lock_date_and_time <= (NOW() - INTERVAL 1 DAY)", nativeQuery = true)
	@Modifying
	public void getAllAccount_LockedAndUnlokedDetails(Date lock_date_and_time);

	@Query(value = "update employee u set u.enabled ='0',u.status='INACTIVE' where u.speration_date <= (NOW() - INTERVAL 2 DAY)", nativeQuery = true)
	@Modifying
	public void getEnableFalse(Date lastWorkingDay);

	@Query("select u.lockDateAndTime from User u")
	public List<Date> getAllLock_Date_And_Time_Records();

	@Query("select u.id from User u")
	public List<Integer> getLastWorkingDay_Records();

//	@Query(value="update database_ems.employee u set u.failed_attempts=0 ,u.last_failed_attempt_job=CURRENT_TIMESTAMP where u.id=?1",nativeQuery=true)
//	@Modifying
//	public void getAllFailedAttemptUserRecords(int id);

	@Query("select u.experience from User u")
	public List<Integer> getAllExp();

	@Query(value = "update database_ems.employee u set u.experience  where u.system_date_and_time <= (NOW() - INTERVAL 365 DAY)", nativeQuery = true)
	@Modifying
	public void skills(int experience);

	@Query(value = "update employee u set u.enabled='0',u.status='INACTIVE' where u.new_user_active_or_inactive='0' and u.id=? and u.system_date_and_time <= (NOW() - INTERVAL 30 DAY)", nativeQuery = true)
	@Modifying
	public void disableuserbyid(int id);

	@Query(value = "update employee u set u.failed_attempts='0' where u.failed_attempts < 3 ORDER BY u.system_date_and_time DESC LIMIT 1000000", nativeQuery = true)
	@Modifying
	public void reset_failed_attempt_job();

	@Query(value = "update employee u set u.user_status='0' where u.email= ?1", nativeQuery = true)
	@Modifying
	public void update_user_status(String username);

	@Query(value = "select u.id from employee u order by u.id desc limit 1", nativeQuery = true)
	public int getLastId();
	
	@Query(value = "update employee u set u.enabled='1' where u.company_id= ?1 and status='ACTIVE'", nativeQuery = true)
	@Modifying
	public void update_user_enabled_after_success_payment(String company_id);
	
	@Query(value = "update employee u set u.enabled='0' where u.company_id= ?1 and u.status='ACTIVE' and u.role in ('ROLE_MANAGER','ROLE_HR','ROLE_USER','ROLE_IT')", nativeQuery = true)
	@Modifying
	public void disbaled_expired_plan_users(String company_id);

//	public Optional<User> findByEmailandPhone(String email,String phone);
//	@Query("Update User u set u.AccountNonLocked = 1 ,u.failedAttempt = 0 ,u.lockDateAndTime=null")
//	@Modifying
//	public void getAllAccount_LockedAndFailedAttempt();

}
