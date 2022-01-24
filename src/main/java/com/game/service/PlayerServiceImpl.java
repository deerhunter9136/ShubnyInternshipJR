package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.models.Player;
import com.game.repository.PlayerDao;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class PlayerServiceImpl implements PlayerService {
    //private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");
    private static final long MIN_TIME = 946674000000L; //SDF.parse("2000-01-01").getTime();
    private static final long MAX_TIME = 32535205200000L - 1;//SDF.parse("3001-01-01").getTime()-1;

    @Override
    public Player create(String name, String title, Race race, Profession profession, Long birthday, Boolean banned, Integer experience) {
        boolean nameValid = name != null && !name.isEmpty() && name.length() <= 12;
        if (!nameValid) return null;
        boolean titleValid = title != null && title.length() <= 30;
        if (!titleValid) return null;
        boolean experienceValid = experience != null && experience >= 0 && experience <= 10_000_000;
        if (!experienceValid) return null;
        boolean birthdayValid = birthday != null && birthday >= MIN_TIME && birthday <= MAX_TIME;
        if (!birthdayValid) return null;
        Boolean isBanned = banned != null ? banned : false;
        int level = (int) ((Math.sqrt(2500 + 200 * experience) - 50) / 100);
        int untilNextLevel = 50 * (level + 1) * (level + 2) - experience;
        long id = PlayerDao.getID.incrementAndGet();
        Player player = new Player(id, name, title, race, profession, birthday, isBanned, experience, level, untilNextLevel);
        getPlayers().add(player);
        if (race == null || profession == null) return null;
        PlayerDao.addEntry(id, name, title, race, profession, birthday, isBanned, experience, level, untilNextLevel);
        return player;

    }

    @Override
    public Player update(long id, String name, String title, Race race, Profession profession, Long birthday, Boolean banned, Integer experience) {
        //if (p==null) return null;
        Player player = getPlayers().stream().filter(player1 -> player1.id == id).findFirst().orElse(null);
        if (player == null) return null;
        if (name != null) {
            boolean nameValid = !name.isEmpty() && name.length() <= 12;
            if (!nameValid) return null;
            player.name = name;
        }
        if (title != null) {
            boolean titleValid = title.length() <= 30;
            if (!titleValid) return null;
            player.title = title;
        }
        if (race != null) player.race = race;
        if (profession != null) player.profession = profession;

        if (birthday != null) {
            boolean birthdayValid = birthday >= MIN_TIME && birthday <= MAX_TIME;
            if (!birthdayValid) return null;
            player.birthday = birthday;
        }
        if (banned != null) player.banned = banned;
        if (experience != null) {
            boolean experienceValid = experience >= 0 && experience <= 10_000_000;
            if (!experienceValid) return null;
            player.experience = experience;
            player.level = (int) ((Math.sqrt(2500 + 200 * experience) - 50) / 100);
            player.untilNextLevel = 50 * (player.level + 1) * (player.level + 2) - experience;
        }
        PlayerDao.update(id, player);
        return player;
    }

    @Override
    public int delete(int id) {
        if (id == 0) return 0;
        Player player = getPlayers().stream().filter(player1 -> player1.id == id).findFirst().orElse(null);
        List<Player> playerList = getPlayers();
        boolean isRemoved = playerList.remove(player);
        if (isRemoved) {
            PlayerDao.deleteFromDB(id);
        }

        return isRemoved ? 1 : -1;
    }

    @Override
    public Player read(long id) {
        return getPlayers().stream().filter(s -> s.id == id).findFirst().orElse(null);
    }

    @Override
    public List<Player> readAll(
            String name, String title, Race race, Profession profession, Long after, Long before, Boolean banned, Integer minExperience,
            Integer maxExperience, Integer minLevel, Integer maxLevel, PlayerOrder order, Integer pageNumber, Integer pageSize) {
        List<Player> players = getFilteredPlayers(name, title, race, profession, after, before, banned, minExperience, maxExperience, minLevel, maxLevel);
        Comparator<Player> comparator = (o1, o2) -> o1.id.compareTo(o2.id);
        if (order != null) {
            switch (order) {
                case NAME:
                    comparator = (o1, o2) -> o1.name.compareTo(o2.name);
                    break;
                case EXPERIENCE:
                    comparator = (o1, o2) -> o1.experience.compareTo(o2.experience);
                    break;
                case BIRTHDAY:
                    comparator = (o1, o2) -> o1.birthday.compareTo(o2.birthday);
                    break;
                case LEVEL:
                    comparator = (o1, o2) -> o1.level.compareTo(o2.level);
                    break;
            }
        }
        players.sort(comparator);
        int pN = pageNumber == null ? 0 : pageNumber;
        int pS = pageSize == null ? 3 : pageSize;
        int start = Math.min(pN * pS, players.size());
        int end = Math.min((pN * pS + pS), players.size());
        return players.subList(start, end);
    }

    private List<Player> getFilteredPlayers(String name, String title, Race race, Profession profession, Long after, Long before, Boolean banned, Integer minExperience, Integer maxExperience, Integer minLevel, Integer maxLevel) {
        List<Player> players = getPlayers();
        if (name != null) {
            players = players.stream().filter(player -> player.name.contains(name)).collect(Collectors.toList());
        }
        if (title != null) {
            players = players.stream().filter(player -> player.title.contains(title)).collect(Collectors.toList());
        }
        if (race != null) {
            players = players.stream().filter(player -> player.race == race).collect(Collectors.toList());
        }
        if (profession != null) {
            players = players.stream().filter(player -> player.profession == profession).collect(Collectors.toList());
        }
        if (after != null) {
            players = players.stream().filter(player -> player.birthday > after).collect(Collectors.toList());
        }

        if (before != null) {
            players = players.stream().filter(player -> player.birthday < before).collect(Collectors.toList());
        }

        if (banned != null) {
            players = players.stream().filter(player -> player.banned == banned).collect(Collectors.toList());
        }

        if (minExperience != null) {
            players = players.stream().filter(player -> player.experience >= minExperience).collect(Collectors.toList());
        }

        if (maxExperience != null) {
            players = players.stream().filter(player -> player.experience <= maxExperience).collect(Collectors.toList());
        }

        if (minLevel != null) {
            players = players.stream().filter(player -> player.level >= minLevel).collect(Collectors.toList());
        }

        if (maxLevel != null) {
            players = players.stream().filter(player -> player.level <= maxLevel).collect(Collectors.toList());
        }
        return players;
    }

    @Override
    public Long count(String name, String title, Race race, Profession profession, Long after, Long before, Boolean banned, Integer minExperience, Integer maxExperience, Integer minLevel, Integer maxLevel) {
        return (long) getFilteredPlayers(name, title, race, profession, after, before, banned, minExperience, maxExperience, minLevel, maxLevel).size();
    }

    private List<Player> getPlayers() {
        return PlayerDao.getBase();
    }
}
