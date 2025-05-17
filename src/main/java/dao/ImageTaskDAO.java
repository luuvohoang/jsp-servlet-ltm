package dao;

import model.ImageTask;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static util.DBUtil.connection;

/**
 * DAO implementation for ImageTask entity
 * Handles database operations for the image processing tasks
 */
public class ImageTaskDAO implements TaskDAO<ImageTask> {

    /**
     * Lưu một image task mới vào database
     *
     * @param task - ImageTask cần lưu
     * @return ImageTask với ID đã được thiết lập nếu thành công, null nếu thất bại
     */
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

    /**
     * Cập nhật thông tin của một image task
     *
     * @param task - ImageTask cần cập nhật
     * @return ImageTask đã cập nhật nếu thành công, null nếu thất bại
     */
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

    /**
     * Lấy một image task theo ID
     *
     * @param id - ID của image task cần lấy
     * @return ImageTask hoặc null nếu không tìm thấy
     */
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
    
    /**
     * Lấy tất cả image tasks
     *
     * @return Danh sách tất cả ImageTask
     */
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

    /**
     * Tìm các task theo user ID
     * 
     * @param userId ID của user
     * @return Danh sách các task thuộc về user
     */
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
    
    /**
     * Tìm các task theo trạng thái
     * 
     * @param status Trạng thái task (PENDING, PROCESSING, COMPLETED, FAILED)
     * @return Danh sách các task có trạng thái được chỉ định
     */
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
    
    /**
     * Lấy danh sách image tasks của một user với phân trang và lọc
     *
     * @param userId - ID của user
     * @param filter - Lọc theo trạng thái (all, completed, processing, failed)
     * @param sortBy - Sắp xếp theo trường nào (submitTime, status)
     * @param sortOrder - Thứ tự sắp xếp (asc, desc)
     * @param page - Trang hiện tại
     * @param pageSize - Số lượng kết quả trên mỗi trang
     * @return Danh sách ImageTask
     */
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

        // Thêm sắp xếp
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

        // Thêm phân trang
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

    /**
     * Lấy danh sách các tasks đang chờ xử lý
     *
     * @return Danh sách ImageTask
     */
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

    /**
     * Xóa một image task từ database
     *
     * @param id - ID của task cần xóa
     * @return true nếu xóa thành công, false nếu thất bại
     */
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
    
    /**
     * Map dữ liệu từ ResultSet sang đối tượng ImageTask
     *
     * @param rs - ResultSet chứa dữ liệu
     * @return Đối tượng ImageTask
     * @throws SQLException
     */
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
        
            /**
             * Khởi tạo bảng database nếu chưa tồn tại
             */
            public void initDatabase() {
        String createTableSQL =
                "CREATE TABLE IF NOT EXISTS image_tasks (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                        "user_id BIGINT NOT NULL," +
                        "image_path VARCHAR(255) NOT NULL," +
                        "original_filename VARCHAR(255)," +
                        "description TEXT," +
                        "custom_params TEXT," +
                        "notify_when_complete BOOLEAN DEFAULT FALSE," +
                        "status VARCHAR(20) NOT NULL," +
                        "submit_time TIMESTAMP NOT NULL," +
                        "processing_time TIMESTAMP," +
                        "result_path VARCHAR(255)," +
                        "error_message TEXT," +
                        "INDEX (user_id)," +
                        "INDEX (status)" +
                        ")";
        
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
            }
}
