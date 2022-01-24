package com.game.controller;

import com.game.entity.Player;
import com.game.service.PlayersDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rest")
public class PlayerController {
    @Autowired
    private PlayersDataService service;

    @GetMapping("/players")
    public List<Player> getAll(@RequestParam Map<String, String> params) {


        return service.getAll(params);
    }

    @GetMapping("/players/count")
    public Integer getCount(@RequestParam Map<String, String > params) {
        return service.getCount(params);
    }

    @GetMapping("/players/{id}")
    public ResponseEntity<Player> getPlayer(@PathVariable("id") Long id) {
        return service.getPlayerById(id);
    }

    @PostMapping("/players")
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {
        if (player.getExperience() == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(player);
        Player created = service.createPlayer(player);
        if (created == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(created);
        else return ResponseEntity.ok(created);
    }

    @PostMapping("/players/{id}")
    public ResponseEntity<Player> update(@PathVariable("id") Long id, @RequestBody Player player) {
        return service.update(id, player);
    }

    @DeleteMapping("/players/{id}")
    public ResponseEntity<Long> delete(@PathVariable("id") Long id) {
        return service.deleteById(id);
    }
}
