package com.example.demo.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import com.example.demo.entities.UserDetail;

public interface UserDetailDao extends JpaRepository<UserDetail, Integer> {
	@Query("select u from UserDetail u where u.team = :team and length(u.team) > 1 and u.enabled = 1")
	public List<UserDetail> getUserByTeam(String team);

	@Query(value = "update employeedetail u set u.user_status='0' where u.email= ?1", nativeQuery = true)
	@Modifying
	public void update_user_status(String username);
	
}
