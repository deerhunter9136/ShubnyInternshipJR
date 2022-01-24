package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.models.Player;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface PlayerService {


    Player create(String name, String tile, Race race, Profession profession, Long birthday, Boolean banned, Integer experience);

    Player read(long id);

    List<Player> readAll(String name, String title, Race race, Profession profession, Long after, Long before, Boolean banned, Integer minExperience,
                         Integer maxExperience, Integer minLevel, Integer maxLevel, PlayerOrder order, Integer pageNumber, Integer pageSize);

    Long count(String name, String title, Race race, Profession profession, Long after, Long before, Boolean banned, Integer minExperience,
               Integer maxExperience, Integer minLevel, Integer maxLevel);

    Player update(long id, String name, String title, Race race, Profession profession, Long birthday,  Boolean banned, Integer experience);


    int delete(int id);


}

