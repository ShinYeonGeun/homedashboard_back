package com.lotus.homeDashboard.cmn.mnm.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lotus.homeDashboard.cmn.mnm.entity.MenuEntity;

public interface MenuRepository extends JpaRepository<MenuEntity, Integer> {

}