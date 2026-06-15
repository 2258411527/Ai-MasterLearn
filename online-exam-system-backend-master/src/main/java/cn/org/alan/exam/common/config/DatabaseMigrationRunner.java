package cn.org.alan.exam.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class DatabaseMigrationRunner implements CommandLineRunner {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        migrateColumnToText("t_exam_qu_answer", "ai_reason");
        migrateColumnToText("t_exam_qu_answer", "answer_content");
        migrateColumnToText("t_exercise_record", "answer");
        addColumnIfNotExists("t_user_exercise_record", "subject", "VARCHAR(255) DEFAULT NULL");
        addColumnIfNotExists("t_user_exercise_record", "knowledge_point", "VARCHAR(255) DEFAULT NULL");
        addColumnIfNotExists("t_notice", "is_must_read", "INT DEFAULT 0");
        createTableIfNotExists("t_user_notification",
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id INT NOT NULL," +
                "type VARCHAR(50) NOT NULL," +
                "related_id INT DEFAULT NULL," +
                "title VARCHAR(255) DEFAULT NULL," +
                "content TEXT DEFAULT NULL," +
                "is_read INT DEFAULT 0," +
                "is_must_read INT DEFAULT 0," +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "INDEX idx_user_read (user_id, is_read)," +
                "INDEX idx_user_must (user_id, is_must_read, is_read)");
    }

    private void createTableIfNotExists(String tableName, String columns) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?",
                    Integer.class, tableName);
            if (count != null && count == 0) {
                jdbcTemplate.execute("CREATE TABLE " + tableName + " (" + columns + ") DEFAULT CHARSET=utf8mb4");
                log.info("数据库迁移: 表 {} 已创建", tableName);
            }
        } catch (Exception e) {
            log.warn("数据库迁移创建表失败(可忽略): {}", e.getMessage());
        }
    }

    private void addColumnIfNotExists(String tableName, String columnName, String columnDefinition) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                    Integer.class, tableName, columnName);
            if (count != null && count == 0) {
                jdbcTemplate.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition);
                log.info("数据库迁移: {}.{} 列已添加", tableName, columnName);
            }
        } catch (Exception e) {
            log.warn("数据库迁移添加列失败(可忽略): {}", e.getMessage());
        }
    }

    private void migrateColumnToText(String tableName, String columnName) {
        try {
            String columnType = jdbcTemplate.queryForObject(
                    "SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                    String.class, tableName, columnName);
            if (!"text".equalsIgnoreCase(columnType) && !"longtext".equalsIgnoreCase(columnType)) {
                jdbcTemplate.execute("ALTER TABLE " + tableName + " MODIFY COLUMN " + columnName + " TEXT");
                log.info("数据库迁移: {}.{} 列类型已修改为 TEXT", tableName, columnName);
            }
        } catch (Exception e) {
            log.warn("数据库迁移检查失败(可忽略): {}", e.getMessage());
        }
    }
}
