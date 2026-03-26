package com.intheeast.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Table(name="TEAMS")
@Entity
public class Team {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="TEAM_ID")
	private Long id;
	
	private String name;
	
	@OneToMany(mappedBy="team")
	private List<Member> memberList = new ArrayList<>();
	
	public void addMember(Member member) {
		member.setTeam(this);
		memberList.add(member);
	}
	
	public void removeMember(Member member) {
		memberList.remove(member);
		member.setTeam(null);
	}

	@Override
	public String toString() {
		return "Team [id=" + id + 
				", name=" + name + 
				", member numbers=" + memberList.size() +
				"]";
	}
	
	
	

}
