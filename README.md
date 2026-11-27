# StudyAI Mentor

Ứng dụng Android Kotlin dành cho học sinh. Mở ứng dụng, chọn **Bắt đầu** và đăng ký bằng tên, email và mật khẩu. Tài khoản và lịch sử học tập được lưu trên thiết bị. Trong **Hồ sơ**, học sinh có thể đổi tên, cập nhật email đăng nhập, đổi mật khẩu hoặc đăng xuất.

Trong **Hỏi AI**, chọn môn và gửi câu hỏi. Lần gửi đầu tiên trong phiên cần khóa API ViLao. Khóa chỉ được giữ trong bộ nhớ và được xóa khi đăng xuất. Ứng dụng hiển thị lời giải theo từng bước, lưu câu hỏi và câu trả lời trong **Lịch sử học tập**, đồng thời báo lỗi khi yêu cầu mạng hoặc dịch vụ AI gặp sự cố.

Từ màn hình lời giải, chọn **Tạo bài ôn tập** để tạo ba câu tự luận. Có thể mở lại bài ôn đã tạo từ lịch sử khi offline. Trong **Hồ sơ → Nhắc lịch học**, chọn giờ nhắc hằng ngày và cho phép thông báo khi Android yêu cầu.

Trong **Trang chủ → Tiến độ học tập**, xem tổng số câu, thống kê theo môn, huy hiệu và chuỗi ngày học. Số liệu lấy từ các lời giải đã lưu trong lịch sử của tài khoản.

Chạy cấu hình **app** trong Android Studio hoặc build APK bằng `gradlew.bat :app:assembleDebug`.
