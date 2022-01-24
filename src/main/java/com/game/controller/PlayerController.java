package com.game.controller;

import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.models.Player;
import com.game.repository.PlayerDao;
import com.game.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest")
public class PlayerController {
    private final PlayerService playerService;

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("/players")
    public ResponseEntity<List<Player>> read(@RequestParam(required = false) String name, String title, Race race, Profession profession, Long after, Long before, Boolean banned, Integer minExperience,
                                             Integer maxExperience, Integer minLevel, Integer maxLevel, PlayerOrder order, Integer pageNumber, Integer pageSize) {

        final List<Player> players = playerService.readAll(name, title, race, profession, after, before, banned, minExperience, maxExperience, minLevel, maxLevel, order, pageNumber, pageSize);

        return players != null
                ? new ResponseEntity<>(players, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/players/count")
    public ResponseEntity<Long> read(@RequestParam(required = false) String name, String title, Race race, Profession profession, Long after, Long before, Boolean banned, Integer minExperience,
                                     Integer maxExperience, Integer minLevel, Integer maxLevel) {

        final Long count = playerService.count(name, title, race, profession, after, before, banned, minExperience, maxExperience, minLevel, maxLevel);

        return count != null
                ? new ResponseEntity<>(count, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/players/{id}")
    public ResponseEntity<Player> read(@PathVariable(name = "id") long id) {
        if (id == 0) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        final Player player = playerService.read(id);
        return player != null
                ? new ResponseEntity<>(player, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping(value = "/players/{id}")
    public ResponseEntity<?> delete(@PathVariable(name = "id") int id) {
        final int responce = playerService.delete(id);
        switch (responce) {
            case 0:
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            case -1:
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            default:
                return new ResponseEntity<>(HttpStatus.OK);
        }

    }

    @PostMapping(value = "/players/{id}")
    public ResponseEntity<?> update(@PathVariable(name = "id") int id, @RequestBody Player player) {
        if (id == 0) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        if (PlayerDao.getBase().stream().noneMatch(player1 -> player1.id == id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Player updated = playerService.update(id, player.name, player.title, player.race, player.profession,
                player.birthday, player.banned, player.experience);

        return updated != null
                ? new ResponseEntity<>(updated, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping(value = "/players")
    public ResponseEntity<Player> create(@RequestBody Player player) {

        Player created = playerService.create(player.name, player.title, player.race, player.profession, player.birthday, player.banned, player.experience);
        return created != null
                ? new ResponseEntity<>(created, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

}
