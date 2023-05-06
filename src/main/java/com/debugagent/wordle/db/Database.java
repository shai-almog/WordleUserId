package com.debugagent.wordle.db;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Transactional
public class Database {
    private final JdbcTemplate jdbcTemplate;

    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS activity (
                id IDENTITY PRIMARY KEY, 
                userId VARCHAR(255), 
                guess VARCHAR(255), 
                word VARCHAR(255), 
                time_stamp TIMESTAMP,
                attempt INTEGER)
            """;

    private static final String INSERT_SQL =
            "INSERT INTO activity (userId, guess, word, time_stamp, attempt) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_ALL_SQL = "SELECT * FROM activity where userId = ? order by attempt";

    private static final String COUNT_SQL = "select count(userId) from activity where userId = ? and word = ?";

    @PostConstruct
    public void createTable() {
        jdbcTemplate.execute(CREATE_TABLE_SQL);
    }

    public void insert(DBEntry entry) {
        int attempt = entry.attempt();
        if(entry.user() != null && attempt == 0) {
            attempt = jdbcTemplate.queryForObject(COUNT_SQL,
                    Integer.class,
                    entry.user(),
                    entry.word());
        }
        jdbcTemplate.update(INSERT_SQL,
                entry.user(), entry.guess(), entry.word(), Instant.now(),
                attempt);
    }

    public List<DBEntry> selectAll(String userId) {
        return jdbcTemplate.query(SELECT_ALL_SQL, new ActivityRowMapper(), userId);
    }

    class ActivityRowMapper implements RowMapper<DBEntry> {

        @Override
        public DBEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new DBEntry(rs.getString("userId"),
                    rs.getString("guess"),
                    rs.getString("word"),
                    rs.getInt("attempt"));
        }
    }
}

