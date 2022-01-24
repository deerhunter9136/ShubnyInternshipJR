package com.game.repository;

import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.models.Player;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class PlayerDao {
    private static final List<Player> players = new ArrayList<>();
    public static final String DB_URL = "jdbc:mysql://localhost:3306/rpg?serverTimezone=Europe/Moscow&useSSL=false";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "root";
    private static long initID = 0;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    static {


        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);) {
            Statement statement = conn.createStatement();
            String SQL = "SELECT * FROM player";
            ResultSet resultSet = statement.executeQuery(SQL);
            {
                while (resultSet.next()) {
                    Long id = resultSet.getLong("id");
                    initID = Math.max(initID, id);
                    String name = resultSet.getString("name");
                    String title = resultSet.getString("title");
                    Race race = Race.valueOf(resultSet.getString("race"));
                    Profession profession = Profession.valueOf(resultSet.getString("profession"));
                    Long birthday = SDF.parse(resultSet.getString("birthday")).getTime();
                    Boolean banned = resultSet.getBoolean("banned");
                    Integer experience = resultSet.getInt("experience");
                    Integer level = resultSet.getInt("level");
                    Integer untilNextLevel = resultSet.getInt("untilNextLevel");
                    Player player = new Player(id, name, title, race, profession, birthday, banned, experience, level, untilNextLevel);
                    players.add(player);
                }
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }
    }

    public static final AtomicLong getID = new AtomicLong(initID);

    public static List<Player> getBase() {
        return players;
    }

    public static void deleteFromDB(int id) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);) {
            PreparedStatement preparedStatement = conn.prepareStatement("DELETE FROM player WHERE id=?");

            preparedStatement.setInt(1, id);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addEntry(Long id, String name, String title, Race race, Profession profession, Long birthday, Boolean isBanned,
                                Integer experience, Integer level, Integer untilNextLevel) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);) {
            PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO player VALUES(?,?,?,?,?,?,?,?,?,?)");
            preparedStatement.setLong(1, id);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, title);
            preparedStatement.setString(4, race.toString());
            preparedStatement.setString(5, profession.toString());
            preparedStatement.setString(6, SDF.format(new Date(birthday)));
            preparedStatement.setBoolean(7, isBanned);
            preparedStatement.setInt(8, experience);
            preparedStatement.setInt(9, level);
            preparedStatement.setInt(10, untilNextLevel);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void update(long id, Player player) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);) {
            PreparedStatement preparedStatement = conn.prepareStatement("UPDATE player SET name=?, title=?, race=?, profession=?, birthday=?, banned=?, experience=?, level=?, untilNextLevel=? WHERE id=?");
            preparedStatement.setString(1, player.name);
            preparedStatement.setString(2, player.title);
            preparedStatement.setString(3, player.race.toString());
            preparedStatement.setString(4, player.profession.toString());
            preparedStatement.setString(5, SDF.format(new Date(player.birthday)));
            preparedStatement.setBoolean(6, player.banned);
            preparedStatement.setInt(7, player.experience);
            preparedStatement.setInt(8, player.level);
            preparedStatement.setInt(9, player.untilNextLevel);
            preparedStatement.setLong(10, id);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
