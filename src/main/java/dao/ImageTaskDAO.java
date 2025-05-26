package dao;

import model.ImageTask;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import static util.DBUtil.connection;

public class ImageTaskDAO implements TaskDAO<ImageTask> {
    @Override
    public ImageTask save(ImageTask task) {
        String sql = "INSERT INTO image_tasks (user_id, image_path, original_filename, " +
                "description, custom_params, notify_when_complete, status, submit_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, task.getUserId());
            stmt.setString(2, task.getImagePath());
            stmt.setString(3, task.getOriginalFilename());
            stmt.setString(4, task.getDescription());
            stmt.setString(5, task.getCustomParams());
            stmt.setBoolean(6, task.isNotifyWhenComplete());
            stmt.setString(7, task.getStatus());
            stmt.setTimestamp(8, new Timestamp(task.getSubmitTime().getTime()));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 1) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        task.setId(generatedKeys.getLong(1));
                        return task;
                    }
                }
            }

            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    public ImageTask update(ImageTask task) {
        String sql = "UPDATE image_tasks SET image_path = ?, result_path = ?, status = ?, " +
                "processing_time = ?, error_message = ? WHERE id = ?";
    
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
    
            stmt.setString(1, task.getImagePath());
            stmt.setString(2, task.getResultPath());
            stmt.setString(3, task.getStatus());
    
            if (task.getProcessingTime() != null) {
                stmt.setTimestamp(4, new Timestamp(task.getProcessingTime().getTime()));
            } else {
                stmt.setNull(4, Types.TIMESTAMP);
            }
    
            stmt.setString(5, task.getErrorMessage());
            stmt.setLong(6, task.getId());
    
            int affectedRows = stmt.executeUpdate();
            return affectedRows == 1 ? task : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ImageTask findById(Long id) {
        String sql = "SELECT * FROM image_tasks WHERE id = ?";
    
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
    
            stmt.setLong(1, id);
    
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToImageTask(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        return null;
    }

    @Override
    public List<ImageTask> findAll() {
        List<ImageTask> tasks = new ArrayList<>();
        String sql = "SELECT * FROM image_tasks ORDER BY submit_time DESC";
    
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
    
            while (rs.next()) {
                tasks.add(mapResultSetToImageTask(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        return tasks;
    }

    public List<ImageTask> findByUserId(Long userId) {
        List<ImageTask> tasks = new ArrayList<>();
        String sql = "SELECT * FROM image_tasks WHERE user_id = ? ORDER BY submit_time DESC";
    
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
    
            stmt.setLong(1, userId);
    
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapResultSetToImageTask(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        return tasks;
    }

    public List<ImageTask> findByStatus(String status) {
        List<ImageTask> tasks = new ArrayList<>();
        String sql = "SELECT * FROM image_tasks WHERE status = ? ORDER BY submit_time ASC";
    
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
    
            stmt.setString(1, status);
    
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapResultSetToImageTask(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        return tasks;
    }

    public List<ImageTask> getImageTasksByUserId(long userId, String filter, String sortBy,
                                                 String sortOrder, int page, int pageSize) {
        List<ImageTask> result = new ArrayList<>();

        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM image_tasks WHERE user_id = ?");

        // Thêm điều kiện lọc
        if (!"all".equals(filter)) {
            if ("completed".equals(filter)) {
                sqlBuilder.append(" AND status = 'COMPLETED'");
            } else if ("processing".equals(filter)) {
                sqlBuilder.append(" AND status = 'PROCESSING'");
            } else if ("failed".equals(filter)) {
                sqlBuilder.append(" AND status = 'FAILED'");
            }
        }

        sqlBuilder.append(" ORDER BY ");
        if ("status".equals(sortBy)) {
            sqlBuilder.append("status");
        } else {
            sqlBuilder.append("submit_time");
        }

        if ("asc".equals(sortOrder)) {
            sqlBuilder.append(" ASC");
        } else {
            sqlBuilder.append(" DESC");
        }

        int offset = (page - 1) * pageSize;
        sqlBuilder.append(" LIMIT ").append(pageSize).append(" OFFSET ").append(offset);

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {

            stmt.setLong(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapResultSetToImageTask(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }


    private ImageTask mapResultSetToTask(ResultSet rs) throws SQLException {
        ImageTask task = new ImageTask();
        task.setId(rs.getLong("id"));
        task.setUserId(rs.getLong("user_id"));
        task.setImagePath(rs.getString("image_path"));
        task.setOriginalFilename(rs.getString("original_filename"));
        task.setDescription(rs.getString("description"));
        task.setCustomParams(rs.getString("custom_params"));
        task.setNotifyWhenComplete(rs.getBoolean("notify_when_complete"));
        task.setStatus(rs.getString("status"));
        task.setSubmitTime(rs.getTimestamp("submit_time"));

        Timestamp processedTime = rs.getTimestamp("processed_time");
        if (processedTime != null) {
            task.setProcessingTime(processedTime);
        }

        task.setResultPath(rs.getString("result_path"));
        task.setErrorMessage(rs.getString("error_message"));

        return task;
    }
    public int countImageTasksByUserId(long userId, String filter) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT(*) FROM image_tasks WHERE user_id = ?");

        if (!"all".equals(filter)) {
            if ("completed".equals(filter)) {
                sqlBuilder.append(" AND status = 'COMPLETED'");
            } else if ("processing".equals(filter)) {
                sqlBuilder.append(" AND status = 'PROCESSING'");
            } else if ("failed".equals(filter)) {
                sqlBuilder.append(" AND status = 'FAILED'");
            }
        }

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {

            stmt.setLong(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public List<ImageTask> getPendingTasks() {
        List<ImageTask> result = new ArrayList<>();
        String sql = "SELECT * FROM image_tasks WHERE status = 'PENDING' ORDER BY submit_time ASC";
    
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
    
            while (rs.next()) {
                result.add(mapResultSetToTask(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM image_tasks WHERE id = ?";
    
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
    
            stmt.setLong(1, id);
    
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private ImageTask mapResultSetToImageTask(ResultSet rs) throws SQLException {
        ImageTask task = new ImageTask();

        task.setId(rs.getLong("id"));
        task.setUserId(rs.getLong("user_id"));
        task.setImagePath(rs.getString("image_path"));
        task.setOriginalFilename(rs.getString("original_filename"));
        task.setResultPath(rs.getString("result_path"));
        task.setDescription(rs.getString("description"));
        task.setCustomParams(rs.getString("custom_params"));
        task.setNotifyWhenComplete(rs.getBoolean("notify_when_complete"));
        task.setStatus(rs.getString("status"));
        task.setSubmitTime(rs.getTimestamp("submit_time"));
        task.setProcessingTime(rs.getTimestamp("processing_time"));
        task.setErrorMessage(rs.getString("error_message"));

        return task;
    }
}
