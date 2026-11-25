# StudyAI Mentor — Người 2

Giải nén rồi mở thư mục này trong Android Studio. Dự án dùng Android SDK 37. Chọn cấu hình app và bấm Run. Bản demo có ID ứng dụng riêng và có thể cài song song với bản nhóm.

- Chọn môn và gửi câu hỏi để xem lời giải từng bước.
- Tạo bài ôn gồm 3 câu tự luận từ lời giải, có nút xem đáp án.
- Dùng nút Mở lời giải gần nhất để kiểm tra lại bài ôn đã lưu.
- Ứng dụng hiển thị lỗi mất mạng, hết thời gian chờ và lỗi dịch vụ AI.

Cần Internet và khóa ViLao hợp lệ để gửi câu hỏi hoặc tạo bài mới. Ứng dụng hỏi khóa khi gửi lần đầu và chỉ giữ khóa trong phiên.

Khi tích hợp, dùng VilaoClient.kt và các phương thức Hỏi AI, Kết quả và Bài ôn trong MainActivity.kt. Nối từ trang chủ của nhóm và truyền ID tài khoản để lưu dữ liệu theo người dùng. Phối hợp với Người 3 để dùng cùng cấu trúc lưu câu hỏi, lời giải và bài ôn. Khai báo INTERNET và ACCESS_NETWORK_STATE trong manifest.
