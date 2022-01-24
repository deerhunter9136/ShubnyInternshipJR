package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayersCrudRepository;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Service
public class PlayersDataService {
    @Autowired
    private PlayersCrudRepository playersCrudRepository;

    @Transactional
    public List<Player> getAll(Map<String, String> params) {
        Integer pageNumber, pageSize;
        PlayerOrder order;
        if (params.get("pageNumber") == null)
            pageNumber = 0;
        else pageNumber = Integer.parseInt(params.get("pageNumber"));
        if (params.get("pageSize") == null)
            pageSize = 3;
        else pageSize = Integer.parseInt(params.get("pageSize"));
        if (params.get("order") == null)
            order = PlayerOrder.ID;
        else order = PlayerOrder.valueOf(params.get("order"));

        List<Player> filteredList = createFilteredList(params);
        List<Player> resultList = new ArrayList<>();

        try {
            for (int i = pageNumber * pageSize; i < (pageNumber + 1) * pageSize; i++) {
                resultList.add(filteredList.get(i));
            }
        } catch (IndexOutOfBoundsException ignored) {}
        resultList.sort(new Comparator<Player>() {
            @Override
            public int compare(Player o1, Player o2) {
                switch(order) {
                    case NAME: return o1.getName().compareTo(o2.getName());
                    case EXPERIENCE: return o1.getExperience().compareTo(o2.getExperience());
                    case BIRTHDAY: return o1.getBirthday().compareTo(o2.getBirthday());
                    case LEVEL: return o1.getLevel().compareTo(o2.getLevel());
                    default: return o1.getId().compareTo(o2.getId());
                }
            }
        });
        return resultList;
    }

    @Transactional
    public Integer getCount(Map<String, String> params) {
        List<Player> filteredList = createFilteredList(params);
        return filteredList.size();
    }

    private List<Player> createFilteredList(Map<String, String> params) {
        String inputName = params.get("name");
        String inputTitle = params.get("title");
        Date inputBirthdayAfter = null;
        Date inputBirthdayBefore = null;
        Integer inputExperienceMin = null;
        Integer inputExperienceMax = null;
        Integer inputLevelMin = null;
        Integer inputLevelMax = null;
        Race inputRace = null;
        Profession inputProfession = null;
        Boolean inlineRadioOptions = null;
        if (params.get("after") != null)
            inputBirthdayAfter = new Date(Long.parseLong(params.get("after")));
        if (params.get("before") != null)
            inputBirthdayBefore = new Date(Long.parseLong(params.get("before")));
        if (params.get("minExperience") != null)
            inputExperienceMin = Integer.parseInt(params.get("minExperience"));
        if (params.get("maxExperience") != null)
            inputExperienceMax = Integer.parseInt(params.get("maxExperience"));
        if (params.get("minLevel") != null)
            inputLevelMin = Integer.parseInt(params.get("minLevel"));
        if (params.get("maxLevel") != null)
            inputLevelMax = Integer.parseInt(params.get("maxLevel"));
        if (params.get("race") != null)
            inputRace = Race.valueOf(params.get("race"));
        if (params.get("profession") != null)
            inputProfession = Profession.valueOf(params.get("profession"));
        if (params.get("banned") != null)
            inlineRadioOptions = Boolean.valueOf(params.get("banned"));

        List<Player> fullList = new ArrayList<>();
        List<Player> filteredList = new ArrayList<>();
        Iterable<Player> iterable = playersCrudRepository.findAll();
        iterable.forEach(fullList::add);
        for (int i = 0; i < fullList.size(); i++) {
            if ((inputName == null || fullList.get(i).getName().contains(inputName)) &&
                    (inputTitle == null || fullList.get(i).getTitle().contains(inputTitle)) &&
                    (inputBirthdayAfter == null || fullList.get(i).getBirthday().after(inputBirthdayAfter)) &&
                    (inputBirthdayBefore == null || fullList.get(i).getBirthday().before(inputBirthdayBefore)) &&
                    (inputExperienceMin == null || fullList.get(i).getExperience() > inputExperienceMin) &&
                    (inputExperienceMax == null || fullList.get(i).getExperience() < inputExperienceMax) &&
                    (inputLevelMin == null || fullList.get(i).getLevel() > inputLevelMin) &&
                    (inputLevelMax == null || fullList.get(i).getLevel() < inputLevelMax) &&
                    (inputRace == null || fullList.get(i).getRace().equals(inputRace)) &&
                    (inputProfession == null || fullList.get(i).getProfession().equals(inputProfession)) &&
                    (inlineRadioOptions == null || fullList.get(i).getBanned().equals(inlineRadioOptions))
            )
                filteredList.add(fullList.get(i));
        }
        return filteredList;
    }

    @Transactional
    public ResponseEntity<Player> getPlayerById(Long id) {
        if (id < 1)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Player());
        Optional<Player> optionalPlayer = playersCrudRepository.findById(id);
        if (optionalPlayer.isPresent())
            return ResponseEntity.ok(optionalPlayer.get());
        else return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Player());
    }

    @Transactional
    public Player createPlayer(Player player) {
        if (player == null) return null;
        player.calculateLevelExperience();

        if(!player.getName().isEmpty() &&
                player.getBirthday().getTime() > 0 &&
                player.getTitle().length() <= 30 &&
                player.getExperience() <= 10000000) {

            playersCrudRepository.save(player);
            Optional<Player> savedPlayer = playersCrudRepository.findById(player.getId());
            return savedPlayer.orElseGet(Player::new);
        }
        else return null;
    }

    @Transactional
    public ResponseEntity<Player> update(Long id, Player player) {
        if (id < 1)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(player);
        if (!playersCrudRepository.existsById(id))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(player);
        if (isEmptyPlayer(player)) {
            Optional<Player> saved = playersCrudRepository.findById(id);
            if (saved.isPresent())
                return ResponseEntity.ok(saved.get());
        }

        if (player.getExperience() != null && (player.getExperience() < 0 || player.getExperience() > 10000000))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(player);
        if (player.getBirthday() != null && (player.getBirthday().getTime() < 0 || player.getBirthday().getTime() > new Date().getTime()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(player);
        Optional<Player> saved = playersCrudRepository.findById(id);
        if (saved.isPresent()) {
            Player savedPlayer = saved.get();
            if (player.getName() != null)
                savedPlayer.setName(player.getName());
            if (player.getTitle() != null)
                savedPlayer.setTitle(player.getTitle());
            if (player.getRace() != null)
                savedPlayer.setRace(player.getRace());
            if (player.getProfession() != null)
                savedPlayer.setProfession(player.getProfession());
            if (player.getBirthday() != null)
                savedPlayer.setBirthday(player.getBirthday());
            if (player.getExperience() != null)
                savedPlayer.setExperience(player.getExperience());
            if (player.getBanned() != null)
                savedPlayer.setBanned(player.getBanned());
            savedPlayer.calculateLevelExperience();
            return ResponseEntity.ok(savedPlayer);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(player);
    }

    private boolean isEmptyPlayer(Player player) {
        return player.getName() == null &&
                player.getTitle() == null &&
                player.getRace() == null &&
                player.getProfession() == null &&
                player.getExperience() == null &&
                player.getLevel() == null &&
                player.getUntilNextLevel() == null &&
                player.getBirthday() == null &&
                player.getBanned() == null;
    }

    @Transactional
    public ResponseEntity<Long> deleteById(Long id) {
        if (id < 1)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(id);
        if (playersCrudRepository.existsById(id)) {
            playersCrudRepository.deleteById(id);
            return ResponseEntity.ok(id);
        }
        else return ResponseEntity.status(HttpStatus.NOT_FOUND).body(id);
    }

}