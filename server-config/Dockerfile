# Sử dụng base image OpenJDK
FROM eclipse-temurin:21-jdk-jammy

# Tạo thư mục làm việc trong container
WORKDIR /server-config

COPY server-config-0.0.1-SNAPSHOT.jar server-config-0.0.1-SNAPSHOT.jar

# Mở cổng 8888 (hoặc cổng mà ứng dụng sử dụng)
EXPOSE 8888

# Lệnh khởi chạy ứng dụng
ENTRYPOINT ["java", "-jar", "server-config-0.0.1-SNAPSHOT.jar"]
