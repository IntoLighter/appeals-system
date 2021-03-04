package com.intolighter.appealssystem.persistence.repositories;

import com.intolighter.appealssystem.persistence.models.Appeal;
import lombok.val;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class AppealRepository extends RepositoryUtils {

    private static UserRepository userRepository;

    public AppealRepository(JdbcTemplate jdbcTemplate, UserRepository userRepository) {
        super(jdbcTemplate, "appeals");
        AppealRepository.userRepository = userRepository;
    }

    private static Appeal mapToAppeal(ResultSet rs, int rowNum) throws SQLException {
        val id = rs.getLong("user_id");
        val user =  userRepository.findById(id).orElseThrow(() ->
                new UsernameNotFoundException("User with id " + id + " is not found"));

        return new Appeal(
                rs.getLong("id"),
                rs.getDate("request_time"),
                rs.getString("classifier"),
                rs.getString("description"),
                rs.getBoolean("archived"),
                user);
    }

    public Appeal save(Appeal appeal) {
        jdbcTemplate.update("INSERT INTO appeals " +
                        "(classifier, description, request_time, user_id, archived) VALUES (?, ?, ?, ?, ?)",
                appeal.getClassifier(), appeal.getDescription(), appeal.getRequestTime(),
                appeal.getUser().getId(), appeal.isArchived());

        return appeal;
    }

    public List<Appeal> findAllByUserId(long userId, boolean archived) {
        return jdbcTemplate.query("SELECT * FROM appeals WHERE user_id = ? AND archived = ?",
                AppealRepository::mapToAppeal, userId, archived);
    }

    public List<Appeal> findAll(boolean archived) {
        return jdbcTemplate.query("SELECT * FROM appeals WHERE archived = ?",
                AppealRepository::mapToAppeal, archived);
    }

    public Optional<Appeal> findById(long id) {
        return Optional.ofNullable(
                jdbcTemplate.queryForObject("SELECT * FROM appeals WHERE id = ?",
                        AppealRepository::mapToAppeal, id));

    }

    public void update(Appeal appeal) {
        jdbcTemplate.update("UPDATE appeals SET classifier = ?, description = ? WHERE id = ?",
                appeal.getClassifier(), appeal.getDescription(), appeal.getId());
    }

    public void deleteById(long id) {
        jdbcTemplate.update("DELETE FROM appeals WHERE id = ?", id);
    }

    public boolean existsByDescription(String description) {
        return checkForExistence("description", description);
    }

    @PostConstruct
    private void createDb() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS appeals()");
    }
}
